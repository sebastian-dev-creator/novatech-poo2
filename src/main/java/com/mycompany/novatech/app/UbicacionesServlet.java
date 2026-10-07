package com.mycompany.novatech.app;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/ubicaciones")
public final class UbicacionesServlet extends HttpServlet {
    private final UbicacionRepository repo=new UbicacionRepository();
    private String valor(HttpServletRequest q,String clave){String v=q.getParameter(clave);return v==null?"":v.trim();}
    private int numero(HttpServletRequest q,String clave){try{String v=valor(q,clave);return v.isEmpty()?0:Integer.parseInt(v);}catch(NumberFormatException e){throw new IllegalArgumentException("Identificador inválido.");}}
    private String nivel(HttpServletRequest q){String n=valor(q,"nivel");if(n.isEmpty())n="pais";if(!Arrays.asList("pais","departamento","provincia","distrito").contains(n))throw new IllegalArgumentException("Nivel inválido.");return n;}
    private void mostrar(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException{
        String nivel=nivel(q);List<String[]> lista=repo.listar(nivel);String[] editando=(String[])q.getAttribute("ubicacionEditando");
        if(editando==null){int id=numero(q,"editar");if(id>0){for(String[] r:lista)if(Integer.parseInt(r[0])==id)editando=r;if(editando==null){s.sendError(404);return;}}else editando=new String[]{"0","","0","1"};}
        String padre="departamento".equals(nivel)?"pais":"provincia".equals(nivel)?"departamento":"distrito".equals(nivel)?"provincia":"";
        q.setAttribute("ubicaciones",lista);q.setAttribute("nivelUbicacion",nivel);q.setAttribute("ubicacionEditando",editando);q.setAttribute("padres",padre.isEmpty()?Collections.emptyList():repo.superiores(padre));
        q.setAttribute("titulo","Ubicaciones");q.setAttribute("vista","ubicaciones.jsp");q.setAttribute("rutaActual","/ubicaciones");
        if(q.getAttribute("aviso")==null)q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));q.getSession().removeAttribute("mensaje");
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException{
        try{mostrar(q,s);}catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Consultar ubicaciones",e);s.sendError(503);}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException{
        try{
            String nivel=nivel(q),accion=valor(q,"accion");int id=numero(q,"id");
            if("guardar".equals(accion)){
                int padre=numero(q,"padre");String nombre=valor(q,"nombre");boolean activo=q.getParameter("activo")!=null;
                q.setAttribute("ubicacionEditando",new String[]{String.valueOf(id),nombre,String.valueOf(padre),activo?"1":"0"});
                repo.guardar(nivel,id,nombre,padre,activo,((Usuario)q.getAttribute("actual")).username);
            }else if("eliminar".equals(accion))repo.eliminar(nivel,id);else throw new IllegalArgumentException("Acción inválida.");
            q.getSession().setAttribute("mensaje","Ubicación guardada o eliminada correctamente.");s.sendRedirect(q.getContextPath()+"/ubicaciones?nivel="+nivel);
        }catch(IllegalArgumentException|SQLException e){
            if(e instanceof SQLException)getServletContext().log("Guardar ubicación",e);
            q.setAttribute("aviso",e instanceof SQLException?"No se pudo guardar o eliminar: nombre duplicado, ubicación relacionada o base no disponible.":e.getMessage());s.setStatus(400);
            try{mostrar(q,s);}catch(SQLException|IllegalArgumentException ex){s.sendError(400,"No se pudo procesar la ubicación.");}
        }
    }
}
