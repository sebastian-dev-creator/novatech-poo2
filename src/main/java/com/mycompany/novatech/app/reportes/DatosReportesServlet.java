package com.mycompany.novatech.app.reportes;

import com.mycompany.novatech.config.ConexionBD;
import com.mycompany.novatech.app.comercial.Validacion;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/datos-reportes")
public final class DatosReportesServlet extends HttpServlet {
    private static final Map<String,String[]> CAMPOS;
    static {
        Map<String,String[]> campos=new LinkedHashMap<>();
        campos.put("usuarios",new String[]{"usuarios","id_usuario","nombre_usuario","area","Área organizativa"});
        campos.put("clientes",new String[]{"clientes","id","CONCAT('Cliente ',id,' · ',IF(razon_social='',tipo_documento,razon_social))","segmento","Segmento de cliente"});
        campos.put("proveedores",new String[]{"proveedores","id","razon_social","categoria","Categoría de proveedor"});
        campos.put("productos",new String[]{"productos","id","CONCAT(codigo,' · ',nombre)","costo","Costo unitario S/"});
        CAMPOS=Collections.unmodifiableMap(campos);
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            Map<String,List<String[]>> registros=new LinkedHashMap<>();
            for(Map.Entry<String,String[]> entrada:CAMPOS.entrySet()) {
                String[] campo=entrada.getValue();List<String[]> filas=new ArrayList<>();
                try(Statement p=c.createStatement();ResultSet r=p.executeQuery("SELECT "+campo[1]+","+campo[2]+","+campo[3]+" FROM "+campo[0]+" ORDER BY "+campo[1]+" LIMIT 1000")) {
                    while(r.next())filas.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});
                }
                registros.put(entrada.getKey(),filas);
            }
            q.setAttribute("camposReporte",CAMPOS);q.setAttribute("registrosReporte",registros);
            q.setAttribute("titulo","Datos para reportes");q.setAttribute("vista","datos-reportes.jsp");q.setAttribute("rutaActual","/reportes");
            q.getRequestDispatcher("/WEB-INF/pagina.jsp").forward(q,s);
        }catch(SQLException e){getServletContext().log("Consultar datos para reportes",e);s.sendError(503,"No se pudo consultar los datos.");}
    }
    @Override protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException,ServletException {
        try {
            String tipo=q.getParameter("tipo");String[] campo=CAMPOS.get(tipo);
            if(campo==null)throw new IllegalArgumentException("Tipo inválido.");
            int id=Validacion.id(q.getParameter("id"));String valor=Validacion.texto(q.getParameter("valor"),"Valor",80,false);
            BigDecimal costo=null;
            if("productos".equals(tipo)&&!valor.isEmpty())costo=Validacion.decimal(valor,"Costo",2);
            String motivo=Validacion.texto(q.getParameter("motivo"),"Motivo del costo",100,"productos".equals(tipo));
            try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
                c.setAutoCommit(false);
                try {
                    try(PreparedStatement p=c.prepareStatement("SET @nt_motivo=?")){p.setString(1,motivo);p.execute();}
                    try(PreparedStatement p=c.prepareStatement("UPDATE "+campo[0]+" SET "+campo[3]+"=? WHERE "+campo[1]+"=?")) {
                        if("productos".equals(tipo))p.setBigDecimal(1,costo);else p.setString(1,valor.isEmpty()?null:valor);
                        p.setInt(2,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("Registro inexistente.");
                    }
                    c.commit();
                }catch(SQLException|RuntimeException e){c.rollback();throw e;}
            }
            q.setAttribute("aviso","Dato guardado correctamente.");doGet(q,s);
        }catch(IllegalArgumentException e){s.setStatus(400);q.setAttribute("aviso",e.getMessage());doGet(q,s);}
        catch(SQLException e){getServletContext().log("Guardar dato de reporte",e);s.sendError(503,"No se pudo guardar el dato.");}
    }
}
