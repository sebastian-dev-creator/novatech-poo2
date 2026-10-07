<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="helpers.jspf"%>
<%String base=request.getContextPath(),nombre=(String)request.getAttribute("nombreRolPermisos");boolean reservado="ADMINISTRADOR".equals(nombre);List<String[]> permisos=(List<String[]>)request.getAttribute("permisos");%>
<div class="page-heading"><div><span class="eyebrow">ROLES Y ACCESO</span><h1>Permisos de <%=esc(nombre)%></h1><p class="muted">Selecciona las acciones que pueden realizar los usuarios de este rol.</p></div><a class="button secondary" href="<%=base%>/roles">Volver a roles</a></div>
<form class="card editor" method="post" action="<%=base%>/permisos"><input type="hidden" name="csrf" value="<%=esc(session.getAttribute("csrf"))%>"><input type="hidden" name="rol" value="<%=esc(request.getAttribute("rolPermisos"))%>">
<%for(String[] p:permisos){boolean menu="VER_MENU".equals(p[1]);%><label class="check"><input type="checkbox" name="permiso" value="<%=esc(p[0])%>" <%=menu||"1".equals(p[3])?"checked":""%> <%=reservado||menu?"disabled":""%>><span><strong><%=esc(p[1].replace('_',' '))%></strong><small><%=esc(p[2])%></small></span></label><%}%>
<p class="muted">El inicio permanece disponible para todas las cuentas activas. Desbloquear usuarios requiere también gestionar usuarios. Gestionar cuentas permite crear cuentas y asignar roles; concédelo solo a personas responsables de la administración.</p>
<%if(reservado){%><p>El perfil ADMINISTRADOR conserva sus permisos.</p><%}else{%><button>Guardar permisos</button><%}%></form>
