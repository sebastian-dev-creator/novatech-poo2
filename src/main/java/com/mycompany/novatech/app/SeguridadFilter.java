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
        if("/estilos.css".equals(path)){chain.doFilter(q,s);return;}
        HttpSession session=q.getSession(true);
        if(session.getAttribute("csrf")==null)session.setAttribute("csrf",UUID.randomUUID().toString());
        if("POST".equals(q.getMethod()) && !session.getAttribute("csrf").equals(q.getParameter("csrf"))) {s.sendError(403,"Formulario vencido. Recarga la página.");return;}
        Usuario actual=null;Integer id=(Integer)session.getAttribute("usuarioId");
        if(id!=null) {
            try { actual=new UsuarioRepository().buscar(id); }
            catch(SQLException e){q.getServletContext().log("No se pudo validar la sesión",e);s.sendError(503,"Base de datos no disponible.");return;}
            if(actual==null || !actual.activo || actual.bloqueado){session.removeAttribute("usuarioId");actual=null;}
        }
        q.setAttribute("actual",actual);
        boolean publica=path.isEmpty() || "/".equals(path) || "/login".equals(path);
        if(!publica && actual==null){s.sendRedirect(q.getContextPath()+"/login");return;}
        if(("/usuarios".equals(path) || "/roles".equals(path)) && !actual.esAdministrador()){s.sendError(403,"Acceso exclusivo para administradores.");return;}
        chain.doFilter(q,s);
    }
}
