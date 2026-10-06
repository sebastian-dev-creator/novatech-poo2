package com.mycompany.novatech.app;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(urlPatterns={"/login","/menu","/usuarios","/roles","/salir"})
public final class AppServlet extends HttpServlet {
    private final UsuarioRepository repo=new UsuarioRepository();
    public static String e(Object value){return value==null?"":value.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
    private String valor(HttpServletRequest q,String k){String x=q.getParameter(k);return x==null?"":x;}
    private int numero(HttpServletRequest q,String k){String x=valor(q,k);if(x.isEmpty())return 0;try{return Integer.parseInt(x);}catch(NumberFormatException e){throw new IllegalArgumentException("Identificador inválido.");}}
    private String csrf(HttpServletRequest q){return "<input type='hidden' name='csrf' value='"+e(q.getSession().getAttribute("csrf"))+"'>";}
    private String base(HttpServletRequest q){return q.getContextPath();}
    private String campo(String label,String name,String value,String type,int max,boolean obligatorio){return "<label>"+label+"<input name='"+name+"' type='"+type+"' value='"+e(value)+"' maxlength='"+max+"' "+(obligatorio?"required":"")+"></label>";}
    private void pagina(HttpServletRequest q,HttpServletResponse s,String titulo,String contenido) throws ServletException,IOException {
        String mensaje=(String)q.getSession().getAttribute("mensaje");q.getSession().removeAttribute("mensaje");
        q.setAttribute("titulo",titulo);q.setAttribute("contenido",contenido);q.setAttribute("aviso",mensaje);
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    private void aviso(HttpServletRequest q,HttpServletResponse s,String mensaje,String ruta) throws IOException{q.getSession().setAttribute("mensaje",mensaje);s.sendRedirect(base(q)+ruta);}
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        Usuario actual=(Usuario)q.getAttribute("actual");String path=q.getServletPath();
        try {
            if("/login".equals(path)) {
                if(actual!=null){s.sendRedirect(base(q)+"/menu");return;}
                pagina(q,s,"Acceso al sistema","<section class='login'><div class='intro'><span class='eyebrow'>NOVATECH · GESTIÓN COMERCIAL</span><h1>Todo empieza con una buena conexión.</h1><p>Accede a tu espacio de trabajo para gestionar el negocio.</p><div class='badge'>Avance POO II · Acceso y usuarios</div></div><form class='card' method='post' action='"+base(q)+"/login'>"+csrf(q)+"<h2>Bienvenido</h2><p class='muted'>Ingresa con tu cuenta de NovaTech.</p>"+campo("Usuario","usuario","","text",50,true)+"<label>Contraseña<input type='password' name='password' maxlength='128' autocomplete='current-password' required></label><button>Ingresar al sistema</button><p class='small'>Tras 3 intentos incorrectos, la cuenta se bloquea. Un administrador puede desbloquearla.</p></form></section>");
            } else if("/menu".equals(path)) {
                String cards="<article class='card'><span class='eyebrow'>TU CUENTA</span><h2>"+e(actual.username)+"</h2><p>Rol: "+e(actual.rol)+"</p><p>Sesión activa y acceso validado.</p></article>";
                if(actual.esAdministrador()) cards+="<a class='card linkcard' href='"+base(q)+"/usuarios'><span class='eyebrow'>ADMINISTRACIÓN</span><h2>Gestión de usuarios →</h2><p>Crea cuentas, edita datos y desbloquea accesos.</p></a><a class='card linkcard' href='"+base(q)+"/roles'><span class='eyebrow'>SEGURIDAD</span><h2>Roles →</h2><p>Administra los perfiles de acceso del sistema.</p></a>";
                pagina(q,s,"Menú principal","<span class='eyebrow'>ESPACIO DE TRABAJO</span><h1>Hola, "+e(actual.nombres)+".</h1><p class='muted'>Bienvenido al panel de NovaTech.</p><div class='cards'>"+cards+"</div><section class='card later'><h2>Próximos módulos</h2><p>Clientes · Productos · Proveedores · Pedidos · Reportes</p><p class='small'>Estos módulos quedan fuera de este avance y todavía no están implementados.</p></section>");
            } else if("/usuarios".equals(path)) usuarios(q,s);
            else if("/roles".equals(path)) roles(q,s);
            else {s.sendError(405);}
        }catch(IllegalArgumentException ex){aviso(q,s,ex.getMessage(),"/menu");}
        catch(SQLException ex){getServletContext().log("Error al consultar datos",ex);s.sendError(503,"No se pudieron consultar los datos. Revisa la conexión.");}
    }
    private void usuarios(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException {
        int id=numero(q,"editar");Usuario u=id==0?UsuarioFactory.nuevo():repo.buscar(id);
        if(u==null)throw new IllegalArgumentException("El usuario no existe.");
        StringBuilder b=new StringBuilder("<span class='eyebrow'>ADMINISTRACIÓN</span><h1>Gestión de usuarios</h1><p class='muted'>Cuentas, datos personales y control de acceso.</p><div class='split'><form class='card' method='post' action='"+base(q)+"/usuarios'>"+csrf(q)+"<input type='hidden' name='accion' value='guardar'><input type='hidden' name='id' value='"+u.id+"'><h2>"+(id==0?"Nuevo usuario":"Editar usuario")+"</h2>");
        b.append(campo("Usuario","usuario",u.username,"text",50,true)).append(campo("Nombres","nombres",u.nombres,"text",80,true)).append(campo("Apellidos","apellidos",u.apellidos,"text",80,true));
        b.append("<label>Rol<select name='rol' required>");for(String[] r:repo.roles())if("1".equals(r[3]))b.append("<option value='").append(r[0]).append("' ").append(u.rolId==Integer.parseInt(r[0])?"selected":"").append(">").append(e(r[1])).append("</option>");
        b.append("</select></label><label>").append(id==0?"Contraseña":"Nueva contraseña (vacía para conservar)").append("<input type='password' name='password' autocomplete='new-password' minlength='12' maxlength='128' ").append(id==0?"required":"").append("></label><label class='check'><input type='checkbox' name='activo' ").append(u.activo?"checked":"").append("> Cuenta activa</label><button>Guardar usuario</button><a class='small' href='").append(base(q)).append("/usuarios'>Limpiar formulario</a></form><section class='card tablecard'><h2>Usuarios registrados</h2><div class='scroll'><table><thead><tr><th>Usuario / nombre</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>");
        for(Usuario x:repo.listar()){
            b.append("<tr><td><strong>").append(e(x.username)).append("</strong><br><span class='small'>").append(e(x.nombres)).append(" ").append(e(x.apellidos)).append("</span></td><td>").append(e(x.rol)).append("</td><td>").append(!x.activo?"Inactivo":x.bloqueado?"Bloqueado":"Activo").append("<br><span class='small'>Fallos: ").append(x.intentos).append("/3</span></td><td><a href='").append(base(q)).append("/usuarios?editar=").append(x.id).append("'>Editar</a>");
            if(x.bloqueado)b.append(accion(q,"/usuarios",x.id,"desbloquear","Desbloquear"));
            if(x.id!=((Usuario)q.getAttribute("actual")).id){if(x.activo)b.append(accion(q,"/usuarios",x.id,"desactivar","Desactivar"));b.append("<details><summary>Eliminar…</summary><p class='small'>Se eliminará esta cuenta.</p>").append(accion(q,"/usuarios",x.id,"eliminar","Confirmar eliminación")).append("</details>");}
            b.append("</td></tr>");
        }
        b.append("</tbody></table></div></section></div>");pagina(q,s,"Usuarios",b.toString());
    }
    private String accion(HttpServletRequest q,String ruta,int id,String accion,String texto){return "<form class='inline' method='post' action='"+base(q)+ruta+"'>"+csrf(q)+"<input type='hidden' name='id' value='"+id+"'><input type='hidden' name='accion' value='"+accion+"'><button class='secondary'>"+texto+"</button></form>";}
    private void roles(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException {
        List<String[]> roles=repo.roles();int id=numero(q,"editar");String nombre="",descripcion="";
        for(String[] r:roles)if(Integer.parseInt(r[0])==id){nombre=r[1];descripcion=r[2];}
        StringBuilder b=new StringBuilder("<h1>Roles del sistema</h1><p class='muted'>ADMINISTRADOR gestiona cuentas. Los demás roles acceden al menú del avance.</p><div class='split'><form class='card' method='post' action='"+base(q)+"/roles'>"+csrf(q)+"<input type='hidden' name='accion' value='guardar'><input type='hidden' name='id' value='"+id+"'><h2>"+(id==0?"Nuevo rol":"Editar rol")+"</h2>"+campo("Nombre del rol","nombre",nombre,"text",30,true)+campo("Descripción","descripcion",descripcion,"text",150,false)+"<button>Guardar rol</button><a href='"+base(q)+"/roles'>Limpiar</a></form><section class='card'><h2>Roles registrados</h2><table><tr><th>Rol</th><th>Descripción</th><th>Acciones</th></tr>");
        for(String[] r:roles){b.append("<tr><td>").append(e(r[1])).append("</td><td>").append(e(r[2])).append("</td><td><a href='").append(base(q)).append("/roles?editar=").append(r[0]).append("'>Editar</a>");if(!"ADMINISTRADOR".equals(r[1])&&!"OPERADOR".equals(r[1]))b.append(accion(q,"/roles",Integer.parseInt(r[0]),"eliminar","Eliminar rol"));b.append("</td></tr>");}
        b.append("</table><p class='small'>Solo se pueden eliminar roles adicionales sin usuarios asignados.</p></section></div>");pagina(q,s,"Roles",b.toString());
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        String path=q.getServletPath();
        try {
            if("/login".equals(path)){
                Usuario u=new AccesoFacade().ingresar(valor(q,"usuario"),valor(q,"password"));
                if(u==null){aviso(q,s,"Acceso denegado: revisa tus credenciales o solicita desbloqueo al administrador.","/login");return;}
                q.changeSessionId();q.getSession().setAttribute("usuarioId",u.id);q.getSession().setAttribute("csrf",UUID.randomUUID().toString());s.sendRedirect(base(q)+"/menu");return;
            }
            if("/salir".equals(path)){q.getSession().invalidate();s.sendRedirect(base(q)+"/login");return;}
            String accion=valor(q,"accion");int id=numero(q,"id");
            if("/usuarios".equals(path)){
                if("guardar".equals(accion))repo.guardar(id,valor(q,"usuario").trim(),valor(q,"nombres").trim(),valor(q,"apellidos").trim(),numero(q,"rol"),valor(q,"password"),q.getParameter("activo")!=null);
                else repo.accion(id,((Usuario)q.getAttribute("actual")).id,accion);
            } else if("/roles".equals(path)){
                if("guardar".equals(accion))repo.guardarRol(id,valor(q,"nombre").trim(),valor(q,"descripcion").trim());
                else if("eliminar".equals(accion))repo.eliminarRol(id);else throw new IllegalArgumentException("Acción inválida.");
            } else {s.sendError(405);return;}
            aviso(q,s,"Cambios guardados correctamente.",path);
        }catch(IllegalArgumentException ex){aviso(q,s,ex.getMessage(),path);}
        catch(SQLException ex){getServletContext().log("Error al guardar",ex);aviso(q,s,ex.getSQLState()!=null&&ex.getSQLState().startsWith("23")?"No se pudo guardar: nombre duplicado o registro con relaciones existentes.":"No se pudo completar la operación. Revisa la conexión y el registro del servidor.",path);}
    }
}
