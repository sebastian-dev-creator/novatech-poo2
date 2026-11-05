-- Migración aditiva para instalaciones con etapa6-ventas y etapa6-reglas-profesor.
USE novatech;
CREATE TABLE IF NOT EXISTS solped (
 id INT AUTO_INCREMENT PRIMARY KEY,
 clave CHAR(36) NOT NULL UNIQUE,
 id_cliente INT NOT NULL,
 id_usuario INT NOT NULL,
 id_venta INT NULL UNIQUE,
 tipo VARCHAR(7) NOT NULL,
 porcentaje DECIMAL(5,2) NOT NULL,
 observacion VARCHAR(500) NOT NULL,
 version INT NOT NULL DEFAULT 1,
 fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(id_cliente) REFERENCES clientes(id),
 FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario),
 FOREIGN KEY(id_venta) REFERENCES ventas(id),
 CHECK(tipo IN ('BOLETA','FACTURA')), CHECK(porcentaje BETWEEN 0 AND 100)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS solped_detalle (
 id_solped INT NOT NULL,
 id_producto INT NOT NULL,
 cantidad DECIMAL(12,3) NOT NULL,
 PRIMARY KEY(id_solped,id_producto),
 FOREIGN KEY(id_solped) REFERENCES solped(id) ON DELETE CASCADE,
 FOREIGN KEY(id_producto) REFERENCES productos(id),
 CHECK(cantidad>0)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS guias (
 id INT AUTO_INCREMENT PRIMARY KEY,
 id_venta INT NOT NULL UNIQUE,
 fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado VARCHAR(8) NOT NULL DEFAULT 'EMITIDA',
 FOREIGN KEY(id_venta) REFERENCES ventas(id),
 CHECK(estado IN ('EMITIDA','ANULADA'))
) ENGINE=InnoDB;
