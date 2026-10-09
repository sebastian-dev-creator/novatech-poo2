# Publicación de la etapa 5 — POO II

Publicación: 8 de octubre de 2026. Comprobación final: 9 de octubre de 2026. Autorizada expresamente por el alumno.

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

Se completaron las ediciones desde los formularios publicados y se verificaron por consulta SQL:

- Cliente: correo actualizado a qa.editado@example.invalid.
- Proveedor: correo actualizado a proveedor.editado@example.invalid.
- Producto: precio actualizado de 25.50 a 30.75; conservó stock 0 y la relación con el proveedor id 1.
- Los tres registros conservaron usuario_registro=admin y fechas de actualización iguales o posteriores a las fechas de creación.

Se eliminaron desde la interfaz exclusivamente los registros temporales, en orden producto, proveedor y cliente. El 9 de octubre una consulta final confirmó:

| Tabla / comprobación | Resultado |
| --- | ---: |
| Usuarios | 2 |
| Fichas de datos personales | 2 |
| Clientes | 0 |
| Proveedores | 0 |
| Productos | 0 |
| Fichas personales temporales de esta prueba | 0 |

Los totales de usuarios y fichas personales volvieron a los valores previos a la prueba. No se ejecutó el fixture de QA ni las suites masivas contra Aiven. Las seis tablas comerciales, catálogos iniciales y permisos permanecen instalados.

## Cierre y alcance de la verificación

La etapa 5 está implementada, publicada y comprobada para los CRUD de clientes, proveedores y productos. La evidencia local previa cubre también servicios, insumos, validaciones, CSRF, permisos de administrador/operador, patrones y regresión: 156 comprobaciones satisfactorias en evidencias/regresion-2026-10-08.txt. La prueba en producción fue una comprobación acotada de los tres CRUD con sesión administradora, persistencia, autoría y limpieza; no repitió toda la suite local ni el inicio de sesión como operador.

El análisis Repository por bloques y los patrones complementarios se encuentran en 02-Analisis-Repository.md. El diseño existente se conserva. Ventas corresponde a etapa 6 y reportería a etapa 7. La aceptación académica de las herramientas de prueba sigue sujeta al criterio del profesor; estas pruebas Java de integración no se presentan como JUnit/Selenium.

Durante una interrupción, Render se suspendió y Aiven apareció apagado. Se recuperó el servicio existente sin repetir la migración. La disponibilidad del alojamiento gratuito es una condición operativa independiente del cierre funcional.
