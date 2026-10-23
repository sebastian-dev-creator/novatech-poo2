# Análisis por bloques — módulo de ventas

Entrega para `Etapas!D33` del Excel oficial de POO II. Las rutas de código se entienden desde la raíz del proyecto.

## 1. Modelo relacional y entidades

`database/etapa6-ventas.sql` separa la cabecera `ventas` de sus líneas `detalle_venta`. Una cabecera pertenece a un cliente y al usuario que la registró; tiene muchas líneas, y cada línea refiere un artículo. La combinación venta/artículo es única: las cantidades de un mismo artículo se agrupan.

Las FK impiden dejar ventas sin cliente, usuario o producto. El borrado en cascada opera solo entre cabecera y detalle; el repositorio prohíbe el borrado de cualquier venta; la cascada no se usa desde la aplicación. Catálogos y datos personales siguen en sus tablas de etapas anteriores.

`Venta` representa la cabecera y contiene `List<DetalleVenta>` (ArrayList). `DetalleVenta` representa cantidad, artículo y valores históricos de la línea. Se emplea HashSet para rechazar artículos repetidos y se ordenan las líneas por producto para bloquear siempre en el mismo orden.

Los nombres y documentos de cliente y los datos del producto se copian como **instantáneas del comprobante**: no sustituyen las tablas maestras. Cambiar posteriormente el catálogo no reescribe una venta pagada. Subtotal, descuento y total también se almacenan para consultar el comprobante. Son redundancias deliberadas, calculadas centralmente y escritas en una transacción; no se afirma que el esquema carezca de toda desnormalización. Las tablas de clientes/productos conservan sus claves y catálogos normalizados.

## 2. Controlador MVC

`src/main/java/com/mycompany/novatech/app/ventas/VentasServlet.java` recibe `/ventas`.

- GET carga listado, búsqueda, formulario nuevo o detalle; una petición de edición se rechaza.
- POST `agregar` / `quitar` conserva los campos y reconstruye las líneas sin escribir en BD.
- POST `guardar` valida identificadores y cantidades, construye Venta/DetalleVenta y llama al repositorio.
- POST `pagar` y `anular` delega con la versión mostrada; `eliminar` se rechaza siempre.
- El usuario y autor se toman del objeto autenticado del filtro; un parámetro `usuario_registro` no tiene efecto.
- Éxito aplica Post/Redirect/Get; error de validación responde 400 y conserva el formulario de guardado. Error SQL se registra internamente y responde 503 sin exponer credenciales ni consultas.

`WEB-INF/vistas/ventas.jsp` presenta el formulario, listado y comprobante; `pagina.jsp` la integra en el mismo marco visual. No contiene acceso SQL ni decide los importes. Todo texto externo se escapa con `esc`, incluidas las observaciones y los nombres históricos. Los importes renderizados provienen de BigDecimal.

## 3. Repository y transacción de guardado

`VentaRepository` establece el contrato listar/buscar/guardar/pagar/anular/eliminar. `JdbcVentaRepository` lo implementa; la conexión se obtiene del Singleton `ConexionBD` existente y se cierra con try-with-resources.

`guardar` valida tipo, cliente, clave de creación, descuento, observación y líneas. Rechaza un identificador existente para impedir modificaciones. Las transiciones de estado bloquean cabecera y comparan `version` para rechazar peticiones antiguas. Un UUID de creación único evita crear una segunda venta al reenviar el mismo formulario.

Dentro de la transacción se verifica el cliente, se bloquean los artículos ordenados por ID, se leen sus precios y se calculan importes. Se insertan cabecera y detalle nuevos y se hace commit. Una excepción causa rollback: no quedan cabeceras sin líneas ni registros parciales. La lectura de cabecera/detalle también usa una transacción para obtener una vista coherente.

La clave única protege además reenvíos concurrentes: una colisión simultánea puede responder con error SQL recuperable, pero no crea dos ventas. La repetición secuencial devuelve la venta ya registrada por el mismo usuario.

## 4. Strategy — cálculo comercial

`CalculoVenta` define `descuento(subtotal)` y selecciona una estrategia mediante `para(porcentaje)`.

- `Regular` retorna descuento cero.
- `Porcentaje` calcula y redondea el descuento porcentual.

El repositorio usa la interfaz sin duplicar el flujo de persistencia según la regla elegida. Ejemplo probado: 1.5 × 10.25 = 15.38, más 2 × 10.25 = 20.50, subtotal 35.88, descuento 10% = 3.59 y total 32.29. El servidor ignora importes manipulados del cliente. No se añaden tasas fiscales no definidas en el alcance académico.

## 5. State — ciclo de vida

`EstadoVenta` declara las operaciones permitidas. Sus implementaciones son:

| Estado | Editar / eliminar | Pagar | Anular |
|---|---|---|---|
| Pendiente | Rechazado | Cambia a PAGADA | Cambia a ANULADA sin tocar stock |
| Pagada | Rechazado | Rechazado | Cambia a ANULADA |
| Anulada | Rechazado | Rechazado | Rechazado |

El objeto de estado decide; ocultar botones no constituye la protección. Por eso las peticiones HTTP manuales para editar una pagada o anular dos veces también se rechazan.

## 6. Pago, inventario y concurrencia

`transicion` bloquea primero la cabecera y verifica versión/estado. Al pagar comprueba nuevamente cliente y artículos activos. Para producto e insumo resta cantidad; para servicio no cambia stock. Si cualquier línea tiene stock insuficiente se revierte **toda** la operación, incluidas líneas anteriores ya actualizadas.

Al anular una PAGADA se devuelve exactamente la cantidad guardada; al anular una PENDIENTE no se modifica inventario. Siempre se conserva el comprobante. Estado, versión, marcas de tiempo y actualización de existencias se confirman juntos. Dos pagos por las últimas existencias se serializan mediante bloqueos InnoDB: la prueba de dos ventas de siete unidades contra stock diez permite un solo pago y deja stock tres.

`JdbcProductoRepository` impide cambiar el tipo de artículo mientras tenga una venta PAGADA, porque debe seguir admitiendo una eventual devolución. Las FK impiden eliminar productos/clientes ya referidos. Un cambio de tipo de un artículo de una pendiente obliga a anularla y registrar otra antes del pago.

El formulario existente de productos sigue siendo un ajuste manual del stock actual. No se agregó un kardex ni reservas de inventario. Se conserva el límite de stock usado por ese formulario y se impiden negativos y desbordamientos en las transiciones.

## 7. Autorización y trazabilidad

`SeguridadFilter` exige `GESTIONAR_VENTAS` en todas las peticiones de ventas y mantiene protección CSRF para POST. `MenuMediator` agrega el comando bajo el mismo permiso. Se usan PreparedStatement, listas de valores permitidos y límites en el servidor.

La cabecera conserva usuario original, usuario de la última actualización, fechas de creación/modificación/pago/anulación y versión. Esto aporta trazabilidad básica; no se presenta como un historial completo de todas las ediciones. Ese historial y los reportes son ampliaciones distintas del CRUD de esta etapa.

## 8. Comprobación de los bloques

`docs/etapa6/pruebas/IntegrationEtapa6.java` verifica los bloques mediante peticiones HTTP y consultas reales en la BD local. Comprueba rollback, carrera de stock, versiones, descuentos, estados, permisos, CSRF, escape HTML, protección de referencias y rechazo de edición/eliminación de pendientes. Los fixtures temporales se eliminan en `finally` sin tocar registros anteriores.

El runner `ejecutar.ps1` y `localOnly()` rechazan cualquier URL de BD fuera del host/puerto de QA. El servidor HTTP probado debe apuntar igualmente a esa BD. No se debe ejecutar ninguna suite contra producción.
