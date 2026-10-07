package com.mycompany.novatech.app;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/** Protection Proxy: verifica sesión y permisos antes de ejecutar controladores. */
@WebFilter("/*")
public final class SeguridadFilter implements Filter {
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException {
        HttpServletRequest q=(HttpServletRequest)request;HttpServletResponse s=(HttpServletResponse)response;
        q.setCharacterEncoding("UTF-8");s.setCharacterEncoding("UTF-8");
        s.setHeader("X-Content-Type-Options","nosniff");s.setHeader("X-Frame-Options","DENY");
        s.setHeader("Content-Security-Policy","default-src 'self'; style-src 'self'; img-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
        s.setHeader("Cache-Control","no-store");
        String path=q.getServletPath();
        if("/estilos.css".equals(path) || path.startsWith("/assets/login/")){chain.doFilter(q,s);return;}
        HttpSession session=q.getSession(true);
        if(session.getAttribute("csrf")==null)session.setAttribute("csrf",UUID.randomUUID().toString());
        if("POST".equals(q.getMethod()) && !session.getAttribute("csrf").equals(q.getParameter("csrf"))) {s.sendError(403,"Formulario vencido. Recarga la página.");return;}
        Usuario actual=null;Integer id=(Integer)session.getAttribute("usuarioId");
        if(id!=null) {
            try { actual=new JdbcUsuarioRepository().buscar(id); if(actual!=null)actual.permisos=new PermisoRepository().delUsuario(id); }
            catch(SQLException e){q.getServletContext().log("No se pudo validar la sesión",e);s.sendError(503,"Base de datos no disponible.");return;}
            if(actual==null || !actual.activo || actual.bloqueado){session.removeAttribute("usuarioId");actual=null;}
        }
        q.setAttribute("actual",actual);
        if(actual!=null)q.setAttribute("opcionesMenu",new MenuMediator().opcionesPara(actual));
        boolean publica=path.isEmpty() || "/".equals(path) || "/login".equals(path) || "/acceso-cerrado".equals(path);
        if(!publica && actual==null){s.sendRedirect(q.getContextPath()+"/login");return;}
        String permiso=("/usuarios".equals(path)||"/contactos".equals(path)||"/ubicaciones".equals(path))?"GESTIONAR_USUARIOS":("/roles".equals(path)||"/permisos".equals(path))?"GESTIONAR_ROLES":null;
        if(permiso!=null && !actual.puede(permiso)){s.sendError(403,"Tu rol no tiene permiso para esta opción.");return;}
        if("/permisos".equals(path)&&!actual.esAdministrador()){s.sendError(403,"Solo el administrador asigna permisos.");return;}
        if("/usuarios".equals(path)&&"POST".equals(q.getMethod())&&"desbloquear".equals(q.getParameter("accion"))&&!actual.puede("DESBLOQUEAR_USUARIOS")){s.sendError(403,"Tu rol no puede desbloquear usuarios.");return;}
        chain.doFilter(q,s);
    }
}
