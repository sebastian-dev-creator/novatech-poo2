<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="helpers.jspf"%>
<% String base=request.getContextPath();Usuario actual=(Usuario)request.getAttribute("actual"),u=(Usuario)request.getAttribute("editando");List<Usuario> lista=(List<Usuario>)request.getAttribute("usuarios");List<String[]> roles=(List<String[]>)request.getAttribute("roles");Object token=session.getAttribute("csrf");String filtro=(String)request.getAttribute("estadoFiltro"); %>
<div class="page-heading"><div><span class="eyebrow">ADMINISTRACIÓN</span><h1>Gestión de usuarios</h1><p class="muted">Cuentas, datos personales y permisos de acceso.</p></div><%if(u==null){%><a class="button" href="<%=base%>/usuarios?nuevo=1">＋ Nuevo usuario</a><%}else{%><a class="button secondary" href="<%=base%>/usuarios">Volver al listado</a><%}%></div>
<%if(u!=null){%>
<form class="card editor" method="post" action="<%=base%>/usuarios">
<input type="hidden" name="csrf" value="<%=esc(token)%>"><input type="hidden" name="accion" value="guardar"><input type="hidden" name="id" value="<%=u.id%>">
<div class="section-heading"><h2><%=u.id==0?"Nuevo usuario":"Editar usuario"%></h2><span class="muted">Los campos opcionales están indicados.</span></div>
<fieldset><legend>Datos personales</legend><div class="form-grid">
<%=campo("Nombres","nombres",u.nombres,"text",80,true)%><%=campo("Apellidos","apellidos",u.apellidos,"text",80,true)%>
<%=campo("DNI (opcional)","dni",u.dni,"text",8,false)%>
<%=catalogo("Sexo (opcional)","sexo",u.sexoId,(List<String[]>)request.getAttribute("sexos"))%>
<%=catalogo("Estado civil (opcional)","estadoCivil",u.estadoCivilId,(List<String[]>)request.getAttribute("estadosCiviles"))%>
</div></fieldset>
<fieldset><legend>Acceso al sistema</legend><div class="form-grid">
<%=campo("Usuario","usuario",u.username,"text",50,true)%>
<label>Rol<select name="rol" required><option value="">Selecciona un rol</option><%for(String[] r:roles)if("1".equals(r[3])){%><option value="<%=esc(r[0])%>" <%=u.rolId==Integer.parseInt(r[0])?"selected":""%>><%=esc(r[1])%></option><%}%></select></label>
<label><%=u.id==0?"Contraseña":"Nueva contraseña (vacía para conservar)"%><input type="password" name="password" autocomplete="new-password" minlength="12" maxlength="128" <%=u.id==0?"required":""%>><small>Entre 12 y 128 caracteres.</small></label>
</div><label class="check"><input type="checkbox" name="activo" <%=u.activo?"checked":""%>>Cuenta activa</label></fieldset>
<div class="form-actions"><button>Guardar usuario</button><a href="<%=base%>/usuarios">Cancelar</a></div></form>
<%}else{%>
<section class="card tablecard"><form class="toolbar" method="get" action="<%=base%>/usuarios">
<label>Buscar usuario<input name="q" value="<%=esc(request.getAttribute("busqueda"))%>" maxlength="100" placeholder="Usuario, nombre o DNI" type="search"></label>
<label>Estado<select name="estado"><option value="">Todos los estados</option><%for(String v:new String[]{"activo","inactivo","bloqueado"}){%><option value="<%=v%>" <%=v.equals(filtro)?"selected":""%>><%=Character.toUpperCase(v.charAt(0))+v.substring(1)%></option><%}%></select></label><button>Buscar</button><a href="<%=base%>/usuarios">Limpiar</a></form>
<div class="section-heading"><h2>Usuarios registrados</h2><span class="muted"><%=lista.size()%> resultados</span></div>
<div class="scroll"><table><thead><tr><th>Usuario / nombre</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>
<%for(Usuario x:lista){%><tr><td><strong><%=esc(x.username)%></strong><small><%=esc(x.nombres)%> <%=esc(x.apellidos)%></small></td><td><%=esc(x.rol)%></td><td><span class="pill <%=estado(x).toLowerCase(Locale.ROOT)%>"><%=estado(x)%></span><small>Fallos: <%=x.intentos%>/3</small></td><td class="actions"><a class="text-action" href="<%=base%>/usuarios?editar=<%=x.id%>">Editar</a>
<%if(x.bloqueado){%><%=accion(base,token,x.id,"/usuarios","desbloquear","Desbloquear")%><%}%>
<%if(x.id!=actual.id){if(x.activo){%><%=accion(base,token,x.id,"/usuarios","desactivar","Desactivar")%><%}%>
<details><summary>Eliminar</summary><p>¿Eliminar la cuenta <strong><%=esc(x.username)%></strong> y sus datos personales?</p><%=accion(base,token,x.id,"/usuarios","eliminar","Confirmar eliminación")%></details><%}%>
</td></tr><%}%>
<%if(lista.isEmpty()){%><tr><td class="empty" colspan="4"><strong>No se encontraron usuarios.</strong><p>Cambia la búsqueda o el filtro de estado.</p></td></tr><%}%>
</tbody></table></div></section><%}%>
