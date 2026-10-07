package com.mycompany.novatech.app;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/permisos")
public final class PermisosServlet extends HttpServlet {
    private final PermisoRepository permisos=new PermisoRepository();
    private int rol(HttpServletRequest q){try{return Integer.parseInt(q.getParameter("rol"));}catch(Exception e){throw new IllegalArgumentException("Selecciona un rol válido.");}}
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        try {
            int id=rol(q);String nombre=null;
            for(String[] r:new JdbcUsuarioRepository().roles())if(Integer.parseInt(r[0])==id)nombre=r[1];
            if(nombre==null){s.sendError(404);return;}
            q.setAttribute("rolPermisos",id);q.setAttribute("nombreRolPermisos",nombre);q.setAttribute("permisos",permisos.delRol(id));
            q.setAttribute("titulo","Permisos del rol");q.setAttribute("vista","permisos.jsp");q.setAttribute("rutaActual","/roles");
            q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));q.getSession().removeAttribute("mensaje");
            q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
        }catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Consultar permisos",e);s.sendError(503);}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException {
        try {
            int id=rol(q);Set<Integer> elegidos=new HashSet<>();String[] valores=q.getParameterValues("permiso");
            if(valores!=null)for(String v:valores)elegidos.add(Integer.parseInt(v));
            permisos.guardar(id,elegidos,((Usuario)q.getAttribute("actual")).username);
            q.getSession().setAttribute("mensaje","Permisos guardados. Se aplican en la siguiente petición del usuario.");
            s.sendRedirect(q.getContextPath()+"/permisos?rol="+id);
        }catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Guardar permisos",e);s.sendError(503);}
    }
}
