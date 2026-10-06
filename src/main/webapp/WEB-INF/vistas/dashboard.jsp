<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="helpers.jspf"%>
<% Usuario actual=(Usuario)request.getAttribute("actual");String base=request.getContextPath(); %>
<div class="page-heading"><div><span class="eyebrow">ESPACIO DE TRABAJO</span><h1>Hola, <%=esc(actual.nombres)%>.</h1><p class="muted">Este es el resumen de tu espacio NovaTech.</p></div><%if(actual.esAdministrador()){%><a class="button" href="<%=base%>/usuarios?nuevo=1">＋ Nuevo usuario</a><%}%></div>
<%if(actual.esAdministrador()){%>
<section class="metrics" aria-label="Resumen de usuarios">
<article class="card metric"><span>Usuarios registrados</span><strong><%=request.getAttribute("totalUsuarios")%></strong><a href="<%=base%>/usuarios">Ver todos →</a></article>
<article class="card metric"><span>Con acceso activo</span><strong><%=request.getAttribute("activos")%></strong><a href="<%=base%>/usuarios?estado=activo">Ver activos →</a></article>
<article class="card metric"><span>Cuentas bloqueadas</span><strong><%=request.getAttribute("bloqueados")%></strong><a href="<%=base%>/usuarios?estado=bloqueado">Revisar bloqueos →</a></article>
<article class="card metric"><span>Roles disponibles</span><strong><%=request.getAttribute("totalRoles")%></strong><a href="<%=base%>/roles">Gestionar roles →</a></article>
</section>
<div class="overview-grid"><section class="card"><div class="section-heading"><h2>Gestión de usuarios</h2><a href="<%=base%>/usuarios">Ver listado completo →</a></div><p class="muted">Administra cuentas, datos personales y acceso al sistema.</p>
<div class="scroll"><table><thead><tr><th>Usuario</th><th>Rol</th><th>Estado</th></tr></thead><tbody>
<%List<Usuario> lista=(List<Usuario>)request.getAttribute("usuarios");int mostrados=0;for(Usuario u:lista){if(mostrados++==5)break;%>
<tr><td><a href="<%=base%>/usuarios?editar=<%=u.id%>"><%=esc(u.username)%></a><small><%=esc(u.nombres)%> <%=esc(u.apellidos)%></small></td><td><%=esc(u.rol)%></td><td><span class="pill <%=estado(u).toLowerCase(Locale.ROOT)%>"><%=estado(u)%></span></td></tr><%}%>
<%if(lista.isEmpty()){%><tr><td colspan="3">No hay usuarios registrados.</td></tr><%}%>
</tbody></table></div></section><section class="card account-card"><span class="eyebrow">TU SESIÓN</span><h2><%=esc(actual.username)%></h2><p><span class="pill activo">Acceso validado</span></p><p class="muted">Perfil <strong><%=esc(actual.rol)%></strong></p><hr><p class="muted">Las cuentas bloqueadas necesitan que un administrador restablezca su acceso.</p><a href="<%=base%>/usuarios?estado=bloqueado">Revisar cuentas bloqueadas →</a></section></div>
<%}else{%>
<section class="card account-card"><span class="eyebrow">TU CUENTA</span><h2><%=esc(actual.username)%></h2><p>Rol: <%=esc(actual.rol)%></p><span class="pill activo">Sesión activa y acceso validado</span><p class="muted">Tu acceso a los módulos depende de tu perfil. Contacta al administrador si necesitas otros permisos.</p></section>
<%}%>
