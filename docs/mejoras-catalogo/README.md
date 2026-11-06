# Catálogo comercial y selección mediante búsqueda

Actualización del 6 de noviembre de 2026 para NovaTech.

## Uso

En Ventas → Nueva solicitud, escribe al menos dos caracteres en Cliente o Artículo. Se puede buscar por nombre, código o documento; varias palabras pueden aparecer en distinto orden. Selecciona una coincidencia con el ratón o con flechas y Enter. Escape cierra los resultados. Si cambias el texto, debes elegir nuevamente una coincidencia para evitar enviar el artículo anterior por error.

La consulta devuelve hasta 20 resultados por página. «Mostrar más coincidencias» permite continuar; precisar las palabras suele ser más rápido. En Compras se aplica el mismo mecanismo al proveedor y a cada artículo. Agregar líneas conserva las selecciones anteriores.

## Implementación

- `BusquedaComercialServlet` consulta MySQL mediante parámetros preparados, verifica sesión y permiso del módulo, filtra registros activos y pagina en el servidor. Busca en todo el catálogo, sin depender del límite de 500 filas de los listados administrativos.
- Los formularios inicialmente solo contienen los registros seleccionados. JavaScript espera 300 ms entre cambios, cancela consultas anteriores y descarta respuestas obsoletas.
- El campo visible es un combobox accesible; el identificador elegido permanece en el select del formulario. Los precios, permisos y existencias siguen validándose en el servidor al operar.
- No se incorporan servicios de búsqueda externos. La búsqueda por subcadena usa `LOCATE` y puede recorrer filas; la prueba con 10 000 artículos es una comprobación funcional local, no una garantía de latencia para catálogos de millones de filas. Para esa escala se evaluaría un índice de búsqueda específico.

## Datos académicos

`database/catalogo-novatech.sql` incorpora 128 artículos (120 productos/insumos y 8 servicios), 60 clientes y 12 proveedores. Incluye categorías, marcas, unidades, precios, costos y existencias iniciales. Los nombres y documentos se han generado para uso académico; no proceden de una base de personas reales. Los correos usan `example.com` y no se cargan teléfonos reales. Los importes no son cotizaciones comerciales.

La carga es transaccional e idempotente mediante `AplicarCatalogo.java`: inserta claves inexistentes y comprueba las cantidades antes del commit. No sobrescribe cambios posteriores del usuario ni inventa ventas o compras. El código y el cliente de prueba de la etapa 6 se desactivan mediante coincidencias exactas; se conservan sus ventas, comprobantes y auditoría. Por ello los nombres antiguos todavía pueden aparecer en el historial.

## Verificación

- Compilación Maven aprobada.
- 19 comprobaciones de búsqueda HTTP/JDBC, incluyendo un fixture de 10 000 productos, paginación sin repetición, búsqueda fuera de los últimos 500, múltiples palabras, caracteres literales, inyección SQL tratada como texto, permisos y conservación de selecciones al agregar líneas. El fixture se retira al terminar.
- Regresión de etapa 6: 94 comprobaciones aprobadas.
- Regresión de etapa 7: 372 comprobaciones aprobadas.
- La carga se ejecutó dos veces en la base local para verificar que no duplica los registros.
- Prueba visual en Chrome: selección de cliente con flechas y Enter, artículos por nombre y código, páginas de 20 y 40 coincidencias, conservación al agregar una línea de venta y agregar/retirar líneas de compra. No se registraron transacciones comerciales durante esta comprobación visual.

Para repetir las pruebas usa la base local aislada `127.0.0.1:23307/novatech`, con el esquema del proyecto y el catálogo cargado, y ejecuta `docs/mejoras-catalogo/ejecutar.ps1`. El script rechaza otras bases. Las credenciales se reciben por variables de entorno y no se guardan en Git.

## Respaldo y publicación

Antes de cargar Aiven se guardó un respaldo local completo de las 31 tablas (estructura y datos), fuera del repositorio. El usuario de aplicación no exporta triggers: sus definiciones se conservan en los scripts versionados de la base. No se modificó el esquema para esta mejora.

La carga en Aiven confirmó 128 artículos, 60 clientes y 12 proveedores, con 301 sentencias dentro de una transacción. El despliegue del código se publica por la rama `main` de GitHub a Render.
