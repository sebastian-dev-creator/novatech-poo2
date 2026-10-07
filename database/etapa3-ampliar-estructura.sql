-- NovaTech · Etapa 3 · Ampliacion compatible con el esquema existente.
-- MySQL 8.4. Ejecutar con una cuenta administradora DESPUES de verificar respaldo.
-- No elimina ni reinserta usuarios. Los nuevos datos personales quedan NULL.
-- DDL hace commit implicito: si hay error, detenerse y revisar; ROLLBACK no revierte ALTER.
-- Reejecutable sobre la version original y sobre esta misma migracion.
-- No ejecutar mientras otros procesos modifiquen la estructura.
USE novatech;
SET NAMES utf8mb4;
SET SESSION lock_wait_timeout = 30;

CREATE TABLE IF NOT EXISTS permisos (
id_permiso INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(60) NOT NULL UNIQUE,
descripcion VARCHAR(150) NULL,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS sexos (
id_sexo INT AUTO_INCREMENT PRIMARY KEY,
descripcion VARCHAR(30) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS estados_civiles (
id_estado_civil INT AUTO_INCREMENT PRIMARY KEY,
descripcion VARCHAR(30) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS paises (
id_pais INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS departamentos (
id_departamento INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
id_pais INT NOT NULL,
UNIQUE KEY uq_departamentos_nombre (id_pais,nombre),
CONSTRAINT fk_departamentos_paises FOREIGN KEY (id_pais) REFERENCES paises(id_pais),
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS provincias (
id_provincia INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
id_departamento INT NOT NULL,
UNIQUE KEY uq_provincias_nombre (id_departamento,nombre),
CONSTRAINT fk_provincias_departamentos FOREIGN KEY (id_departamento) REFERENCES departamentos(id_departamento),
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS distritos (
id_distrito INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL,
id_provincia INT NOT NULL,
UNIQUE KEY uq_distritos_nombre (id_provincia,nombre),
CONSTRAINT fk_distritos_provincias FOREIGN KEY (id_provincia) REFERENCES provincias(id_provincia),
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='fecha_creacion') = 0, 'ALTER TABLE roles ADD COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='fecha_actualizacion') = 0, 'ALTER TABLE roles ADD COLUMN fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='usuario_registro') = 0, 'ALTER TABLE roles ADD COLUMN usuario_registro VARCHAR(50) NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='roles' AND COLUMN_NAME='activo') = 0, 'ALTER TABLE roles ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='fecha_creacion') = 0, 'ALTER TABLE datos_personales ADD COLUMN fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='fecha_actualizacion') = 0, 'ALTER TABLE datos_personales ADD COLUMN fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='usuario_registro') = 0, 'ALTER TABLE datos_personales ADD COLUMN usuario_registro VARCHAR(50) NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='activo') = 0, 'ALTER TABLE datos_personales ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuarios' AND COLUMN_NAME='usuario_registro') = 0, 'ALTER TABLE usuarios ADD COLUMN usuario_registro VARCHAR(50) NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuarios' AND COLUMN_NAME='id_permiso') = 0, 'ALTER TABLE usuarios ADD COLUMN id_permiso INT NULL COMMENT ''Permiso directo opcional, adicional a los del rol''', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuarios' AND COLUMN_NAME='token_recuperacion_hash') = 0, 'ALTER TABLE usuarios ADD COLUMN token_recuperacion_hash CHAR(64) NULL COMMENT ''Hash SHA-256 del token, nunca el token en claro''', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='usuarios' AND COLUMN_NAME='token_recuperacion_expira') = 0, 'ALTER TABLE usuarios ADD COLUMN token_recuperacion_expira DATETIME NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='usuarios' AND CONSTRAINT_NAME='fk_usuarios_permiso') = 0, 'ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_permiso FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso) ON DELETE SET NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='dni') = 0, 'ALTER TABLE datos_personales ADD COLUMN dni VARCHAR(15) NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='fecha_nacimiento') = 0, 'ALTER TABLE datos_personales ADD COLUMN fecha_nacimiento DATE NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='id_sexo') = 0, 'ALTER TABLE datos_personales ADD COLUMN id_sexo INT NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND COLUMN_NAME='id_estado_civil') = 0, 'ALTER TABLE datos_personales ADD COLUMN id_estado_civil INT NULL', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND CONSTRAINT_NAME='uq_datos_personales_dni') = 0, 'ALTER TABLE datos_personales ADD CONSTRAINT uq_datos_personales_dni UNIQUE (dni)', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND CONSTRAINT_NAME='fk_datos_sexo') = 0, 'ALTER TABLE datos_personales ADD CONSTRAINT fk_datos_sexo FOREIGN KEY (id_sexo) REFERENCES sexos(id_sexo)', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

SET @novatech_ddl = IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='datos_personales' AND CONSTRAINT_NAME='fk_datos_estado_civil') = 0, 'ALTER TABLE datos_personales ADD CONSTRAINT fk_datos_estado_civil FOREIGN KEY (id_estado_civil) REFERENCES estados_civiles(id_estado_civil)', 'DO 0');
PREPARE novatech_stmt FROM @novatech_ddl;
EXECUTE novatech_stmt;
DEALLOCATE PREPARE novatech_stmt;

CREATE TABLE IF NOT EXISTS roles_permisos (
id_rol INT NOT NULL,
id_permiso INT NOT NULL,
PRIMARY KEY (id_rol,id_permiso),
CONSTRAINT fk_roles_permisos_rol FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE,
CONSTRAINT fk_roles_permisos_permiso FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso) ON DELETE CASCADE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS correos_electronicos (
id_correo INT AUTO_INCREMENT PRIMARY KEY,
id_datos_personales INT NOT NULL,
correo VARCHAR(254) NOT NULL,
tipo_correo VARCHAR(20) NOT NULL DEFAULT 'PERSONAL',
UNIQUE KEY uq_correo_persona (id_datos_personales,correo),
CONSTRAINT fk_correo_datos FOREIGN KEY (id_datos_personales) REFERENCES datos_personales(id_datos_personales) ON DELETE CASCADE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS telefonos (
id_telefono INT AUTO_INCREMENT PRIMARY KEY,
id_datos_personales INT NOT NULL,
numero VARCHAR(20) NOT NULL,
tipo_telefono VARCHAR(20) NOT NULL DEFAULT 'MOVIL',
UNIQUE KEY uq_telefono_persona (id_datos_personales,numero),
CONSTRAINT fk_telefono_datos FOREIGN KEY (id_datos_personales) REFERENCES datos_personales(id_datos_personales) ON DELETE CASCADE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS direcciones (
id_direccion INT AUTO_INCREMENT PRIMARY KEY,
id_datos_personales INT NOT NULL,
id_distrito INT NOT NULL,
calle_avenida VARCHAR(150) NOT NULL,
tipo_direccion VARCHAR(20) NOT NULL DEFAULT 'DOMICILIO',
piso VARCHAR(10) NULL,
numero VARCHAR(10) NULL,
referencia VARCHAR(255) NULL,
CONSTRAINT fk_direccion_datos FOREIGN KEY (id_datos_personales) REFERENCES datos_personales(id_datos_personales) ON DELETE CASCADE,
CONSTRAINT fk_direccion_distrito FOREIGN KEY (id_distrito) REFERENCES distritos(id_distrito),
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Si no se conoce quien creo un registro anterior, usuario_registro queda NULL.
-- Las fechas nuevas de roles/datos personales reflejan la migracion, no fechas historicas reconstruidas.
-- Se conserva el estado BOOLEAN activo; equivale a Activo/Inactivo del Excel.
-- No se cargan personas, DNI, correos, ubicaciones ni contrasenas del ejemplo.
START TRANSACTION;

INSERT INTO sexos(descripcion,usuario_registro) SELECT 'Masculino','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM sexos WHERE descripcion='Masculino');
INSERT INTO sexos(descripcion,usuario_registro) SELECT 'Femenino','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM sexos WHERE descripcion='Femenino');
INSERT INTO sexos(descripcion,usuario_registro) SELECT 'No binario','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM sexos WHERE descripcion='No binario');
INSERT INTO sexos(descripcion,usuario_registro) SELECT 'Prefiere no decirlo','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM sexos WHERE descripcion='Prefiere no decirlo');
INSERT INTO estados_civiles(descripcion,usuario_registro) SELECT 'Soltero','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM estados_civiles WHERE descripcion='Soltero');
INSERT INTO estados_civiles(descripcion,usuario_registro) SELECT 'Casado','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM estados_civiles WHERE descripcion='Casado');
INSERT INTO estados_civiles(descripcion,usuario_registro) SELECT 'Divorciado','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM estados_civiles WHERE descripcion='Divorciado');
INSERT INTO estados_civiles(descripcion,usuario_registro) SELECT 'Viudo','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM estados_civiles WHERE descripcion='Viudo');
INSERT INTO estados_civiles(descripcion,usuario_registro) SELECT 'Conviviente','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM estados_civiles WHERE descripcion='Conviviente');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'VER_MENU','Acceder al menu principal','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='VER_MENU');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_USUARIOS','Crear, consultar, modificar y eliminar usuarios','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_USUARIOS');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_ROLES','Administrar roles','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_ROLES');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'DESBLOQUEAR_USUARIOS','Restablecer el acceso de usuarios bloqueados','MIGRACION_ETAPA3' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='DESBLOQUEAR_USUARIOS');
INSERT INTO roles_permisos(id_rol,id_permiso,usuario_registro)
SELECT r.id_rol,p.id_permiso,'MIGRACION_ETAPA3' FROM roles r CROSS JOIN permisos p
WHERE ((r.nombre='ADMINISTRADOR' AND p.nombre IN ('VER_MENU','GESTIONAR_USUARIOS','GESTIONAR_ROLES','DESBLOQUEAR_USUARIOS'))
OR (r.nombre='OPERADOR' AND p.nombre='VER_MENU'))
AND NOT EXISTS(SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=r.id_rol AND rp.id_permiso=p.id_permiso);
COMMIT;
SELECT 'Estructura de etapa 3 aplicada. Consultar docs/cumplimiento para el estado funcional de cada campo.' AS resultado;
SELECT COUNT(*) AS usuarios_conservados FROM usuarios;
SHOW TABLES;
