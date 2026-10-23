# Retomar NovaTech — etapa 6

Última actualización: 2026-10-22. Curso **PROGRAMACIÓN ORIENTADA A OBJETOS II**.

## Mensaje para la siguiente sesión

> Continúa NovaTech etapa 6. Primero lee docs/etapa6/RETOMAR.md y README.md del proyecto. Conserva los cambios existentes. La implementación ajustada al profesor y 230 comprobaciones están terminadas. El alumno ya autorizó publicación y pruebas. Consultar 04-Publicacion-produccion.md antes de operar; no repetir migraciones ni modificar datos sin comprobar el estado actual.

## Estado histórico del 9 de octubre (sustituido por 03-Aclaraciones-profesor y 04-Publicacion-produccion)

- Proyecto: `C:/Users/gamep/Downloads/NovaTech-login-usuarios`.
- Base Git anterior: `0c2b6cc`, etapa 5. Los cambios de etapa 6 están en el disco, sin commit/push/despliegue.
- Nuevo paquete `app/ventas`, JSP, migración `database/etapa6-ventas.sql`, integración del menú/permiso y protección del tipo de producto con ventas pagadas.
- Maven package correcto; 63 comprobaciones de etapa 6 y 156 de regresión correctas. Detalle en README y evidencias.
- SQL de etapa 6 ejecutado dos veces **solo localmente**. Aiven y Render siguen con etapa 5.
- Una venta ficticia PENDIENTE (ID 6 en esta instancia) con observación `DEMO ETAPA6 LOCAL · Revisión visual, sin transacción real.` se dejó para revisar la pantalla. Pertenece a `demo_etapa5_local`; no cambió stock. No confundirla con datos reales. La demostración anterior de clientes/productos se conservó.
- Las suites sí retiraron sus registros temporales. No se deben borrar las tablas ni reiniciar el esquema para reanudar.

## Qué páginas abrir

Para continuar trabajando localmente, abrir **http://127.0.0.1:18081/novatech/ventas**. Si el servidor está activo, se puede iniciar sesión en la cuenta ficticia de QA documentada en `docs/etapa5/pruebas/FixtureEtapa5.java`. No usar contraseñas reales de producción para las pruebas.

No hace falta conservar la pestaña para conservar el código. Si aparece un error de conexión después de reiniciar el PC, el agente debe levantar los servicios locales existentes con los parámetros siguientes; no crear una BD nueva. No hay que dejar Aiven/Render abiertos durante trabajo local.

Para comprobar producción se usan: dashboard Aiven del servicio `novatech-db`, dashboard Render `novatech-poo2` y https://novatech-poo2.onrender.com (administrador para la verificación). El alumno autorizó expresamente la etapa 6 el 22 de octubre.

## Servicios locales y reanudación técnica

MySQL usa la carpeta EXISTENTE:

```text
C:/Program Files/MySQL/MySQL Server 8.4/bin/mysqld.exe
--no-defaults
--datadir=C:/Users/gamep/Documents/Codex/2026-10-04/e/work/mysql-etapa3-data
--port=23307 --bind-address=127.0.0.1 --mysqlx=0 --console
```

No ejecutar `--initialize`, scripts de recreación ni borrar esa carpeta. Antes de arrancar, comprobar si ya escucha el puerto y verificar el proceso. El administrador root sin contraseña corresponde **solo a esta instancia aislada en loopback**; el usuario de aplicación es `etapa3_test`, contraseña de QA `local-test`.

Tomcat:

- JDK: `C:/Program Files/Apache NetBeans/jdk`.
- Catalina home: `C:/Users/gamep/Downloads/apache-tomcat-10.1.60/apache-tomcat-10.1.60`.
- Catalina base: `C:/Users/gamep/Documents/Codex/2026-10-04/e/work/tomcat-etapa5` (se reutiliza deliberadamente).
- Puerto: 18081; contexto `/novatech`.
- DocBase: `C:/Users/gamep/Downloads/NovaTech-login-usuarios/target/novatech-1.0-SNAPSHOT`.
- Arranque Java: `-Dcatalina.home=<home> -Dcatalina.base=<base> -Djava.io.tmpdir=<base>/temp -cp <home>/bin/bootstrap.jar;<home>/bin/tomcat-juli.jar org.apache.catalina.startup.Bootstrap start`.
- Iniciar procesos en segundo plano con `Start-Process -WindowStyle Hidden`; verificar el PID y la línea de comandos antes de parar cualquiera. Los PID cambian tras cada reinicio.

**Sobrescribir explícitamente las variables en el proceso local antes de iniciar Java**: el entorno general puede contener credenciales de Aiven. No imprimirlas ni heredarlas para QA.

```powershell
$env:JAVA_HOME='C:/Program Files/Apache NetBeans/jdk'
$env:NOVATECH_DB_URL='jdbc:mysql://127.0.0.1:23307/novatech?sslMode=DISABLED&allowPublicKeyRetrieval=true'
$env:NOVATECH_DB_USER='etapa3_test'
$env:NOVATECH_DB_PASSWORD='local-test'
```

Desde la raíz del proyecto, si cambió código:

```powershell
& 'C:/Program Files/Apache NetBeans/java/maven/bin/mvn.cmd' -B -ntp package
# Reiniciar solo el Tomcat local comprobado para cargar las clases nuevas.
& './docs/etapa6/pruebas/ejecutar.ps1'
& './docs/etapa5/pruebas/ejecutar.ps1'
```

Git requiere `git -c safe.directory=C:/Users/gamep/Downloads/NovaTech-login-usuarios ...` dentro del repo. Revisar `git status` antes de editar y conservar los cambios ajenos. Cada push puede disparar Render automáticamente; revisar y probar antes.

## Fuentes oficiales

En `C:/Users/gamep/Downloads/`:

1. `13.-Módulos básicos para una solución informática_Formas Normales.xlsx`.
2. `13.1 Etapas del desarrollo de la solución informática.xlsx`.

La etapa 6 está en `Etapas!D29:D33`; ventas, cabecera/detalle y análisis. La correspondencia está en README. No sustituir estas fuentes por el PDF de Gerenciamiento de Datos I.

## Pendiente real

Ver estado vigente y evidencia de publicación en 04-Publicacion-produccion.md. El respaldo nuevo del 22 de octubre y las dos migraciones ya se realizaron. Mantener la etiqueta académica de los comprobantes; SUNAT, pago bancario y reportes de etapa 7 no están implementados aquí.
