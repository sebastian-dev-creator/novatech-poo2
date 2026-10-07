import com.mycompany.novatech.app.*;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.net.http.*;
public class IntegrationExcel extends IntegrationEtapa4 {
 static int geo(UbicacionRepository r,String nivel,String nombre)throws Exception{return r.listar(nivel).stream().filter(x->x[1].equals(nombre)).mapToInt(x->Integer.parseInt(x[0])).findFirst().orElseThrow();}
 public static void main(String[] args)throws Exception{
  localOnly();
  JdbcUsuarioRepository users=new JdbcUsuarioRepository();JdbcContactoRepository contacts=new JdbcContactoRepository();UbicacionRepository geo=new UbicacionRepository();
  String suffix=Long.toString(System.currentTimeMillis()),an="qa_excel_a"+suffix,un="qa_excel_u"+suffix,pw="LocalExcelPrueba!";
  String[] niveles={"pais","departamento","provincia","distrito"};int[] ids=new int[4];int aid=0,uid=0,rid=0;
  try(Connection c=ConexionBD.getInstancia().obtenerConexion()){
   try{
    int ar=0,op=0;try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT id_rol,nombre FROM roles")){while(r.next()){if("ADMINISTRADOR".equals(r.getString(2)))ar=r.getInt(1);if("OPERADOR".equals(r.getString(2)))op=r.getInt(1);}}
    users.guardar(0,an,"Excel","Admin",ar,pw,true);aid=id(c,an);users.guardar(0,un,"Excel","Operador",op,pw,true);uid=id(c,un);
    ok(users.buscar(aid) instanceof UsuarioAdministrador,"Factory Method creates administrator");ok(users.buscar(uid) instanceof UsuarioOperativo,"Factory Method creates operator");
    HttpClient h=client();post(h,"/login","csrf",token(get(h,"/login").body()),"usuario",an,"password",pw);String csrf=token(get(h,"/menu").body());
    ok(get(h,"/abrir?opcion=usuarios").headers().firstValue("location").orElse("").endsWith("/usuarios"),"Menu command redirects");ok(get(h,"/abrir?opcion=incorrecta").statusCode()==404,"Mediator rejects unknown command");
    for(int i=0;i<4;i++){
     String name="ExcelQA"+suffix+i;var response=post(h,"/ubicaciones","csrf",csrf,"accion","guardar","nivel",niveles[i],"id","0","nombre",name,"padre",String.valueOf(i==0?0:ids[i-1]),"activo","on");
     ok(response.statusCode()==302,"HTTP location create "+niveles[i]);ids[i]=geo(geo,niveles[i],name);ok(get(h,"/ubicaciones?nivel="+niveles[i]+"&editar="+ids[i]).statusCode()==200,"Location JSP "+niveles[i]);
    }
    ok(post(h,"/ubicaciones","csrf",csrf,"accion","eliminar","nivel","pais","id",String.valueOf(ids[0])).statusCode()==400,"FK preserves location with children");
    String[] clases={"correo","telefono","direccion"},values={"excel@example.com","+51 999 123 456","Avenida Ejemplo"};
    for(int i=0;i<3;i++){
     String cl=clases[i];String path="/contactos?usuarioId="+uid+"&clase="+cl;
     ok(get(h,path).statusCode()==200,"Contact JSP "+cl);
     ok(post(h,"/contactos","csrf",csrf,"accion","guardar","usuarioId",String.valueOf(uid),"clase",cl,"id","0","valor",values[i],"tipo","personal","activo","on","distrito",String.valueOf(ids[3]),"numero","123","piso","2","referencia","Cerca del parque").statusCode()==302,"HTTP contact create "+cl);
     Contacto x=contacts.listar(uid,cl).get(0);x.tipo="laboral";contacts.guardar(x,cl,an);ok(contacts.listar(uid,cl).get(0).tipo.equals("laboral"),"Contact update "+cl);
     ok(get(h,path+"&editar="+x.id).body().contains("laboral"),"Edit JSP persists contact "+cl);
     ok(post(h,"/contactos","csrf",csrf,"accion","eliminar","usuarioId",String.valueOf(aid),"clase",cl,"id",String.valueOf(x.id)).statusCode()==400,"Contact ownership protected "+cl);
    }
    ok(post(h,"/contactos","csrf",csrf,"accion","guardar","usuarioId",String.valueOf(uid),"clase","correo","id","0","valor","excel@example.com","tipo","personal","activo","on").statusCode()==400,"Duplicate email rejected");
    ok(post(h,"/contactos","csrf",csrf,"accion","guardar","usuarioId",String.valueOf(uid),"clase","telefono","id","0","valor","-----","tipo","personal","activo","on").statusCode()==400,"Invalid phone rejected");
    HttpClient operator=client();post(operator,"/login","csrf",token(get(operator,"/login").body()),"usuario",un,"password",pw);
    ok(!get(operator,"/menu").body().contains("opcion=usuarios"),"Mediator hides administrator choices");
    for(String path:new String[]{"/abrir?opcion=usuarios","/contactos?usuarioId="+uid,"/ubicaciones"})ok(get(operator,path).statusCode()==403,"Operator denied "+path);
    ok(post(h,"/roles","csrf",csrf,"accion","guardar","id","0","nombre","QA_EXCEL_TEMP","descripcion","Permisos temporales","actor","FALSIFICADO","usuario_registro","FALSIFICADO").statusCode()==302,"HTTP role create");
    for(String[] r:users.roles())if(r[1].equals("QA_EXCEL_TEMP"))rid=Integer.parseInt(r[0]);
    ok(rid>0,"Role persisted");
    try(PreparedStatement p=c.prepareStatement("SELECT usuario_registro FROM roles WHERE id_rol=?")){p.setInt(1,rid);try(ResultSet r=p.executeQuery()){ok(r.next()&&an.equals(r.getString(1)),"Role creator comes from authenticated session, not form");}}
    users.guardarRol(rid,"QA_EXCEL_TEMP","Descripción editada",un);
    try(PreparedStatement p=c.prepareStatement("SELECT usuario_registro,descripcion FROM roles WHERE id_rol=?")){p.setInt(1,rid);try(ResultSet r=p.executeQuery()){ok(r.next()&&an.equals(r.getString(1))&&"Descripción editada".equals(r.getString(2)),"Editing preserves original role creator");}}
    boolean actorRechazado=false;
    try{users.guardarRol(0,"QA_SIN_ACTOR","No debe guardarse",null);}catch(IllegalArgumentException expected){actorRechazado=true;}
    ok(actorRechazado,"Role creation requires an actor");
    users.guardar(uid,un,"Excel","Operador",rid,"",true);
    PermisoRepository permisos=new PermisoRepository();int gestion=0;for(String[] p:permisos.delRol(rid))if(p[1].equals("GESTIONAR_USUARIOS"))gestion=Integer.parseInt(p[0]);
    ok(get(h,"/permisos?rol="+rid).statusCode()==200,"Permission JSP renders");
    ok(post(h,"/permisos","csrf",csrf,"rol",String.valueOf(rid),"permiso",String.valueOf(gestion)).statusCode()==302,"HTTP permission assignment");
    ok(get(operator,"/usuarios").statusCode()==200,"Granted permission applies without login again");
    ok(get(operator,"/menu").body().contains("opcion=usuarios"),"Mediator reflects granted permission");
    ok(get(operator,"/permisos?rol="+rid).statusCode()==403,"Only administrator can assign permissions");
    String ot=token(get(operator,"/menu").body());ok(post(operator,"/usuarios","csrf",ot,"accion","desbloquear","id",String.valueOf(uid)).statusCode()==403,"Separate unlock permission enforced");
    ok(post(h,"/permisos","csrf",csrf,"rol",String.valueOf(rid)).statusCode()==302,"HTTP permission revocation");
    ok(get(operator,"/usuarios").statusCode()==403,"Revocation applies to current session");
    ok(post(h,"/permisos","csrf",csrf,"rol",String.valueOf(ar)).statusCode()==400,"Administrator permissions protected");
    users.accion(uid,aid,"eliminar");for(String cl:clases)ok(contacts.listar(uid,cl).isEmpty(),"User deletion cascades "+cl);
    uid=0;
   }finally{
    for(int key:new int[]{uid,aid})if(key>0&&users.buscar(key)!=null)users.accion(key,-1,"eliminar");
    if(rid>0)users.eliminarRol(rid);
    for(int i=3;i>=0;i--)if(ids[i]>0)geo.eliminar(niveles[i],ids[i]);
    System.out.println("Excel QA accounts and locations removed.");
   }
  }
 }
}
