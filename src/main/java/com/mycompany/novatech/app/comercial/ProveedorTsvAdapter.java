package com.mycompany.novatech.app.comercial;
import java.util.*;

/** Adapter: convierte filas de una hoja de cálculo (TSV) en entidades del repositorio. */
public final class ProveedorTsvAdapter {
    public List<Proveedor> convertir(String tsv) {
        if(tsv==null || tsv.isBlank() || tsv.length()>50000)
            throw new IllegalArgumentException("Pega entre 1 y 50 filas (máximo 50000 caracteres).");
        String[] filas=tsv.strip().split("\\R");
        if(filas.length>50) throw new IllegalArgumentException("Máximo 50 proveedores por importación.");
        List<Proveedor> resultado=new ArrayList<>(); Set<String> documentos=new HashSet<>();
        for(int i=0;i<filas.length;i++) {
            String[] columnas=filas[i].split("\\t",-1);
            if(columnas.length<4 || columnas.length>6)
                throw new IllegalArgumentException("Fila "+(i+1)+": usa las seis columnas indicadas; correo y teléfono pueden quedar vacíos.");
            Proveedor p=new Proveedor();
            p.documento=columnas[0]; p.razonSocial=columnas[1]; p.nombres=columnas[2]; p.apellidos=columnas[3];
            p.correo=columnas.length>4?columnas[4]:""; p.telefono=columnas.length>5?columnas[5]:"";
            try {p.validar(true);} catch(IllegalArgumentException e){throw new IllegalArgumentException("Fila "+(i+1)+": "+e.getMessage());}
            if(!documentos.add(p.documento)) throw new IllegalArgumentException("RUC repetido dentro de la importación.");
            resultado.add(p);
        }
        return resultado;
    }
}
