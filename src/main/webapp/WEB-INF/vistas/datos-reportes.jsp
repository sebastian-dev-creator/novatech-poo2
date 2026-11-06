<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.novatech.app.*,java.util.*"%>
<%String base=request.getContextPath();Map<String,String[]> campos=(Map<String,String[]>)request.getAttribute("camposReporte");Map<String,List<String[]>> registros=(Map<String,List<String[]>>)request.getAttribute("registrosReporte");%>
<link rel="stylesheet" href="<%=base%>/assets/reportes/reportes.css">
<div class="page-heading"><div><h1>Datos para reportes</h1><p>Completa las clasificaciones y costos que utiliza la reportería.</p></div><a href="<%=base%>/reportes" class="button secondary">Volver a reportes</a></div>
<p class="muted">El costo inicial representa el costo promedio de las existencias actuales. Las compras posteriores actualizan ese promedio al recibirse. Las ventas guardan el costo al confirmar el pago. Vacío significa pendiente de registro.</p>
<%for(Map.Entry<String,String[]> entrada:campos.entrySet()){String tipo=entrada.getKey();boolean costo="productos".equals(tipo);%>
<section class="card report-config"><h2><%=AppServlet.e(entrada.getValue()[4])%></h2>
<form method="post" action="<%=base%>/datos-reportes"><input type="hidden" name="csrf" value="<%=AppServlet.e(session.getAttribute("csrf"))%>"><input type="hidden" name="tipo" value="<%=tipo%>">
<div class="form-grid"><label>Registro<select name="id" required><option value="">Selecciona</option><%for(String[] r:registros.get(tipo)){%><option value="<%=r[0]%>"><%=AppServlet.e(r[1]+" · Actual: "+(r[2]==null?"Sin dato":r[2]))%></option><%}%></select></label>
<label><%=AppServlet.e(entrada.getValue()[4])%><input name="valor" maxlength="80" <%if(costo){%>type="number" min="0" max="999999999.99" step="0.01"<%}%>></label>
<%if(costo){%><label>Motivo del registro o corrección<input name="motivo" maxlength="100" required></label><%}%></div><button>Guardar</button></form>
</section><%}%>
