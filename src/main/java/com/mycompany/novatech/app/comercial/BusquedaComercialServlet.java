package com.mycompany.novatech.app.comercial;

import com.mycompany.novatech.app.Usuario;
import com.mycompany.novatech.config.ConexionBD;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/** Búsqueda remota: consulta todo el catálogo y entrega páginas pequeñas. */
@WebServlet("/buscar-comercial")
public final class BusquedaComercialServlet extends HttpServlet {
    static String json(String texto) {
        StringBuilder b=new StringBuilder("\"");
        for(char c:texto.toCharArray()) {
            if(c=='"'||c=='\\')b.append('\\').append(c);
            else if(c<32)b.append(String.format("\\u%04x",(int)c));
            else b.append(c);
        }
        return b.append('"').toString();
    }
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException {
        s.setContentType("application/json;charset=UTF-8");
        Usuario usuario=(Usuario)q.getAttribute("actual");
        String contexto=q.getParameter("contexto"),tipo=q.getParameter("tipo");
        boolean venta="ventas".equals(contexto),compra="compras".equals(contexto);
        if(usuario==null || (!venta&&!compra) || !usuario.puede(venta?"GESTIONAR_VENTAS":"GESTIONAR_COMPRAS")) {
            s.setStatus(403);s.getWriter().write("{\"error\":\"Sin permiso para esta búsqueda.\"}");return;
        }
        try {
            String texto=Validacion.texto(q.getParameter("q"),"Búsqueda",100,false);
            int pagina=Validacion.id(q.getParameter("pagina"));
            if(pagina>100000)throw new IllegalArgumentException("Refina la búsqueda.");
            if(texto.length()<2){s.getWriter().write("{\"resultados\":[],\"mas\":false}");return;}
            String select,campo;
            if("productos".equals(tipo)) {
                select="SELECT id,CONCAT(codigo,' · ',nombre,' · S/ ',precio,' · Stock: ',stock,' · ',tipo) etiqueta FROM productos WHERE activo=1";
                campo="CONCAT_WS(' ',codigo,nombre,tipo)";
            }else if(venta&&"clientes".equals(tipo)) {
                select="SELECT c.id,CONCAT(IF(c.tipo_documento='RUC',c.ruc,d.dni),' · ',IF(c.tipo_documento='RUC',c.razon_social,CONCAT_WS(' ',d.nombres,d.apellidos))) etiqueta FROM clientes c JOIN datos_personales d ON d.id_datos_personales=c.id_datos_personales WHERE c.activo=1";
                campo="CONCAT_WS(' ',c.ruc,c.razon_social,d.dni,d.nombres,d.apellidos)";
            }else if(compra&&"proveedores".equals(tipo)) {
                select="SELECT id,CONCAT(ruc,' · ',razon_social) etiqueta FROM proveedores WHERE activo=1";
                campo="CONCAT_WS(' ',ruc,razon_social)";
            }else throw new IllegalArgumentException("Tipo de búsqueda inválido.");
            String[] palabras=texto.split("\\s+");
            StringBuilder sql=new StringBuilder(select);
            for(String palabra:palabras)sql.append(" AND LOCATE(?,").append(campo).append(")>0");
            sql.append(" ORDER BY etiqueta,id LIMIT 21 OFFSET ?");
            List<String> filas=new ArrayList<>();boolean mas=false;
            try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql.toString())) {
                p.setQueryTimeout(10);int n=1;for(String palabra:palabras)p.setString(n++,palabra);p.setInt(n,pagina*20);
                try(ResultSet r=p.executeQuery()){while(r.next()){
                    if(filas.size()==20){mas=true;break;}
                    filas.add("{\"id\":"+r.getInt(1)+",\"texto\":"+json(r.getString(2))+"}");
                }}
            }
            s.getWriter().write("{\"resultados\":["+String.join(",",filas)+"],\"mas\":"+mas+"}");
        }catch(IllegalArgumentException e){s.setStatus(400);s.getWriter().write("{\"error\":"+json(e.getMessage())+"}");}
        catch(SQLException e){getServletContext().log("Buscar catálogo comercial",e);s.setStatus(503);s.getWriter().write("{\"error\":\"No se pudo buscar. Intenta nuevamente.\"}");}
    }
}
