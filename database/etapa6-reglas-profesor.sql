-- Actualiza también instalaciones locales previas sin borrar ventas. Reejecutable.
USE novatech;
SET SESSION lock_wait_timeout=30;
ALTER TABLE ventas DROP CHECK ck_venta_fechas, ADD CONSTRAINT ck_venta_fechas CHECK((estado='PENDIENTE' AND fecha_pago IS NULL AND medio_pago IS NULL AND fecha_anulacion IS NULL) OR (estado='PAGADA' AND fecha_pago IS NOT NULL AND medio_pago IS NOT NULL AND fecha_anulacion IS NULL) OR (estado='ANULADA' AND fecha_anulacion IS NOT NULL AND ((fecha_pago IS NULL AND medio_pago IS NULL) OR (fecha_pago IS NOT NULL AND medio_pago IS NOT NULL))));
