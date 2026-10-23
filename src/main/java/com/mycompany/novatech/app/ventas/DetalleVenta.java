package com.mycompany.novatech.app.ventas;
import java.math.BigDecimal;
public final class DetalleVenta {
    public int productoId;
    public String codigo="",nombre="",tipo="";
    public BigDecimal cantidad,precio=BigDecimal.ZERO,subtotal=BigDecimal.ZERO;
    public DetalleVenta(int productoId,BigDecimal cantidad){this.productoId=productoId;this.cantidad=cantidad;}
}
