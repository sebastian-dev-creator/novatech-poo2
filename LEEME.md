# NovaTech — POO II

Aplicación académica Java con Maven, Servlets/JSP, Tomcat y MySQL. Código en GitHub; aplicación en Render y base de datos en Aiven.

## Funcionalidad

- Acceso con PBKDF2, sesiones, bloqueo persistente al tercer error y desbloqueo administrativo.
- «Abrir ventana de acceso» abre el formulario mediante JavaScript para permitir el cierre real al tercer fallo. La pestaña inicial queda abierta.
- Menú por permisos y CRUD de usuarios, roles, clientes, proveedores y productos/servicios/insumos.
- Ventas: registrar cabecera y líneas, consultar, registrar pago y anular. Ninguna venta registrada se edita ni elimina, aunque esté pendiente. Para corregirla, anular y crear otra.
- Pago descuenta stock; anulación de pagada lo devuelve una sola vez; anulación de pendiente no lo modifica.
- Boletas/facturas académicas imprimibles, sin validez tributaria ni facturación electrónica.
- CSRF, permisos en servidor, consultas parametrizadas y escape HTML.

## Ejecutar

Abrir en NetBeans la carpeta que contiene `pom.xml`. Configurar Tomcat 10.1 y contexto `/novatech`. Maven compila y empaqueta con `mvn package` y genera el WAR. Configurar en el proceso Java `NOVATECH_DB_URL` (URL JDBC de MySQL/esquema novatech), `NOVATECH_DB_USER` y `NOVATECH_DB_PASSWORD`. Nunca subir contraseñas a Git.

La cuenta web es distinta de la cuenta MySQL. No recrear la base ni ejecutar utilidades de creación de administrador sobre una instalación existente. Las migraciones dependen de las etapas previas; respaldar antes. En una instalación de etapa 5, aplicar `database/etapa6-ventas.sql` y `database/etapa6-reglas-profesor.sql` antes del nuevo código.

## Arquitectura y pruebas

`ConexionBD` implementa Singleton como gestor único con conexiones independientes por operación. Los repositorios separan persistencia y controladores. Ventas usa State para transiciones y Strategy para descuentos. El profesor aclaró que no exige dos patrones por módulo ni todas las estructuras del Excel; se justifican las utilizadas.

Maven construye el WAR. Las suites Java HttpClient/JDBC se ejecutan por separado sobre MySQL local aislado, nunca sobre Aiven. La revisión del 22 de octubre aprobó 74 comprobaciones de ventas y 156 de regresión. También se comprobó el cierre real en Chrome.

- [Etapa 6 y alcance](docs/etapa6/README.md)
- [Análisis por bloques](docs/etapa6/02-Analisis-codigo.md)
- [Aclaraciones del profesor](docs/etapa6/03-Aclaraciones-profesor.md)
- [Publicación y verificación](docs/etapa6/04-Publicacion-produccion.md)
- [Reanudar el entorno local](docs/etapa6/RETOMAR.md)

La reportería de etapa 7 está fuera de este cambio. Comprobar la disponibilidad de los alojamientos gratuitos antes de exponer.
