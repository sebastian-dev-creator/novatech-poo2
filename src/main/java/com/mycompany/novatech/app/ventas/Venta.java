package com.mycompany.novatech.app.ventas;
import java.math.BigDecimal;
import java.util.*;

/** Cabecera y detalle de una operación; los importes los calcula el servidor. */
public final class Venta {
    public int id,clienteId,usuarioId,version;
    public String clave=UUID.randomUUID().toString(),tipo="BOLETA",estado="PENDIENTE",medioPago="",observacion="";
    public String clienteNombre="",clienteDocumento="",creador="",actualizador="",fecha="",fechaPago="",fechaAnulacion="";
    public BigDecimal porcentaje=BigDecimal.ZERO,subtotal=BigDecimal.ZERO,descuento=BigDecimal.ZERO,total=BigDecimal.ZERO;
    public final List<DetalleVenta> detalles=new ArrayList<>();
    public String numero(){return ("FACTURA".equals(tipo)?"F":"B")+String.format(java.util.Locale.ROOT,"-%08d",id);}
}
