package com.mycompany.novatech.app.reportes;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import javax.xml.stream.*;

/** Libro Office Open XML. Los textos se escriben como cadenas, nunca como fórmulas. */
public final class ExcelReporte {
    private ExcelReporte() {}
    public static void escribir(OutputStream salida, Reporte reporte, FiltroReporte filtro,
            ResultadoReporte datos) throws IOException {
        try(ZipOutputStream zip=new ZipOutputStream(salida,StandardCharsets.UTF_8)) {
            archivo(zip,"[Content_Types].xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/><Override PartName=\"/xl/worksheets/sheet2.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
            archivo(zip,"_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            archivo(zip,"xl/workbook.xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Reporte\" sheetId=\"1\" r:id=\"rId1\"/><sheet name=\"Criterios\" sheetId=\"2\" r:id=\"rId2\"/></sheets></workbook>");
            archivo(zip,"xl/_rels/workbook.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet2.xml\"/></Relationships>");
            hoja(zip,"xl/worksheets/sheet1.xml",datos.columnas,datos.filas);
            List<List<Object>> criterios=new ArrayList<>();
            criterios.add(Arrays.asList("Reporte",reporte.titulo));
            criterios.add(Arrays.asList("Criterio",reporte.descripcion));
            criterios.add(Arrays.asList("Desde",reporte.fecha==null?"Estado actual":filtro.desde==null?"Sin límite":filtro.desde.toString()));
            criterios.add(Arrays.asList("Hasta inclusive",reporte.fecha==null?"Estado actual":filtro.hasta==null?"Sin límite":filtro.hasta.toString()));
            criterios.add(Arrays.asList("Agrupación",filtro.periodo));
            criterios.add(Arrays.asList("Inicio de captura histórica",datos.cobertura));
            criterios.add(Arrays.asList("Generado",java.time.Instant.now().toString()));
            criterios.add(Arrays.asList("Moneda","PEN · soles"));
            criterios.add(Arrays.asList("Celdas vacías","Dato sin registrar o no aplicable"));
            hoja(zip,"xl/worksheets/sheet2.xml",Arrays.asList("Criterio","Valor"),criterios);
        }
    }
    private static void archivo(ZipOutputStream zip,String nombre,String contenido)throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));zip.write(contenido.getBytes(StandardCharsets.UTF_8));zip.closeEntry();
    }
    private static String columna(int indice) {
        StringBuilder nombre=new StringBuilder();
        for(int n=indice+1;n>0;n=(n-1)/26) nombre.insert(0,(char)('A'+(n-1)%26));
        return nombre.toString();
    }
    private static void hoja(ZipOutputStream zip,String nombre,List<String> columnas,List<List<Object>> filas)throws IOException {
        zip.putNextEntry(new ZipEntry(nombre));
        try {
            XMLStreamWriter xml=XMLOutputFactory.newFactory().createXMLStreamWriter(zip,"UTF-8");
            xml.writeStartDocument("UTF-8","1.0");xml.writeStartElement("worksheet");
            xml.writeDefaultNamespace("http://schemas.openxmlformats.org/spreadsheetml/2006/main");
            xml.writeStartElement("sheetViews");xml.writeStartElement("sheetView");xml.writeAttribute("workbookViewId","0");
            xml.writeEmptyElement("pane");xml.writeAttribute("ySplit","1");xml.writeAttribute("topLeftCell","A2");xml.writeAttribute("state","frozen");
            xml.writeEndElement();xml.writeEndElement();
            xml.writeStartElement("cols");xml.writeEmptyElement("col");xml.writeAttribute("min","1");xml.writeAttribute("max",String.valueOf(columnas.size()));xml.writeAttribute("width","25");xml.writeAttribute("customWidth","1");xml.writeEndElement();
            xml.writeStartElement("sheetData");
            fila(xml,1,new ArrayList<Object>(columnas));
            int numero=2;for(List<Object> fila:filas)fila(xml,numero++,fila);
            xml.writeEndElement();
            xml.writeEmptyElement("autoFilter");xml.writeAttribute("ref","A1:"+columna(columnas.size()-1)+(filas.size()+1));
            xml.writeEndElement();xml.writeEndDocument();xml.flush();
        }catch(XMLStreamException e){throw new IOException("No se pudo generar Excel",e);}
        zip.closeEntry();
    }
    private static void fila(XMLStreamWriter xml,int numero,List<Object> valores)throws XMLStreamException {
        xml.writeStartElement("row");xml.writeAttribute("r",String.valueOf(numero));
        for(int i=0;i<valores.size();i++) {
            Object valor=valores.get(i);if(valor==null)continue;
            xml.writeStartElement("c");xml.writeAttribute("r",columna(i)+numero);
            if(valor instanceof Number) { xml.writeStartElement("v");xml.writeCharacters(valor.toString());xml.writeEndElement(); }
            else {
                xml.writeAttribute("t","inlineStr");xml.writeStartElement("is");xml.writeStartElement("t");
                xml.writeAttribute("xml:space","preserve");
                String limpio=valor.toString().replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]","");
                xml.writeCharacters(limpio);xml.writeEndElement();xml.writeEndElement();
            }
            xml.writeEndElement();
        }
        xml.writeEndElement();
    }
}
