# NovaTech: correspondencia con el Excel, etapas 1 a 4

Revisión actualizada: 7 de octubre de 2026. Esta matriz separa implementación, evidencia y asuntos que requieren confirmación académica. No certifica una calificación ni el cumplimiento de todo el proyecto final.

## Fuentes y criterio de alcance

- **13.-Módulos básicos para una solución informática_Formas Normales.xlsx**: hojas `1`, `Mod`, `Estructura`, `M3`, `fn`, `M3.1`, `M3.2`, `Clases`, `Patrones`, `Jconect`, `Hoja 5`, `xxx`.
- **13.1 Etapas del desarrollo de la solución informática.xlsx**, hoja `Etapas`: establece el avance por semanas.
- `Estructura!B2` exige **mínimo dos patrones por módulo**. Las listas de `I5:I7` se usan para elegirlos; no se afirma que todos sean obligatorios simultáneamente.
- `M3!B2` dice «tablas recomendadas» y `D3` «tipo de dato sugerido». Se adopta ese modelo para usuarios con las equivalencias documentadas abajo.
- Las columnas de tecnologías y los ejemplos de otras empresas no se convierten automáticamente en funciones nuevas para NovaTech. No se añaden IA, IoT, realidad aumentada, microservicios ni conexión con la web comercial.
- El alumno confirmó el 7 de octubre que el enlace de Google Sheets en `Etapas!D12` corresponde a `13.-Módulos básicos para una solución informática_Formas Normales.xlsx`, ya entregado y revisado. Esa duda de correspondencia queda cerrada.

## Requisitos de las primeras cuatro etapas

| Fuente | Requisito | Implementación / evidencia | Estado |
|---|---|---|---|
| Etapas!D2:D3 | Login JSP/HTML publicado | `WEB-INF/login.jsp`; Render y acceso mostrados por el alumno | Base publicada y comprobada |
| Etapas!D5:D8; Mod!D4 | BD, usuarios y autenticación | MySQL, `ConexionBD`, `JdbcUsuarioRepository.autenticarConEstado` | Implementado y probado |
| Etapas!D10,D16 | Singleton y análisis por bloques | `ConexionBD` con constructor privado y holder; `docs/etapa3/01-Analisis-Singleton.md` | Implementado y documentado |
| Etapas!D11; M3!B4:F120 | Reestructuración de usuarios | Script `database/etapa3-ampliar-estructura.sql`, 14 tablas; formularios de datos personales, contactos y ubicaciones | Estructura aplicada; formularios probados localmente; correo guardado y consultado en Render por el alumno |
| Etapas!D13; Mod!E5 | Bloqueo al tercer fallo | Contador persistente, bloqueo, invalidación de sesión, página «Acceso cerrado» | Probado |
| Etapas!D13 | Cierre de ventana | `cierre.js` intenta cerrar; ofrece cierre manual cuando el navegador lo impide | Adaptación técnica explícita, no equivalencia literal garantizada |
| Etapas!D14 | Contraseña protegida | Hash PBKDF2 con sal; no se guarda texto plano | Probado; explicar hash frente a cifrado reversible |
| Etapas!D18 | Plantilla de dashboard y CRUD | `pagina.jsp` reutilizable, vistas JSP y CSS; alta, listado, edición, eliminación | Probado; plantilla propia, no comercial |
| Etapas!D19:D20 | Repository y análisis por bloques | Interfaz `UsuarioRepository`, implementación `JdbcUsuarioRepository`, análisis de etapa 4 | Implementado y documentado |
| Estructura!B2,I5 | Dos patrones en login | Singleton + Facade (`AccesoFacade`) | Implementados; no depende de contabilizar el filtro como Proxy GoF |
| Estructura!B2,I6 | Dos patrones en menú | Command (`AbrirModuloCommand`) + Mediator (`MenuMediator`) | Implementados y usados por la navegación |
| Estructura!B2,I7 | Dos patrones en usuarios | Repository + Factory Method (`UsuarioFactory`, creadores y productos concretos) | Implementados y probados |
| Estructura!H5:H7 | MVC | Servlet/controlador, entidades/repositorios/modelo y JSP/vista | Separación funcional; paquetes no copian literalmente el árbol ilustrativo |
| Estructura!J3:J4 | MySQL y hasta 3FN | Modelo y dependencias en `03-Modelo-y-normalizacion.md` | Documentado para el esquema actual |
| 1!E6; Mod!D9 | CRUD de usuarios, roles y contraseñas | Formularios existentes; contraseña vacía conserva el hash al editar | Probado |
| M3!B26:F32; ejemplo roles-permisos | Permisos | Asignación de permisos existentes por rol, autorización en servidor, menú acorde al permiso | Probado; solo ADMINISTRADOR asigna permisos |

**Publicación:** las correcciones `92c72fd` y `740ca79` se publicaron en Render. El 7 de octubre se creó, editó y eliminó un rol temporal en producción y se comprobó por SQL que `usuario_registro=admin` se conservó al editar. La eliminación dejó cero filas del rol temporal. No se afirma que se haya repetido toda la suite local en producción.

## Equivalencias del modelo

| Excel | NovaTech | Decisión |
|---|---|---|
| M3!C4:C16: usuario, rol, permiso, acceso, intentos, bloqueo, recuperación y auditoría | `usuarios`: `id_usuario`, `nombre_usuario`, `password_hash`, `id_rol`, `id_permiso`, `ultimo_acceso`, `intentos_fallidos`, `bloqueado`, campos de recuperación y auditoría | Nombres SQL consistentes; `id_permiso` es opcional y se suma a los permisos del rol. Su asignación directa no tiene formulario; la gestión normal es por rol. |
| M3!C34:C44: datos personales | `datos_personales`, `sexos`, `estados_civiles` | Usuario único por ficha; DNI opcional validado como DNI peruano de 8 dígitos. Los catálogos se seleccionan en el formulario. |
| M3!C60:C89: correos, teléfonos y direcciones | Tablas del mismo significado y formularios separados por tipo de contacto | Varios contactos por persona; alta, lectura, edición y eliminación. Correo limitado a 80 caracteres en el formulario, aunque la columna permite 254. |
| M3!C91:C120: territorio | `paises → departamentos → provincias → distritos` | Formulario de catálogos para registrar solo ubicaciones necesarias; no se inventa un catálogo nacional completo. |
| `estado` activo/inactivo | `activo BOOLEAN` | Misma información representada por verdadero/falso. `bloqueado` se conserva aparte. |
| `fechaCreacion`, `fechaActualizacion`, `usuarioRegistro` | `fecha_creacion`, `fecha_actualizacion`, `usuario_registro` | Fechas por MySQL; los roles nuevos ahora registran al usuario autenticado, al igual que las altas de usuarios, contactos y ubicaciones desde sus formularios. Editar un rol conserva su creador. Los registros antiguos sin autor conocido pueden mantener NULL; no se inventan autores históricos. No equivale a una bitácora completa de cambios. |
| Token de recuperación | Hash del token y vencimiento, columnas reservadas | La recuperación por correo no está implementada; `1!G4` la presenta como posible, no obligatoria. |
| Ejemplos alternativos de M3.1/M3.2/xxx | Se adopta principalmente M3 | No se mezclan direcciones únicas y múltiples, IDs de texto e integer o todos los tipos sugeridos sin criterio. Fecha de nacimiento existe como campo opcional heredado; no tiene formulario en este avance. |

Los nombres y descripciones de permisos están definidos por las acciones que reconoce el programa: no hay un botón para inventar permisos que después no ejecuten ninguna función. El administrador conserva sus permisos; un rol nuevo puede entrar al inicio y recibe accesos de administración solo cuando se asignan. Gestionar usuarios incluye asignar roles, por lo que es un permiso administrativo amplio. Desbloquear requiere además el permiso específico.

## Estructuras y POO realmente utilizadas

| Fuente | Uso verificable |
|---|---|
| Estructura!F3:F4 | Arrays `String[]` en catálogos; listas `List<Usuario>`, `List<Contacto>` y comandos; `HashSet` en permisos y `HashMap` en etiquetas territoriales. `ArrayList` es una lista basada en array, no una lista enlazada. No se afirma usar pila, cola ni array tridimensional. |
| Estructura!G3:G4 | Clases concretas (repositorios/controladores), abstractas (`Usuario`, `UsuarioFactory`), métodos estáticos de utilidad, clase interna del holder Singleton y colecciones genéricas. No se afirma haber creado una clase propia `Sesion<T>` ni todos los ejemplos de Hoja 5. |
| Estructura!P4 | Se ejecutaron pruebas Java de integración y navegador Edge con Playwright como herramienta de verificación. No son pruebas JUnit/Selenium/Appium; si el docente exige una herramienta concreta de esta lista, debe incorporarse esa suite antes de acreditar ese criterio. |

Las columnas F/G/P no indican en el archivo cuántas alternativas deben implementarse. Esta matriz identifica las elegidas y evita presentarlas todas como realizadas.

## Requisitos del proyecto final todavía abiertos

- Etapa 5: CRUD comerciales implementados y probados localmente; falta aplicar su migración en Aiven y publicarlos. Ver `docs/etapa5`. Etapas 6 y 7: ventas y reportería/exportación todavía pendientes.
- `1!D12:E12`: seguridad y auditoría completa. Hay controles de acceso y campos de auditoría, pero falta la bitácora de accesos/cambios necesaria para varios reportes de etapa 7.
- `1!D13:E13`: respaldo y recuperación. El alumno realizó un respaldo con mysqldump; aún hace falta documentar y probar la restauración para acreditar recuperación.
- El alumno aclaró que BootstrapDash no es obligatorio y decidió conservar la plantilla propia; no usarlo no es un incumplimiento. Siguen abiertas la aceptación del cierre web condicionado y las alternativas de estructuras/POO/pruebas. No se atribuye al profesor una aprobación que no ha dado.

Los pendientes de despliegue del ajuste de roles y correspondencia del enlace externo ya están resueltos. Antes de declarar cumplimiento literal total, queda aclarar la aceptación del cierre de pestaña condicionado y cuáles alternativas de estructuras/POO/herramientas de pruebas son obligatorias. La implementación funcional no sustituye ese criterio académico.
