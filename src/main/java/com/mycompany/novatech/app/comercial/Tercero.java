package com.mycompany.novatech.app.comercial;

/** Ficha comercial. La identidad personal se almacena en datos_personales. */
public class Tercero {
    public int id, personaId, distritoId;
    public String nombres="", apellidos="", documento="", razonSocial="", correo="", telefono="", direccion="";
    public String tipoDocumento="DNI";
    public boolean activo=true;
    public String nombreVisible() { return razonSocial.isEmpty() ? (nombres+" "+apellidos).trim() : razonSocial; }
    public void validar(boolean proveedor) {
        if(id<0 || distritoId<0) throw new IllegalArgumentException("Identificador inválido.");
        nombres=Validacion.texto(nombres,"Nombres del contacto",80,true);
        apellidos=Validacion.texto(apellidos,"Apellidos del contacto",80,true);
        if(!proveedor && !"DNI".equals(tipoDocumento) && !"RUC".equals(tipoDocumento))throw new IllegalArgumentException("Selecciona DNI o RUC.");
        boolean empresa=proveedor||"RUC".equals(tipoDocumento);
        documento=Validacion.texto(documento,empresa?"RUC":"DNI",empresa?11:8,true);
        if(!documento.matches(empresa?"[0-9]{11}":"[0-9]{8}"))
            throw new IllegalArgumentException(empresa?"El RUC debe tener 11 dígitos.":"El DNI debe tener 8 dígitos.");
        razonSocial=Validacion.texto(razonSocial,"Razón social",150,empresa);
        if(!empresa)razonSocial="";
        correo=Validacion.texto(correo,"Correo",254,false);
        telefono=Validacion.texto(telefono,"Teléfono",20,false);
        direccion=Validacion.texto(direccion,"Dirección",255,false);
        Validacion.contacto(correo,telefono);
        if(!direccion.isEmpty() && distritoId==0) throw new IllegalArgumentException("Selecciona el distrito de la dirección.");
    }
}
