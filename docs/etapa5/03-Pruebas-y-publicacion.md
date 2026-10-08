# Pruebas y publicación de etapa 5

## Estado

Implementado inicialmente el 7 de octubre de 2026 y verificado de nuevo el 8 de octubre, tras corregir la conservación del proveedor al editar productos. **Todavía no se aplicó la migración en Aiven ni se publicó esta etapa en Render.** La producción conserva su versión anterior. Las acciones de publicación de este documento requieren autorización expresa del alumno.

## Pruebas realizadas

- Maven `package`: compilación de 44 clases Java y creación del WAR correctas. Maven por sí solo no ejecuta las pruebas de integración de esta carpeta.
- MySQL aislado en `127.0.0.1:23307`, base `novatech`: migración aplicada, conservación de usuarios y fichas previas, segunda ejecución sin duplicar catálogos o permisos. La versión final incluye clientes con DNI/RUC.
- Tomcat local en `127.0.0.1:18081/novatech`: `IntegrationEtapa5` verifica altas, consultas, modificaciones y eliminaciones, clientes corporativos, identidad compartida con usuarios, validación numérica, unicidad, protección por FK, CSRF, permisos GET/POST, HTML escapado, autor de sesión y conservación del creador.
- Importación TSV: caso correcto y rollback completo cuando una fila posterior duplica un RUC existente.
- Patrones: copia de cliente sin identidad, independencia de la copia, adaptación tabular y construcción validada de productos.
- Regresión del 8 de octubre: `IntegrationEtapa4`, `IntegrationExcel` e `IntegrationEtapa5` pasaron consecutivamente con la versión final DNI/RUC y la corrección del selector. Incluye acceso, bloqueo tras tres fallos, desbloqueo, CRUD de usuarios/roles, contactos, territorio y permisos.
- Selector de proveedores: se crearon 500 proveedores posteriores al proveedor de un producto. Se verificó que el proveedor antiguo siga seleccionado al editar, después de un error de validación y al guardar. Los registros temporales se retiraron al terminar. La prueba respeta lotes de importación de hasta 50 filas.
- Validaciones adicionales: stock negativo, exceso de decimales, NaN, desbordamiento y categoría inexistente se rechazan sin modificar el producto original.
- Revisión de interfaz en Chrome sobre QA: navegación comercial, listas y formularios; alta visible de un servicio de demostración. Se conservó el diseño de NovaTech.

La evidencia más reciente de las tres suites está en `evidencias/regresion-2026-10-08.txt`; `evidencias/integracion-local.txt` conserva la ejecución comercial anterior. Estas son pruebas Java de integración, no una suite JUnit/Selenium. No se ejecutan contra producción. La revisión del 8 de octubre compiló el WAR y ejercitó HTTP, JSP y MySQL local; no repitió la revisión visual de Chrome del día anterior.

## Reproducir

1. Utilizar una instancia MySQL de pruebas separada en el puerto 23307, con base `novatech`. Ejecutar en orden `database/esquema.sql`, `database/etapa3-ampliar-estructura.sql` y `database/etapa5-comercial.sql`.
2. Configurar las tres variables `NOVATECH_DB_URL`, `NOVATECH_DB_USER`, `NOVATECH_DB_PASSWORD` exclusivamente en el proceso de QA. La URL debe comenzar con `jdbc:mysql://127.0.0.1:23307/novatech?`. No modificar las variables de producción para probar.
3. Compilar con Maven y desplegar el WAR en un Tomcat de pruebas en puerto 18081 y contexto `/novatech`, con esas mismas variables locales.
4. Con `JAVA_HOME` apuntando al JDK y el proyecto compilado, ejecutar `docs/etapa5/pruebas/ejecutar.ps1` en un proceso con las variables locales. El script rechaza cualquier URL de BD ajena a la instancia de QA.
5. `FixtureEtapa5` sirve para crear datos ficticios solo en QA y `FixtureEtapa5 limpiar` los retira. No entregar sus credenciales de demostración como credenciales reales.

## Publicar, en este orden

1. Con Aiven encendido, obtener un respaldo del esquema y datos de `novatech`. Mantenerlo fuera de Git.
2. Abrir `database/etapa5-comercial.sql` en la conexión administradora de Aiven y ejecutarlo completo. Usa la base `novatech`, no `defaultdb`. DDL realiza commits implícitos: detenerse si aparece un error; no confiar en ROLLBACK para deshacer cambios de estructura.
3. Verificar las seis tablas nuevas y los tres permisos comerciales. El usuario de la aplicación debe tener SELECT/INSERT/UPDATE/DELETE sobre las tablas nuevas, normalmente mediante sus permisos existentes sobre `novatech.*`; no necesita privilegios de modificación de estructura.
4. Revisar los cambios de código y documentación y subirlos al repositorio. Render debe publicar después de la migración, nunca antes. No hay que cambiar la contraseña ni la URL de la base.

Comandos CMD desde el proyecto, después de completar la migración:

```bat
cd /d C:\Users\gamep\Downloads\NovaTech-login-usuarios
git status
git add src database/etapa5-comercial.sql docs/etapa5 docs/cumplimiento/01-Matriz-Excel-etapas1-4.md
git commit -m "feat: agregar CRUD comerciales de etapa 5"
git push origin main
```

5. Entrar como administrador en Render. Deben aparecer Clientes, Proveedores y Productos y servicios. Registrar un cliente ficticio, un proveedor ficticio y un producto/servicio de prueba claramente identificado; editar, consultar y eliminar en orden producto → proveedor/cliente. No borrar registros de negocio. Revisar `usuario_registro` y las fechas.
6. Confirmar que un usuario sin los permisos comerciales no puede entrar directamente a las rutas. Registrar la evidencia de publicación antes de dar esta etapa por verificada en producción.

Consultas de comprobación sin credenciales:

```sql
USE novatech;
SHOW TABLES;
SELECT nombre FROM permisos WHERE nombre IN
 ('GESTIONAR_CLIENTES','GESTIONAR_PROVEEDORES','GESTIONAR_PRODUCTOS');
SELECT id, tipo_documento, razon_social, usuario_registro, fecha_creacion FROM clientes;
SELECT id, razon_social, usuario_registro, fecha_creacion FROM proveedores;
SELECT id, codigo, tipo, precio, stock, usuario_registro FROM productos;
```

No cargar los ejemplos académicos como personas reales ni ejecutar el fixture local en Aiven. La migración inicial solo carga categorías/unidades y permisos; los registros comerciales se crean desde los formularios.
