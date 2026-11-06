package com.mycompany.novatech.app.reportes;

import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class ReporteRepository {
    public ResultadoReporte consultar(Reporte reporte, FiltroReporte filtro, int limite) throws SQLException {
        String condicion="";
        if(reporte.fecha!=null) {
            if(filtro.desde!=null) condicion+=" AND "+reporte.fecha+">=?";
            if(filtro.hasta!=null) condicion+=" AND "+reporte.fecha+"<?";
        }
        String sql=reporte.sql.replace("{filtro}",condicion).replace("{periodo}",filtro.formatoSql())+" LIMIT ?";
        ResultadoReporte resultado=new ResultadoReporte();
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setReadOnly(true);
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(sql)) {
                p.setQueryTimeout(30);
                int indice=1;
                if(reporte.fecha!=null) {
                    if(filtro.desde!=null) p.setTimestamp(indice++,Timestamp.valueOf(filtro.desde.atStartOfDay()));
                    if(filtro.hasta!=null) p.setTimestamp(indice++,Timestamp.valueOf(filtro.hasta.plusDays(1).atStartOfDay()));
                }
                p.setInt(indice,limite+1);
                try(ResultSet r=p.executeQuery()) {
                    ResultSetMetaData meta=r.getMetaData();
                    for(int i=1;i<=meta.getColumnCount();i++) resultado.columnas.add(meta.getColumnLabel(i));
                    while(r.next()) {
                        if(resultado.filas.size()==limite) { resultado.truncado=true;break; }
                        List<Object> fila=new ArrayList<>();
                        for(int i=1;i<=meta.getColumnCount();i++) {
                            Object valor=r.getObject(i);
                            fila.add(valor instanceof Number ? valor : valor==null ? null : r.getString(i));
                        }
                        resultado.filas.add(fila);
                    }
                }
                try(Statement cobertura=c.createStatement(); ResultSet r=cobertura.executeQuery("SELECT inicio FROM reporte_cobertura WHERE id=1")) {
                    if(r.next()) resultado.cobertura=r.getString(1);
                }
                c.commit();
            } catch(SQLException|RuntimeException e) { c.rollback();throw e; }
        }
        return resultado;
    }
}
