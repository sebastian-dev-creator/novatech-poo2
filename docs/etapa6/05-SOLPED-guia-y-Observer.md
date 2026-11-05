# Etapa 6 actualizada · proceso de ventas

## Punto 13. Desde la solicitud hasta la guía

En **Ventas → Nueva solicitud** se elige el cliente, los artículos, las cantidades, el descuento y el tipo de comprobante. Guardar registra la cabecera en `solped` y las líneas en `solped_detalle`. Una solicitud abierta admite consulta, edición y eliminación. El control de versión rechaza un formulario antiguo.

**Generar venta pendiente** bloquea la solicitud, vuelve a leer sus líneas y verifica cliente y productos. El servidor calcula la venta con los precios vigentes. Guarda `ventas`, `detalle_venta` y el vínculo único `solped.id_venta` en la misma transacción. Un reenvío recupera la venta existente. La venta registrada conserva sus precios, cantidades y cliente. Las ventas anteriores a la migración siguen disponibles con sus documentos originales.

La boleta o factura académica se ve desde **Ver detalle**. La factura requiere un cliente con RUC. Al confirmar el pago de una venta con productos o insumos se emite automáticamente una guía académica. La guía identifica comprobante, fecha, destinatario, artículos físicos y cantidades a partir de las líneas históricas e inmutables de la venta. Una venta exclusivamente de servicios carece de traslado físico. Las guías se conservan al anular, con su estado actualizado. La impresión está disponible desde el navegador.

## Punto 14. Análisis por bloques del patrón Observer

1. **Registro.** El constructor de `JdbcVentaRepository` suscribe `EventosVenta::actualizarGuia` mediante `suscribir`. El emisor mantiene una colección de objetos que implementan el contrato `Observador`.
2. **Publicación.** `JdbcVentaRepository.transicion` valida la versión y la transición mediante State, cambia las existencias y actualiza la venta. Después llama a `eventos.publicar(conexion, venta, nuevoEstado)`.
3. **Notificación.** `EventosVenta.publicar` recorre la colección e invoca `alCambiar` en cada suscriptor. El repositorio anuncia el cambio de estado y el observador decide qué hacer con él.
4. **Respuesta.** El suscriptor inserta una guía al pasar a PAGADA con artículos físicos. Al pasar a ANULADA actualiza la guía si existe. El índice único sobre `guias.id_venta` identifica una sola guía por venta.
5. **Consistencia.** La notificación es síncrona dentro de la transacción JDBC. Un error al escribir la guía revierte también estado y existencias. Un pago rechazado por stock insuficiente no emite guía.

## Evidencia y despliegue

El 5 de noviembre se compiló el WAR con Maven y se aprobaron 94 comprobaciones de etapa 6 y 156 de regresión con Tomcat y MySQL locales. El registro del módulo está en `evidencias/documentos-local-2026-11-05.txt`. Se comprobó desde HTTP el alta, consulta y edición de SOLPED, la conversión, los reintentos, la protección de solicitudes convertidas, la guía emitida, su anulación y el caso de servicios.

El respaldo previo de Aiven del 5 de noviembre contenía las 22 tablas anteriores. Para desplegar esta versión se debe ejecutar primero `database/etapa6-documentos.sql` con una cuenta administradora en Aiven. Luego se publica el WAR en Render y se comprueba allí el recorrido con datos ficticios.
