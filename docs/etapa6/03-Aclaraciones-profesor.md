# Cambios según las aclaraciones del profesor

22 de octubre de 2026. Fuente: transcripción de la explicación en clase proporcionada por el alumno. Esta interpretación sustituye las dudas del correo del 9 de octubre para este avance.

1. **Cierre del acceso.** La página inicial abre una ventana mediante el botón «Abrir ventana de acceso». `assets/login/login.js` usa `window.open` desde esa acción del usuario. El formulario se muestra dentro de esa ventana. Al tercer fallo, el servidor bloquea la cuenta, invalida la sesión y redirige a `/acceso-cerrado`; `assets/login/cierre.js` ejecuta `window.close`. Se comprobó el cierre real en Chrome y el bloqueo persistente (3 intentos, bloqueado=1). La pestaña inicial permanece abierta. Si el navegador bloquea ventanas emergentes, debe permitirse la apertura para NovaTech; una pestaña abierta manualmente no ofrece la misma garantía de cierre.
2. **Singleton.** Se conserva el gestor único `ConexionBD`: constructor privado, `InstanciaHolder`, `getInstancia()` y `obtenerConexion()`. Sus cinco bloques ya están identificados en comentarios. Cada operación obtiene y cierra su propia conexión; no se comparte una conexión física entre usuarios. Es la solución aceptada en la explicación del profesor.
3. **Patrones.** Para este avance, el profesor señaló Singleton y Repository. No se interpreta como obligación implementar dos patrones en cada módulo. Los patrones State y Strategy ya desarrollados para ventas se mantienen porque resuelven estados y descuentos; no se presentan como una exigencia adicional.
4. **Pruebas.** JUnit y Selenium no son obligatorios. Se conservan las comprobaciones Java HttpClient/JDBC contra MySQL local y la comprobación visual en Chrome. Maven compila/empaqueta; las pruebas de integración se ejecutan por separado.
5. **Estructuras y POO.** Se justifican las utilizadas: List/ArrayList para líneas y resultados, HashSet para evitar artículos duplicados, Map para reconstruir formularios, interfaces para repositorios/cálculo y clases de estado. No se añaden estructuras sin una necesidad del proceso.
6. **Ventas inmutables.** Una venta registrada, incluso PENDIENTE, no se edita ni elimina. Puede consultarse, pagarse cuando corresponde o anularse. Los botones de edición/borrado se retiraron y el servidor/repositorio rechazan intentos manuales. Para corregir, anular y crear otra venta. Antes de registrar sí se pueden preparar o retirar líneas del formulario.
7. **Anulación e inventario.** Anular una pendiente conserva cabecera y líneas sin tocar stock ni inventar un pago. Anular una pagada devuelve las cantidades una sola vez. La restricción de fechas admite ambos casos y se actualiza con `database/etapa6-reglas-profesor.sql`.
8. **Comprobantes.** Boleta y factura son académicas, con identificación interna y vista imprimible. No hay facturación electrónica, envío a SUNAT ni cobro bancario real.

## Evidencia local

- 74 comprobaciones de ventas aprobadas: `evidencias/reglas-profesor-2026-10-22.txt`.
- 156 comprobaciones de regresión aprobadas: `evidencias/regresion-profesor-2026-10-22.txt`.
- Total: 230. Incluyen rechazo de edición/borrado, anulación pendiente/pagada, stock, doble anulación, permisos, CSRF y concurrencia.
- Chrome: el tercer fallo mostró «Acceso cerrado» y la ventana desapareció del inventario de pestañas sin cierre manual. SQL confirmó 3 intentos y bloqueo. La cuenta local desechable se retiró después; no se bloqueó ninguna cuenta real.
- Estas verificaciones locales no equivalen por sí solas a una comprobación del despliegue en producción; consultar el documento de publicación.
