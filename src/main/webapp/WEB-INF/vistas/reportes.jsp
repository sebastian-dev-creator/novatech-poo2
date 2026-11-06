<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.novatech.app.reportes.*,com.mycompany.novatech.app.*,java.util.*"%>
<% Reporte reporte=(Reporte)request.getAttribute("reporte"); ResultadoReporte datos=(ResultadoReporte)request.getAttribute("resultado"); FiltroReporte filtro=(FiltroReporte)request.getAttribute("filtro"); String base=request.getContextPath(); Usuario actual=(Usuario)request.getAttribute("actual"); %>
<link rel="stylesheet" href="<%=base%>/assets/reportes/reportes.css">
<div class="page-heading"><div><h1>Reportes</h1><p>Consulta resultados y descarga los datos en Excel.</p></div>
<%if(actual.esAdministrador()){%><a class="button secondary" href="<%=base%>/datos-reportes">Datos para reportes</a><%}%></div>
<section class="card">
<form class="toolbar" method="get" action="<%=base%>/reportes">
<label>Reporte<select name="reporte"><%String grupo="";for(Reporte r:(List<Reporte>)request.getAttribute("reportes")){if(!grupo.equals(r.grupo)){if(!grupo.isEmpty()){%></optgroup><%}grupo=r.grupo;%><optgroup label="<%=AppServlet.e(grupo)%>"><%}%><option value="<%=r.id%>" <%=r==reporte?"selected":""%>><%=AppServlet.e(r.titulo)%></option><%}%></optgroup></select></label>
<label>Desde<input type="date" name="desde" value="<%=filtro.desde==null?"":filtro.desde%>"></label>
<label>Hasta<input type="date" name="hasta" value="<%=filtro.hasta==null?"":filtro.hasta%>"></label>
<label>Agrupar<select name="periodo"><%String[][] periodos={{"DIA","Día"},{"SEMANA","Semana"},{"MES","Mes"},{"ANIO","Año"}};for(String[] p:periodos){%><option value="<%=p[0]%>" <%=p[0].equals(filtro.periodo)?"selected":""%>><%=p[1]%></option><%}%></select></label>
<button>Consultar</button><button class="secondary" name="formato" value="xlsx">Descargar Excel</button></form>
<h2><%=AppServlet.e(reporte.titulo)%></h2><p class="muted"><%=AppServlet.e(reporte.descripcion)%></p>
<p class="small muted">Importes en soles. Historial de accesos, cambios y movimientos capturado desde <%=AppServlet.e(datos.cobertura)%>. Las celdas «Sin dato» indican información pendiente.</p>
<%if(datos.truncado){%><div class="notice">La pantalla muestra las primeras 1 000 filas. El gráfico resume esas filas. Excel admite hasta 50 000 filas; reduce las fechas para resultados mayores.</div><%}%>
<%if(datos.filas.isEmpty()){%><p class="empty" role="status">No hay registros para estos criterios.</p><%}else{%>
<p id="grafico-descripcion" class="small muted">Gráfico de los primeros 20 grupos de la tabla. Consulta el detalle completo debajo.</p>
<div id="grafico-reporte" class="report-chart" role="img" aria-label="Gráfico del reporte" aria-describedby="grafico-descripcion" data-etiqueta="<%=reporte.etiqueta%>" data-medida="<%=reporte.medida%>"></div><%}%>
<div class="scroll"><table id="tabla-reporte"><caption><%=datos.filas.size()%> filas<%=datos.truncado?" mostradas":""%></caption><thead><tr><%for(String col:datos.columnas){%><th scope="col"><%=AppServlet.e(col)%></th><%}%></tr></thead><tbody>
<%for(List<Object> fila:datos.filas){%><tr><%for(Object v:fila){%><td <%if(v instanceof Number){%>data-numero="<%=AppServlet.e(v)%>"<%}%>><%=v==null?"Sin dato":AppServlet.e(v)%></td><%}%></tr><%}%>
</tbody></table></div></section>
<script src="<%=base%>/assets/reportes/echarts.min.js" defer></script>
<script src="<%=base%>/assets/reportes/reportes.js" defer></script>
