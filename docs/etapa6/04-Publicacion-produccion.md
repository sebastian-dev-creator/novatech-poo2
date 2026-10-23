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

Preparado para publicar tras las 230 comprobaciones locales y el cierre real en Chrome. El estado Live de Render y la verificación posterior se registrarán una vez comprobados; la migración sola no acredita el despliegue.

Aplicación: https://novatech-poo2.onrender.com

## Alcance

Las suites masivas se ejecutan solo contra MySQL local. Los comprobantes son académicos y las ventas registradas únicamente se anulan. No hay cobro real ni integración tributaria.
