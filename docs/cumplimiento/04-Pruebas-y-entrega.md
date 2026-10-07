# Comprobación y entrega del ajuste al Excel

## Pruebas realizadas el 6 de octubre de 2026

Entorno aislado: MySQL local en el puerto 23307, Tomcat 10.1 en 18081 y cuentas ficticias. Estas pruebas no escribieron en Aiven. Maven compiló y empaquetó 28 clases Java. Las JSP se comprobaron ejecutándolas en Tomcat, no solo compilando el WAR.

| Grupo | Resultado comprobado |
|---|---|
| Login y usuarios | Alta, edición, eliminación, escape HTML, CSRF, acceso anónimo, operador, cierre de sesión, bloqueo al tercer fallo, rechazo de contraseña correcta mientras está bloqueado y desbloqueo |
| Factory Method | El repositorio devuelve UsuarioAdministrador o UsuarioOperativo de acuerdo con el rol leído de la BD |
| Menú | El comando redirige; una opción desconocida devuelve 404; una no autorizada devuelve 403; enlaces visibles según permisos |
| Contactos | Alta por HTTP y edición de correo, teléfono y dirección; formularios recuperan datos; duplicados y teléfono inválido rechazados |
| Integridad | No puede eliminarse el contacto de otro usuario alterando el ID; el borrado de usuario elimina sus contactos; no se elimina un país con dependencias |
| Permisos | Alta y retirada por HTTP en rol temporal; efecto en sesión ya iniciada; permiso separado para desbloquear; administrador protegido; operador sin acceso a asignación |
| Navegador | Edge: navegación por comando, alta/edición/borrado de correo, formularios de direcciones, ubicaciones y permisos; comprobación de ancho a 320 y 390 px |

Scripts de integración Java y navegador se conservaron en `pruebas/` junto a este documento. No están conectados automáticamente a `mvn test` y no son una suite JUnit/Selenium. Requieren un entorno local preparado; no deben ejecutarse contra la base compartida. El navegador de verificación usa Playwright como herramienta externa, no como dependencia de NovaTech.

## Prueba manual para el alumno

1. En NetBeans: **Clean and Build** y luego **Run** del proyecto NovaTech. Iniciar sesión como administrador.
2. En Usuarios, crear una cuenta temporal y abrir **Contactos**. Registrar un correo y un teléfono, editar uno y comprobar que se conserva al volver a abrir.
3. En **Ubicaciones**, registrar país, departamento, provincia y distrito en ese orden, usando datos coherentes. Después registrar una dirección de la cuenta temporal y elegir ese distrito. No volver a ejecutar la migración por este cambio: se usan las tablas ya creadas.
4. En **Roles y acceso**, crear un rol temporal y abrir **Permisos**. Asignar solo los permisos que se quieran probar; gestionar usuarios permite administrar cuentas y asignar roles. No utilizar la cuenta principal para probar bloqueos.
5. Con una cuenta de ese rol en otra ventana, verificar las opciones disponibles. Retirar el permiso desde administración y comprobar que la URL deja de permitir el acceso al recargar.
6. Eliminar la cuenta temporal y el rol al terminar; conservar las ubicaciones reales si van a utilizarse.

## Publicación pendiente de esta versión

Revisar los cambios y subirlos en la carpeta real del proyecto:

```bat
cd /d C:\Users\gamep\Downloads\NovaTech-login-usuarios
git status --short
git add src docs/etapa4 docs/cumplimiento
git commit -m "feat: completar patrones y datos de usuarios segun Excel"
git push origin main
```

Esperar el nuevo despliegue de Render, comprobar el commit publicado y repetir una prueba de contacto/permisos con una cuenta temporal. No dar por publicada esta ampliación por ver el despliegue anterior `6fe2860`.

## Evidencias para entregar

- Matriz Excel y límites: `01-Matriz-Excel-etapas1-4.md`.
- Explicación de patrones: `02-Patrones-para-sustentar.md`.
- Diagrama propio y 3FN: `03-Modelo-y-normalizacion.md`.
- Análisis de Singleton y Repository en las carpetas de etapas 3 y 4.
- Capturas reales de la aplicación publicada después de esta ampliación. Las capturas locales de esta revisión contienen datos ficticios y se identifican como pruebas, no como datos del negocio.

La hoja de etapas enlaza otro Google Sheets de campos obligatorios que no fue accesible durante esta revisión. Antes de una afirmación académica de cumplimiento total, cotejar ese contenido y confirmar la aceptación de la plantilla propia, del cierre condicionado por el navegador y de las alternativas de estructuras/POO/herramientas de pruebas.
