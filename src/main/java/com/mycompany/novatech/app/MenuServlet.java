package com.mycompany.novatech.app;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
@WebServlet("/abrir")
public final class MenuServlet extends HttpServlet {
    private final MenuMediator menu=new MenuMediator();
    @Override protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException{
        menu.seleccionar(q.getParameter("opcion"),(Usuario)q.getAttribute("actual"),q,s);
    }
}
