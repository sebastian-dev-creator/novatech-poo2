# Publicación de la etapa 5 — POO II

Fecha: 8 de octubre de 2026. Autorizada expresamente por el alumno.

## Respaldo y migración

- Respaldo completo de novatech con mysqldump: tablas, datos, rutinas, triggers y eventos; salida 0 y marcador final de volcado comprobados. Se conserva fuera del repositorio.
- Archivo: novatech-completo-antes-etapa5-20261008-123240.sql (20 928 bytes).
- SHA-256: 81bf71832962520ff7f33acc0095c33245bea28895bcc5c98cf687127d583e39.
- Migración database/etapa5-comercial.sql ejecutada sin errores en Aiven. La base pasó de 14 a 20 tablas; conservó los 2 usuarios y las 2 fichas personales anteriores.
- Tres permisos comerciales concedidos al rol ADMINISTRADOR. El rol OPERADOR no recibió esos permisos.
- La cuenta de aplicación pudo insertar, consultar, actualizar y eliminar una marca dentro de una transacción que terminó en ROLLBACK; no se amplió su acceso.
- Se retiró el archivo temporal de credenciales administrativas. No se guardaron credenciales ni respaldos en Git.

## Publicación

Código enviado a main en el commit 132299f591d719acd32fce75644e7857d58ea006. Render inició automáticamente el despliegue dep-db3t88gjo6nc73bf6ab0.

Render confirmó Deploy succeeded / Live, duración 1m51s. El login responde HTTP 200. Sin sesión, las cuatro rutas comerciales redirigen a /login (HTTP 302).

## Comprobaciones funcionales de producción

Se inició sesión como administrador y se comprobaron las altas desde la interfaz publicada:

- Cliente temporal con DNI 99081026 (id 1).
- Proveedor temporal con RUC 20990810260 (id 1).
- Producto temporal QA-ET5-081026 (id 1), precio 25.50, stock 0 y relación al proveedor de prueba.
- Consulta directa confirmó la persistencia de los tres registros y usuario_registro=admin.

Las ediciones y eliminaciones todavía no están acreditadas en producción. Las primeras tentativas de edición no persistieron los cambios; deben repetirse y verificarse antes de declarar la prueba completa. La prueba se interrumpió por límite de uso. Al retomarla, Render estaba suspendido y Aiven apagado; se encendió el servicio existente, sin repetir la migración. Aiven volvió a Running y una consulta SQL confirmó que se conservan los dos usuarios y los tres registros temporales indicados. La sesión web expiró y se solicitó al alumno que vuelva a iniciar sesión.

Pendiente: terminar las ediciones y eliminaciones de estos tres registros, comprobar que se conserven los datos anteriores y registrar evidencia final. No ejecutar el fixture de QA ni las suites masivas contra Aiven.
