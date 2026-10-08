# Cierre local de la etapa 5 — POO II

Fecha: 8 de octubre de 2026. Proyecto: NovaTech.

## Referencias oficiales

- `13.1 Etapas del desarrollo de la solución informática.xlsx`, hoja `Etapas`, D22:D24 y D27: tablas comerciales, registro de datos, CRUD y análisis Repository por bloques. Semana 9.
- `13.-Módulos básicos para una solución informática_Formas Normales.xlsx`, hojas `Mod`, `Estructura`, `fn`, `Clases` y `Patrones`: módulos, MVC, mínimo dos patrones por módulo, normalización y atributos de referencia.
- Se excluye el PDF de Gerenciamiento de Datos I. Se conserva el diseño propio de NovaTech, según instrucción del alumno.

## Entrega comprobada

| Requisito | Implementación y evidencia |
| --- | --- |
| Crear o reestructurar tablas | `database/etapa5-comercial.sql`: clientes, proveedores, productos y catálogos; relaciones con datos personales y distritos. Aplicación local documentada en la sesión anterior. |
| Registrar datos | Altas mediante HTTP y repositorios, datos ficticios locales y consulta posterior de persistencia. |
| CRUD clientes | Alta DNI/RUC, consulta, actualización, estado y eliminación; protección de identidad compartida con usuarios. |
| CRUD proveedores | Alta, consulta, actualización y eliminación; importación transaccional; protección de relaciones con productos. |
| CRUD artículos | Productos/mercadería, insumos y servicios, precio, stock, categoría, marca, unidad y proveedor; eliminación protegida. |
| Repository | Contratos y JDBC en los tres módulos, analizados por bloques en `02-Analisis-Repository.md`. |
| Dos patrones por módulo | Clientes: Repository y Prototype. Proveedores: Repository y Adapter. Artículos: Repository y Builder. |
| Seguridad y validación | Permisos por módulo, CSRF, HTML escapado, actor de sesión, unicidad, precisión decimal, relaciones y transacciones. |

## Trabajo del cierre

Se conservó la implementación previa. Se corrigió el selector para mantener el proveedor actual cuando queda fuera de las 500 fichas recientes, tanto al editar como al mostrar errores. Se añadió una prueba con 500 proveedores posteriores, verificación del guardado y limpieza de los datos temporales. También se añadieron casos de stock inválido y categoría inexistente.

Maven `package` terminó correctamente, compilando las 44 clases y generando el WAR. Después se ejecutaron `IntegrationEtapa4`, `IntegrationExcel` e `IntegrationEtapa5`, con 156 comprobaciones satisfactorias en total y salida correcta del ejecutor. La evidencia completa está en `evidencias/regresion-2026-10-08.txt`.

Estas son pruebas Java de integración contra MySQL local en el puerto 23307 y Tomcat local en 18081. No equivalen a una suite JUnit/Selenium ni a pruebas en producción. La revisión visual previa queda documentada en `03-Pruebas-y-publicacion.md`.

## Pendiente para publicar

La implementación y verificación local de la etapa 5 están terminadas dentro del alcance descrito. Falta autorización expresa para respaldar/aplicar la migración en Aiven, subir los cambios que disparan Render y comprobar los CRUD en producción. No se ejecutó ninguna de esas acciones durante este cierre.

La selección de proveedores nuevos sigue limitada a las 500 fichas recientes; ahora editar conserva siempre la relación seleccionada si el proveedor existe. La búsqueda paginada del selector sería una ampliación de capacidad, no un requisito explícito de esta etapa.

Ventas cabecera/detalle pertenece a la etapa 6. Reportería y exportación pertenecen a la etapa 7. La aceptación académica de alternativas de herramientas de pruebas no se da por confirmada: `Estructura!P4` las enumera sin fijar una exigencia concreta para la etapa 5.
