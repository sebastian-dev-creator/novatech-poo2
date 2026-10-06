<%@page contentType="text/html" pageEncoding="UTF-8" session="false"%>
<!DOCTYPE html>
<html lang="es"><head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Acceso cerrado · NovaTech</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/login/fonts.css">
<link rel="stylesheet" href="<%= request.getContextPath() %>/assets/login/login.css">
<script src="<%= request.getContextPath() %>/assets/login/cierre.js" defer></script>
</head><body>
<main class="access"><section class="form-panel">
<div><div class="brand"><span class="brand-name">NOVATECH</span></div>
<h1>Acceso cerrado</h1>
<p class="subtitle" role="alert">La cuenta está bloqueada por tres intentos incorrectos. Se cerró la sesión.</p>
<p class="help">Solicita a un administrador que desbloquee tu cuenta antes de volver a ingresar.</p>
<p class="help">Si esta ventana permanece abierta, ciérrala con la X de la pestaña. El navegador puede impedir el cierre automático.</p>
<button class="submit" id="cerrar-ventana" type="button" hidden>Cerrar ventana</button>
<noscript><p class="help">Cierra esta pestaña manualmente.</p></noscript>
</div></section><section class="showcase" aria-hidden="true"><img src="<%= request.getContextPath() %>/assets/login/products.jpg" alt="" width="512" height="512"></section></main>
</body></html>
