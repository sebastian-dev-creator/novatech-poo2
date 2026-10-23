package com.mycompany.novatech.app.ventas;
import java.math.*;

/** Strategy: precio regular o descuento porcentual, sin cambiar el repositorio. */
public interface CalculoVenta {
    BigDecimal descuento(BigDecimal subtotal);
    static CalculoVenta para(BigDecimal porcentaje) {
        if(porcentaje==null || porcentaje.scale()>2 || porcentaje.signum()<0 || porcentaje.compareTo(new BigDecimal("100"))>0)
            throw new IllegalArgumentException("Descuento: indica de 0 a 100 con hasta dos decimales.");
        return porcentaje.signum()==0?new Regular():new Porcentaje(porcentaje);
    }
    final class Regular implements CalculoVenta {
        public BigDecimal descuento(BigDecimal subtotal){return BigDecimal.ZERO.setScale(2);}
    }
    final class Porcentaje implements CalculoVenta {
        private final BigDecimal porcentaje;
        public Porcentaje(BigDecimal porcentaje){this.porcentaje=porcentaje;}
        public BigDecimal descuento(BigDecimal subtotal){return subtotal.multiply(porcentaje).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);}
    }
}
