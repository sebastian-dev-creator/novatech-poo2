package com.mycompany.novatech.app.reportes;

/** Definición inmutable. El SQL procede del catálogo del servidor. */
public final class Reporte {
    public final String id, grupo, titulo, descripcion, sql, fecha, permiso;
    public final int etiqueta, medida;
    Reporte(String id, String grupo, String titulo, String descripcion, String sql,
            String fecha, String permiso, int etiqueta, int medida) {
        this.id=id; this.grupo=grupo; this.titulo=titulo; this.descripcion=descripcion;
        this.sql=sql; this.fecha=fecha; this.permiso=permiso;
        this.etiqueta=etiqueta; this.medida=medida;
    }
}
