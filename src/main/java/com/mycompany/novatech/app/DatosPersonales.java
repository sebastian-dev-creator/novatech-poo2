package com.mycompany.novatech.app;

/** Datos opcionales: los registros anteriores pueden completarse progresivamente. */
public final class DatosPersonales {
    public final String dni;
    public final Integer sexoId, estadoCivilId;
    public DatosPersonales(String dni, Integer sexoId, Integer estadoCivilId) {
        this.dni=dni==null || dni.trim().isEmpty()?null:dni.trim();
        this.sexoId=sexoId; this.estadoCivilId=estadoCivilId;
    }
    public void validar() {
        if(dni!=null && !dni.matches("[0-9]{8}")) throw new IllegalArgumentException("El DNI debe tener 8 dígitos, o dejarse vacío si no se conoce.");
        if((sexoId!=null && sexoId<=0) || (estadoCivilId!=null && estadoCivilId<=0)) throw new IllegalArgumentException("Identificador de catálogo inválido.");
    }
}
