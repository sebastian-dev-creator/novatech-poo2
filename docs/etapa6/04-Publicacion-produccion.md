# Publicación de etapa 6

22 de octubre de 2026. Autorización expresa del alumno para ajustes, respaldo, migración y publicación.

## Respaldo y migración verificados

- Respaldo nuevo fuera del repositorio: `novatech-antes-etapa6-20261022.sql`, 30 361 bytes.
- SHA-256: `D02617F241E9D6D83C2B46D10AB2D161378F654335A78839DF3772C4452685C8`.
- mysqldump finalizó con código 0; marcador final y 20 definiciones CREATE TABLE comprobados. Incluye datos, rutinas, triggers y eventos. No se ha ensayado una restauración completa de este respaldo.
- Aiven antes: 20 tablas, 2 usuarios, 0 clientes, 0 productos.
- Aplicadas `database/etapa6-ventas.sql` y `database/etapa6-reglas-profesor.sql` sin errores.
- Aiven después: 22 tablas, 2 usuarios, 0 ventas. Confirmada la restricción que permite anular una pendiente sin inventar un pago.
- `GESTIONAR_VENTAS` asignado a ADMINISTRADOR; no se concedió al rol OPERADOR.

## Código y despliegue

Código publicado en `main`: `316b9e8f21cee53a2217ef9dd03cd48106e45f42`. Render lo identifica como el último commit desplegado correctamente; despliegue `dep-dbddkptbedkc73cskukg`.

El login público responde HTTP 200 y contiene el nuevo botón de apertura. Se abrió desde esa página el formulario de acceso en Chrome. El 23 de octubre el alumno inició sesión como administrador y se completó la comprobación funcional descrita abajo.

Se retiró el archivo temporal de credenciales administrativas después de comprobar la migración. El usuario de aplicación conserva sus permisos DML sobre `novatech.*`; no se ampliaron sus privilegios.

Aplicación: https://novatech-poo2.onrender.com

## Comprobación autenticada en producción — 23 de octubre

Realizada desde Chrome sobre la aplicación publicada, con datos ficticios identificados como DEMO ACADÉMICA. Se creó el cliente «Cliente Demo Prueba Etapa Seis» y el artículo `DEMO-ET6-231026`, precio S/ 10.25 y stock inicial 10 unidades.

| Caso | Resultado observado |
| --- | --- |
| B-00000001: dos unidades, descuento 10% | Subtotal S/ 20.50, descuento S/ 2.05, total S/ 18.45. Registrada PENDIENTE y luego ANULADA; comprobante y detalle conservados, sin fecha de pago. Stock permanece 10.000. |
| B-00000002: dos unidades, sin descuento | Total S/ 20.50. PENDIENTE → PAGADA con EFECTIVO simulado: stock 8.000. PAGADA → ANULADA: stock restituido a 10.000; fechas de pago y anulación conservadas. |
| Acciones visibles | Las ventas registradas no muestran editar ni eliminar. Las anuladas solo permiten consultar el detalle. |

Ambas ventas permanecen ANULADAS como evidencia; cliente y artículo ficticios se conservaron. No se realizó ningún cobro ni devolución real. Esta comprobación funcional no sustituye la suite local de permisos, concurrencia, CSRF y rechazo de peticiones manipuladas (230 comprobaciones aprobadas).

Evidencia visual: [comprobantes anulados](evidencias/ventas-produccion-2026-10-23.png) y [stock final](evidencias/stock-produccion-2026-10-23.png).

## Alcance

Las suites masivas se ejecutan solo contra MySQL local. Los comprobantes son académicos y las ventas registradas únicamente se anulan. No hay cobro real ni integración tributaria.
