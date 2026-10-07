package com.mycompany.novatech.app;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(urlPatterns={"/login","/menu","/usuarios","/roles","/salir","/acceso-cerrado"})
public final class AppServlet extends HttpServlet {
    private final UsuarioRepository repo=new JdbcUsuarioRepository();
    public static String e(Object value){return value==null?"":value.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;");}
    private String valor(HttpServletRequest q,String k){String x=q.getParameter(k);return x==null?"":x;}
    private int numero(HttpServletRequest q,String k){String x=valor(q,k);if(x.isEmpty())return 0;try{return Integer.parseInt(x);}catch(NumberFormatException e){throw new IllegalArgumentException("Identificador inválido.");}}
    private Integer opcional(HttpServletRequest q,String k) { return valor(q,k).isBlank()?null:numero(q,k); }
    private String base(HttpServletRequest q){return q.getContextPath();}
    private void pagina(HttpServletRequest q,HttpServletResponse s,String titulo,String vista) throws ServletException,IOException {
        if(q.getAttribute("aviso")==null) q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));
        q.getSession().removeAttribute("mensaje");
        q.setAttribute("titulo",titulo);q.setAttribute("vista",vista);q.setAttribute("rutaActual",q.getServletPath());
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    private void aviso(HttpServletRequest q,HttpServletResponse s,String mensaje,String ruta) throws IOException{q.getSession().setAttribute("mensaje",mensaje);s.sendRedirect(base(q)+ruta);}
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        Usuario actual=(Usuario)q.getAttribute("actual");String path=q.getServletPath();
        try {
            if("/acceso-cerrado".equals(path)) {
                q.getRequestDispatcher("/WEB-INF/acceso-cerrado.jsp").forward(q,s);
            } else if("/login".equals(path)) {
                if(actual!=null){s.sendRedirect(base(q)+"/menu");return;}
                q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));
                q.getSession().removeAttribute("mensaje");
                q.getRequestDispatcher("/WEB-INF/login.jsp").forward(q,s);
            } else if("/menu".equals(path)) {
                if(actual.esAdministrador()) {
                    List<Usuario> usuarios=repo.listar();
                    q.setAttribute("usuarios",usuarios);
                    q.setAttribute("totalUsuarios",usuarios.size());
                    q.setAttribute("activos",usuarios.stream().filter(u->u.activo && !u.bloqueado).count());
                    q.setAttribute("bloqueados",usuarios.stream().filter(u->u.bloqueado).count());
                    q.setAttribute("inactivos",usuarios.stream().filter(u->!u.activo).count());
                    q.setAttribute("totalRoles",repo.roles().size());
                }
                pagina(q,s,"Resumen","dashboard.jsp");
            } else if("/usuarios".equals(path)) usuarios(q,s);
            else if("/roles".equals(path)) roles(q,s);
            else {s.sendError(405);}
        }catch(IllegalArgumentException ex){aviso(q,s,ex.getMessage(),"/menu");}
        catch(SQLException ex){getServletContext().log("Error al consultar datos",ex);s.sendError(503,"No se pudieron consultar los datos. Revisa la conexión.");}
    }
    private void usuarios(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException {
        Usuario editando=(Usuario)q.getAttribute("editando");
        if(editando==null && (!valor(q,"editar").isEmpty() || "1".equals(valor(q,"nuevo")))) {
            int id=numero(q,"editar");
            editando=id==0?UsuarioFactory.nuevo():repo.buscar(id);
            if(editando==null)throw new IllegalArgumentException("El usuario no existe.");
        }
        String busqueda=valor(q,"q").trim();
        String estado=valor(q,"estado");
        if(busqueda.length()>100) throw new IllegalArgumentException("La búsqueda admite hasta 100 caracteres.");
        if(!Arrays.asList("","activo","inactivo","bloqueado").contains(estado)) throw new IllegalArgumentException("Estado de búsqueda inválido.");
        String texto=busqueda.toLowerCase(Locale.ROOT);
        List<Usuario> lista=new ArrayList<>();
        for(Usuario u:repo.listar()) {
            boolean coincide=(u.username+" "+u.nombres+" "+u.apellidos+" "+(u.dni==null?"":u.dni)).toLowerCase(Locale.ROOT).contains(texto);
            boolean filtro=estado.isEmpty() || ("activo".equals(estado)&&u.activo&&!u.bloqueado) || ("inactivo".equals(estado)&&!u.activo) || ("bloqueado".equals(estado)&&u.bloqueado);
            if(coincide&&filtro)lista.add(u);
        }
        q.setAttribute("usuarios",lista);q.setAttribute("editando",editando);
        q.setAttribute("busqueda",busqueda);q.setAttribute("estadoFiltro",estado);
        q.setAttribute("roles",repo.roles());
        if(editando!=null) {q.setAttribute("sexos",repo.catalogoPersonal(true));q.setAttribute("estadosCiviles",repo.catalogoPersonal(false));}
        pagina(q,s,"Usuarios","usuarios.jsp");
    }
    private void roles(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException {
        List<String[]> lista=repo.roles();int id=numero(q,"editar");String[] editando=(String[])q.getAttribute("rolEditando");
        if("1".equals(valor(q,"nuevo")))editando=new String[]{"0","","","1"};
        for(String[] r:lista)if(Integer.parseInt(r[0])==id)editando=r;
        if(id!=0&&editando==null)throw new IllegalArgumentException("El rol no existe.");
        q.setAttribute("roles",lista);q.setAttribute("rolEditando",editando);
        pagina(q,s,"Roles y acceso","roles.jsp");
    }
    private void errorFormulario(HttpServletRequest q,HttpServletResponse s,String mensaje,String path) throws IOException,ServletException {
        if(!"guardar".equals(valor(q,"accion")) || !("/usuarios".equals(path)||"/roles".equals(path))) {aviso(q,s,mensaje,path);return;}
        try {
            q.setAttribute("aviso",mensaje);q.setAttribute("errorFormulario",true);s.setStatus(400);
            if("/usuarios".equals(path)) {
                Usuario u=UsuarioFactory.nuevo();u.id=numero(q,"id");u.username=valor(q,"usuario");u.nombres=valor(q,"nombres");u.apellidos=valor(q,"apellidos");
                u.dni=valor(q,"dni");u.rolId=numero(q,"rol");u.sexoId=opcional(q,"sexo");u.estadoCivilId=opcional(q,"estadoCivil");u.activo=q.getParameter("activo")!=null;
                q.setAttribute("editando",u);usuarios(q,s);
            }else{
                q.setAttribute("rolEditando",new String[]{String.valueOf(numero(q,"id")),valor(q,"nombre"),valor(q,"descripcion"),"1"});roles(q,s);
            }
        }catch(IllegalArgumentException ex){aviso(q,s,mensaje,path);}
        catch(SQLException ex){getServletContext().log("No se pudo recuperar el formulario",ex);s.sendError(503,"No se pudieron consultar los datos.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        String path=q.getServletPath();
        try {
            if("/login".equals(path)){
                ResultadoAcceso resultado=new AccesoFacade().intentar(valor(q,"usuario"),valor(q,"password"));
                if(resultado.estado==ResultadoAcceso.Estado.BLOQUEADO) {
                    q.getSession().invalidate();
                    s.sendRedirect(base(q)+"/acceso-cerrado");
                    return;
                }
                Usuario u=resultado.usuario;
                if(u==null){aviso(q,s,"Acceso denegado: revisa tus credenciales o solicita desbloqueo al administrador.","/login");return;}
                q.changeSessionId();q.getSession().setAttribute("usuarioId",u.id);q.getSession().setAttribute("csrf",UUID.randomUUID().toString());s.sendRedirect(base(q)+"/menu");return;
            }
            if("/salir".equals(path)){q.getSession().invalidate();s.sendRedirect(base(q)+"/login");return;}
            String accion=valor(q,"accion");int id=numero(q,"id");
            if("/usuarios".equals(path)){
                if("guardar".equals(accion))repo.guardar(id,valor(q,"usuario").trim(),valor(q,"nombres").trim(),valor(q,"apellidos").trim(),numero(q,"rol"),valor(q,"password"),q.getParameter("activo")!=null,new DatosPersonales(valor(q,"dni"),opcional(q,"sexo"),opcional(q,"estadoCivil")),((Usuario)q.getAttribute("actual")).username);
                else repo.accion(id,((Usuario)q.getAttribute("actual")).id,accion);
            } else if("/roles".equals(path)){
                if("guardar".equals(accion))repo.guardarRol(id,valor(q,"nombre").trim(),valor(q,"descripcion").trim(),((Usuario)q.getAttribute("actual")).username);
                else if("eliminar".equals(accion))repo.eliminarRol(id);else throw new IllegalArgumentException("Acción inválida.");
            } else {s.sendError(405);return;}
            aviso(q,s,"Cambios guardados correctamente.",path);
        }catch(IllegalArgumentException ex){errorFormulario(q,s,ex.getMessage(),path);}
        catch(SQLException ex){getServletContext().log("Error al guardar",ex);errorFormulario(q,s,ex.getSQLState()!=null&&ex.getSQLState().startsWith("23")?"No se pudo guardar: usuario o DNI duplicado, o registro con relaciones existentes.":"No se pudo completar la operación. Revisa la conexión y el registro del servidor.",path);}
    }
}
