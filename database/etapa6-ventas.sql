-- POO II: etapa 6. Aplicar después de etapa5-comercial.sql; respaldar antes.
USE novatech;
SET NAMES utf8mb4;
SET SESSION lock_wait_timeout=30;
CREATE TABLE IF NOT EXISTS ventas (
 id INT AUTO_INCREMENT PRIMARY KEY,
 clave_creacion CHAR(36) NOT NULL UNIQUE,
 id_cliente INT NOT NULL,
 id_usuario INT NOT NULL,
 cliente_nombre VARCHAR(170) NOT NULL,
 cliente_documento VARCHAR(11) NOT NULL,
 tipo_comprobante VARCHAR(7) NOT NULL,
 estado VARCHAR(9) NOT NULL DEFAULT 'PENDIENTE',
 medio_pago VARCHAR(13) NULL,
 subtotal DECIMAL(14,2) NOT NULL,
 descuento_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,
 descuento DECIMAL(14,2) NOT NULL,
 total DECIMAL(14,2) NOT NULL,
 observacion VARCHAR(500) NOT NULL DEFAULT '',
 version INT NOT NULL DEFAULT 1,
 fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 fecha_pago DATETIME NULL,
 fecha_anulacion DATETIME NULL,
 usuario_registro VARCHAR(50) NOT NULL,
 usuario_actualizacion VARCHAR(50) NOT NULL,
 CONSTRAINT fk_venta_cliente FOREIGN KEY(id_cliente) REFERENCES clientes(id),
 CONSTRAINT fk_venta_usuario FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario),
 CONSTRAINT ck_venta_tipo CHECK(tipo_comprobante IN ('BOLETA','FACTURA')),
 CONSTRAINT ck_venta_estado CHECK(estado IN ('PENDIENTE','PAGADA','ANULADA')),
 CONSTRAINT ck_venta_pago CHECK(medio_pago IS NULL OR medio_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA')),
 CONSTRAINT ck_venta_importes CHECK(subtotal>=0 AND descuento_porcentaje BETWEEN 0 AND 100 AND descuento>=0 AND descuento<=subtotal AND total=subtotal-descuento),
 CONSTRAINT ck_venta_fechas CHECK((estado='PENDIENTE' AND fecha_pago IS NULL AND medio_pago IS NULL AND fecha_anulacion IS NULL) OR (estado='PAGADA' AND fecha_pago IS NOT NULL AND medio_pago IS NOT NULL AND fecha_anulacion IS NULL) OR (estado='ANULADA' AND fecha_anulacion IS NOT NULL AND ((fecha_pago IS NULL AND medio_pago IS NULL) OR (fecha_pago IS NOT NULL AND medio_pago IS NOT NULL))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS detalle_venta (
 id INT AUTO_INCREMENT PRIMARY KEY,
 id_venta INT NOT NULL,
 id_producto INT NOT NULL,
 codigo VARCHAR(40) NOT NULL,
 nombre VARCHAR(150) NOT NULL,
 tipo VARCHAR(10) NOT NULL,
 cantidad DECIMAL(12,3) NOT NULL,
 precio_unitario DECIMAL(11,2) NOT NULL,
 subtotal DECIMAL(14,2) NOT NULL,
 CONSTRAINT uq_detalle_producto UNIQUE(id_venta,id_producto),
 CONSTRAINT fk_detalle_venta FOREIGN KEY(id_venta) REFERENCES ventas(id) ON DELETE CASCADE,
 CONSTRAINT fk_detalle_producto FOREIGN KEY(id_producto) REFERENCES productos(id),
 CONSTRAINT ck_detalle_valores CHECK(cantidad>0 AND precio_unitario>=0 AND subtotal>=0),
 CONSTRAINT ck_detalle_tipo CHECK(tipo IN ('PRODUCTO','SERVICIO','INSUMO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
START TRANSACTION;
INSERT INTO permisos(nombre,descripcion,usuario_registro)
SELECT 'GESTIONAR_VENTAS','Registrar y gestionar ventas','MIGRACION_ETAPA6'
WHERE NOT EXISTS(SELECT 1 FROM permisos WHERE nombre='GESTIONAR_VENTAS');
INSERT INTO roles_permisos(id_rol,id_permiso,usuario_registro)
SELECT r.id_rol,p.id_permiso,'MIGRACION_ETAPA6' FROM roles r CROSS JOIN permisos p
WHERE r.nombre='ADMINISTRADOR' AND p.nombre='GESTIONAR_VENTAS'
AND NOT EXISTS(SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=r.id_rol AND rp.id_permiso=p.id_permiso);
COMMIT;
