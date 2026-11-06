package com.mycompany.novatech.app.reportes;

import com.mycompany.novatech.app.Usuario;
import com.mycompany.novatech.app.comercial.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@WebServlet("/compras")
public final class ComprasServlet extends HttpServlet {
    private final CompraRepository repository=new CompraRepository();
    private String valor(HttpServletRequest q,String campo){String v=q.getParameter(campo);return v==null?"":v.trim();}
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        try {
            q.setAttribute("compras",repository.listar());
            int ver=Validacion.id(q.getParameter("ver"));
            if(ver>0)q.setAttribute("compra",repository.buscar(ver));
            if(q.getParameter("nuevo")!=null) {
                q.setAttribute("proveedoresCompra",new JdbcProveedorRepository().listar(""));
                q.setAttribute("productosCompra",new JdbcProductoRepository().listar(""));
                q.setAttribute("claveCompra",q.getAttribute("claveCompra")==null?UUID.randomUUID().toString():q.getAttribute("claveCompra"));
            }
            q.setAttribute("titulo","Compras");q.setAttribute("vista","compras.jsp");q.setAttribute("rutaActual","/compras");
            q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
        }catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}
        catch(SQLException e){getServletContext().log("Consultar compras",e);s.sendError(503,"No se pudo consultar compras.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        try {
            String accion=valor(q,"accion");int id=Validacion.id(q.getParameter("id"));
            if("registrar".equals(accion)) {
                CompraRepository.Compra x=new CompraRepository.Compra();x.clave=valor(q,"clave");x.proveedor=Validacion.id(q.getParameter("proveedor"));
                x.prevista=LocalDate.parse(valor(q,"prevista"));x.observacion=valor(q,"observacion");
                String[] productos=q.getParameterValues("producto"),cantidades=q.getParameterValues("cantidad"),costos=q.getParameterValues("costo");
                if(productos==null||cantidades==null||costos==null||productos.length!=cantidades.length||productos.length!=costos.length||productos.length>20)
                    throw new IllegalArgumentException("Revisa las líneas de compra.");
                for(int i=0;i<productos.length;i++) {
                    if(productos[i].isBlank())continue;
                    CompraRepository.Linea l=new CompraRepository.Linea();l.producto=Validacion.id(productos[i]);
                    l.cantidad=Validacion.decimal(cantidades[i],"Cantidad",3);l.costo=Validacion.decimal(costos[i],"Costo",2);x.lineas.add(l);
                }
                id=repository.registrar(x,((Usuario)q.getAttribute("actual")).username);
            }else repository.cambiar(id,Validacion.id(q.getParameter("version")),accion,Validacion.id(q.getParameter("evaluacion")));
            s.sendRedirect(q.getContextPath()+"/compras?ver="+id);
        }catch(DateTimeParseException e){error(q,s,"Selecciona una fecha prevista válida.");}
        catch(IllegalArgumentException e){error(q,s,e.getMessage());}
        catch(SQLException e){getServletContext().log("Guardar compra",e);s.sendError(503,"No se pudo guardar. Reabre Compras para comprobar su estado antes de reintentar.");}
    }
    private void error(HttpServletRequest q,HttpServletResponse s,String mensaje)throws IOException,ServletException {
        s.setStatus(400);q.setAttribute("aviso",mensaje);q.setAttribute("claveCompra",valor(q,"clave"));doGet(q,s);
    }
}
