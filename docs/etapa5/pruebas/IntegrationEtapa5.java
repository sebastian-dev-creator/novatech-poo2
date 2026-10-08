import com.mycompany.novatech.app.*;
import com.mycompany.novatech.app.comercial.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.net.http.*;
import java.util.*;

/** Integración HTTP + MySQL real, exclusivamente en la instancia local de QA. */
public class IntegrationEtapa5 extends IntegrationEtapa4 {
 static int key(Connection c,String table,String col,String value)throws Exception {
  try(PreparedStatement p=c.prepareStatement("SELECT id FROM "+table+" WHERE "+col+"=?")){p.setString(1,value);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}}
 }
 static String scalar(Connection c,String sql,int id)throws Exception {
  try(PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}}
 }
 static void invalid(Runnable fn,String msg){boolean rejected=false;try{fn.run();}catch(IllegalArgumentException e){rejected=true;}ok(rejected,msg);}
 static void supplierOutsidePage(Connection c,HttpClient h,String csrf,int productId,int supplierId,String suffix,String cat,String unit,String actor)throws Exception {
  ProveedorRepository repo=new JdbcProveedorRepository();String owner=actor+"_selector";
  List<Proveedor> lote=new ArrayList<>();
  for(int i=0;i<500;i++) {
   Proveedor p=new Proveedor();p.documento="98"+suffix.substring(suffix.length()-6)+String.format("%03d",i);
   p.razonSocial="QA selector "+suffix+" "+i;p.nombres="Prueba";p.apellidos="Selector";lote.add(p);
  }
  try {
   for(int inicio=0;inicio<lote.size();inicio+=50)repo.importar(lote.subList(inicio,inicio+50),owner);
   ok(repo.listar("").stream().noneMatch(p->p.id==supplierId),"Fixture puts current supplier beyond first 500");
   String option="<option value=\""+supplierId+"\" selected>";
   ok(get(h,"/productos?editar="+productId).body().contains(option),"Edit keeps supplier beyond first 500 selected");
   HttpResponse<String> error=product(h,csrf,productId,"QA-"+suffix,"PRODUCTO","-1","3",cat,unit,""+supplierId);
   ok(error.statusCode()==400&&error.body().contains(option),"Validation error keeps older supplier selected");
   ok(product(h,csrf,productId,"QA-"+suffix,"PRODUCTO","19.90","3",cat,unit,""+supplierId).statusCode()==302,"Save with older supplier succeeds");
   ok(new JdbcProductoRepository().buscar(productId).proveedorId==supplierId,"Save preserves supplier relation");
  } finally {
   List<Integer> personas=new ArrayList<>();
   try(PreparedStatement p=c.prepareStatement("SELECT id_datos_personales FROM proveedores WHERE usuario_registro=?")) {
    p.setString(1,owner);try(ResultSet r=p.executeQuery()){while(r.next())personas.add(r.getInt(1));}
   }
   try(PreparedStatement p=c.prepareStatement("DELETE FROM proveedores WHERE usuario_registro=?")){p.setString(1,owner);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("DELETE FROM datos_personales WHERE id_datos_personales=? AND id_usuario IS NULL")) {
    for(int id:personas){p.setInt(1,id);p.addBatch();}p.executeBatch();
   }
  }
 }
 static HttpResponse<String> product(HttpClient h,String csrf,int id,String code,String type,String price,String stock,String cat,String unit,String supplier)throws Exception {
  return post(h,"/productos","csrf",csrf,"accion","guardar","id",""+id,"codigo",code,"nombre","Audio <script>","tipo",type,"precio",price,"stock",stock,"minimo","0","categoria",cat,"unidad",unit,"proveedor",supplier,"activo","on","usuario_registro","FALSO");
 }
 public static void main(String[] args)throws Exception {
  localOnly();String suffix=""+System.currentTimeMillis(),an="qa_c_"+suffix,un="qa_o_"+suffix,pw="TemporalEtapaCinco!";
  String dni="9"+suffix.substring(suffix.length()-7),ruc="20"+suffix.substring(suffix.length()-9);
  JdbcUsuarioRepository users=new JdbcUsuarioRepository();ClienteRepository clients=new JdbcClienteRepository();ProveedorRepository suppliers=new JdbcProveedorRepository();ProductoRepository products=new JdbcProductoRepository();CatalogoComercialRepository catalogs=new CatalogoComercialRepository();
  int aid=0,uid=0,cid=0,pid=0,prod=0,catid=0,sharedId=0,companyId=0;
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
   try {
    int ar=0,op=0;for(String[] role:users.roles()){if(role[1].equals("ADMINISTRADOR"))ar=Integer.parseInt(role[0]);if(role[1].equals("OPERADOR"))op=Integer.parseInt(role[0]);}
    users.guardar(0,an,"QA","Comercial",ar,pw,true);aid=id(c,an);users.guardar(0,un,"QA","Operador",op,pw,true);uid=id(c,un);
    HttpClient h=client(),operator=client();
    for(String route:new String[]{"/clientes","/proveedores","/productos","/catalogos-productos"})ok(get(h,route).statusCode()==302,"Anonymous denied "+route);
    post(h,"/login","csrf",token(get(h,"/login").body()),"usuario",an,"password",pw);
    post(operator,"/login","csrf",token(get(operator,"/login").body()),"usuario",un,"password",pw);
    String csrf=token(get(h,"/menu").body()),ot=token(get(operator,"/menu").body());
    for(String route:new String[]{"/clientes","/proveedores","/productos","/catalogos-productos"}) {
     ok(get(h,route).statusCode()==200,"Admin JSP "+route);
     ok(get(operator,route).statusCode()==403,"Operator GET denied "+route);
     ok(post(operator,route,"csrf",ot,"accion","guardar").statusCode()==403,"Operator POST denied "+route);
     ok(post(h,route,"accion","guardar").statusCode()==403,"CSRF protected "+route);
    }
    ok(get(h,"/abrir?opcion=clientes").statusCode()==302,"Client menu command");
    ok(get(operator,"/abrir?opcion=productos").statusCode()==403,"Product menu denied");
    ok(post(h,"/clientes","csrf",csrf,"accion","guardar","id","0","nombres","Cliente <script>","apellidos",suffix,"documento",dni,"correo","cliente@example.invalid","activo","on","usuario_registro","FALSO").statusCode()==302,"Client HTTP create");
    for(Cliente x:clients.listar(dni))cid=x.id;ok(cid>0,"Client stored");
    ok(an.equals(scalar(c,"SELECT usuario_registro FROM clientes WHERE id=?",cid)),"Client actor from session");
    String html=get(h,"/clientes?q="+dni).body();ok(html.contains("Cliente &lt;script&gt;")&&!html.contains("Cliente <script>"),"Client HTML escaped");
    Cliente original=clients.buscar(cid);original.telefono="999111222";clients.guardar(original,an);
    Cliente copy=original.copiarParaNuevo();ok(copy.id==0&&copy.personaId==0&&copy.documento.isEmpty()&&copy.nombres.isEmpty()&&copy.telefono.equals(original.telefono),"Prototype clears identity but copies contact context");copy.telefono="888111222";ok(!copy.telefono.equals(original.telefono),"Prototype independent");
    ok(get(h,"/clientes?copiar="+cid).body().contains("999111222"),"Prototype used by form");
    ok(post(h,"/clientes","csrf",csrf,"accion","guardar","nombres",original.nombres,"apellidos",suffix,"documento",dni).statusCode()==400,"Duplicate client rejected");
    original.correo="editado@example.invalid";original.activo=false;clients.guardar(original,un);
    ok(clients.buscar(cid).correo.equals(original.correo)&&!clients.buscar(cid).activo,"Client update and inactive state");
    ok(an.equals(scalar(c,"SELECT usuario_registro FROM clientes WHERE id=?",cid)),"Client creator preserved on edit by another actor");
    String companyRuc="22"+suffix.substring(suffix.length()-9);
    ok(post(h,"/clientes","csrf",csrf,"accion","guardar","tipoDocumento","RUC","documento",companyRuc,"razonSocial","Empresa cliente QA","nombres","Contacto","apellidos",suffix,"activo","on").statusCode()==302,"Corporate client HTTP create");
    companyId=key(c,"clientes","ruc",companyRuc);ok(companyId>0&&clients.buscar(companyId).documento.equals(companyRuc),"Corporate RUC round trip");
    Cliente company=clients.buscar(companyId);company.razonSocial="Empresa editada";clients.guardar(company,un);
    ok(clients.buscar(companyId).nombreVisible().equals("Empresa editada"),"Corporate client update");
    clients.eliminar(companyId);companyId=0;
    String sharedDni="8"+suffix.substring(suffix.length()-7);
    users.guardar(uid,un,"QA","Operador",op,"",true,new DatosPersonales(sharedDni,null,null),an);
    Cliente shared=new Cliente();shared.nombres="QA";shared.apellidos="Operador";shared.documento=sharedDni;
    sharedId=clients.guardar(shared,an);shared=clients.buscar(sharedId);
    ok(shared.personaId==Integer.parseInt(scalar(c,"SELECT id_datos_personales FROM datos_personales WHERE id_usuario=?",uid)),"Client reuses user identity by DNI");
    shared.nombres="Cambio indebido";boolean rejected=false;try{clients.guardar(shared,an);}catch(IllegalArgumentException ex){rejected=true;}
    ok(rejected&&users.buscar(uid).nombres.equals("QA"),"Commercial edit cannot overwrite shared user identity");
    clients.eliminar(sharedId);sharedId=0;ok(users.buscar(uid)!=null,"Deleting customer preserves shared user");
    ok(post(h,"/proveedores","csrf",csrf,"accion","guardar","nombres","Contacto","apellidos",suffix,"documento",ruc,"razonSocial","Proveedor QA "+suffix,"correo","proveedor@example.invalid","activo","on").statusCode()==302,"Supplier HTTP create");
    pid=key(c,"proveedores","ruc",ruc);ok(pid>0,"Supplier stored");
    Proveedor supplier=suppliers.buscar(pid);supplier.razonSocial="Proveedor actualizado "+suffix;suppliers.guardar(supplier,un);
    ok(suppliers.buscar(pid).razonSocial.equals(supplier.razonSocial),"Supplier update");ok(an.equals(scalar(c,"SELECT usuario_registro FROM proveedores WHERE id=?",pid)),"Supplier creator preserved");
    ProveedorTsvAdapter adapter=new ProveedorTsvAdapter();
    invalid(()->adapter.convertir("sin columnas"),"Adapter rejects invalid shape");
    String importedRuc="21"+suffix.substring(suffix.length()-9);
    String rows=importedRuc+"\tNuevo\tContacto\tQA\timportado@example.invalid\t999222111\n"+ruc+"\tDuplicado\tContacto\tQA";
    ok(post(h,"/proveedores","csrf",csrf,"accion","importar","tsv",rows).statusCode()==400,"Atomic import rejects existing RUC");
    ok(key(c,"proveedores","ruc",importedRuc)==0,"Import rolls back earlier rows");
    ok(post(h,"/proveedores","csrf",csrf,"accion","importar","tsv",rows.split("\n")[0]).statusCode()==302,"Adapter HTTP import success");
    int imported=key(c,"proveedores","ruc",importedRuc);ok(imported>0,"Imported supplier stored");suppliers.eliminar(imported);
    String category="QA_"+suffix;
    ok(post(h,"/catalogos-productos","csrf",csrf,"accion","guardar","clase","categoria","nombre",category,"activo","on").statusCode()==302,"Category HTTP create");catid=key(c,"categorias","nombre",category);
    String cat=""+catid,unit=catalogs.listar("unidad").get(0)[0];
    ok(product(h,csrf,0,"QA-"+suffix,"PRODUCTO","19.90","3",cat,unit,""+pid).statusCode()==302,"Product HTTP create");prod=key(c,"productos","codigo","QA-"+suffix);
    ok(products.buscar(prod).precio.toPlainString().equals("19.90"),"Price precision preserved");
    ok(get(h,"/productos").body().contains("Audio &lt;script&gt;"),"Product HTML escaped");
    ok(an.equals(scalar(c,"SELECT usuario_registro FROM productos WHERE id=?",prod)),"Product actor from session");
    supplierOutsidePage(c,h,csrf,prod,pid,suffix,cat,unit,an);
    ok(post(h,"/proveedores","csrf",csrf,"accion","eliminar","id",""+pid).statusCode()==400,"Supplier FK protects referenced product");
    ok(post(h,"/catalogos-productos","csrf",csrf,"accion","eliminar","clase","categoria","id",cat).statusCode()==400,"Category FK protects product");
    ok(post(h,"/productos","csrf",csrf,"accion","eliminar","id",""+prod).statusCode()==400,"Product with stock cannot be deleted");
    for(String price:new String[]{"-1","1.234","NaN","1000000000"})ok(product(h,csrf,prod,"QA-"+suffix,"PRODUCTO",price,"3",cat,unit,""+pid).statusCode()==400,"Invalid price "+price);
    for(String stock:new String[]{"-1","1.2345","NaN","1000000000"})ok(product(h,csrf,prod,"QA-"+suffix,"PRODUCTO","19.90",stock,cat,unit,""+pid).statusCode()==400,"Invalid stock "+stock);
    ok(product(h,csrf,prod,"QA-"+suffix,"PRODUCTO","19.90","3","2147483647",unit,""+pid).statusCode()==400,"Missing category rejected");
    ok(product(h,csrf,prod,"QA-"+suffix,"SERVICIO","19.90","3",cat,unit,""+pid).statusCode()==400,"Service stock rejected");
    ok(products.buscar(prod).stock.intValue()==3,"Invalid updates left original intact");
    ok(product(h,csrf,prod,"QA-"+suffix,"SERVICIO","20.00","0",cat,unit,""+pid).statusCode()==302,"Valid product update to service");
    ok(product(h,csrf,0,"QA-"+suffix,"SERVICIO","20","0",cat,unit,""+pid).statusCode()==400,"Duplicate SKU rejected");
    for(String route:new String[]{"/clientes","/proveedores","/productos"})ok(get(h,route+"?editar=2147483647").statusCode()==404,"Missing record "+route);
    ok(get(h,"/clientes?editar=-1").statusCode()==400,"Negative identifier rejected");
    ok(get(h,"/catalogos-productos?clase=usuarios").statusCode()==400,"Catalog allowlist");
    ok(post(h,"/productos","csrf",csrf,"accion","eliminar","id",""+prod).statusCode()==302,"Product HTTP delete");prod=0;
    ok(post(h,"/proveedores","csrf",csrf,"accion","eliminar","id",""+pid).statusCode()==302,"Supplier HTTP delete");pid=0;
    int person=clients.buscar(cid).personaId;
    ok(post(h,"/clientes","csrf",csrf,"accion","eliminar","id",""+cid).statusCode()==302,"Client HTTP delete");cid=0;
    ok(scalar(c,"SELECT nombres FROM datos_personales WHERE id_datos_personales=?",person)==null,"Unowned personal record cleaned");
    System.out.println("ETAPA 5: ALL CHECKS PASSED");
   }finally {
    if(prod>0){try(PreparedStatement p=c.prepareStatement("DELETE FROM productos WHERE id=?")){p.setInt(1,prod);p.executeUpdate();}}
    if(pid>0)suppliers.eliminar(pid);if(cid>0)clients.eliminar(cid);if(sharedId>0)clients.eliminar(sharedId);if(companyId>0)clients.eliminar(companyId);if(catid>0)catalogs.eliminar("categoria",catid);
    if(uid>0)users.accion(uid,-1,"eliminar");if(aid>0)users.accion(aid,-1,"eliminar");
   }
  }
 }
}
