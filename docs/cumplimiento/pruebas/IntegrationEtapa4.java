import com.mycompany.novatech.app.*;
import com.mycompany.novatech.config.ConexionBD;
import com.mycompany.novatech.seguridad.PasswordUtil;
import java.sql.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
public class IntegrationEtapa4 {
 static final String ROOT="http://127.0.0.1:18081/novatech";
 static HttpClient client(){return HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();}
 static HttpResponse<String> get(HttpClient h,String path)throws Exception{return h.send(HttpRequest.newBuilder(URI.create(ROOT+path)).GET().build(),HttpResponse.BodyHandlers.ofString());}
 static String token(String html){Matcher m=Pattern.compile("name=['\"]csrf['\"] value=['\"]([^'\"]+)").matcher(html);if(!m.find())throw new AssertionError("Missing CSRF");return m.group(1);}
 static HttpResponse<String> post(HttpClient h,String path,String... kv)throws Exception{StringJoiner b=new StringJoiner("&");for(int i=0;i<kv.length;i+=2)b.add(URLEncoder.encode(kv[i],StandardCharsets.UTF_8)+"="+URLEncoder.encode(kv[i+1],StandardCharsets.UTF_8));return h.send(HttpRequest.newBuilder(URI.create(ROOT+path)).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(b.toString())).build(),HttpResponse.BodyHandlers.ofString());}
 static void ok(boolean x,String m){if(!x)throw new AssertionError(m);System.out.println("PASS: "+m);}
 static int id(Connection c,String name)throws Exception{try(PreparedStatement p=c.prepareStatement("SELECT id_usuario FROM usuarios WHERE nombre_usuario=?")){p.setString(1,name);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}}}
 public static void main(String[] args)throws Exception{
  localOnly();
  String suffix=Long.toString(System.currentTimeMillis());String admin="qa_admin_"+suffix,user="qa_user_"+suffix,password=UUID.randomUUID().toString();
  UsuarioRepository repo=new JdbcUsuarioRepository();int aid=0,uid=0,op=0;
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()){
   try{
    int ar=0;try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id_rol,nombre FROM roles")){while(r.next()){if(r.getString(2).equals("ADMINISTRADOR"))ar=r.getInt(1);if(r.getString(2).equals("OPERADOR"))op=r.getInt(1);}}
    repo.guardar(0,admin,"Prueba","Temporal",ar,password,true);aid=id(c,admin);
    HttpClient h=client();ok(get(h,"/usuarios").statusCode()==302,"Anonymous redirected");
    HttpResponse<String> login=get(h,"/login");ok(login.statusCode()==200 && login.body().contains("Bienvenido"),"JSP login renders");
    ok(post(h,"/login","usuario",admin,"password",password).statusCode()==403,"CSRF enforced");
    ok(post(h,"/login","csrf",token(login.body()),"usuario",admin,"password",password).headers().firstValue("location").orElse("").endsWith("/menu"),"Login redirects to menu");
    ok(get(h,"/menu").body().contains("Gestión de usuarios"),"Admin menu renders");
    String csrf=token(get(h,"/usuarios").body());
    post(h,"/usuarios","csrf",csrf,"accion","guardar","id","0","usuario",user,"nombres","Prueba <script>","apellidos","Temporal","rol",String.valueOf(op),"password",password,"activo","on");uid=id(c,user);ok(uid>0,"HTTP user create");
    String table=get(h,"/usuarios").body();ok(table.contains("Prueba &lt;script&gt;") && !table.contains("Prueba <script>"),"Stored HTML escaped");
    ok(get(h,"/usuarios?editar="+uid).body().contains("<option value='"+op+"'"),"Role options populated");
    post(h,"/usuarios","csrf",csrf,"accion","guardar","id",String.valueOf(uid),"usuario",user,"nombres","Editado","apellidos","Temporal","rol",String.valueOf(op),"password","","activo","on");ok(repo.buscar(uid).nombres.equals("Editado"),"HTTP user update");
    HttpClient other=client();String t=token(get(other,"/login").body());post(other,"/login","csrf",t,"usuario",user,"password",password);ok(get(other,"/menu").statusCode()==200,"Operator login");ok(get(other,"/usuarios").statusCode()==403,"Operator denied user administration");
    t=token(get(other,"/menu").body());post(other,"/salir","csrf",t);ok(get(other,"/menu").statusCode()==302,"Logout invalidates session");
    for(int i=0;i<3;i++){t=token(get(other,"/login").body());HttpResponse<String> failure=post(other,"/login","csrf",t,"usuario",user,"password","incorrect-password"); ok(failure.headers().firstValue("location").orElse("").endsWith(i==2?"/acceso-cerrado":"/login"),"Failure redirect "+(i+1));}
    ok(get(other,"/acceso-cerrado").body().contains("Acceso cerrado"),"Closed page renders"); ok(get(other,"/menu").statusCode()==302,"No access after lock"); ok(repo.buscar(uid).bloqueado && repo.buscar(uid).intentos==3,"Three failures persist lock");ok(repo.autenticar(user,password)==null,"Correct password cannot bypass lock");
    post(h,"/usuarios","csrf",csrf,"accion","desbloquear","id",String.valueOf(uid));ok(repo.autenticar(user,password)!=null,"HTTP unlock restores access");
    post(h,"/usuarios","csrf",csrf,"accion","desactivar","id",String.valueOf(uid));ok(repo.autenticar(user,password)==null,"Inactive user denied");
    ok(get(h,"/roles").statusCode()==200,"Roles page renders");
    post(h,"/usuarios","csrf",csrf,"accion","eliminar","id",String.valueOf(uid));ok(repo.buscar(uid)==null,"HTTP user deletion");
   }finally{
    for(String name:new String[]{user,admin}){int key=id(c,name);if(key>0){try(PreparedStatement p=c.prepareStatement("DELETE FROM datos_personales WHERE id_usuario=?")){p.setInt(1,key);p.executeUpdate();}try(PreparedStatement p=c.prepareStatement("DELETE FROM usuarios WHERE id_usuario=?")){p.setInt(1,key);p.executeUpdate();}}}
    System.out.println("Temporary accounts removed.");
   }
  }
 }
 static void localOnly(){String url=System.getenv("NOVATECH_DB_URL");if(url==null||!url.startsWith("jdbc:mysql://127.0.0.1:23307/novatech?"))throw new IllegalStateException("Estas pruebas requieren la BD local aislada en 23307.");}
}


