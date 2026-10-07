package com.mycompany.novatech.app;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
/** Command: una opción encapsula la acción de abrir un módulo. */
public interface ComandoMenu {
    String getClave();
    String getTitulo();
    String getRuta();
    boolean disponiblePara(Usuario usuario);
    void ejecutar(HttpServletRequest request,HttpServletResponse response) throws IOException;
}
