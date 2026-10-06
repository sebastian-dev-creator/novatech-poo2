# NovaTech — Entrega de etapa 3

Fecha: 6 de octubre de 2026.

## Alcance

Esta entrega implementa Singleton en el gestor JDBC, amplía la estructura de usuarios, mantiene las contraseñas con hash y completa el flujo de bloqueo y cierre del acceso. No acredita la totalidad del CRUD, los reportes ni las demás etapas.

| Requisito | Evidencia |
|---|---|
| Singleton | `ConexionBD`: constructor privado, configuración final, clase interna estática y `getInstancia()`. Análisis por bloques en `01-Analisis-Singleton.md`. |
| Reestructurar usuarios | Migración `database/etapa3-ampliar-estructura.sql`: 14 tablas y relaciones. Aplicada por el usuario a Aiven con respaldo previo. |
| Validaciones | DNI peruano opcional de ocho dígitos y único, catálogos válidos, campos obligatorios, restricciones de roles y transacciones. |
| Tres intentos | Contador y bloqueo persistentes; lectura con `FOR UPDATE` y actualización en una transacción. |
| Protección de contraseña | PBKDF2-HMAC-SHA256, salt individual, 600 000 iteraciones. Se verifica el hash; no se almacena la contraseña en claro. |
| Cierre del acceso | Al detectar el tercer fallo o una cuenta ya bloqueada se invalida la sesión y se redirige a `/acceso-cerrado`, sin formulario. |

## Análisis del bloqueo por bloques

1. **AccesoFacade.intentar:** valida las entradas y delega la autenticación al repositorio.
2. **UsuarioRepository.autenticarConEstado:** abre una conexión mediante Singleton y bloquea la fila durante la transacción. Distingue cuenta bloqueada, denegación y éxito.
3. **PasswordUtil:** compara la contraseña ingresada con el hash. La autenticación correcta restablece los intentos; un fallo incrementa el contador hasta tres.
4. **ResultadoAcceso:** transporta un estado explícito y únicamente entrega un usuario cuando el acceso es correcto. El método anterior `autenticar` se conserva como adaptador compatible.
5. **AppServlet:** ante BLOQUEADO invalida la sesión y redirige al cierre. No crea una sesión autenticada para una cuenta bloqueada.
6. **SeguridadFilter:** comprueba en cada petición protegida si la cuenta sigue activa y desbloqueada. Una sesión anterior tampoco permite eludir un bloqueo posterior.
7. **acceso-cerrado.jsp y cierre.js:** muestran el resultado sin solicitar credenciales e intentan cerrar la ventana después de 1,5 segundos. Hay botón de cierre y alternativa manual.

## Adaptaciones que deben explicarse al profesor

**Singleton:** la instancia única es el gestor de conexiones por cargador de clases de la aplicación, no una conexión JDBC física compartida. Cada operación recibe una conexión independiente para aislar transacciones.

**“Contraseña encriptada”:** se interpreta como almacenamiento protegido mediante hash de contraseña; no es cifrado reversible.

**“Se cierra la ventana”:** el cierre automático funciona en ventanas que el navegador permite cerrar mediante scripts. Una pestaña abierta manualmente puede permanecer abierta; en ese caso se muestra “Acceso cerrado”, la sesión anterior está invalidada y la cuenta continúa bloqueada. Esto no garantiza el cierre físico de cualquier pestaña y debe presentarse como adaptación web del requisito. [Documentación de Window.close](https://developer.mozilla.org/en-US/docs/Web/API/Window/close).

**Modelo:** los campos personales nuevos son opcionales para no inventar datos de usuarios existentes. El Excel contiene distintas variantes de roles/permisos; se conserva un rol por usuario, con permisos directos opcionales y relación roles-permisos. La autorización actual sigue siendo por rol. Los formularios de múltiples contactos/direcciones y la recuperación por token no se dan por implementados.

## Verificación realizada

- Compilación Maven del WAR: correcta.
- Singleton: identidad concurrente del gestor y conexiones independientes; prueba funcional local confirmada por el usuario.
- Migración: conservación de datos, reejecución, integridad referencial y compatibilidad del CRUD en MySQL aislado. Aplicación en Aiven y funcionamiento posterior confirmados por el usuario.
- Campos personales: persistencia, duplicados con rollback, campos inválidos, conservación de contraseña, bloqueo y desbloqueo.
- Integración HTTP en Tomcat y MySQL locales: protección anónima, CSRF, login, menú, altas/edición/bajas, escape de HTML, permisos de operador, logout, redirección de los tres intentos, pantalla de cierre, bloqueo persistente y desbloqueo administrativo.
- Navegador Microsoft Edge automatizado: una pestaña con historial mantiene el aviso cuando no puede cerrarse; una ventana abierta mediante script se cierra automáticamente. Pantalla móvil de 390 px sin desbordamiento horizontal.

Las pruebas automáticas usan cuentas ficticias en MySQL local; no bloquearon el administrador de Aiven.

## Comprobación final en hosting

Después de publicar y confirmar el despliegue, crear una cuenta OPERADOR de prueba desde Usuarios y conservar la sesión del administrador. En una ventana privada, ingresar tres contraseñas incorrectas para esa cuenta de prueba. Verificar el cierre del acceso y el estado Bloqueado / 3 fallos desde la sesión administradora. Desbloquearla y verificar un ingreso correcto. No usar la única cuenta administradora para esta demostración.

Registrar capturas del resultado sin contraseñas, hashes ni datos personales reales. La comprobación funcional en hosting queda pendiente hasta que se ejecute este recorrido; no se sustituye por las pruebas locales.
