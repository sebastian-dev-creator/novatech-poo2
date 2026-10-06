<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.novatech.app.AppServlet"%>
<% String base=request.getContextPath(); %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Iniciar sesión · NovaTech</title>
  <link rel="stylesheet" href="<%= base %>/assets/login/fonts.css">
  <link rel="stylesheet" href="<%= base %>/assets/login/login.css">
  <script src="<%= base %>/assets/login/login.js" defer></script>
</head>
<body>
  <main class="access" aria-labelledby="welcome">
    <section class="form-panel">
      <div>
        <div class="brand">
          <img src="<%= base %>/assets/login/logo.png" alt="" width="44" height="44">
          <div><span class="brand-name">NOVATECH</span><span class="brand-caption">Enterprise Access</span></div>
        </div>
        <h1 id="welcome">Bienvenido a NovaTech</h1>
        <p class="subtitle">Inicia sesión para continuar</p>
        <% if(request.getAttribute("aviso")!=null){ %>
        <div class="notice" role="alert"><%= AppServlet.e(request.getAttribute("aviso")) %></div>
        <% } %>
        <form method="post" action="<%= base %>/login">
          <input type="hidden" name="csrf" value="<%= AppServlet.e(session.getAttribute("csrf")) %>">
          <div class="field">
            <label for="usuario">Usuario</label>
            <div class="input-wrap">
              <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/></svg>
              <input id="usuario" name="usuario" type="text" maxlength="50" autocomplete="username" autocapitalize="none" spellcheck="false" placeholder="Tu nombre de usuario" required>
            </div>
          </div>
          <div class="field">
            <label for="password">Contraseña</label>
            <div class="input-wrap">
              <svg class="field-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 4v3"/></svg>
              <input id="password" name="password" type="password" maxlength="128" autocomplete="current-password" placeholder="Ingresa tu contraseña" aria-describedby="access-help" required>
              <button class="toggle" type="button" id="toggle-password" aria-label="Mostrar contraseña" aria-controls="password" aria-pressed="false" hidden>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>
              </button>
            </div>
          </div>
          <p class="help" id="access-help">Tras 3 intentos incorrectos, la cuenta se bloquea. Un administrador puede desbloquearla.</p>
          <button class="submit" type="submit">Iniciar sesión <span aria-hidden="true">→</span></button>
        </form>
      </div>
      <p class="account-help">¿Necesitas una cuenta o recuperar el acceso?<br><span>Contacta al administrador de NovaTech.</span></p>
    </section>
    <section class="showcase" aria-label="Tecnología NovaTech">
      <img src="<%= base %>/assets/login/products.jpg" alt="Auriculares, lentes inteligentes y un altavoz sobre un fondo de cristal azul y violeta" width="512" height="512">
      <div class="product-badge"><span aria-hidden="true"></span>AUDIO · ESTILO · INNOVACIÓN</div>
    </section>
  </main>
  <footer>NovaTech · Proyecto académico de Programación Orientada a Objetos II</footer>
</body>
</html>

