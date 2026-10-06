package com.mycompany.novatech.app;
import com.mycompany.novatech.config.ConexionBD;
import com.mycompany.novatech.seguridad.PasswordUtil;
import java.sql.*;
import java.util.Arrays;
import javax.swing.*;

/** Utilidad LOCAL de instalación. Ejecutar archivo (Shift+F6); no es una ruta web. */
public final class CrearAdministrador {
    public static void main(String[] args) {
        JTextField nombre=new JTextField("admin");JPasswordField password=new JPasswordField();
        JPasswordField repetir=new JPasswordField();
        Object[] campos={"Usuario administrador:",nombre,"Contraseña nueva (12 a 128 caracteres):",password,"Repite la contraseña:",repetir};
        if(JOptionPane.showConfirmDialog(null,campos,"Primer administrador NovaTech",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        char[] a=password.getPassword(),b=repetir.getPassword();
        try {
            if(!Arrays.equals(a,b))throw new IllegalArgumentException("Las contraseñas no coinciden.");
            String login=nombre.getText().trim();
            if(!login.matches("[A-Za-z0-9._-]{3,50}")||a.length<12||a.length>128)throw new IllegalArgumentException("Revisa el usuario (3 a 50 caracteres) y la longitud de contraseña.");
            String hash=PasswordUtil.generarHash(new String(a));
            try(Connection c=ConexionBD.obtenerConexion()) {
                c.setAutoCommit(false);
                try {
                    int rol;
                    try(PreparedStatement p=c.prepareStatement("SELECT id_rol FROM roles WHERE nombre='ADMINISTRADOR' AND activo=1 FOR UPDATE");ResultSet r=p.executeQuery()) {if(!r.next())throw new SQLException("Falta el rol ADMINISTRADOR.");rol=r.getInt(1);}
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM usuarios u JOIN roles r ON u.id_rol=r.id_rol WHERE r.nombre='ADMINISTRADOR'");ResultSet r=p.executeQuery()) {r.next();if(r.getInt(1)>0)throw new IllegalArgumentException("Ya existe un administrador. Usa el login; esta utilidad no modifica cuentas existentes.");}
                    int id;
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO usuarios(nombre_usuario,password_hash,id_rol) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)) {p.setString(1,login);p.setString(2,hash);p.setInt(3,rol);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();id=r.getInt(1);}}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO datos_personales(id_usuario,nombres,apellidos) VALUES(?,'Administrador','NovaTech')")){p.setInt(1,id);p.executeUpdate();}
                    c.commit();
                }catch(Exception e){c.rollback();throw e;}
            }
            JOptionPane.showMessageDialog(null,"Administrador creado. Ahora ejecuta el proyecto e inicia sesión.");
        }catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"No se pudo crear",JOptionPane.ERROR_MESSAGE);}
        finally{Arrays.fill(a,'\0');Arrays.fill(b,'\0');password.setText("");repetir.setText("");}
    }
}
