import com.mycompany.novatech.app.*;
import com.mycompany.novatech.app.comercial.*;
import com.mycompany.novatech.app.ventas.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.net.http.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

/** HTTP real + InnoDB local. No credenciales ni destinos de producción. */
public class IntegrationEtapa6 extends IntegrationEtapa4 {
 interface Checked {void run() throws Exception;}
 static void rejects(Checked f,String message)throws Exception {boolean rejected=false;try{f.run();}catch(IllegalArgumentException|SQLException e){rejected=true;}ok(rejected,message);}
 static BigDecimal n(String s){return new BigDecimal(s);}
 static void same(BigDecimal actual,String expected,String message){ok(actual.compareTo(n(expected))==0,message+" ("+actual+")");}
 static Venta sale(int client,int product,String qty){Venta v=new Venta();v.clienteId=client;v.detalles.add(new DetalleVenta(product,n(qty)));return v;}
 static HttpResponse<String> save(HttpClient h,String csrf,Venta v)throws Exception {
  List<String> values=new ArrayList<>(Arrays.asList("csrf",csrf,"accion","guardar","id",""+v.id,"version",""+v.version,"clave",v.clave,"cliente",""+v.clienteId,"tipo",v.tipo,"porcentaje",v.porcentaje.toPlainString(),"observacion",v.observacion,"usuario_registro","FALSO"));
  for(DetalleVenta d:v.detalles)Collections.addAll(values,"producto",""+d.productoId,"cantidad",d.cantidad.toPlainString());
  return post(h,"/ventas",values.toArray(new String[0]));
 }
 static HttpResponse<String> action(HttpClient h,String t,Venta v,String action)throws Exception{return post(h,"/ventas","csrf",t,"accion",action,"id",""+v.id,"version",""+v.version,"medio","EFECTIVO");}
 static int savedId(HttpResponse<String> r){return Integer.parseInt(r.headers().firstValue("location").orElseThrow().split("ver=")[1]);}
 public static void main(String[] args)throws Exception {
  localOnly();String suffix=""+System.currentTimeMillis(),actor="qa_v_"+suffix,operator="qa_vo_"+suffix,pw=UUID.randomUUID().toString();
  JdbcUsuarioRepository users=new JdbcUsuarioRepository();JdbcClienteRepository clients=new JdbcClienteRepository();JdbcProductoRepository products=new JdbcProductoRepository();JdbcVentaRepository sales=new JdbcVentaRepository();
  List<Integer> clientIds=new ArrayList<>(),productIds=new ArrayList<>();
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {try {
   int ar=0,op=0;for(String[] role:users.roles()){if(role[1].equals("ADMINISTRADOR"))ar=Integer.parseInt(role[0]);if(role[1].equals("OPERADOR"))op=Integer.parseInt(role[0]);}
   users.guardar(0,actor,"QA","Ventas",ar,pw,true);users.guardar(0,operator,"QA","Operador ventas",op,pw,true);final int aid=id(c,actor);
   Cliente cl=new Cliente();cl.nombres="Cliente <script>";cl.apellidos=suffix;cl.documento="7"+suffix.substring(suffix.length()-7);final int cid=clients.guardar(cl,actor);clientIds.add(cid);
   Cliente empresa=new Cliente();empresa.nombres="Contacto";empresa.apellidos=suffix;empresa.tipoDocumento="RUC";empresa.documento="27"+suffix.substring(suffix.length()-9);empresa.razonSocial="Empresa QA";int empresaId=clients.guardar(empresa,actor);clientIds.add(empresaId);
   CatalogoComercialRepository catalogs=new CatalogoComercialRepository();int cat=Integer.parseInt(catalogs.listar("categoria").get(0)[0]),unit=Integer.parseInt(catalogs.listar("unidad").get(0)[0]);
   for(String type:new String[]{"PRODUCTO","SERVICIO","INSUMO"})productIds.add(products.guardar(new Producto.Builder().identidad(0,"QA6-"+suffix+"-"+type,"Artículo <script> "+type).clasificacion(type,cat,0,unit,0).valores("10.25",type.equals("SERVICIO")?"0":"10","0").build(),actor));
   final int prod=productIds.get(0),svc=productIds.get(1),ins=productIds.get(2);
   HttpClient h=client(),other=client();ok(get(h,"/ventas").statusCode()==302,"Anonymous sales denied");
   post(h,"/login","csrf",token(get(h,"/login").body()),"usuario",actor,"password",pw);post(other,"/login","csrf",token(get(other,"/login").body()),"usuario",operator,"password",pw);
   String t=token(get(h,"/menu").body()),ot=token(get(other,"/menu").body());
   ok(get(h,"/ventas").statusCode()==200,"Sales JSP renders");ok(get(h,"/ventas?nuevo=1").statusCode()==200,"New sale form renders");
   ok(get(h,"/abrir?opcion=ventas").statusCode()==302,"Sales menu command");ok(get(other,"/ventas").statusCode()==403,"Operator GET denied");
   ok(post(other,"/ventas","csrf",ot,"accion","guardar").statusCode()==403,"Operator POST denied");ok(post(h,"/ventas","accion","guardar").statusCode()==403,"CSRF required");
   Venta v=sale(cid,prod,"1.5");v.detalles.add(new DetalleVenta(svc,n("2")));v.porcentaje=n("10");v.observacion="Prueba <script>";
   HttpResponse<String> response=save(h,t,v);ok(response.statusCode()==302,"HTTP create header and two lines");v.id=savedId(response);Venta stored=sales.buscar(v.id);
   ok(stored.detalles.size()==2&&stored.estado.equals("PENDIENTE"),"Pending sale and lines persisted");same(stored.subtotal,"35.88","Line rounding HALF_UP");same(stored.descuento,"3.59","Percentage discount rounded");same(stored.total,"32.29","Server total");same(products.buscar(prod).stock,"10","Draft does not reserve stock");
   ok(stored.creador.equals(actor)&&stored.usuarioId==aid,"Actor taken from session");
   Venta replay=sale(cid,prod,"1.5");replay.clave=v.clave;ok(savedId(save(h,t,replay))==v.id,"Repeated creation key does not duplicate sale");
   String detail=get(h,"/ventas?ver="+v.id).body();ok(detail.contains("Cliente &lt;script&gt;")&&detail.contains("Artículo &lt;script&gt;")&&!detail.contains("Prueba <script>"),"Receipt escapes user input");
   ok(detail.contains("BOLETA")&&detail.contains("Sin validez tributaria"),"Academic boleta rendered");
   ok(get(h,"/ventas?editar="+v.id).statusCode()==400,"Registered sale edit form rejected");
   ok(!detail.contains("Editar cabecera")&&!detail.contains("Eliminar pendiente")&&detail.contains("Confirmar anulación"),"Pending receipt offers annulment, never edit/delete");
   String add=post(h,"/ventas","csrf",t,"accion","agregar","id","0","clave",UUID.randomUUID().toString(),"producto",""+prod,"cantidad","2").body();ok(add.contains("Cantidad 2"),"Add detail row without saving");
   String remove=post(h,"/ventas","csrf",t,"quitar","0","id","0","producto",""+prod,"cantidad","2","producto",""+svc,"cantidad","1").body();ok(!remove.contains("Cantidad 2")&&remove.contains("value=\""+svc+"\" selected"),"Remove detail row preserves remaining selection");
   int oldVersion=stored.version;stored.detalles.remove(1);stored.detalles.get(0).cantidad=n("2");stored.porcentaje=n("0");
   ok(save(h,t,stored).statusCode()==400,"HTTP edit of pending sale rejected");
   rejects(()->sales.guardar(stored,aid,actor),"Repository edit of pending sale rejected");
   rejects(()->sales.eliminar(stored.id,stored.version),"Repository deletion of pending sale rejected");
   ok(action(h,t,stored,"eliminar").statusCode()==400,"HTTP deletion of pending sale rejected");
   ok(post(h,"/ventas","csrf",t,"accion","agregar","id",""+stored.id).statusCode()==400,"Cannot add lines to registered sale");
   Venta fresh=sales.buscar(v.id);ok(fresh.version==oldVersion&&fresh.detalles.size()==2,"Rejected changes preserve version and lines");same(fresh.total,"32.29","Rejected edit preserves total");
   Venta regular=sale(cid,svc,"2");sales.guardar(regular,aid,actor);same(sales.buscar(regular.id).total,"20.50","Regular Strategy without discount");
   Venta bad=sale(cid,prod,"0");ok(save(h,t,bad).statusCode()==400,"Zero quantity rejected");bad=sale(cid,prod,"1.0001");ok(save(h,t,bad).statusCode()==400,"Excess precision rejected");
   bad=sale(cid,prod,"1");bad.detalles.add(new DetalleVenta(prod,n("2")));ok(save(h,t,bad).statusCode()==400,"Duplicate product rejected");
   bad=sale(cid,prod,"1");bad.porcentaje=n("100.01");ok(save(h,t,bad).statusCode()==400,"Discount over 100 rejected");
   bad=sale(cid,prod,"1");bad.tipo="FACTURA";ok(save(h,t,bad).statusCode()==400,"Invoice requires RUC");
   Venta invoice=sale(empresaId,svc,"2");invoice.tipo="FACTURA";invoice.id=savedId(save(h,t,invoice));ok(get(h,"/ventas?ver="+invoice.id).body().contains("FACTURA F-"),"Academic invoice for RUC");
   same(products.buscar(svc).stock,"0","Service stock stays zero");
   final int pendingId=fresh.id;Venta badUpdate=sales.buscar(pendingId);badUpdate.detalles.add(new DetalleVenta(Integer.MAX_VALUE,n("1")));
   rejects(()->sales.guardar(badUpdate,aid,actor),"Registered sale rejects changed detail");same(sales.buscar(pendingId).total,"32.29","Original header survives rollback");ok(sales.buscar(pendingId).detalles.size()==2,"Original detail survives rollback");
   Cliente inactive=clients.buscar(cid);inactive.activo=false;clients.guardar(inactive,actor);rejects(()->sales.guardar(sale(cid,prod,"1"),aid,actor),"Inactive customer rejected");inactive.activo=true;clients.guardar(inactive,actor);
   ok(action(h,t,fresh,"pagar").statusCode()==302,"HTTP confirm payment");Venta paid=sales.buscar(fresh.id);same(products.buscar(prod).stock,"8.5","Payment deducts product stock");ok(paid.estado.equals("PAGADA")&&paid.fechaPago!=null,"Paid state/date persisted");
   ok(action(h,t,paid,"pagar").statusCode()==400,"Cannot pay twice");same(products.buscar(prod).stock,"8.5","Duplicate payment leaves stock unchanged");ok(save(h,t,paid).statusCode()==400,"Paid edit denied");ok(action(h,t,paid,"eliminar").statusCode()==400,"Paid deletion denied");
   Producto p=products.buscar(prod);rejects(()->products.guardar(new Producto.Builder().identidad(p.id,p.codigo,p.nombre).clasificacion("INSUMO",cat,0,unit,0).valores("10.25","8.5","0").build(),actor),"Type change with paid sale blocked");
   products.guardar(new Producto.Builder().identidad(p.id,p.codigo,"Nombre nuevo").clasificacion(p.tipo,cat,0,unit,0).valores("99","8.5","0").build(),actor);same(sales.buscar(paid.id).detalles.get(0).precio,"10.25","Receipt price preserved after catalog edit");ok(sales.buscar(paid.id).detalles.get(0).nombre.contains("<script>"),"Receipt name preserved");
   ok(action(h,t,paid,"anular").statusCode()==302,"HTTP annul paid sale");Venta cancelled=sales.buscar(paid.id);same(products.buscar(prod).stock,"10","Annul restores exact stock");ok(cancelled.estado.equals("ANULADA")&&cancelled.fechaAnulacion!=null,"Annul preserves receipt and date");ok(action(h,t,cancelled,"anular").statusCode()==400,"Cannot annul twice");same(products.buscar(prod).stock,"10","No double stock return");
   Venta service=sales.buscar(invoice.id);ok(action(h,t,service,"pagar").statusCode()==302,"Service payment without stock");same(products.buscar(svc).stock,"0","Paid service does not decrement inventory");rejects(()->products.eliminar(svc),"Referenced service cannot be deleted");rejects(()->clients.eliminar(empresaId),"Referenced customer cannot be deleted");
   Venta shortage=sale(cid,prod,"1");shortage.detalles.add(new DetalleVenta(ins,n("11")));sales.guardar(shortage,aid,actor);rejects(()->sales.pagar(shortage.id,1,"EFECTIVO",actor),"Insufficient second line rejects payment");same(products.buscar(prod).stock,"10","First stock write rolled back atomically");ok(sales.buscar(shortage.id).estado.equals("PENDIENTE"),"Failed payment leaves draft pending");
   Venta pending=sales.buscar(shortage.id);
   ok(action(h,t,pending,"anular").statusCode()==302,"HTTP annul unpaid sale");
   Venta annulledPending=sales.buscar(pending.id);
   ok(annulledPending.estado.equals("ANULADA")&&annulledPending.fechaPago==null&&annulledPending.medioPago==null&&annulledPending.fechaAnulacion!=null,"Unpaid cancellation preserves dates without inventing payment");
   ok(annulledPending.detalles.size()==2,"Pending annulment preserves every line");
   same(products.buscar(prod).stock,"10","Unpaid cancellation does not return stock");same(products.buscar(ins).stock,"10","Unpaid cancellation does not increase second stock");
   ok(action(h,t,annulledPending,"anular").statusCode()==400,"Cannot annul unpaid sale twice");
   ok(action(h,t,annulledPending,"pagar").statusCode()==400,"Cannot pay annulled sale");
   ok(action(h,t,annulledPending,"eliminar").statusCode()==400,"Cannot delete annulled sale");
   Venta stale=sales.buscar(regular.id);sales.pagar(stale.id,stale.version,"EFECTIVO",actor);ok(action(h,t,stale,"anular").statusCode()==400,"Stale version cannot annul changed sale");
   // Two independent sales compete for the same last 10 units. Only one may pay.
   Venta a=sale(cid,ins,"7"),b=sale(cid,ins,"7");sales.guardar(a,aid,actor);sales.guardar(b,aid,actor);ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch go=new CountDownLatch(1);
   try{List<Future<Boolean>> results=new ArrayList<>();for(Venta x:Arrays.asList(a,b))results.add(pool.submit(()->{go.await();try{sales.pagar(x.id,1,"EFECTIVO",actor);return true;}catch(IllegalArgumentException e){return false;}}));go.countDown();int successes=0;for(Future<Boolean> r:results)if(r.get(15,TimeUnit.SECONDS))successes++;ok(successes==1,"Concurrent payments cannot oversell");same(products.buscar(ins).stock,"3","Concurrent final stock correct");}finally{pool.shutdownNow();}
   ok(get(h,"/ventas?q="+cl.documento).body().contains("Ver detalle"),"Sales search by customer document");
  }finally{
   try(PreparedStatement p=c.prepareStatement("DELETE FROM ventas WHERE usuario_registro=?")){p.setString(1,actor);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("DELETE FROM productos WHERE usuario_registro=?")){p.setString(1,actor);p.executeUpdate();}
   for(int x:clientIds)clients.eliminar(x);
   for(String name:new String[]{operator,actor}){int key=id(c,name);if(key>0)users.accion(key,-1,"eliminar");}
   System.out.println("Stage 6 temporary fixtures removed.");
  }}
 }
}
