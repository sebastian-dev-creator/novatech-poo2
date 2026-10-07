# NovaTech — Etapa 4: dashboard y Repository

## Resultado de este avance

Se preparó un dashboard web con navegación lateral, resumen de usuarios, búsqueda por usuario/nombre/DNI, filtros de estado y pantallas separadas de listado y edición. Se conservan altas, consultas, modificaciones, eliminación con confirmación, desactivación y desbloqueo. El acceso a usuarios y roles se verifica por los permisos asignados. Por defecto el operador accede al inicio y a su sesión; solo ADMINISTRADOR asigna permisos.

El dashboard usa una **plantilla JSP propia y reutilizable**, no una plantilla comercial o de terceros: `pagina.jsp` define la estructura compartida y carga las vistas permitidas por los controladores. El estilo claro con barra lateral azul oscuro utiliza el logo y las fuentes locales del login. No conecta ni modifica la web comercial del negocio.

## Problema anterior

`UsuarioRepository` era una clase concreta que concentraba el SQL, pero los consumidores estaban acoplados directamente a ella. `AppServlet` también construía las páginas mediante cadenas HTML extensas. Esto dificultaba mantener y explicar por separado la presentación y la persistencia.

## Flujo actual

Petición → SeguridadFilter → AppServlet → UsuarioRepository → JdbcUsuarioRepository → ConexionBD (Singleton) → MySQL.

La respuesta vuelve al controlador, que coloca los datos como atributos de la petición. La plantilla y las JSP presentan esos datos con escape de HTML. Las JSP no ejecutan SQL.

## Análisis por bloques

| Bloque | Responsabilidad | Evidencia en el código |
|---|---|---|
| Contrato | Define operaciones disponibles sin incluir consultas ni Connection. | Interfaz `UsuarioRepository`. |
| Implementación | Ejecuta SQL con JDBC y cumple el contrato. | `JdbcUsuarioRepository implements UsuarioRepository`. |
| Mapeo | Convierte filas del ResultSet en objetos Usuario. | `map`. |
| Lecturas | Consulta usuarios, roles y catálogos. | `listar`, `buscar`, `roles`, `catalogoPersonal`. |
| Alta y edición | Valida los datos y guarda usuario y datos personales como una unidad. | `guardar`, `setAutoCommit(false)`, `commit`, `rollback`. |
| Acciones | Desactiva, desbloquea o elimina dentro de transacciones. | `accion`. |
| Integridad del administrador | Evita dejar el sistema sin un administrador utilizable al modificar cuentas. | `bloquearAdministradores`, `comprobarAdministrador`. |
| Autenticación | Conserva el bloqueo persistente y los resultados explícitos de etapa 3. | `autenticarConEstado`. |
| Recursos | Obtiene conexiones independientes del gestor Singleton y las cierra. | `try-with-resources`. |
| Controlador | Recibe parámetros, invoca el contrato y selecciona vistas. | `AppServlet`. |
| Presentación | Muestra listado, formularios y métricas. | `pagina.jsp`, `vistas/dashboard.jsp`, `vistas/usuarios.jsp`, `vistas/roles.jsp`. |

## Ejemplo para la sustentación

```java
private final UsuarioRepository repo = new JdbcUsuarioRepository();

// El controlador pide objetos, no escribe una consulta SQL.
List<Usuario> usuarios = repo.listar();
request.setAttribute("usuarios", usuarios);
```

La variable usa el contrato y la implementación concreta se selecciona al construirla. No se afirma que exista inyección de dependencias automática. El contrato todavía declara SQLException, y el repositorio conserva validaciones de negocio y operaciones de roles del módulo; por tanto no es una arquitectura completamente independiente de SQL ni una capa de servicios separada. Esto puede refinarse al crecer el proyecto.

La búsqueda actual filtra la lista en memoria, apropiada para el volumen pequeño de este avance. No hay paginación SQL. Los contadores se calculan con datos leídos de la base; no son estadísticas inventadas. Un usuario inactivo y bloqueado puede pertenecer a ambos filtros; los estados no deben sumarse como categorías excluyentes.

## Validación

- Compilación Maven del WAR correcta.
- Integración HTTP con Tomcat y MySQL locales: login, CSRF, permisos, CRUD, escape de HTML, bloqueo por tres fallos y desbloqueo sin regresiones detectadas.
- Navegador Edge: altas, edición de datos personales, desactivación y eliminación con confirmación; alta/edición/eliminación de un rol temporal.
- Búsqueda, filtros de bloqueados e inactivos y estado vacío.
- Error por usuario duplicado conserva los datos del formulario, pero nunca repuebla la contraseña.
- Escritorio y móvil de 390 y 320 píxeles; tablas con desplazamiento interno y formularios sin desbordamiento horizontal.

Las capturas de prueba contienen datos ficticios de una base local. Las pruebas no modificaron Aiven.

## Límites y siguientes comprobaciones

La ampliación del 6 de octubre conecta los formularios de correos, teléfonos, direcciones y ubicaciones; aplica permisos por rol y convierte `UsuarioFactory` en un creador abstracto con Factory Method. Menú usa Command y Mediator. La explicación de los dos patrones seleccionados por módulo, la matriz del Excel y la normalización están en `docs/cumplimiento`. Se verificaron nuevamente login, bloqueo y CRUD, además de las nuevas operaciones y restricciones.

El 6 de octubre de 2026 el usuario comprobó el panel ejecutado desde NetBeans: compartió el dashboard y la edición guardada de `prueba_etapa3`, confirmó la búsqueda y los filtros, y confirmó la creación y posterior eliminación de `prueba_etapa4`. Las capturas de esta última prueba muestran el alta y la confirmación de eliminación; el resultado final de borrado fue confirmado por texto. Esta ejecución local usa la base configurada por el usuario y se distingue de las pruebas automáticas aisladas descritas arriba.

El dashboard inicial fue publicado en `6fe2860`. El alumno comprobó posteriormente el despliegue `2806fbd` y el guardado/lectura de un correo en Render. Esto no equivale a repetir allí toda la suite local. El 7 de octubre se corrigió el registro del creador de roles: AppServlet obtiene el nombre de la sesión autenticada y lo entrega al repositorio; una edición conserva el creador original. Este ajuste posterior todavía requiere commit y despliegue. El alumno pospuso la adaptación visual a BootstrapDash; se conserva la plantilla JSP propia.
