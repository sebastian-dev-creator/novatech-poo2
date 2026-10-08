import com.mycompany.novatech.app.*;
import com.mycompany.novatech.app.comercial.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;

/** Datos ficticios para revisar las pantallas. Nunca puede apuntar a Aiven. */
public class FixtureEtapa5 extends IntegrationEtapa4 {
 public static void main(String[] args)throws Exception {
  localOnly();
  JdbcUsuarioRepository users=new JdbcUsuarioRepository();
  ClienteRepository clients=new JdbcClienteRepository();ProveedorRepository suppliers=new JdbcProveedorRepository();ProductoRepository products=new JdbcProductoRepository();
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
   if(args.length>0&&args[0].equals("limpiar")) {
    for(Producto p:products.listar("DEMO-ETAPA5")){try(PreparedStatement st=c.prepareStatement("DELETE FROM productos WHERE id=?")){st.setInt(1,p.id);st.executeUpdate();}}
    for(Proveedor p:suppliers.listar("DEMO Etapa Cinco"))suppliers.eliminar(p.id);
    for(Cliente x:clients.listar("DEMO Etapa Cinco"))clients.eliminar(x.id);
    int uid=id(c,"demo_etapa5_local");if(uid>0)users.accion(uid,-1,"eliminar");System.out.println("Datos DEMO locales retirados.");return;
   }
   if(id(c,"demo_etapa5_local")>0)throw new IllegalStateException("El usuario demo ya existe. No se sobrescribe.");
   int ar=0;for(String[] r:users.roles())if(r[1].equals("ADMINISTRADOR"))ar=Integer.parseInt(r[0]);
   users.guardar(0,"demo_etapa5_local","Demo","Etapa Cinco",ar,"DemoLocalEtapaCinco!",true);
   Cliente cl=new Cliente();cl.nombres="DEMO Etapa Cinco";cl.apellidos="Cliente ficticio";cl.documento="90000001";cl.correo="cliente@example.invalid";cl.telefono="999000001";clients.guardar(cl,"demo_etapa5_local");
   Proveedor pr=new Proveedor();pr.nombres="Contacto";pr.apellidos="Ficticio";pr.razonSocial="DEMO Etapa Cinco · Distribuidor";pr.documento="20000000001";pr.correo="proveedor@example.invalid";int proveedor=suppliers.guardar(pr,"demo_etapa5_local");
   CatalogoComercialRepository catalogs=new CatalogoComercialRepository();int cat=Integer.parseInt(catalogs.listar("categoria").get(0)[0]),unit=Integer.parseInt(catalogs.listar("unidad").get(0)[0]);
   products.guardar(new Producto.Builder().identidad(0,"DEMO-ETAPA5-AUDIO","Auriculares de demostración").clasificacion("PRODUCTO",cat,0,unit,proveedor).valores("249.90","12","3").descripcion("Registro ficticio para revisar el CRUD local.").build(),"demo_etapa5_local");
   products.guardar(new Producto.Builder().identidad(0,"DEMO-ETAPA5-SERVICIO","Configuración de audio · Demo").clasificacion("SERVICIO",cat,0,unit,proveedor).valores("35.00","0","0").build(),"demo_etapa5_local");
   System.out.println("Datos ficticios creados exclusivamente en QA local.");
  }
 }
}
