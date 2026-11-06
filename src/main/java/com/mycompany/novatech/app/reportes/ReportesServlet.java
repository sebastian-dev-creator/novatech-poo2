package com.mycompany.novatech.app.reportes;

import com.mycompany.novatech.app.Usuario;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/reportes")
public final class ReportesServlet extends HttpServlet {
    private final ReporteRepository repository=new ReporteRepository();
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        Usuario usuario=(Usuario)q.getAttribute("actual");
        List<Reporte> disponibles=CatalogoReportes.TODOS.stream()
                .filter(r->usuario.puede(r.permiso))
                .filter(r->!r.id.matches("s0[5-9]|s10") || usuario.puede("GESTIONAR_COMPRAS"))
                .collect(Collectors.toList());
        if(disponibles.isEmpty()){s.sendError(403,"Necesitas permiso sobre algún módulo para consultar sus reportes.");return;}
        try {
            String id=q.getParameter("reporte");
            Reporte reporte=id==null?disponibles.get(0):CatalogoReportes.buscar(id);
            if(!disponibles.contains(reporte)){s.sendError(403,"No tienes acceso a este reporte.");return;}
            FiltroReporte filtro=new FiltroReporte(q.getParameter("desde"),q.getParameter("hasta"),q.getParameter("periodo"));
            boolean excel="xlsx".equals(q.getParameter("formato"));
            ResultadoReporte datos=repository.consultar(reporte,filtro,excel?50000:1000);
            if(excel) {
                if(datos.truncado){s.sendError(400,"El resultado supera 50 000 filas. Reduce el período para exportarlo completo.");return;}
                s.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                s.setHeader("Content-Disposition","attachment; filename=\"novatech-"+reporte.id+".xlsx\"");
                ExcelReporte.escribir(s.getOutputStream(),reporte,filtro,datos);return;
            }
            q.setAttribute("reportes",disponibles);q.setAttribute("reporte",reporte);q.setAttribute("resultado",datos);q.setAttribute("filtro",filtro);
            q.setAttribute("titulo","Reportes");q.setAttribute("vista","reportes.jsp");q.setAttribute("rutaActual","/reportes");
            q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
        }catch(IllegalArgumentException e){s.sendError(400,e.getMessage());}
        catch(SQLException e){getServletContext().log("Consultar reporte",e);s.sendError(503,"No se pudo consultar el reporte. Revisa la conexión y la migración de etapa 7.");}
    }
}
