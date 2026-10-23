# Etapa 6 — Ventas (POO II)

Revisión: 22 de octubre de 2026. Ajustada a las aclaraciones del profesor; 230 comprobaciones locales aprobadas. Migración de Aiven realizada tras respaldo nuevo; publicación y comprobación documentadas en `04-Publicacion-produccion.md`. Véase también [Aclaraciones del profesor](03-Aclaraciones-profesor.md).

## Referencias oficiales y alcance

Se leyeron los dos Excel originales de Downloads mediante openpyxl, sin modificarlos. No se usó documentación de Gerenciamiento de Datos I.

| Fuente | Requisito | Implementación / evidencia |
|---|---|---|
| `13.1 Etapas del desarrollo de la solución informática.xlsx`, `Etapas!D29` | Crear o reestructurar ventas cabecera y detalle | `database/etapa6-ventas.sql`: `ventas` y `detalle_venta`, FK, restricciones e índices |
| Mismo libro, `Etapas!D30` | Registrar datos en esas tablas | Alta transaccional desde `/ventas`; integración HTTP con MySQL y demostración local |
| Mismo libro, `Etapas!D31` | CRUD de cabecera y detalle | Crear, consultar/buscar, pagar y anular; por aclaración del profesor ninguna venta registrada se edita ni elimina |
| Mismo libro, `Etapas!D33` | Análisis del código fuente por bloques del módulo ventas | [Análisis por bloques](02-Analisis-codigo.md) |
| `13.-Módulos básicos para una solución informática_Formas Normales.xlsx`, `Mod!D17` | Salida de productos/servicios y boleta o factura | Pago descuenta productos e insumos; servicio no maneja stock; comprobante académico en pantalla |
| Mismo libro, hoja `Estructura`, encabezado mínimo 2 patrones por módulo y fila Ventas | MVC y patrones aplicados | Singleton y Repository señalados por el profesor; State y Strategy complementan ventas |
| Mismo libro, hojas `Clases`, `Patrones` y `fn` | Venta/DetalleVenta, métodos de venta, estados/cálculos, relaciones | Entidades y análisis de relaciones, instantáneas históricas y cálculos en documento adjunto |

## Comportamiento

- El permiso `GESTIONAR_VENTAS` habilita el menú y las peticiones GET/POST. La migración lo asigna inicialmente a ADMINISTRADOR. OPERADOR no recibe acceso automáticamente.
- Una venta tiene un cliente activo, BOLETA o FACTURA, observación opcional, descuento de 0 a 100% y de 1 a 50 líneas distintas. FACTURA requiere cliente registrado con RUC.
- Cantidad positiva, hasta tres decimales; importes calculados con BigDecimal, redondeo por línea a dos decimales HALF_UP y descuento sobre la suma. No se aceptan precios ni totales enviados por el navegador.
- Antes de registrar, el formulario permite preparar cabecera y líneas. Una vez registrada, PENDIENTE no permite cambios ni eliminación; puede pagarse o anularse. No reserva existencias. Al registrar se toman los precios vigentes.
- Confirmar un pago cambia a PAGADA y descuenta existencias en la misma transacción. Se elige efectivo, tarjeta o transferencia para registrar el medio recibido; no existe una pasarela que cobre dinero.
- PAGADA conserva cabecera, detalle, precio y nombre históricos. Para corregirla se anula y se registra una nueva. La anulación de una PAGADA devuelve stock una sola vez; la de una PENDIENTE no altera stock. Ambas conservan el comprobante.
- No se elimina ninguna venta registrada. Las peticiones de borrado y edición se rechazan también en servidor y repositorio, aunque se manipule el navegador.
- La boleta/factura lleva número interno B/F basado en el identificador, estado, cliente, artículos, cantidades, precios, descuento, total, fechas y autor. Es un **comprobante académico**, sin validez tributaria, sin envío a SUNAT y sin desglose automático de impuestos. La vista incluye estilos de impresión para usar Imprimir/Guardar PDF del navegador. No se ha integrado facturación electrónica.
- Listados y selectores muestran hasta 500 registros; los elementos ya seleccionados fuera de ese límite se recuperan al reconstruir el formulario todavía no registrado. La búsqueda permite acotar la lista de ventas por cliente, documento o ID numérico.

## Validación realizada

`mvn -B -ntp package`: BUILD SUCCESS. Maven compila y empaqueta; **no es la ejecución de las pruebas de integración**, que se ejecutan por separado.

- `docs/etapa6/pruebas/ejecutar.ps1`: **74 comprobaciones aprobadas** con HTTP real y MySQL local.
- `docs/etapa5/pruebas/ejecutar.ps1`: **156 comprobaciones previas aprobadas** (IntegrationEtapa4, IntegrationExcel e IntegrationEtapa5).
- Total: **230 comprobaciones**. Incluyen transacciones, rollback, dos pagos simultáneos disputando stock, estados, versiones, permisos, CSRF, escape HTML, cabecera/detalle y eliminación de las cuentas/datos temporales de las suites.
- Migración ejecutada dos veces únicamente en MySQL local `127.0.0.1:23307`; sin duplicar tablas ni permisos. No es una herramienta de reparación de tablas de versiones futuras: `IF NOT EXISTS` no actualiza una tabla ya existente con distinta definición.
- Revisión visual en Chrome local: acceso, menú Ventas, formulario, conservación de campos al agregar línea, guardado de dos líneas y comprobante pendiente con descuento. [Captura](evidencias/ventas-local.png).
- `git diff --check`: sin errores de whitespace. Los avisos LF/CRLF son del entorno Windows.

No se ejecutaron JUnit/Selenium; estas pruebas usan Java HttpClient y JDBC contra una instancia local aislada. La salida impresa/PDF no fue revisada como artefacto PDF; se comprobó la vista HTML y se añadieron estilos de impresión.

## Publicación

El alumno autorizó expresamente continuar los cambios y la publicación. El 22 de octubre se respaldó Aiven y se aplicaron `etapa6-ventas.sql` y `etapa6-reglas-profesor.sql`, conservando los usuarios existentes. Consultar el documento de publicación para el estado verificado del despliegue.

La migración debe preceder al despliegue: el nuevo código consulta ventas y detalle, y la protección del cambio de tipo del producto también depende de esas tablas. La etapa 7 de reportes sigue fuera de este cambio.

Para retomar una sesión interrumpida, leer [RETOMAR](RETOMAR.md).
