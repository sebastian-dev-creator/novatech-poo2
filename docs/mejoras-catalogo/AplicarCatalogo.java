import java.nio.file.*;
import java.sql.*;
import com.mycompany.novatech.config.ConexionBD;

/** Carga transaccional e idempotente. Requiere un respaldo previo en producción. */
public class AplicarCatalogo {
    public static void main(String[] args)throws Exception {
        String url=System.getenv("NOVATECH_DB_URL");
        boolean local=url!=null&&url.startsWith("jdbc:mysql://127.0.0.1:23307/novatech?");
        if(!local && !(url!=null&&url.contains("aivencloud.com")&&args.length==1&&"--produccion-respaldada".equals(args[0])))
            throw new IllegalStateException("Entorno no autorizado para carga.");
        String script=Files.readString(Path.of("database/catalogo-novatech.sql")).replaceAll("(?m)^--[^\\r\\n]*", "");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try(Statement s=c.createStatement()) {
                int n=0;for(String sql:script.split(";"))if(!sql.isBlank()){s.execute(sql);n++;}
                for(String tabla:new String[]{"productos","clientes","proveedores"}) {
                    try(ResultSet r=s.executeQuery("SELECT COUNT(*) FROM "+tabla+" WHERE usuario_registro='CATALOGO_NOVATECH_2026'")) {
                        r.next();int count=r.getInt(1),esperado=tabla.equals("productos")?128:tabla.equals("clientes")?60:12;
                        if(count!=esperado)throw new SQLException("Cantidad inesperada en "+tabla+": "+count);
                        System.out.println(tabla+": "+count);
                    }
                }
                c.commit();System.out.println("Catálogo confirmado: "+n+" sentencias.");
            }catch(Exception e){c.rollback();throw e;}
        }
    }
}
