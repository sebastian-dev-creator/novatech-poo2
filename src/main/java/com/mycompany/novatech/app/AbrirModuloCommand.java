package com.mycompany.novatech.app;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
/** Receptor: la respuesta HTTP realiza la navegación solicitada por el comando. */
public final class AbrirModuloCommand implements ComandoMenu {
    private final String clave,titulo,ruta;
    private final boolean administrativo;
    public AbrirModuloCommand(String clave,String titulo,String ruta,boolean administrativo) {
        this.clave=clave;this.titulo=titulo;this.ruta=ruta;this.administrativo=administrativo;
    }
    public String getClave(){return clave;}
    public String getTitulo(){return titulo;}
    public String getRuta(){return ruta;}
    public boolean disponiblePara(Usuario usuario){return usuario!=null && (!administrativo||usuario.puede("roles".equals(clave)?"GESTIONAR_ROLES":"GESTIONAR_USUARIOS"));}
    public void ejecutar(HttpServletRequest q,HttpServletResponse s)throws IOException{s.sendRedirect(q.getContextPath()+ruta);}
}
