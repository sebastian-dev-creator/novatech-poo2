-- Etapa 7. Migración aditiva MySQL 8.4. Respaldar antes de aplicar.

USE novatech;

SET NAMES utf8mb4;

SET SESSION lock_wait_timeout=30;

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='usuarios' AND column_name='area')=0,'ALTER TABLE usuarios ADD COLUMN area VARCHAR(80) NULL','DO 0');

PREPARE etapa7 FROM @ddl;

EXECUTE etapa7;

DEALLOCATE PREPARE etapa7;

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='clientes' AND column_name='segmento')=0,'ALTER TABLE clientes ADD COLUMN segmento VARCHAR(80) NULL','DO 0');

PREPARE etapa7 FROM @ddl;

EXECUTE etapa7;

DEALLOCATE PREPARE etapa7;

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='proveedores' AND column_name='categoria')=0,'ALTER TABLE proveedores ADD COLUMN categoria VARCHAR(80) NULL','DO 0');

PREPARE etapa7 FROM @ddl;

EXECUTE etapa7;

DEALLOCATE PREPARE etapa7;

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='productos' AND column_name='costo')=0,'ALTER TABLE productos ADD COLUMN costo DECIMAL(11,2) NULL','DO 0');

PREPARE etapa7 FROM @ddl;

EXECUTE etapa7;

DEALLOCATE PREPARE etapa7;

SET @ddl=IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='detalle_venta' AND column_name='costo_unitario')=0,'ALTER TABLE detalle_venta ADD COLUMN costo_unitario DECIMAL(11,2) NULL','DO 0');

PREPARE etapa7 FROM @ddl;

EXECUTE etapa7;

DEALLOCATE PREPARE etapa7;

CREATE TABLE IF NOT EXISTS reporte_cobertura (id INT PRIMARY KEY, inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB;

INSERT IGNORE INTO reporte_cobertura(id) VALUES (1);

CREATE TABLE IF NOT EXISTS accesos (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 usuario VARCHAR(50) NOT NULL, resultado VARCHAR(12) NOT NULL,
 INDEX ix_acceso_fecha(fecha), INDEX ix_acceso_usuario(usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS auditoria (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 usuario VARCHAR(50) NOT NULL, modulo VARCHAR(30) NOT NULL, accion VARCHAR(20) NOT NULL,
 registro INT NOT NULL, detalle VARCHAR(500) NOT NULL, INDEX ix_auditoria_fecha(fecha)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movimientos_inventario (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 id_producto INT NOT NULL, codigo VARCHAR(40) NOT NULL, nombre VARCHAR(150) NOT NULL,
 cantidad DECIMAL(13,3) NOT NULL, saldo DECIMAL(12,3) NOT NULL,
 motivo VARCHAR(120) NOT NULL, usuario VARCHAR(50) NOT NULL,
 apertura INT NULL UNIQUE, INDEX ix_movimiento_fecha(fecha)
) ENGINE=InnoDB;

INSERT INTO movimientos_inventario(id_producto,codigo,nombre,cantidad,saldo,motivo,usuario,apertura)
SELECT id,codigo,nombre,stock,stock,'Saldo al iniciar etapa 7','Migración',id FROM productos p
WHERE tipo<>'SERVICIO' AND NOT EXISTS(SELECT 1 FROM movimientos_inventario m WHERE m.apertura=p.id);

CREATE TABLE IF NOT EXISTS compras (
 id INT AUTO_INCREMENT PRIMARY KEY, clave CHAR(36) NOT NULL UNIQUE,
 id_proveedor INT NOT NULL, proveedor_nombre VARCHAR(150) NOT NULL,
 fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, fecha_prevista DATE NOT NULL,
 fecha_recepcion DATETIME NULL, fecha_pago DATETIME NULL, fecha_anulacion DATETIME NULL,
 total DECIMAL(14,2) NOT NULL, estado VARCHAR(10) NOT NULL DEFAULT 'PENDIENTE',
 evaluacion INT NULL, observacion VARCHAR(500) NOT NULL DEFAULT '',
 usuario VARCHAR(50) NOT NULL, version INT NOT NULL DEFAULT 1,
 FOREIGN KEY(id_proveedor) REFERENCES proveedores(id),
 CHECK(total>=0), CHECK(estado IN ('PENDIENTE','RECIBIDA','ANULADA')),
 CHECK(evaluacion IS NULL OR evaluacion BETWEEN 1 AND 5),
 CHECK((estado='PENDIENTE' AND fecha_recepcion IS NULL AND evaluacion IS NULL AND fecha_anulacion IS NULL) OR
 (estado='RECIBIDA' AND fecha_recepcion IS NOT NULL AND evaluacion IS NOT NULL AND fecha_anulacion IS NULL) OR
 (estado='ANULADA' AND fecha_recepcion IS NULL AND fecha_pago IS NULL AND fecha_anulacion IS NOT NULL)),
 INDEX ix_compra_fecha(fecha)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_compra (
 id_compra INT NOT NULL, id_producto INT NOT NULL, nombre VARCHAR(150) NOT NULL,
 tipo VARCHAR(10) NOT NULL, cantidad DECIMAL(12,3) NOT NULL,
 costo DECIMAL(11,2) NOT NULL, subtotal DECIMAL(14,2) NOT NULL,
 PRIMARY KEY(id_compra,id_producto), FOREIGN KEY(id_compra) REFERENCES compras(id),
 FOREIGN KEY(id_producto) REFERENCES productos(id), CHECK(cantidad>0 AND costo>=0 AND subtotal>=0)
) ENGINE=InnoDB;

CREATE TRIGGER IF NOT EXISTS etapa7_usuarios_insert AFTER INSERT ON usuarios FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'usuarios','CREAR',NEW.id_usuario,LEFT(CONCAT('Usuario ',NEW.nombre_usuario,', rol ',NEW.id_rol,', activo ',NEW.activo,', área ',COALESCE(NEW.area,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_usuarios_update AFTER UPDATE ON usuarios FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'usuarios','ACTUALIZAR',NEW.id_usuario,LEFT(CONCAT('Antes: ',CONCAT('Usuario ',OLD.nombre_usuario,', rol ',OLD.id_rol,', activo ',OLD.activo,', área ',COALESCE(OLD.area,'Sin asignar')),' | Después: ',CONCAT('Usuario ',NEW.nombre_usuario,', rol ',NEW.id_rol,', activo ',NEW.activo,', área ',COALESCE(NEW.area,'Sin asignar'))),500) WHERE NOT(OLD.nombre_usuario<=>NEW.nombre_usuario AND OLD.id_rol<=>NEW.id_rol AND OLD.activo<=>NEW.activo AND OLD.bloqueado<=>NEW.bloqueado AND OLD.password_hash<=>NEW.password_hash AND OLD.area<=>NEW.area);

CREATE TRIGGER IF NOT EXISTS etapa7_usuarios_delete AFTER DELETE ON usuarios FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'usuarios','ELIMINAR',OLD.id_usuario,LEFT(CONCAT('Usuario ',OLD.nombre_usuario,', rol ',OLD.id_rol,', activo ',OLD.activo,', área ',COALESCE(OLD.area,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_clientes_insert AFTER INSERT ON clientes FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'clientes','CREAR',NEW.id,LEFT(CONCAT('Cliente ',NEW.id,', activo ',NEW.activo,', segmento ',COALESCE(NEW.segmento,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_clientes_update AFTER UPDATE ON clientes FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'clientes','ACTUALIZAR',NEW.id,LEFT(CONCAT('Antes: ',CONCAT('Cliente ',OLD.id,', activo ',OLD.activo,', segmento ',COALESCE(OLD.segmento,'Sin asignar')),' | Después: ',CONCAT('Cliente ',NEW.id,', activo ',NEW.activo,', segmento ',COALESCE(NEW.segmento,'Sin asignar'))),500);

CREATE TRIGGER IF NOT EXISTS etapa7_clientes_delete AFTER DELETE ON clientes FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'clientes','ELIMINAR',OLD.id,LEFT(CONCAT('Cliente ',OLD.id,', activo ',OLD.activo,', segmento ',COALESCE(OLD.segmento,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_proveedores_insert AFTER INSERT ON proveedores FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'proveedores','CREAR',NEW.id,LEFT(CONCAT(NEW.razon_social,', activo ',NEW.activo,', categoría ',COALESCE(NEW.categoria,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_proveedores_update AFTER UPDATE ON proveedores FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'proveedores','ACTUALIZAR',NEW.id,LEFT(CONCAT('Antes: ',CONCAT(OLD.razon_social,', activo ',OLD.activo,', categoría ',COALESCE(OLD.categoria,'Sin asignar')),' | Después: ',CONCAT(NEW.razon_social,', activo ',NEW.activo,', categoría ',COALESCE(NEW.categoria,'Sin asignar'))),500);

CREATE TRIGGER IF NOT EXISTS etapa7_proveedores_delete AFTER DELETE ON proveedores FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'proveedores','ELIMINAR',OLD.id,LEFT(CONCAT(OLD.razon_social,', activo ',OLD.activo,', categoría ',COALESCE(OLD.categoria,'Sin asignar')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_productos_insert AFTER INSERT ON productos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'productos','CREAR',NEW.id,LEFT(CONCAT(NEW.codigo,', precio ',NEW.precio,', stock ',NEW.stock,', costo ',COALESCE(NEW.costo,'Sin registrar'),', activo ',NEW.activo,', motivo ',COALESCE(@nt_motivo,'Actualización')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_productos_update AFTER UPDATE ON productos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'productos','ACTUALIZAR',NEW.id,LEFT(CONCAT('Antes: ',CONCAT(OLD.codigo,', precio ',OLD.precio,', stock ',OLD.stock,', costo ',COALESCE(OLD.costo,'Sin registrar'),', activo ',OLD.activo),' | Después: ',CONCAT(NEW.codigo,', precio ',NEW.precio,', stock ',NEW.stock,', costo ',COALESCE(NEW.costo,'Sin registrar'),', activo ',NEW.activo,', motivo ',COALESCE(@nt_motivo,'Actualización'))),500);

CREATE TRIGGER IF NOT EXISTS etapa7_productos_delete AFTER DELETE ON productos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'productos','ELIMINAR',OLD.id,LEFT(CONCAT(OLD.codigo,', precio ',OLD.precio,', stock ',OLD.stock,', costo ',COALESCE(OLD.costo,'Sin registrar'),', activo ',OLD.activo),500);

CREATE TRIGGER IF NOT EXISTS etapa7_ventas_insert AFTER INSERT ON ventas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'ventas','CREAR',NEW.id,LEFT(CONCAT('Venta ',NEW.id,', estado ',NEW.estado,', total ',NEW.total),500);

CREATE TRIGGER IF NOT EXISTS etapa7_ventas_update AFTER UPDATE ON ventas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'ventas','ACTUALIZAR',NEW.id,LEFT(CONCAT('Antes: ',CONCAT('Venta ',OLD.id,', estado ',OLD.estado,', total ',OLD.total),' | Después: ',CONCAT('Venta ',NEW.id,', estado ',NEW.estado,', total ',NEW.total)),500);

CREATE TRIGGER IF NOT EXISTS etapa7_ventas_delete AFTER DELETE ON ventas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'ventas','ELIMINAR',OLD.id,LEFT(CONCAT('Venta ',OLD.id,', estado ',OLD.estado,', total ',OLD.total),500);

CREATE TRIGGER IF NOT EXISTS etapa7_compras_insert AFTER INSERT ON compras FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'compras','CREAR',NEW.id,LEFT(CONCAT('Compra ',NEW.id,', estado ',NEW.estado,', pago ',IF(NEW.fecha_pago IS NULL,'Pendiente','Pagado')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_compras_update AFTER UPDATE ON compras FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'compras','ACTUALIZAR',NEW.id,LEFT(CONCAT('Antes: ',CONCAT('Compra ',OLD.id,', estado ',OLD.estado,', pago ',IF(OLD.fecha_pago IS NULL,'Pendiente','Pagado')),' | Después: ',CONCAT('Compra ',NEW.id,', estado ',NEW.estado,', pago ',IF(NEW.fecha_pago IS NULL,'Pendiente','Pagado'))),500);

CREATE TRIGGER IF NOT EXISTS etapa7_compras_delete AFTER DELETE ON compras FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) SELECT COALESCE(@nt_actor,'Sistema'),'compras','ELIMINAR',OLD.id,LEFT(CONCAT('Compra ',OLD.id,', estado ',OLD.estado,', pago ',IF(OLD.fecha_pago IS NULL,'Pendiente','Pagado')),500);

CREATE TRIGGER IF NOT EXISTS etapa7_stock_insert AFTER INSERT ON productos FOR EACH ROW
INSERT INTO movimientos_inventario(id_producto,codigo,nombre,cantidad,saldo,motivo,usuario)
SELECT NEW.id,NEW.codigo,NEW.nombre,NEW.stock,NEW.stock,'Stock inicial',COALESCE(@nt_actor,'Sistema') WHERE NEW.tipo<>'SERVICIO';

CREATE TRIGGER IF NOT EXISTS etapa7_stock_update AFTER UPDATE ON productos FOR EACH ROW
INSERT INTO movimientos_inventario(id_producto,codigo,nombre,cantidad,saldo,motivo,usuario)
SELECT NEW.id,NEW.codigo,NEW.nombre,NEW.stock-OLD.stock,NEW.stock,COALESCE(@nt_motivo,'Ajuste de inventario'),COALESCE(@nt_actor,'Sistema') WHERE NEW.stock<>OLD.stock;

START TRANSACTION;

INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'VER_REPORTES','Consultar reportes y exportar Excel','MIGRACION_ETAPA7' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='VER_REPORTES');

INSERT INTO permisos(nombre,descripcion,usuario_registro) SELECT 'GESTIONAR_COMPRAS','Registrar compras, recibir y pagar','MIGRACION_ETAPA7' WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_COMPRAS');

INSERT INTO roles_permisos(id_rol,id_permiso,usuario_registro)
SELECT r.id_rol,p.id_permiso,'MIGRACION_ETAPA7' FROM roles r CROSS JOIN permisos p
WHERE r.nombre='ADMINISTRADOR' AND p.nombre IN ('VER_REPORTES','GESTIONAR_COMPRAS')
AND NOT EXISTS(SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=r.id_rol AND rp.id_permiso=p.id_permiso);

COMMIT;

CREATE TRIGGER IF NOT EXISTS etapa7_roles_insert AFTER INSERT ON roles FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'roles','CREAR',NEW.id_rol,LEFT(CONCAT('roles: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_roles_update AFTER UPDATE ON roles FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'roles','ACTUALIZAR',NEW.id_rol,LEFT(CONCAT('roles: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_roles_delete AFTER DELETE ON roles FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'roles','ELIMINAR',OLD.id_rol,LEFT(CONCAT('roles: ',COALESCE(OLD.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_permisos_insert AFTER INSERT ON permisos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'permisos','CREAR',NEW.id_permiso,LEFT(CONCAT('permisos: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_permisos_update AFTER UPDATE ON permisos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'permisos','ACTUALIZAR',NEW.id_permiso,LEFT(CONCAT('permisos: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_permisos_delete AFTER DELETE ON permisos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'permisos','ELIMINAR',OLD.id_permiso,LEFT(CONCAT('permisos: ',COALESCE(OLD.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_datos_personales_insert AFTER INSERT ON datos_personales FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'datos_personales','CREAR',NEW.id_datos_personales,LEFT(CONCAT('datos_personales: ',COALESCE(NEW.nombres,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_datos_personales_update AFTER UPDATE ON datos_personales FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'datos_personales','ACTUALIZAR',NEW.id_datos_personales,LEFT(CONCAT('datos_personales: ',COALESCE(NEW.nombres,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_datos_personales_delete AFTER DELETE ON datos_personales FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'datos_personales','ELIMINAR',OLD.id_datos_personales,LEFT(CONCAT('datos_personales: ',COALESCE(OLD.nombres,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_correos_electronicos_insert AFTER INSERT ON correos_electronicos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'correos_electronicos','CREAR',NEW.id_correo,LEFT(CONCAT('correos_electronicos: ',COALESCE(NEW.tipo_correo,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_correos_electronicos_update AFTER UPDATE ON correos_electronicos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'correos_electronicos','ACTUALIZAR',NEW.id_correo,LEFT(CONCAT('correos_electronicos: ',COALESCE(NEW.tipo_correo,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_correos_electronicos_delete AFTER DELETE ON correos_electronicos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'correos_electronicos','ELIMINAR',OLD.id_correo,LEFT(CONCAT('correos_electronicos: ',COALESCE(OLD.tipo_correo,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_telefonos_insert AFTER INSERT ON telefonos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'telefonos','CREAR',NEW.id_telefono,LEFT(CONCAT('telefonos: ',COALESCE(NEW.tipo_telefono,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_telefonos_update AFTER UPDATE ON telefonos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'telefonos','ACTUALIZAR',NEW.id_telefono,LEFT(CONCAT('telefonos: ',COALESCE(NEW.tipo_telefono,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_telefonos_delete AFTER DELETE ON telefonos FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'telefonos','ELIMINAR',OLD.id_telefono,LEFT(CONCAT('telefonos: ',COALESCE(OLD.tipo_telefono,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_direcciones_insert AFTER INSERT ON direcciones FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'direcciones','CREAR',NEW.id_direccion,LEFT(CONCAT('direcciones: ',COALESCE(NEW.tipo_direccion,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_direcciones_update AFTER UPDATE ON direcciones FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'direcciones','ACTUALIZAR',NEW.id_direccion,LEFT(CONCAT('direcciones: ',COALESCE(NEW.tipo_direccion,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_direcciones_delete AFTER DELETE ON direcciones FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'direcciones','ELIMINAR',OLD.id_direccion,LEFT(CONCAT('direcciones: ',COALESCE(OLD.tipo_direccion,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_categorias_insert AFTER INSERT ON categorias FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'categorias','CREAR',NEW.id,LEFT(CONCAT('categorias: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_categorias_update AFTER UPDATE ON categorias FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'categorias','ACTUALIZAR',NEW.id,LEFT(CONCAT('categorias: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_categorias_delete AFTER DELETE ON categorias FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'categorias','ELIMINAR',OLD.id,LEFT(CONCAT('categorias: ',COALESCE(OLD.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_marcas_insert AFTER INSERT ON marcas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'marcas','CREAR',NEW.id,LEFT(CONCAT('marcas: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_marcas_update AFTER UPDATE ON marcas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'marcas','ACTUALIZAR',NEW.id,LEFT(CONCAT('marcas: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_marcas_delete AFTER DELETE ON marcas FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'marcas','ELIMINAR',OLD.id,LEFT(CONCAT('marcas: ',COALESCE(OLD.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_unidades_medida_insert AFTER INSERT ON unidades_medida FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'unidades_medida','CREAR',NEW.id,LEFT(CONCAT('unidades_medida: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_unidades_medida_update AFTER UPDATE ON unidades_medida FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'unidades_medida','ACTUALIZAR',NEW.id,LEFT(CONCAT('unidades_medida: ',COALESCE(NEW.nombre,'')),500));
CREATE TRIGGER IF NOT EXISTS etapa7_unidades_medida_delete AFTER DELETE ON unidades_medida FOR EACH ROW INSERT INTO auditoria(usuario,modulo,accion,registro,detalle) VALUES(COALESCE(@nt_actor,'Sistema'),'unidades_medida','ELIMINAR',OLD.id,LEFT(CONCAT('unidades_medida: ',COALESCE(OLD.nombre,'')),500));
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='productos' AND constraint_name='ck_etapa7_productos_costo')=0,'ALTER TABLE productos ADD CONSTRAINT ck_etapa7_productos_costo CHECK (costo IS NULL OR costo>=0)','DO 0');
PREPARE etapa7 FROM @ddl;
EXECUTE etapa7;
DEALLOCATE PREPARE etapa7;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name='detalle_venta' AND constraint_name='ck_etapa7_detalle_venta_costo')=0,'ALTER TABLE detalle_venta ADD CONSTRAINT ck_etapa7_detalle_venta_costo CHECK (costo_unitario IS NULL OR costo_unitario>=0)','DO 0');
PREPARE etapa7 FROM @ddl;
EXECUTE etapa7;
DEALLOCATE PREPARE etapa7;
