# NovaTech: acceso, menú y gestión de usuarios

## Abrir y ejecutar en tu NetBeans

1. Cierra el proyecto anterior en NetBeans (clic derecho > Close). Se conserva en su carpeta.
2. File > Open Project: selecciona esta carpeta `novatech-avance`, que contiene `pom.xml`.
3. Clic derecho en el proyecto > Properties > Run: selecciona tu Apache Tomcat. Usa Context Path `/novatech`. En la plataforma Java selecciona JDK 25 si te lo solicita.
4. Clic derecho en el proyecto > Clean and Build. Espera BUILD SUCCESS.
5. En Source Packages > `com.mycompany.novatech.app`, abre `CrearAdministrador.java`. Clic derecho dentro del archivo > Run File (Shift+F6). Escribe el usuario y una contraseña nueva de 12 a 128 caracteres, dos veces. Esta es la cuenta de la página; no es la cuenta MySQL `novatech_app`.
6. Clic derecho en el proyecto > Run. Abre `http://localhost:8080/novatech/login` e ingresa con la cuenta que acabas de crear.

`PasswordUtil` y `ConexionBD` son clases auxiliares: no se ejecutan solas. `CrearAdministrador` se ejecuta una sola vez; para abrir la web se ejecuta el proyecto completo.

## Base de datos

Se usa tu base MySQL `novatech`, con las tablas `roles`, `usuarios` y `datos_personales`. Si ya las creaste, no necesitas ejecutar nuevamente el SQL. Para instalar desde cero en otra computadora, un administrador de MySQL debe ejecutar `database/esquema.sql` y crear una cuenta de aplicación con SELECT, INSERT, UPDATE y DELETE sobre `novatech.*`.

El proceso Java necesita las variables de entorno que ya configuraste:

| Variable | Valor |
|---|---|
| NOVATECH_DB_URL | `jdbc:mysql://localhost:3306/novatech?sslMode=REQUIRED` |
| NOVATECH_DB_USER | `novatech_app` |
| NOVATECH_DB_PASSWORD | La contraseña de esa cuenta MySQL |

Tras cambiar variables, detén Tomcat y cierra y vuelve a abrir NetBeans. Nunca subas contraseñas a GitHub.

## Incluido

- Login con contraseñas PBKDF2, sesiones y cierre de sesión.
- Bloqueo persistente al tercer intento incorrecto; desbloqueo por administrador.
- Menú según el rol.
- Crear, listar, editar, desactivar, reactivar y eliminar usuarios; cambiar contraseña y rol.
- Crear, listar, editar y eliminar roles adicionales sin usuarios asignados. ADMINISTRADOR tiene permisos de gestión; los demás roles tienen acceso al menú.
- Protección de rutas, formularios con CSRF, consultas parametrizadas y escape de datos en HTML.
- Se impide eliminar la propia cuenta y guardar cambios que dejen al sistema sin un administrador activo y desbloqueado.

Si se bloquean TODOS los administradores por intentos incorrectos, un administrador de MySQL debe restablecer `bloqueado=0` e `intentos_fallidos=0` para una cuenta administrativa concreta. La utilidad de creación inicial no restablece cuentas existentes.

## Comprobación rápida

1. Ingresa como administrador y crea un usuario OPERADOR.
2. Abre una ventana privada e intenta ingresar tres veces con una contraseña incorrecta del operador. Comprueba el estado Bloqueado desde la cuenta administrativa.
3. Desbloquea al operador; comprueba que entra con la contraseña correcta y no puede acceder a `/usuarios` ni `/roles`.
4. Edita el usuario, cambia la contraseña, desactívalo y comprueba que no puede entrar. Reactívalo desde Editar.
5. Elimina la cuenta de prueba. Crea y elimina un rol adicional sin asignaciones.
6. Cierra sesión y comprueba que `/menu` redirige al login.

## Organización

`ConexionBD`: conexión por JDBC. `PasswordUtil`: hashes de contraseñas. `UsuarioRepository`: SQL y transacciones. `AccesoFacade`: entrada al proceso de autenticación. `SeguridadFilter`: control de sesión y permisos. `AppServlet`: rutas y formularios. `WEB-INF/pagina.jsp`: estructura de la página. `UsuarioFactory`: creación inicial de objetos Usuario.

Este avance no acredita por sí solo una exigencia académica de dos patrones GoF por módulo: esa parte debe documentarse y ajustarse a la rúbrica del profesor.

## Estado de validación y pendientes

Compilado con Maven y JDK 25; desplegado en Tomcat 10.1.60; se comprobó la respuesta HTTP 200 de la página de login. La prueba completa de persistencia de estos módulos queda pendiente en tu sesión: el entorno de revisión no pudo leer tus variables de usuario de Windows. La conexión JDBC de tu proyecto anterior sí fue verificada por ti.

Este paquete cubre los tres módulos de la última captura. Clientes, proveedores, productos, ventas, reportes, auditoría y respaldos todavía no están implementados.

GitHub y el enlace público también están pendientes. `localhost` solo funciona en tu equipo. Para que el profesor pruebe el login se requiere alojar la aplicación Java y MySQL, configurar las tres variables en el servidor y HTTPS. GitHub almacena el código; GitHub Pages no ejecuta este servidor Java. El `.gitignore` excluye compilados y archivos de configuración locales.
