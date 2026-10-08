package com.mycompany.novatech.app.comercial;

import com.mycompany.novatech.app.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/** Controlador MVC: interpreta HTTP y usa contratos Repository; no contiene SQL. */
@WebServlet(urlPatterns={"/clientes","/proveedores","/productos","/catalogos-productos"})
public final class ComercialServlet extends HttpServlet {
    private final ClienteRepository clientes=new JdbcClienteRepository();
    private final ProveedorRepository proveedores=new JdbcProveedorRepository();
    private final ProductoRepository productos=new JdbcProductoRepository();
    private final CatalogoComercialRepository catalogos=new CatalogoComercialRepository();
    private String valor(HttpServletRequest q,String key){String v=q.getParameter(key);return v==null?"":v.trim();}
    private int id(HttpServletRequest q,String key){return Validacion.id(q.getParameter(key));}
    private Map<String,String> formulario(HttpServletRequest q) {
        Map<String,String> f=new HashMap<>();
        for(String k:new String[]{"id","codigo","nombre","descripcion","tipo","categoria","marca","unidad","proveedor","precio","stock","minimo","activo"})f.put(k,valor(q,k));
        return f;
    }
    private Map<String,String> formulario(Producto p) {
        Map<String,String> f=new HashMap<>();
        f.put("id",String.valueOf(p.id));f.put("codigo",p.codigo);f.put("nombre",p.nombre);f.put("descripcion",p.descripcion);f.put("tipo",p.tipo);
        f.put("categoria",String.valueOf(p.categoriaId));f.put("marca",String.valueOf(p.marcaId));f.put("unidad",String.valueOf(p.unidadId));f.put("proveedor",String.valueOf(p.proveedorId));
        f.put("precio",p.precio.toPlainString());f.put("stock",p.stock.toPlainString());f.put("minimo",p.stockMinimo.toPlainString());f.put("activo",p.activo?"on":"");return f;
    }
    private void mostrar(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException,SQLException {
        String ruta=q.getServletPath(),busqueda=valor(q,"q"),titulo,vista;
        if("/clientes".equals(ruta)||"/proveedores".equals(ruta)) {
            boolean prov="/proveedores".equals(ruta);titulo=prov?"Proveedores":"Clientes";vista="terceros.jsp";
            q.setAttribute("esProveedor",prov);q.setAttribute("terceros",prov?proveedores.listar(busqueda):clientes.listar(busqueda));
            q.setAttribute("distritosComerciales",catalogos.distritos());
            if(q.getAttribute("terceroForm")==null) {
                int editar=id(q,"editar"),copiar=id(q,"copiar");Tercero x=prov?new Proveedor():new Cliente();
                if(editar>0){x=prov?proveedores.buscar(editar):clientes.buscar(editar);if(x==null){s.sendError(404);return;}}
                else if(copiar>0&&!prov) {Cliente original=clientes.buscar(copiar);if(original==null){s.sendError(404);return;}x=original.copiarParaNuevo();q.setAttribute("aviso","Se copiaron dirección y teléfono. Completa la identidad del nuevo cliente.");}
                q.setAttribute("terceroForm",x);q.setAttribute("mostrarForm",editar>0||copiar>0||q.getParameter("nuevo")!=null);
            }
        }else if("/productos".equals(ruta)) {
            titulo="Productos y servicios";vista="productos.jsp";
            q.setAttribute("productos",productos.listar(busqueda));
            if(q.getAttribute("productoForm")==null) {
                Map<String,String> f=new HashMap<>();f.put("id","0");f.put("tipo","PRODUCTO");f.put("precio","0.00");f.put("stock","0");f.put("minimo","0");f.put("activo","on");
                int editar=id(q,"editar");if(editar>0){Producto p=productos.buscar(editar);if(p==null){s.sendError(404);return;}f=formulario(p);}
                q.setAttribute("productoForm",f);q.setAttribute("mostrarForm",editar>0||q.getParameter("nuevo")!=null);
            }
            q.setAttribute("categorias",catalogos.listar("categoria"));q.setAttribute("marcas",catalogos.listar("marca"));q.setAttribute("unidades",catalogos.listar("unidad"));
            List<Proveedor> opciones=new ArrayList<>(proveedores.listar(""));
            Object seleccionado=((Map<?,?>)q.getAttribute("productoForm")).get("proveedor");
            int proveedorActual=0;
            try {proveedorActual=Validacion.id((String)seleccionado);}
            catch(IllegalArgumentException e) { /* Mantener el error original del formulario. */ }
            boolean incluido=false;
            for(Proveedor p:opciones)if(p.id==proveedorActual)incluido=true;
            if(proveedorActual>0&&!incluido) {
                Proveedor actual=proveedores.buscar(proveedorActual);
                if(actual!=null)opciones.add(actual);
            }
            q.setAttribute("proveedoresProducto",opciones);
        }else {
            titulo="Catálogos de productos";vista="catalogos-productos.jsp";
            String clase=valor(q,"clase");if(clase.isEmpty())clase="categoria";catalogos.tabla(clase);
            List<String[]> lista=catalogos.listar(clase);q.setAttribute("claseCatalogo",clase);q.setAttribute("catalogosComerciales",lista);
            if(q.getAttribute("catalogoForm")==null){String[] f={"0","","1"};int editar=id(q,"editar");if(editar>0){f=null;for(String[] r:lista)if(Integer.parseInt(r[0])==editar)f=r;if(f==null){s.sendError(404);return;}}q.setAttribute("catalogoForm",f);}
        }
        q.setAttribute("busquedaComercial",busqueda);q.setAttribute("titulo",titulo);q.setAttribute("vista",vista);q.setAttribute("rutaActual","/catalogos-productos".equals(ruta)?"/productos":ruta);
        if(q.getAttribute("aviso")==null)q.setAttribute("aviso",q.getSession().getAttribute("mensaje"));q.getSession().removeAttribute("mensaje");
        q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException {
        try{mostrar(q,s);}catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}catch(SQLException e){getServletContext().log("Consultar módulo comercial",e);s.sendError(503,"No se pudo consultar el módulo comercial. Verifica conexión y migración de etapa 5.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException {
        String ruta=q.getServletPath();
        try {
            String actor=((Usuario)q.getAttribute("actual")).username,accion=valor(q,"accion");int id=id(q,"id");
            if("/clientes".equals(ruta)||"/proveedores".equals(ruta)) {
                boolean prov="/proveedores".equals(ruta);
                if("importar".equals(accion)&&prov){proveedores.importar(new ProveedorTsvAdapter().convertir(q.getParameter("tsv")),actor);}
                else if("guardar".equals(accion)) {
                    Tercero x=prov?new Proveedor():new Cliente();x.id=id;x.nombres=valor(q,"nombres");x.apellidos=valor(q,"apellidos");x.documento=valor(q,"documento");x.razonSocial=valor(q,"razonSocial");x.correo=valor(q,"correo");x.telefono=valor(q,"telefono");x.direccion=valor(q,"direccion");x.activo=q.getParameter("activo")!=null;
                    q.setAttribute("terceroForm",x);q.setAttribute("mostrarForm",true);x.distritoId=id(q,"distrito");
                    if(!prov)x.tipoDocumento=valor(q,"tipoDocumento").isEmpty()?"DNI":valor(q,"tipoDocumento");
                    if(prov)proveedores.guardar((Proveedor)x,actor);else clientes.guardar((Cliente)x,actor);
                }else if("eliminar".equals(accion)){if(prov)proveedores.eliminar(id);else clientes.eliminar(id);}
                else throw new IllegalArgumentException("Acción inválida.");
            }else if("/productos".equals(ruta)) {
                if("guardar".equals(accion)) {
                    q.setAttribute("productoForm",formulario(q));q.setAttribute("mostrarForm",true);
                    Producto p=new Producto.Builder().identidad(id,valor(q,"codigo"),valor(q,"nombre"))
                        .clasificacion(valor(q,"tipo"),id(q,"categoria"),id(q,"marca"),id(q,"unidad"),id(q,"proveedor"))
                        .descripcion(valor(q,"descripcion")).valores(valor(q,"precio"),valor(q,"stock"),valor(q,"minimo"))
                        .activo(q.getParameter("activo")!=null).build();productos.guardar(p,actor);
                }else if("eliminar".equals(accion))productos.eliminar(id);else throw new IllegalArgumentException("Acción inválida.");
            }else {
                String clase=valor(q,"clase");catalogos.tabla(clase);
                if("guardar".equals(accion)) {q.setAttribute("catalogoForm",new String[]{String.valueOf(id),valor(q,"nombre"),q.getParameter("activo")!=null?"1":"0"});catalogos.guardar(clase,id,valor(q,"nombre"),q.getParameter("activo")!=null,actor);}
                else if("eliminar".equals(accion))catalogos.eliminar(clase,id);else throw new IllegalArgumentException("Acción inválida.");
                ruta+="?clase="+clase;
            }
            q.getSession().setAttribute("mensaje","Cambios guardados correctamente.");s.sendRedirect(q.getContextPath()+ruta);
        }catch(IllegalArgumentException|SQLException e) {
            int estado=400;String mensaje=e.getMessage();
            if(e instanceof SQLException){getServletContext().log("Guardar módulo comercial",e);String code=((SQLException)e).getSQLState();boolean integridad=code!=null&&code.startsWith("23");estado=integridad?400:503;mensaje=integridad?"No se pudo guardar: DNI, RUC, código o nombre duplicado, o registro relacionado. Revisa los datos; si tiene relaciones, usa Inactivo.":"No se pudo guardar. Revisa la conexión y la migración de etapa 5.";}
            s.setStatus(estado);q.setAttribute("aviso",mensaje);
            try{mostrar(q,s);}catch(SQLException|IllegalArgumentException ex){s.sendError(estado,mensaje);}
        }
    }
}
