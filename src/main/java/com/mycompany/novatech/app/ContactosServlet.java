package com.mycompany.novatech.app;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
@WebServlet("/contactos")
public final class ContactosServlet extends HttpServlet {
    private final ContactoRepository contactos=new JdbcContactoRepository();
    private final UsuarioRepository usuarios=new JdbcUsuarioRepository();
    private final UbicacionRepository ubicaciones=new UbicacionRepository();
    private String valor(HttpServletRequest q,String clave){String v=q.getParameter(clave);return v==null?"":v.trim();}
    private int numero(HttpServletRequest q,String clave){try{String v=valor(q,clave);return v.isEmpty()?0:Integer.parseInt(v);}catch(NumberFormatException e){throw new IllegalArgumentException("Identificador inválido.");}}
    private String clase(HttpServletRequest q){String c=valor(q,"clase");if(c.isEmpty())c="correo";if(!Arrays.asList("correo","telefono","direccion").contains(c))throw new IllegalArgumentException("Tipo de contacto inválido.");return c;}
    private void mostrar(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException{
        int id=numero(q,"usuarioId");Usuario usuario=usuarios.buscar(id);if(usuario==null){s.sendError(404,"El usuario no existe.");return;}
        String clase=clase(q);List<Contacto> lista=contactos.listar(id,clase);Contacto editando=(Contacto)q.getAttribute("contactoEditando");
        if(editando==null){
            int editar=numero(q,"editar");
            if(editar>0){for(Contacto c:lista)if(c.id==editar)editando=c;if(editando==null){s.sendError(404,"El contacto no pertenece al usuario.");return;}}
            else {editando=new Contacto();editando.usuarioId=id;}
        }
        q.setAttribute("personaContacto",usuario);q.setAttribute("claseContacto",clase);q.setAttribute("contactos",lista);q.setAttribute("contactoEditando",editando);q.setAttribute("distritos",ubicaciones.distritosCompletos());
        q.setAttribute("titulo","Contactos y direcciones");q.setAttribute("vista","contactos.jsp");q.setAttribute("rutaActual","/usuarios");
        if(q.getAttribute("aviso")==null)q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));q.getSession().removeAttribute("mensaje");
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException{
        try{mostrar(q,s);}catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Consultar contactos",e);s.sendError(503,"No se pudieron consultar los contactos.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException{
        try{
            int usuario=numero(q,"usuarioId"),id=numero(q,"id");String clase=clase(q),accion=valor(q,"accion");
            if("eliminar".equals(accion))contactos.eliminar(usuario,id,clase);
            else if("guardar".equals(accion)){
                Contacto c=new Contacto();c.id=id;c.usuarioId=usuario;c.valor=valor(q,"valor");c.tipo=valor(q,"tipo");c.activo=q.getParameter("activo")!=null;
                c.distritoId=numero(q,"distrito");c.piso=valor(q,"piso");c.numero=valor(q,"numero");c.referencia=valor(q,"referencia");q.setAttribute("contactoEditando",c);
                contactos.guardar(c,clase,((Usuario)q.getAttribute("actual")).username);
            }else throw new IllegalArgumentException("Acción inválida.");
            q.getSession().setAttribute("mensaje","Contacto guardado o eliminado correctamente.");s.sendRedirect(q.getContextPath()+"/contactos?usuarioId="+usuario+"&clase="+clase);
        }catch(IllegalArgumentException|SQLException e){
            if(e instanceof SQLException)getServletContext().log("Guardar contactos",e);
            String mensaje=e instanceof SQLException?"No se pudo guardar: contacto duplicado, ubicación inválida o base no disponible.":e.getMessage();
            q.setAttribute("aviso",mensaje);s.setStatus(400);
            try{mostrar(q,s);}catch(SQLException|IllegalArgumentException ex){s.sendError(400,"No se pudo procesar el contacto.");}
        }
    }
}
