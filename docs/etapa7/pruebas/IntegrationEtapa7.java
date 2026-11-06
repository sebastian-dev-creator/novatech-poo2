import com.mycompany.novatech.app.*;
import com.mycompany.novatech.app.comercial.*;
import com.mycompany.novatech.app.reportes.*;
import com.mycompany.novatech.app.ventas.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.math.BigDecimal;
import java.net.*;
import java.net.http.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.*;
import java.nio.file.*;

/** Ejercita las entradas HTTP y MySQL reales, exclusivamente en QA local. */
public class IntegrationEtapa7 extends IntegrationEtapa4 {
    static int checks;
    static void check(boolean value,String text){ok(value,text);checks++;}
    interface Op { void run()throws Exception; }
    static void reject(Op op,String text)throws Exception {
        boolean rechazo=false;try{op.run();}catch(IllegalArgumentException|SQLException e){rechazo=true;}check(rechazo,text);
    }
    static BigDecimal num(String value){return new BigDecimal(value);}
    static void execute(Connection c,String sql,Object... values)throws Exception {
        try(PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();}
    }
    static BigDecimal scalar(Connection c,String sql,int id)throws Exception {
        try(PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){r.next();return r.getBigDecimal(1);}}
    }
    static CompraRepository.Compra compra(int proveedor,int producto,String cantidad,String costo) {
        CompraRepository.Compra x=new CompraRepository.Compra();x.proveedor=proveedor;x.clave=UUID.randomUUID().toString();
        x.prevista=LocalDate.now().plusDays(2);x.observacion="Prueba <script>";
        CompraRepository.Linea l=new CompraRepository.Linea();l.producto=producto;l.cantidad=num(cantidad);l.costo=num(costo);x.lineas.add(l);return x;
    }
    static ResultadoReporte report(String id)throws Exception {
        return new ReporteRepository().consultar(CatalogoReportes.buscar(id),new FiltroReporte("2090-02-01","2090-02-02","DIA"),1000);
    }
    static List<Object> row(ResultadoReporte r,String contains) {
        return r.filas.stream().filter(x->x.get(0).toString().contains(contains)).findFirst().orElseThrow();
    }
    public static void main(String[] args)throws Exception {
        localOnly();String suf=Long.toString(System.currentTimeMillis()),actor="qa7_"+suf,operador="qa7op_"+suf,password=UUID.randomUUID().toString();
        JdbcUsuarioRepository users=new JdbcUsuarioRepository();JdbcClienteRepository clients=new JdbcClienteRepository();
        JdbcProveedorRepository suppliers=new JdbcProveedorRepository();JdbcProductoRepository products=new JdbcProductoRepository();
        CompraRepository purchases=new CompraRepository();JdbcVentaRepository sales=new JdbcVentaRepository();
        int cid=0,pid=0,sid=0;
        ContextoOperacion.establecer(actor);
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            try {
                int admin=0,operator=0;for(String[] r:users.roles()){if(r[1].equals("ADMINISTRADOR"))admin=Integer.parseInt(r[0]);if(r[1].equals("OPERADOR"))operator=Integer.parseInt(r[0]);}
                users.guardar(0,actor,"QA","Reportes",admin,password,true);users.guardar(0,operador,"QA","Operador",operator,password,true);int uid=id(c,actor);
                Cliente cliente=new Cliente();cliente.nombres="Cliente QA7";cliente.apellidos=suf;cliente.documento="8"+suf.substring(suf.length()-7);cid=clients.guardar(cliente,actor);
                Proveedor proveedor=new Proveedor();proveedor.razonSocial="Proveedor QA7 "+suf;proveedor.nombres="QA";proveedor.apellidos="Reportes";proveedor.documento="28"+suf.substring(suf.length()-9);sid=suppliers.guardar(proveedor,actor);
                CatalogoComercialRepository cats=new CatalogoComercialRepository();int cat=Integer.parseInt(cats.listar("categoria").get(0)[0]),unit=Integer.parseInt(cats.listar("unidad").get(0)[0]);
                pid=products.guardar(new Producto.Builder().identidad(0,"QA7-"+suf,"=1+1 <script>").clasificacion("PRODUCTO",cat,0,unit,sid).valores("20.00","10","2").build(),actor);
                execute(c,"UPDATE productos SET costo=5 WHERE id=?",pid);
                HttpClient h=client(),other=client();check(get(h,"/reportes").statusCode()==302,"Reportes requiere sesión");
                post(h,"/login","csrf",token(get(h,"/login").body()),"usuario",actor,"password",password);
                post(other,"/login","csrf",token(get(other,"/login").body()),"usuario",operador,"password",password);
                String csrf=token(get(h,"/menu").body());
                for(String path:Arrays.asList("/reportes","/compras","/datos-reportes"))check(get(other,path).statusCode()==403,"Operador sin permiso rechazado "+path);
                for(String path:Arrays.asList("/reportes","/compras","/compras?nuevo=1","/datos-reportes"))check(get(h,path).statusCode()==200,"Vista JSP "+path);
                check(post(h,"/compras","accion","registrar").statusCode()==403,"CSRF compras");
                check(get(h,"/reportes?reporte=v02&desde=2090-02-03&hasta=2090-02-01").statusCode()==400,"Fechas invertidas rechazadas");
                check(get(h,"/reportes?reporte=inexistente").statusCode()==400,"Reporte arbitrario rechazado");
                check(get(h,"/reportes?reporte=v02&periodo=SQL").statusCode()==400,"Agrupación fuera de catálogo rechazada");
                check(post(h,"/datos-reportes","csrf",csrf,"tipo","usuarios","id",""+uid,"valor","Ventas QA7").statusCode()==200,"Asignar área por HTTP");
                check(post(h,"/datos-reportes","csrf",csrf,"tipo","clientes","id",""+cid,"valor","Frecuente QA7").statusCode()==200,"Asignar segmento por HTTP");
                check(post(h,"/datos-reportes","csrf",csrf,"tipo","proveedores","id",""+sid,"valor","Tecnología QA7").statusCode()==200,"Asignar categoría por HTTP");
                check(post(h,"/datos-reportes","csrf",csrf,"tipo","productos","id",""+pid,"valor","-1","motivo","Prueba").statusCode()==400,"Costo negativo rechazado");
                CompraRepository.Compra x=compra(sid,pid,"10","7.00");
                HttpResponse<String> created=post(h,"/compras?nuevo=1","csrf",csrf,"accion","registrar","clave",x.clave,"proveedor",""+sid,"prevista",x.prevista.toString(),"observacion",x.observacion,"producto",""+pid,"cantidad","10","costo","7.00");
                check(created.statusCode()==302,"Registrar compra por HTTP");int purchase=Integer.parseInt(created.headers().firstValue("location").orElseThrow().split("ver=")[1]);
                check(purchases.registrar(x,actor)==purchase,"Reenvío de clave no duplica compra");
                check(get(h,"/compras?ver="+purchase).body().contains("&lt;script&gt;"),"Compra escapa HTML almacenado");
                check(post(h,"/compras","csrf",csrf,"accion","recibir","id",""+purchase,"version","1","evaluacion","5").statusCode()==302,"Recibir compra por HTTP");
                check(products.buscar(pid).stock.compareTo(num("20"))==0,"Recepción suma stock una vez");
                check(scalar(c,"SELECT costo FROM productos WHERE id=?",pid).compareTo(num("6"))==0,"Costo promedio ponderado 6.00");
                reject(()->purchases.cambiar(purchase,1,"recibir",5),"Reintento de recepción obsoleta rechazado");
                check(products.buscar(pid).stock.compareTo(num("20"))==0,"Reintento conserva stock");
                check(post(h,"/compras","csrf",csrf,"accion","pagar","id",""+purchase,"version","2").statusCode()==302,"Pago de compra por HTTP");
                reject(()->purchases.cambiar(purchase,3,"pagar",0),"Pago repetido rechazado");
                CompraRepository.Compra cancel=compra(sid,pid,"1","3");int cancelled=purchases.registrar(cancel,actor);purchases.cambiar(cancelled,1,"anular",0);
                check(purchases.buscar(cancelled).estado.equals("ANULADA"),"Anular compra pendiente conserva orden");
                reject(()->purchases.cambiar(cancelled,2,"recibir",4),"Compra anulada no recibible");
                final CompraRepository.Compra invalid=compra(sid,pid,"0","5");reject(()->purchases.registrar(invalid,actor),"Cantidad cero rechazada");
                Venta v=new Venta();v.clienteId=cid;v.porcentaje=num("10");v.detalles.add(new DetalleVenta(pid,num("2")));sales.guardar(v,uid,actor);sales.pagar(v.id,1,"EFECTIVO",actor);
                execute(c,"UPDATE ventas SET fecha_creacion='2090-02-01 10:00:00',fecha_pago='2090-02-01 11:00:00' WHERE id=?",v.id);
                List<Object> rent=row(report("v14"),"A-"+pid+" ·");
                check(new BigDecimal(rent.get(1).toString()).compareTo(num("36"))==0,"Rentabilidad usa descuento de cabecera");
                check(new BigDecimal(rent.get(2).toString()).compareTo(num("12"))==0,"Costo de venta congelado 12.00");
                check(new BigDecimal(rent.get(3).toString()).compareTo(num("24"))==0,"Margen bruto 24.00");
                execute(c,"UPDATE productos SET costo=10 WHERE id=?",pid);
                check(new BigDecimal(row(report("v14"),"A-"+pid+" ·").get(3).toString()).compareTo(num("24"))==0,"Cambio de costo actual conserva margen histórico");
                sales.anular(v.id,2,actor);execute(c,"UPDATE ventas SET fecha_anulacion='2090-02-02 12:00:00' WHERE id=?",v.id);
                ResultadoReporte ingresos=report("v12");
                check(new BigDecimal(row(ingresos,"2090-02-01").get(3).toString()).compareTo(num("36"))==0,"Cobro permanece en día original");
                check(new BigDecimal(row(ingresos,"2090-02-02").get(3).toString()).compareTo(num("-36"))==0,"Devolución se informa en día de anulación");
                check(scalar(c,"SELECT costo FROM productos WHERE id=?",pid).compareTo(num("9.60"))==0,"Devolución incorpora costo histórico al promedio actual");
                check(report("v14").filas.isEmpty(),"Venta anulada excluida del margen vigente");
                execute(c,"UPDATE productos SET costo=NULL WHERE id=?",pid);
                Venta unknown=new Venta();unknown.clienteId=cid;unknown.detalles.add(new DetalleVenta(pid,num("1")));sales.guardar(unknown,uid,actor);sales.pagar(unknown.id,1,"EFECTIVO",actor);
                execute(c,"UPDATE ventas SET fecha_creacion='2090-02-01 23:59:59',fecha_pago='2090-02-01 23:59:59' WHERE id=?",unknown.id);
                check(row(report("v14"),"A-"+pid+" ·").get(3)==null,"Costo desconocido produce margen vacío");
                ResultadoReporte boundary=new ReporteRepository().consultar(CatalogoReportes.buscar("v02"),new FiltroReporte("2090-02-01","2090-02-01","DIA"),1000);
                check(boundary.filas.size()==1,"Fecha final incluye 23:59:59");
                for(Reporte r:CatalogoReportes.TODOS) {
                    ResultadoReporte resultado=new ReporteRepository().consultar(r,new FiltroReporte(null,null,"SEMANA"),50000);
                    check(!resultado.columnas.isEmpty(),"Consulta SQL "+r.id);
                    check(get(h,"/reportes?reporte="+r.id).statusCode()==200,"HTTP reporte "+r.id);
                    HttpResponse<byte[]> excel=h.send(HttpRequest.newBuilder(URI.create(ROOT+"/reportes?reporte="+r.id+"&formato=xlsx")).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
                    check(excel.statusCode()==200 && excel.headers().firstValue("content-type").orElse("").contains("spreadsheetml"),"Excel HTTP "+r.id);
                    int sheets=0;try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(excel.body()))){ZipEntry entry;while((entry=zip.getNextEntry())!=null){byte[] bytes=zip.readAllBytes();if(entry.getName().startsWith("xl/worksheets/")){
                        sheets++;String xml=new String(bytes,java.nio.charset.StandardCharsets.UTF_8);check(!xml.contains("<f>"),"Sin fórmulas inyectadas "+r.id);
                        javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
                    }}}
                    check(sheets==2,"Libro con datos y criterios "+r.id);
                    if(r.id.equals("p10")){Files.createDirectories(Path.of("target/qa-etapa7"));Files.write(Path.of("target/qa-etapa7/precios.xlsx"),excel.body());}
                }
                check(scalar(c,"SELECT COUNT(*) FROM accesos WHERE usuario=(SELECT nombre_usuario FROM usuarios WHERE id_usuario=?)",uid).intValue()>0,"Historial registra acceso correcto");
                check(scalar(c,"SELECT COUNT(*) FROM movimientos_inventario WHERE id_producto=?",pid).intValue()>=5,"Kárdex registra recepción, ventas y devolución");
                check(new ReporteRepository().consultar(CatalogoReportes.buscar("p10"),new FiltroReporte(null,null,null),1).truncado,"Detectar límite de pantalla");
                System.out.println("TOTAL COMPROBACIONES ETAPA 7: "+checks);
            } finally {
                execute(c,"DELETE d FROM detalle_compra d JOIN compras c ON c.id=d.id_compra WHERE c.usuario=?",actor);
                execute(c,"DELETE FROM compras WHERE usuario=?",actor);
                execute(c,"DELETE g FROM guias g JOIN ventas v ON v.id=g.id_venta WHERE v.usuario_registro=?",actor);
                execute(c,"DELETE s FROM solped s JOIN usuarios u ON u.id_usuario=s.id_usuario WHERE u.nombre_usuario=?",actor);
                execute(c,"DELETE FROM ventas WHERE usuario_registro=?",actor);
                if(pid>0)execute(c,"DELETE FROM productos WHERE id=?",pid);
                if(cid>0)clients.eliminar(cid);if(sid>0)suppliers.eliminar(sid);
                for(String usuario:Arrays.asList(actor,operador)){int id=id(c,usuario);if(id>0)users.accion(id,-1,"eliminar");}
                execute(c,"DELETE FROM accesos WHERE usuario IN (?,?)",actor,operador);
                execute(c,"DELETE FROM movimientos_inventario WHERE id_producto=?",pid);
                execute(c,"DELETE FROM auditoria WHERE usuario=?",actor);
                System.out.println("Fixture local retirado.");
            }
        } finally { ContextoOperacion.limpiar(); }
    }
}
