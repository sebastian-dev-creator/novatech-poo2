import com.mycompany.novatech.app.*;
import com.mycompany.novatech.config.ConexionBD;
import java.net.http.*;
import java.sql.*;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class IntegrationBuscador extends IntegrationEtapa4 {
    static String buscar(String tipo,String contexto,String q,int pagina){return "/buscar-comercial?tipo="+tipo+"&contexto="+contexto+"&pagina="+pagina+"&q="+URLEncoder.encode(q,StandardCharsets.UTF_8);}
    static int count(String json){return json.split("\\\"id\\\":",-1).length-1;}
    public static void main(String[] args)throws Exception {
        localOnly();String actor="qa_search_"+System.currentTimeMillis(),password=UUID.randomUUID().toString();
        JdbcUsuarioRepository users=new JdbcUsuarioRepository();int uid=0;
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            try {
                int rol=0;for(String[] r:users.roles())if(r[1].equals("ADMINISTRADOR"))rol=Integer.parseInt(r[0]);
                users.guardar(0,actor,"QA","Buscador",rol,password,true);uid=id(c,actor);
                try(Statement s=c.createStatement()){s.execute("SET @nt_actor='"+actor+"'");}
                c.setAutoCommit(false);
                try(PreparedStatement p=c.prepareStatement("INSERT INTO productos(codigo,nombre,tipo,id_categoria,id_unidad,precio,stock,stock_minimo,usuario_registro,activo) SELECT ?,?,'PRODUCTO',MIN(id),(SELECT MIN(id) FROM unidades_medida),25,5,1,?,? FROM categorias")) {
                    for(int i=0;i<10000;i++){
                        p.setString(1,actor+"-"+String.format("%05d",i));p.setString(2,String.format("BuscadorEscala %05d Auriculares",i));p.setString(3,actor);p.setBoolean(4,i!=9999);p.addBatch();
                        if(i%500==499)p.executeBatch();
                    }
                }
                c.commit();c.setAutoCommit(true);
                HttpClient h=client();ok(get(h,buscar("productos","ventas","audio",0)).statusCode()==302,"Búsqueda exige sesión");
                post(h,"/login","csrf",token(get(h,"/login").body()),"usuario",actor,"password",password);
                long inicio=System.nanoTime();String first=get(h,buscar("productos","ventas","BuscadorEscala",0)).body();
                long ms=(System.nanoTime()-inicio)/1000000;
                ok(count(first)==20&&first.contains("\"mas\":true"),"10000 artículos: primera página limitada a 20");
                String next=get(h,buscar("productos","ventas","BuscadorEscala",1)).body();
                ok(count(next)==20&&!next.equals(first)&&next.contains("00020"),"Página siguiente sin repetir primera página");
                ok(get(h,buscar("productos","ventas","00000 Auriculares",0)).body().contains("00000"),"Encuentra artículo antiguo fuera de los últimos 500");
                ok(get(h,buscar("productos","ventas","auriculares buscadorescala 09998",0)).body().contains("09998"),"Palabras en cualquier orden y mayúsculas/minúsculas");
                ok(count(get(h,buscar("productos","ventas","09999",0)).body())==0,"No ofrece artículos inactivos");
                ok(count(get(h,buscar("productos","ventas","%_",0)).body())==0,"Porcentaje y guion bajo son texto literal");
                ok(count(get(h,buscar("productos","ventas","' OR 1=1 --",0)).body())==0,"Entrada SQL tratada como texto");
                ok(count(get(h,buscar("productos","ventas","a",0)).body())==0,"No carga catálogo completo al escribir un carácter");
                ok(get(h,buscar("productos","ventas","audio",-1)).statusCode()==400,"Paginación negativa rechazada");
                ok(get(h,buscar("proveedores","ventas","Digital",0)).statusCode()==400,"Tipos ajenos al contexto rechazados");
                ok(get(h,buscar("clientes","ventas","Valeria Salazar",0)).body().contains("98010001"),"Busca cliente por nombre y apellido");
                ok(get(h,buscar("clientes","ventas","98010001",0)).body().contains("Valeria"),"Busca cliente por documento");
                ok(get(h,buscar("proveedores","compras","Horizonte",0)).body().contains("Horizonte"),"Busca proveedor por razón social");
                String form=get(h,"/ventas?nuevo=1").body();
                ok(form.contains("data-buscar=\"productos\"")&&!form.contains("BuscadorEscala"),"Formulario no incrusta diez mil productos");
                ok(get(h,"/compras?nuevo=1").statusCode()==200,"Formulario de compras renderiza");
                int clienteId=0,productoId=0;
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT c.id FROM clientes c JOIN datos_personales d ON d.id_datos_personales=c.id_datos_personales WHERE d.dni='98010001'")){r.next();clienteId=r.getInt(1);}
                try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id FROM productos WHERE codigo='NT-AUD-001'")){r.next();productoId=r.getInt(1);}
                String agregado=post(h,"/ventas","csrf",token(form),"accion","agregar","cliente",""+clienteId,"producto",""+productoId,"cantidad","2","tipo","BOLETA","porcentaje","0").body();
                ok(agregado.contains("Valeria")&&agregado.contains("NT-AUD-001")&&agregado.contains("Art\u00edculo 2"),"Agregar línea conserva cliente y artículo elegidos sin cargar todo el catálogo");
                int operador=0;for(String[] r:users.roles())if(r[1].equals("OPERADOR"))operador=Integer.parseInt(r[0]);
                users.guardar(uid,actor,"QA","Buscador",operador,"",true);
                ok(get(h,buscar("productos","ventas","audio",0)).statusCode()==403,"Usuario sin permiso no consulta artículos de ventas");
                ok(get(h,buscar("proveedores","compras","Digital",0)).statusCode()==403,"Usuario sin permiso no consulta proveedores");
                System.out.println("Tiempo HTTP de búsqueda con 10000 artículos locales: "+ms+" ms");
            }finally {
                if(!c.getAutoCommit())c.rollback();c.setAutoCommit(true);
                try(Statement s=c.createStatement()) {
                    s.executeUpdate("DELETE FROM movimientos_inventario WHERE id_producto IN(SELECT id FROM productos WHERE usuario_registro='"+actor+"')");
                    s.executeUpdate("DELETE FROM productos WHERE usuario_registro='"+actor+"'");
                    s.executeUpdate("DELETE FROM datos_personales WHERE id_usuario="+uid);
                    s.executeUpdate("DELETE FROM usuarios WHERE id_usuario="+uid);
                    s.executeUpdate("DELETE FROM auditoria WHERE usuario='"+actor+"'");
                    s.executeUpdate("DELETE FROM accesos WHERE usuario='"+actor+"'");
                }
                System.out.println("Fixture de escala retirado. Catálogo académico conservado.");
            }
        }
    }
}
