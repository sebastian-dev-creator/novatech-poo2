<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.novatech.app.AppServlet,com.mycompany.novatech.app.Usuario"%>
<% Usuario actual=(Usuario)request.getAttribute("actual"); String base=request.getContextPath(); %>
<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title><%= AppServlet.e(request.getAttribute("titulo")) %> · NovaTech</title><link rel="stylesheet" href="<%= base %>/estilos.css"></head>
<body><header><a class="brand" href="<%= base %>/menu"><span class="logo">N</span> NovaTech</a>
<% if(actual!=null){ %><nav><a href="<%= base %>/menu">Inicio</a><% if(actual.esAdministrador()){ %><a href="<%= base %>/usuarios">Usuarios</a><a href="<%= base %>/roles">Roles</a><% } %></nav><div class="account"><span><%= AppServlet.e(actual.username) %></span><form method="post" action="<%= base %>/salir"><input type="hidden" name="csrf" value="<%= AppServlet.e(session.getAttribute("csrf")) %>"><button class="secondary">Cerrar sesión</button></form></div><% } else { %><span class="muted">Sistema de pedidos y disponibilidad</span><% } %>
</header><main><% if(request.getAttribute("aviso")!=null){ %><div class="notice" role="alert"><%= AppServlet.e(request.getAttribute("aviso")) %></div><% } %>
<%-- contenido es HTML construido por el controlador, con todos los datos escapados. --%>
<%= request.getAttribute("contenido") %></main><footer>NovaTech · Proyecto académico de Programación Orientada a Objetos II</footer></body></html>
