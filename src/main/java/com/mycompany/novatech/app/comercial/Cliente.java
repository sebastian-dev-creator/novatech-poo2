package com.mycompany.novatech.app.comercial;

/** Prototype: reutiliza datos de contacto para preparar otra ficha, sin copiar su identidad. */
public final class Cliente extends Tercero {
    public Cliente copiarParaNuevo() {
        Cliente copia=new Cliente();
        copia.direccion=direccion; copia.distritoId=distritoId; copia.telefono=telefono;
        // Una persona del mismo domicilio puede compartir teléfono, pero nunca PK o DNI.
        return copia;
    }
}
