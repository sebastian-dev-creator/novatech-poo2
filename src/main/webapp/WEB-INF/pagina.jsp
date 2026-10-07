<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.novatech.app.*,java.util.*"%>
<% Usuario actual=(Usuario)request.getAttribute("actual");String base=request.getContextPath(),ruta=(String)request.getAttribute("rutaActual");String vista=(String)request.getAttribute("vista"); %>
<!DOCTYPE html><html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title><%=AppServlet.e(request.getAttribute("titulo"))%> · NovaTech</title><link rel="stylesheet" href="<%=base%>/assets/login/fonts.css"><link rel="stylesheet" href="<%=base%>/estilos.css"></head>
<body><a class="skip-link" href="#contenido">Saltar al contenido</a><div class="app-shell">
<aside class="sidebar"><a class="brand" href="<%=base%>/menu"><img src="<%=base%>/assets/login/logo.png" alt="" width="40" height="40"><span>NovaTech<small>ADMINISTRACIÓN</small></span></a>
<p class="nav-label">ESPACIO DE TRABAJO</p><nav aria-label="Navegación principal">
<%for(ComandoMenu opcion:(List<ComandoMenu>)request.getAttribute("opcionesMenu")){%><a href="<%=base%>/abrir?opcion=<%=opcion.getClave()%>" <%=opcion.getRuta().equals(ruta)?"aria-current='page'":""%>><span aria-hidden="true">◇</span><%=AppServlet.e(opcion.getTitulo())%></a><%}%>
</nav>
<div class="sidebar-bottom"><span class="online-dot"></span> Sesión protegida<small>NovaTech · POO II</small></div></aside>
<div class="workspace"><header class="topbar"><div class="breadcrumb">NovaTech <span>/</span> <strong><%=AppServlet.e(request.getAttribute("titulo"))%></strong></div><div class="account"><span class="avatar" aria-hidden="true"><%=AppServlet.e(actual.username.substring(0,1).toUpperCase(java.util.Locale.ROOT))%></span><div><strong><%=AppServlet.e(actual.username)%></strong><small><%=AppServlet.e(actual.rol)%></small></div><form method="post" action="<%=base%>/salir"><input type="hidden" name="csrf" value="<%=AppServlet.e(session.getAttribute("csrf"))%>"><button class="secondary">Cerrar sesión</button></form></div></header>
<main id="contenido"><%if(request.getAttribute("aviso")!=null){%><div class="notice" role="status"><%=AppServlet.e(request.getAttribute("aviso"))%></div><%}%>
<%-- Las vistas son constantes del controlador, nunca parámetros del usuario. --%>
<%if("dashboard.jsp".equals(vista)){%><jsp:include page="vistas/dashboard.jsp"/><%}else if("usuarios.jsp".equals(vista)){%><jsp:include page="vistas/usuarios.jsp"/><%}else if("roles.jsp".equals(vista)){%><jsp:include page="vistas/roles.jsp"/><%}else if("permisos.jsp".equals(vista)){%><jsp:include page="vistas/permisos.jsp"/><%}else if("contactos.jsp".equals(vista)){%><jsp:include page="vistas/contactos.jsp"/><%}else if("ubicaciones.jsp".equals(vista)){%><jsp:include page="vistas/ubicaciones.jsp"/><%}%>
</main><footer>NovaTech · Proyecto académico de Programación Orientada a Objetos II</footer></div></div></body></html>
