package com.mycompany.novatech.app.comercial;
import java.math.BigDecimal;
import java.util.Arrays;

/** Entidad inmutable. Builder reúne identidad, clasificación y valores comerciales. */
public final class Producto {
    public final int id, categoriaId, marcaId, unidadId, proveedorId;
    public final String codigo,nombre,descripcion,tipo;
    public final BigDecimal precio,stock,stockMinimo;
    public final boolean activo;
    private Producto(Builder b) {
        id=b.id;categoriaId=b.categoria;marcaId=b.marca;unidadId=b.unidad;proveedorId=b.proveedor;
        codigo=Validacion.texto(b.codigo,"Código",40,true).toUpperCase(java.util.Locale.ROOT);
        nombre=Validacion.texto(b.nombre,"Nombre",150,true);
        descripcion=Validacion.texto(b.descripcion,"Descripción",500,false);tipo=b.tipo;
        if(id<0||categoriaId<=0||unidadId<=0||marcaId<0||proveedorId<0)throw new IllegalArgumentException("Selecciona categoría y unidad válidas.");
        if(!codigo.matches("[A-Z0-9_-]{2,40}"))throw new IllegalArgumentException("Código: usa de 2 a 40 letras, números, guiones o guion bajo.");
        if(!Arrays.asList("PRODUCTO","SERVICIO","INSUMO").contains(tipo))throw new IllegalArgumentException("Tipo de artículo inválido.");
        precio=Validacion.decimal(b.precio,"Precio",2);
        stock=Validacion.decimal(b.stock,"Stock",3);stockMinimo=Validacion.decimal(b.minimo,"Stock mínimo",3);
        if("SERVICIO".equals(tipo)&&(stock.signum()!=0||stockMinimo.signum()!=0))throw new IllegalArgumentException("Los servicios deben tener stock y stock mínimo en cero.");
        activo=b.activo;
    }
    public static final class Builder {
        private int id,categoria,marca,unidad,proveedor;
        private String codigo,nombre,descripcion="",tipo="PRODUCTO",precio="0",stock="0",minimo="0";
        private boolean activo=true;
        public Builder identidad(int id,String codigo,String nombre){this.id=id;this.codigo=codigo;this.nombre=nombre;return this;}
        public Builder clasificacion(String tipo,int categoria,int marca,int unidad,int proveedor){this.tipo=tipo;this.categoria=categoria;this.marca=marca;this.unidad=unidad;this.proveedor=proveedor;return this;}
        public Builder descripcion(String valor){descripcion=valor;return this;}
        public Builder valores(String precio,String stock,String minimo){this.precio=precio;this.stock=stock;this.minimo=minimo;return this;}
        public Builder activo(boolean valor){activo=valor;return this;}
        public Producto build(){return new Producto(this);}
    }
}
