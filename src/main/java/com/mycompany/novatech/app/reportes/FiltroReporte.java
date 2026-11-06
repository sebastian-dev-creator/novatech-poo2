package com.mycompany.novatech.app.reportes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public final class FiltroReporte {
    public final LocalDate desde, hasta;
    public final String periodo;
    public FiltroReporte(String desde, String hasta, String periodo) {
        try {
            this.desde = desde==null || desde.isBlank() ? null : LocalDate.parse(desde);
            this.hasta = hasta==null || hasta.isBlank() ? null : LocalDate.parse(hasta);
        } catch(DateTimeParseException e) { throw new IllegalArgumentException("Introduce fechas válidas."); }
        for(LocalDate fecha:Arrays.asList(this.desde,this.hasta))
            if(fecha!=null && (fecha.getYear()<1000 || fecha.getYear()>9998))
                throw new IllegalArgumentException("Fecha fuera de rango.");
        if(this.desde!=null && this.hasta!=null && this.desde.isAfter(this.hasta))
            throw new IllegalArgumentException("La fecha inicial debe ser anterior o igual a la final.");
        this.periodo=periodo==null || periodo.isEmpty()?"MES":periodo;
        if(!Arrays.asList("DIA","SEMANA","MES","ANIO").contains(this.periodo))
            throw new IllegalArgumentException("Agrupación inválida.");
    }
    public String formatoSql() {
        switch(periodo) {
            case "DIA": return "%Y-%m-%d";
            case "SEMANA": return "%x-S%v";
            case "ANIO": return "%Y";
            default: return "%Y-%m";
        }
    }
}
