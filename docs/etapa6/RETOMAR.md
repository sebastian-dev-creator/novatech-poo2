# Retomar NovaTech — etapa 6

Actualizado: 23 de octubre de 2026. Curso Programación Orientada a Objetos II.

## Estado vigente

- El alumno autorizó expresamente los cambios del profesor, respaldo, migración, publicación y comprobación. No volver a solicitar esa misma autorización.
- Proyecto: `C:/Users/gamep/Downloads/NovaTech-login-usuarios`.
- Código publicado: `316b9e8f21cee53a2217ef9dd03cd48106e45f42`; GitHub `sebastian-dev-creator/novatech-poo2`, rama main.
- Render desplegó ese commit y el login público responde 200 con el botón «Abrir ventana de acceso». URL: https://novatech-poo2.onrender.com
- Aiven tiene 22 tablas. El respaldo nuevo del 22 de octubre y las dos migraciones están verificados. No volver a ejecutar ni recrear el esquema sin necesidad.
- 74 comprobaciones de ventas y 156 de regresión aprobadas (230). Cierre real en Chrome y bloqueo tras tres fallos comprobados; fixture local de cierre retirado.
- Reglas: ventas registradas no se editan ni eliminan, solo se pagan/anulan según estado; pendiente anulada no cambia stock, pagada anulada lo devuelve una vez. Comprobantes académicos.
- Comprobación autenticada en producción completada: B-00000001 pendiente anulada conserva stock 10; B-00000002 pagada descuenta stock a 8 y anulada lo devuelve a 10. Ambas ventas ficticias quedan ANULADAS; cliente y artículo DEMO conservados. Consultar `04-Publicacion-produccion.md` y capturas del 23 de octubre. No repetir estas altas por una interrupción de créditos.
- Los antiguos registros ficticios locales se conservaron; no se limpiaron tablas ni se restauró la base.
- El archivo temporal de credenciales de migración fue eliminado. No guardar credenciales ni respaldos en Git.

## Documentos

Leer LEEME.md, README.md de esta carpeta, 02-Analisis-codigo.md, 03-Aclaraciones-profesor.md y 04-Publicacion-produccion.md. La explicación oral del profesor sustituye las dudas del correo del 9 de octubre. Los archivos de evidencia del 9 de octubre son históricos; usar los del 22 para las reglas actuales.

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


## Alcance pendiente

La reportería de etapa 7 es una entrega distinta. No hay envío a SUNAT, cobro bancario ni facturación electrónica. Los comprobantes se pueden imprimir como PDF desde el navegador.
