# Etapa 7 — Reportes de NovaTech

## Uso para la demostración

1. Iniciar sesión como administrador y abrir **Reportes** en el menú.
2. Seleccionar un reporte, opcionalmente Desde/Hasta y agrupación; pulsar **Consultar**.
3. Mostrar el gráfico Apache ECharts y la tabla. La descripción explica qué fecha y estado utiliza cada reporte.
4. Pulsar **Descargar Excel**. El archivo XLSX contiene las hojas Reporte y Criterios; los importes conservan tipo numérico.
5. En **Datos para reportes**, completar área del usuario, segmento del cliente, categoría del proveedor y costo actual del producto. Las correcciones de costo requieren motivo. No inventar costos históricos.
6. En **Compras**, registrar proveedor, fecha prevista, productos, cantidades y costos. Al recibir se registra evaluación de 1 a 5 y se actualiza stock/costo promedio. Después se registra el pago. Las compras pendientes sin pago pueden anularse.
7. Consultar compras por proveedor, cuentas pendientes, cumplimiento y evaluación. Mostrar movimientos de inventario y rentabilidad después de una venta pagada.

## Reglas y límites

- 55 reportes: 8 de usuarios, 10 de clientes, 10 de proveedores, 12 de productos y 15 de ventas.
- Historial de accesos/auditoría/movimientos comienza al instalar esta etapa. La fecha de cobertura aparece en pantalla y Excel. Se conserva un saldo de apertura del inventario; no se reconstruyen movimientos antiguos inexistentes.
- Fechas del servidor. Hasta incluye todo el día seleccionado. Stock, precios y valorización muestran estado actual.
- Pantalla: hasta 1.000 filas con aviso si se supera. Gráfico: primeros 20 grupos de esas filas. Excel: hasta 50.000 filas; si se supera pide reducir fechas, sin exportación silenciosamente incompleta.
- Costos desconocidos aparecen como Sin dato (celda vacía en Excel). La rentabilidad es margen bruto de ventas pagadas vigentes, con descuento proporcional y costo capturado al pagar; no incluye gastos generales. Ventas históricas sin costo no se presentan con ganancia artificial.
- Ingresos refleja cobros y devoluciones en sus respectivas fechas. Ventas anuladas no se incluyen como ventas vigentes.
- Compras académicas: recepción completa y pago completo; no pagos ni entregas parciales. Registrar pago no mueve dinero real ni integra bancos.
- Las ventas conservan la regla del profesor: no se editan ni eliminan, solo se anulan. Se mantienen solicitud, comprobante y guía de etapa 6.

## Análisis del código por bloques

- **CatalogoReportes / Reporte:** catálogo inmutable de consultas, permisos, descripción, columna de agrupación y medida. El navegador solo envía identificador; nunca SQL.
- **FiltroReporte:** valida fechas y permite día, semana ISO, mes o año. Evita introducir expresiones SQL por parámetros.
- **ReporteRepository:** patrón Repository; usa el Singleton ConexionBD, PreparedStatement, transacción de lectura, límite y timeout. Consulta datos existentes y cobertura histórica.
- **ReportesServlet:** controlador MVC, valida permisos generales y de cada módulo; despacha a JSP o ExcelReporte. Los reportes de compras requieren también su permiso.
- **reportes.jsp / reportes.js:** vista con filtros, tabla escapada y gráfico Apache ECharts 6.0.0 servido localmente, sin depender de CDN. Se conserva la política CSP del proyecto.
- **ExcelReporte:** crea un archivo Office Open XML mediante ZIP y StAX. Escribe textos como cadenas, nunca como fórmulas ejecutables. Incluye filtros y criterios en segunda hoja.
- **CompraRepository:** operaciones transaccionales; bloquea productos en orden para recibir, actualizar stock y costo promedio. Clave única evita compras duplicadas por reenvío; versión de estado impide repetir recepción/pago/anulación.
- **DatosReportesServlet:** completa atributos necesarios para los reportes. Acceso exclusivo al administrador y validación de datos y motivos.
- **ContextoOperacion / ConexionBD / SeguridadFilter:** propagan el usuario actual a la conexión para auditoría y limpian el contexto al terminar cada solicitud.
- **etapa7-reportes.sql:** migración aditiva con seis tablas nuevas, cinco columnas, 47 triggers y permisos. Los triggers registran auditoría y movimientos dentro de la misma transacción; no registran contraseñas.
- **JdbcUsuarioRepository:** registra resultados de acceso sin modificar el bloqueo al tercer fallo.
- **JdbcVentaRepository:** captura costo al cobrar y restituye inventario/costo al anular; conserva las transacciones y Observer de etapa 6.

## Validación

- 372 comprobaciones automáticas locales de etapa 7: 55 consultas, 55 pantallas HTTP y 55 exportaciones, XML, permisos, CSRF, filtros, límites, compras, inventario, costos, descuentos, anulaciones e ingresos.
- Regresión de etapas 5 y 6 aprobada. Ver evidencias de ejecución.
- Descarga XLSX desde Chrome comprobada además con openpyxl: dos hojas y precios numéricos.
- Migración Aiven aplicada con respaldo previo: 25 → 31 tablas, 47 triggers. La aplicación conserva su usuario limitado a datos; no usa credenciales administrativas.

## Catálogo y criterios

| ID | Grupo | Reporte | Criterio |
|---|---|---|---|
| u01 | Usuarios | Usuarios registrados | Fecha de registro del usuario. Estado actual de la cuenta. |
| u02 | Usuarios | Usuarios activos e inactivos | Incluye el estado del rol y el bloqueo actual. |
| u03 | Usuarios | Usuarios por rol o perfil | Roles actuales de las cuentas registradas en el período. |
| u04 | Usuarios | Accesos al sistema | Historial capturado desde el inicio de etapa 7. Fechas del servidor. |
| u05 | Usuarios | Actividades realizadas por usuario | Operaciones registradas en auditoría desde etapa 7. |
| u06 | Usuarios | Usuarios por área o departamento | Área organizativa asignada en Datos para reportes. |
| u07 | Usuarios | Intentos de acceso y bloqueos | Intentos fallidos, bloqueos e inactividad registrados desde etapa 7. |
| u08 | Usuarios | Cambios realizados por usuario | Historial de altas, modificaciones y bajas. No incluye contraseñas. |
| c01 | Clientes | Clientes registrados | Fecha de registro y estado actual. |
| c02 | Clientes | Clientes activos e inactivos | Estado actual de los clientes registrados en el período. |
| c03 | Clientes | Clientes por ubicación | Ubicación registrada. Sin asignar agrupa clientes sin distrito. |
| c04 | Clientes | Clientes nuevos por período | Altas de clientes por fecha de registro. Agrupación configurable. |
| c10 | Clientes | Evolución de clientes | Altas de clientes por fecha de registro. Agrupación configurable. |
| c05 | Clientes | Clientes frecuentes | Compras pagadas vigentes por cliente. Frecuente significa al menos dos compras pagadas en el período. |
| c06 | Clientes | Clientes con mayor volumen de compras | Clientes ordenados por importe de compras pagadas vigentes en el período. |
| c07 | Clientes | Historial de compras por cliente | Incluye ventas pendientes, pagadas y anuladas. Fecha de registro. |
| c08 | Clientes | Clientes con pagos pendientes | Saldo completo de las ventas pendientes a la fecha de consulta. |
| c09 | Clientes | Clientes por segmento o categoría | Segmento asignado en Datos para reportes. |
| s01 | Proveedores | Proveedores registrados | Proveedores por fecha de registro. |
| s02 | Proveedores | Proveedores activos e inactivos | Estado actual. |
| s03 | Proveedores | Proveedores por categoría | Categoría asignada en Datos para reportes. |
| s04 | Proveedores | Proveedores por ubicación | Ubicación registrada del proveedor. |
| s05 | Proveedores | Compras por proveedor | Órdenes de compra vigentes por fecha de registro. |
| s06 | Proveedores | Proveedores con mayor volumen de suministro | Importe recibido y número de entregas completas. Evita sumar unidades diferentes. |
| s07 | Proveedores | Productos y servicios adquiridos por proveedor | Detalle de compras recibidas, agrupado por proveedor y artículo. |
| s08 | Proveedores | Cuentas pendientes con proveedores | Importe de órdenes vigentes todavía sin pago completo. |
| s09 | Proveedores | Cumplimiento de entregas | A tiempo compara fecha de recepción con fecha prevista. Incluye órdenes pendientes y atrasadas. |
| s10 | Proveedores | Evaluación de proveedores | Promedio de calificaciones de 1 a 5 registradas al recibir cada compra. |
| p01 | Productos | Productos registrados | Artículos registrados. Incluye productos, servicios e insumos. |
| p02 | Productos | Productos por categoría | Catálogo actual, con artículos activos e inactivos. |
| p03 | Productos | Productos con mayor demanda | Cantidad vendida en ventas pagadas vigentes. Incluye artículos sin ventas. Comparar artículos con la misma unidad. |
| p04 | Productos | Productos con menor demanda | Cantidad vendida en ventas pagadas vigentes. Incluye artículos sin ventas. Comparar artículos con la misma unidad. |
| p05 | Productos | Stock disponible | Existencias actuales de productos e insumos activos. Las fechas no modifican esta fotografía actual. |
| p06 | Productos | Productos con stock mínimo | Existencias actuales de productos e insumos activos. Las fechas no modifican esta fotografía actual. |
| p07 | Productos | Productos agotados | Existencias actuales de productos e insumos activos. Las fechas no modifican esta fotografía actual. |
| p08 | Productos | Entradas y salidas de mercadería | Movimientos desde etapa 7. El saldo inicial se muestra por separado. |
| p09 | Productos | Movimientos de inventario | Kárdex desde etapa 7. Cantidad positiva es entrada y negativa es salida. Saldo posterior a cada operación. |
| p10 | Productos | Precios de productos y servicios | Precios actuales de venta en soles. |
| p11 | Productos | Productos dados de baja | Artículos marcados como inactivos. Fecha de última actualización. |
| p12 | Productos | Valorización del inventario | Stock actual por costo registrado. Vacío significa costo pendiente. El costo se actualiza por promedio ponderado al recibir compras. |
| v01 | Ventas | Ventas por período | Ventas por fecha de registro y estado actual. Importe incluye el descuento. |
| v02 | Ventas | Ventas por día, semana, mes y año | Ventas pagadas vigentes por fecha de pago. Selecciona la agrupación. |
| v03 | Ventas | Ventas por usuario o vendedor | Ventas pagadas vigentes por vendedor que registró la venta. |
| v04 | Ventas | Ventas por cliente | Ventas pagadas vigentes por fecha de pago. |
| v05 | Ventas | Ventas por producto o servicio | Ventas pagadas vigentes. Distribuye proporcionalmente el descuento de cabecera entre líneas. |
| v06 | Ventas | Ventas por categoría | Categoría actual del artículo. Ventas pagadas y descuento proporcional. |
| v07 | Ventas | Productos más vendidos | Cantidad vendida en ventas pagadas vigentes. Incluye artículos sin ventas. Comparar artículos con la misma unidad. |
| v08 | Ventas | Productos menos vendidos | Cantidad vendida en ventas pagadas vigentes. Incluye artículos sin ventas. Comparar artículos con la misma unidad. |
| v09 | Ventas | Ventas por forma de pago | Ventas pagadas vigentes, según su medio de pago. |
| v10 | Ventas | Ventas anuladas o canceladas | Fecha de anulación. Conserva el importe y el historial del comprobante. |
| v11 | Ventas | Ventas pendientes de pago | Ventas pendientes por fecha de registro. |
| v12 | Ventas | Ingresos por período | Cobros y devoluciones por sus fechas respectivas. Una venta pagada y luego anulada genera ingreso y devolución. |
| v13 | Ventas | Ticket promedio | Importe promedio de ventas pagadas vigentes por fecha de pago. |
| v14 | Ventas | Rentabilidad por producto o servicio | Margen bruto de ventas pagadas. Usa costo congelado al pagar y descuento proporcional. Si falta algún costo, el margen del artículo queda vacío. |
| v15 | Ventas | Evolución de ventas | Serie de ventas pagadas vigentes por fecha de pago. |
