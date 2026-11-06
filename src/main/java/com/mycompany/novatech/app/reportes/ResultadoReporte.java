package com.mycompany.novatech.app.reportes;

import java.util.ArrayList;
import java.util.List;

public final class ResultadoReporte {
    public final List<String> columnas = new ArrayList<>();
    public final List<List<Object>> filas = new ArrayList<>();
    public boolean truncado;
    public String cobertura;
}
