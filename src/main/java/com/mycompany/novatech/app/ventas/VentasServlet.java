package com.mycompany.novatech.app.ventas;
import com.mycompany.novatech.app.Usuario;
import com.mycompany.novatech.app.comercial.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/ventas")
public final class VentasServlet extends HttpServlet {
    private final VentaRepository ventas=new JdbcVentaRepository();
    private final ClienteRepository clientes=new JdbcClienteRepository();
    private final ProductoRepository productos=new JdbcProductoRepository();
    private String valor(HttpServletRequest q,String key){String s=q.getParameter(key);return s==null?"":s.trim();}
    private int id(HttpServletRequest q,String key){return Validacion.id(q.getParameter(key));}
    private Map<String,String> cabecera(Venta v){Map<String,String> f=new HashMap<>();f.put("id",""+v.id);f.put("version",""+v.version);f.put("clave",v.clave);f.put("cliente",""+v.clienteId);f.put("tipo",v.tipo);f.put("porcentaje",v.porcentaje.toPlainString());f.put("observacion",v.observacion);return f;}
    private void mostrar(HttpServletRequest q,HttpServletResponse s)throws SQLException,ServletException,IOException {
        boolean enviado=Boolean.TRUE.equals(q.getAttribute("formEnviado"));
        int editar=id(q,"editar"),ver=id(q,"ver");Venta actual=null;
        if(editar>0)throw new IllegalArgumentException("Las ventas registradas no se editan. Utiliza la anulación.");
        if(editar>0||ver>0){actual=ventas.buscar(editar>0?editar:ver);if(actual==null){s.sendError(404);return;}if(editar>0)EstadoVenta.de(actual.estado).editar();}
        if(enviado||editar>0||q.getParameter("nuevo")!=null) {
            Map<String,String> f;List<String[]> lineas=new ArrayList<>();
            if(enviado){f=new HashMap<>();for(String k:new String[]{"id","version","clave","cliente","tipo","porcentaje","observacion"})f.put(k,valor(q,k));
                String[] ps=q.getParameterValues("producto"),cs=q.getParameterValues("cantidad");
                if(ps!=null&&cs!=null&&ps.length==cs.length&&ps.length<=50)for(int i=0;i<ps.length;i++)lineas.add(new String[]{ps[i],cs[i]});
                if(q.getParameter("quitar")!=null){int n=id(q,"quitar");if(n<lineas.size())lineas.remove(n);}
                if("agregar".equals(valor(q,"accion"))&&lineas.size()<50)lineas.add(new String[]{"","1"});
            }else{Venta v=actual==null?new Venta():actual;f=cabecera(v);for(DetalleVenta d:v.detalles)lineas.add(new String[]{""+d.productoId,d.cantidad.toPlainString()});}
            if(lineas.isEmpty())lineas.add(new String[]{"","1"});
            List<Cliente> opcionesCliente=new ArrayList<>(clientes.listar(""));List<Producto> opcionesProducto=new ArrayList<>(productos.listar(""));
            int elegido=0;try{elegido=Validacion.id(f.get("cliente"));}catch(IllegalArgumentException ignored){}
            boolean existe=false;for(Cliente x:opcionesCliente)if(x.id==elegido)existe=true;
            if(elegido>0&&!existe){Cliente x=clientes.buscar(elegido);if(x!=null)opcionesCliente.add(x);}
            Set<Integer> incluidos=new HashSet<>();for(Producto p:opcionesProducto)incluidos.add(p.id);
            for(String[] l:lineas){int n=0;try{n=Validacion.id(l[0]);}catch(IllegalArgumentException ignored){}if(n>0&&incluidos.add(n)){Producto p=productos.buscar(n);if(p!=null)opcionesProducto.add(p);}}
            q.setAttribute("ventaForm",f);q.setAttribute("lineasForm",lineas);q.setAttribute("clientesVenta",opcionesCliente);q.setAttribute("productosVenta",opcionesProducto);
        }
        q.setAttribute("ventaVista",ver>0?actual:null);
        String busqueda=Validacion.texto(q.getParameter("q"),"Búsqueda",100,false);q.setAttribute("busquedaVentas",busqueda);q.setAttribute("ventas",ventas.listar(busqueda));
        q.setAttribute("titulo","Ventas");q.setAttribute("vista","ventas.jsp");q.setAttribute("rutaActual","/ventas");
        if(q.getAttribute("aviso")==null)q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));q.getSession().removeAttribute("mensaje");
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException {
        try{mostrar(q,s);}catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Consultar ventas",e);s.sendError(503,"No se pudo consultar ventas.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException {
        try {
            Usuario u=(Usuario)q.getAttribute("actual");String accion=valor(q,"accion");int id=id(q,"id"),version=id(q,"version");
            if(id>0 && (q.getParameter("quitar")!=null || Arrays.asList("guardar","agregar","eliminar").contains(accion)))throw new IllegalArgumentException("Las ventas registradas no se editan ni se eliminan. Utiliza la anulación.");
            if(q.getParameter("quitar")!=null||"agregar".equals(accion)){q.setAttribute("formEnviado",true);mostrar(q,s);return;}
            if("guardar".equals(accion)){
                q.setAttribute("formEnviado",true);Venta v=new Venta();v.id=id;v.version=version;v.clave=valor(q,"clave");v.clienteId=id(q,"cliente");v.tipo=valor(q,"tipo");v.observacion=valor(q,"observacion");v.porcentaje=Validacion.decimal(valor(q,"porcentaje"),"Descuento",2);
                String[] ps=q.getParameterValues("producto"),cs=q.getParameterValues("cantidad");
                if(ps==null||cs==null||ps.length!=cs.length||ps.length>50)throw new IllegalArgumentException("Revisa las líneas de la venta (máximo 50).");
                for(int i=0;i<ps.length;i++){if(ps[i].isBlank())throw new IllegalArgumentException("Selecciona un artículo en cada línea o retira las líneas vacías.");v.detalles.add(new DetalleVenta(Validacion.id(ps[i]),Validacion.decimal(cs[i],"Cantidad",3)));}
                id=ventas.guardar(v,u.id,u.username);
            }else if("pagar".equals(accion))ventas.pagar(id,version,valor(q,"medio"),u.username);
            else if("anular".equals(accion))ventas.anular(id,version,u.username);
            else if("eliminar".equals(accion))throw new IllegalArgumentException("Las ventas no se eliminan. Utiliza la anulación.");
            else throw new IllegalArgumentException("Acción inválida.");
            q.getSession().setAttribute("mensaje","Cambios guardados correctamente.");s.sendRedirect(q.getContextPath()+"/ventas"+(id>0?"?ver="+id:""));
        }catch(IllegalArgumentException|SQLException e){
            if(e instanceof SQLException)getServletContext().log("Guardar venta",e);
            int status=e instanceof SQLException?503:400;
            String mensaje=e instanceof SQLException?"No se pudo guardar la venta. No se aplicó ningún cambio; vuelve a abrirla y comprueba los datos.":e.getMessage();
            s.setStatus(status);q.setAttribute("aviso",mensaje);
            try{mostrar(q,s);}catch(SQLException|IllegalArgumentException ex){s.sendError(status,mensaje);}
        }
    }
}
