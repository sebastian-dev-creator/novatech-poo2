import com.mycompany.novatech.app.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
/** Cuenta desechable exclusiva del entorno local para comprobar el cierre real. */
public class FixtureCierre extends IntegrationEtapa4 {
 public static void main(String[] args)throws Exception {
  localOnly();String name="qa_cierre_profesor";
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
   JdbcUsuarioRepository users=new JdbcUsuarioRepository();int uid=id(c,name);
   if(args.length>0&&args[0].equals("limpiar")){if(uid>0)users.accion(uid,-1,"eliminar");return;}
   if(uid>0)throw new IllegalStateException("Ya existe la cuenta de prueba.");
   int role=0;for(String[] r:users.roles())if(r[1].equals("OPERADOR"))role=Integer.parseInt(r[0]);
   users.guardar(0,name,"Prueba","Cierre local",role,"CierreLocalPrueba!2026",true);
   System.out.println("Cuenta local de prueba creada.");
  }
 }
}
