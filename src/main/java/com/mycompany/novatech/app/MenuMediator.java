package com.mycompany.novatech.app;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
/** Mediator: la vista y el controlador solicitan opciones/selecciones a un único coordinador. */
public final class MenuMediator {
    private final List<ComandoMenu> opciones=Arrays.asList(
        new AbrirModuloCommand("inicio","Inicio","/menu",false),
        new AbrirModuloCommand("usuarios","Usuarios","/usuarios",true),
        new AbrirModuloCommand("roles","Roles y acceso","/roles",true),
        new AbrirModuloCommand("ubicaciones","Ubicaciones","/ubicaciones",true));
    public List<ComandoMenu> opcionesPara(Usuario usuario){
        List<ComandoMenu> visibles=new ArrayList<>();
        for(ComandoMenu opcion:opciones)if(opcion.disponiblePara(usuario))visibles.add(opcion);
        return visibles;
    }
    public void seleccionar(String clave,Usuario usuario,HttpServletRequest q,HttpServletResponse s)throws IOException{
        for(ComandoMenu opcion:opciones)if(opcion.getClave().equals(clave)){
            if(!opcion.disponiblePara(usuario)){s.sendError(403,"No tienes acceso a esta opción.");return;}
            opcion.ejecutar(q,s);return;
        }
        s.sendError(404,"La opción no existe.");
    }
}
