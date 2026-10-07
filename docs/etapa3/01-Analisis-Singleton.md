# Etapa 3 · Singleton en NovaTech

Actualizado el 7 de octubre de 2026. Singleton está implementado y analizado por bloques. El modelo ampliado y las validaciones se describen en la entrega de etapa 3 y en la matriz de cumplimiento; las limitaciones del cierre de pestaña siguen documentadas.

## Problema y solución

La clase anterior tenía constructor privado y un método estático que abría conexiones. No mantenía una instancia única, por lo que no implementaba Singleton.

Ahora `ConexionBD.getInstancia()` devuelve siempre el mismo gestor dentro de la aplicación. Este objeto conserva la configuración y centraliza la apertura de conexiones. Tanto `JdbcUsuarioRepository` como `CrearAdministrador` lo utilizan explícitamente.

## Análisis por bloques

| Bloque en ConexionBD.java | Función | Justificación |
|---|---|---|
| 1. Campos privados final | Guardan URL, usuario y contraseña obtenidos del entorno. | Configuración inmutable; no se escriben credenciales en el código. |
| 2. Constructor privado | Inicializa la configuración. | Impide crear gestores con new desde otras clases. |
| 3. InstanciaHolder | Contiene static final INSTANCIA. | Creación al primer acceso. Java sincroniza la inicialización de clases. |
| 4. getInstancia() | Devuelve la instancia única. | Punto de acceso compartido por la aplicación. |
| 5. obtenerConexion() | Valida configuración y abre una conexión JDBC. | Separa las transacciones de distintas operaciones y permite cerrar los recursos. |

## Uso en el repositorio

```java
try (Connection c = ConexionBD.getInstancia().obtenerConexion()) {
    // Consulta o transacción del repositorio.
}
```

Primero se obtiene el gestor único; después se le pide una conexión. `try-with-resources` cierra esa conexión al terminar, también ante una excepción. El gestor sigue existiendo.

## Alcance y adaptación del ejemplo del profesor

El Excel describe una instancia global de conexión. En esta aplicación web aplicamos Singleton al **gestor de conexiones**, no a una única conexión JDBC física para todas las personas. Compartir esa conexión podría mezclar transacciones y permitir que una petición cierre el recurso que otra necesita. Esta adaptación debe explicarse en la sustentación; no debe presentarse como una conexión física única.

La instancia es única por cargador de clases de la aplicación. Tomcat local y Render tienen gestores separados. Un reinicio o redespliegue crea una instancia nueva. No es un pool de conexiones. Cambiar variables de entorno requiere reiniciar la aplicación.

## Verificación

La prueba técnica comprueba identidad de instancia bajo concurrencia y aislamiento del cierre y auto-commit de conexiones usando un controlador JDBC simulado. La simulación no acredita conectividad a Aiven.

Prueba funcional local confirmada por el usuario el 6 de octubre de 2026. Recorrido solicitado:

1. Ejecutar Clean and Build y después Run en NetBeans.
2. Abrir una sesión nueva del login e ingresar con la cuenta existente.
3. Abrir Usuarios y Roles para comprobar las lecturas.
4. Cerrar sesión y volver a ingresar.
5. Guardar capturas como evidencia sin contraseñas ni variables secretas.

## Explicación para la sustentación

“Uso Singleton para tener un solo gestor de acceso a la base de datos por aplicación. Su constructor es privado y getInstancia devuelve la instancia alojada en una clase interna estática. El gestor abre conexiones independientes para que las operaciones concurrentes no compartan transacciones. Repository utiliza este gestor y cierra cada conexión al finalizar.”

## Fuentes técnicas

- [Java: inicialización de clases, sección 12.4.2](https://docs.oracle.com/en/java/javase/26/docs/specs/jls/jls-12.html#jls-12.4.2).
- [Oracle: conexiones JDBC y múltiples hilos](https://docs.oracle.com/en/database/oracle/oracle-database/26/jjdbc/JDBC-coding-tips.html).
