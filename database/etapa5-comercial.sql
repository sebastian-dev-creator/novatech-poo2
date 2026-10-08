-- Etapa 5. Aplicar después de esquema.sql y etapa3-ampliar-estructura.sql.
-- Respaldar antes de ejecutar con cuenta de migración. MySQL DDL hace commit implícito.
-- Reejecutable sobre este esquema. No elimina datos ni carga personas ficticias.
USE novatech;
SET NAMES utf8mb4;
SET SESSION lock_wait_timeout=30;
-- Las personas comerciales no necesitan credenciales de acceso; conserva UNIQUE/FK.
ALTER TABLE datos_personales MODIFY id_usuario INT NULL;
CREATE TABLE IF NOT EXISTS clientes (
id INT AUTO_INCREMENT PRIMARY KEY,
tipo_documento VARCHAR(3) NOT NULL DEFAULT 'DNI',
ruc CHAR(11) NULL UNIQUE,
razon_social VARCHAR(150) NOT NULL DEFAULT '',
CONSTRAINT ck_cliente_tipo CHECK(tipo_documento IN ('DNI','RUC')),
CONSTRAINT ck_cliente_empresa CHECK((tipo_documento='DNI' AND ruc IS NULL) OR (tipo_documento='RUC' AND ruc IS NOT NULL AND razon_social<>'')),
id_datos_personales INT NOT NULL UNIQUE,
correo VARCHAR(254) NOT NULL DEFAULT '',
telefono VARCHAR(20) NOT NULL DEFAULT '',
direccion VARCHAR(255) NOT NULL DEFAULT '',
id_distrito INT NULL,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE,
CONSTRAINT fk_clientes_persona FOREIGN KEY(id_datos_personales) REFERENCES datos_personales(id_datos_personales),
CONSTRAINT fk_clientes_distrito FOREIGN KEY(id_distrito) REFERENCES distritos(id_distrito)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS proveedores (
id INT AUTO_INCREMENT PRIMARY KEY,
id_datos_personales INT NOT NULL UNIQUE,
ruc CHAR(11) NOT NULL UNIQUE,
razon_social VARCHAR(150) NOT NULL,
correo VARCHAR(254) NOT NULL DEFAULT '',
telefono VARCHAR(20) NOT NULL DEFAULT '',
direccion VARCHAR(255) NOT NULL DEFAULT '',
id_distrito INT NULL,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE,
CONSTRAINT fk_proveedores_persona FOREIGN KEY(id_datos_personales) REFERENCES datos_personales(id_datos_personales),
CONSTRAINT fk_proveedores_distrito FOREIGN KEY(id_distrito) REFERENCES distritos(id_distrito)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS categorias (
id INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS marcas (
id INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS unidades_medida (
id INT AUTO_INCREMENT PRIMARY KEY,
nombre VARCHAR(100) NOT NULL UNIQUE,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS productos (
id INT AUTO_INCREMENT PRIMARY KEY,
codigo VARCHAR(40) NOT NULL UNIQUE,
nombre VARCHAR(150) NOT NULL,
descripcion VARCHAR(500) NOT NULL DEFAULT '',
tipo VARCHAR(10) NOT NULL,
id_categoria INT NOT NULL,
id_marca INT NULL,
id_unidad INT NOT NULL,
id_proveedor INT NULL,
precio DECIMAL(11,2) NOT NULL,
stock DECIMAL(12,3) NOT NULL DEFAULT 0,
stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0,
fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
usuario_registro VARCHAR(50) NOT NULL,
activo BOOLEAN NOT NULL DEFAULT TRUE,
CONSTRAINT fk_producto_categoria FOREIGN KEY(id_categoria) REFERENCES categorias(id),
CONSTRAINT fk_producto_marca FOREIGN KEY(id_marca) REFERENCES marcas(id),
CONSTRAINT fk_producto_unidad FOREIGN KEY(id_unidad) REFERENCES unidades_medida(id),
CONSTRAINT fk_producto_proveedor FOREIGN KEY(id_proveedor) REFERENCES proveedores(id),
CONSTRAINT ck_producto_tipo CHECK(tipo IN ('PRODUCTO','SERVICIO','INSUMO')),
CONSTRAINT ck_producto_valores CHECK(precio>=0 AND stock>=0 AND stock_minimo>=0),
CONSTRAINT ck_servicio_stock CHECK(tipo<>'SERVICIO' OR (stock=0 AND stock_minimo=0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
START TRANSACTION;
INSERT INTO categorias(nombre,usuario_registro) SELECT 'Audio','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM categorias WHERE nombre='Audio');
INSERT INTO categorias(nombre,usuario_registro) SELECT 'Accesorios','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM categorias WHERE nombre='Accesorios');
INSERT INTO categorias(nombre,usuario_registro) SELECT 'Servicios','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM categorias WHERE nombre='Servicios');
INSERT INTO unidades_medida(nombre,usuario_registro) SELECT 'Unidad','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM unidades_medida WHERE nombre='Unidad');
INSERT INTO unidades_medida(nombre,usuario_registro) SELECT 'Servicio','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM unidades_medida WHERE nombre='Servicio');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_CLIENTES','Administrar clientes','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_CLIENTES');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_PROVEEDORES','Administrar proveedores','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_PROVEEDORES');
INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_PRODUCTOS','Administrar productos, servicios y catálogos','MIGRACION_ETAPA5' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_PRODUCTOS');
INSERT INTO roles_permisos(id_rol,id_permiso,usuario_registro)
SELECT r.id_rol,p.id_permiso,'MIGRACION_ETAPA5' FROM roles r CROSS JOIN permisos p
WHERE r.nombre='ADMINISTRADOR' AND p.nombre IN ('GESTIONAR_CLIENTES','GESTIONAR_PROVEEDORES','GESTIONAR_PRODUCTOS')
AND NOT EXISTS(SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=r.id_rol AND rp.id_permiso=p.id_permiso);
COMMIT;
SELECT 'Etapa 5 preparada. Reiniciar/publicar la aplicación después de verificar las tablas.' AS resultado;
