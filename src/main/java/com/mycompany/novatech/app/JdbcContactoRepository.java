package com.mycompany.novatech.app;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

public final class JdbcContactoRepository implements ContactoRepository {
    /** Solo identificadores constantes; nunca se incorpora un nombre SQL recibido del formulario. */
    private String[] campos(String clase){
        switch(clase){
            case "correo":return new String[]{"correos_electronicos","id_correo","correo","tipo_correo"};
            case "telefono":return new String[]{"telefonos","id_telefono","numero","tipo_telefono"};
            case "direccion":return new String[]{"direcciones","id_direccion","calle_avenida","tipo_direccion"};
            default:throw new IllegalArgumentException("Tipo de contacto inválido.");
        }
    }
    private void validar(Contacto x,String clase){
        campos(clase);
        int max="correo".equals(clase)?80:"telefono".equals(clase)?20:150;
        if(x.usuarioId<=0||x.id<0||x.valor.isBlank()||x.valor.length()>max)throw new IllegalArgumentException("Completa el contacto respetando su longitud máxima.");
        if(x.tipo.isBlank()||x.tipo.length()>20)throw new IllegalArgumentException("El tipo es obligatorio; máximo 20 caracteres.");
        if("correo".equals(clase)&&!x.valor.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))throw new IllegalArgumentException("Introduce un correo válido.");
        if("telefono".equals(clase)&&(!x.valor.matches("[+0-9() .-]{5,20}")||!x.valor.matches(".*[0-9].*")))throw new IllegalArgumentException("Revisa el número de teléfono.");
        if("direccion".equals(clase)&&(x.distritoId<=0||x.piso.length()>10||x.numero.length()>10||x.referencia.length()>255))throw new IllegalArgumentException("Selecciona un distrito y revisa los datos de dirección.");
    }
    public List<Contacto> listar(int usuario,String clase)throws SQLException{
        String[] f=campos(clase);
        String extra="direccion".equals(clase)?",c.id_distrito,c.piso,c.numero,c.referencia": "";
        String sql="SELECT c."+f[1]+" AS contacto_id,c."+f[2]+" AS valor,c."+f[3]+" AS tipo,c.activo"+extra+" FROM "+f[0]+" c JOIN datos_personales d ON d.id_datos_personales=c.id_datos_personales WHERE d.id_usuario=? ORDER BY c."+f[1];
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,usuario);try(ResultSet r=p.executeQuery()){
                List<Contacto> lista=new ArrayList<>();while(r.next()){
                    Contacto x=new Contacto();x.usuarioId=usuario;x.id=r.getInt("contacto_id");x.valor=r.getString("valor");x.tipo=r.getString("tipo");x.activo=r.getBoolean("activo");
                    if("direccion".equals(clase)){x.distritoId=r.getInt("id_distrito");x.piso=r.getString("piso");x.numero=r.getString("numero");x.referencia=r.getString("referencia");}
                    lista.add(x);
                }return lista;
            }
        }
    }
    public void guardar(Contacto x,String clase,String actor)throws SQLException{
        validar(x,clase);String[] f=campos(clase);boolean direccion="direccion".equals(clase);
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()){
            c.setAutoCommit(false);
            try{
                int persona;
                try(PreparedStatement p=c.prepareStatement("SELECT id_datos_personales FROM datos_personales WHERE id_usuario=? FOR UPDATE")){
                    p.setInt(1,x.usuarioId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("El usuario no tiene datos personales. Guárdalos primero.");persona=r.getInt(1);}
                }
                if(direccion){
                    try(PreparedStatement p=c.prepareStatement("SELECT d.id_distrito FROM distritos d JOIN provincias p ON p.id_provincia=d.id_provincia JOIN departamentos de ON de.id_departamento=p.id_departamento JOIN paises pa ON pa.id_pais=de.id_pais WHERE d.id_distrito=? AND d.activo=1 AND p.activo=1 AND de.activo=1 AND pa.activo=1")){
                        p.setInt(1,x.distritoId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Selecciona un distrito activo.");}
                    }
                }
                String sql=x.id==0?
                    "INSERT INTO "+f[0]+" ("+f[2]+","+f[3]+",activo"+(direccion?",id_distrito,piso,numero,referencia":"")+",id_datos_personales,usuario_registro) VALUES (?,?,?"+(direccion?",?,?,?,?":"")+",?,?)":
                    "UPDATE "+f[0]+" SET "+f[2]+"=?,"+f[3]+"=?,activo=?"+(direccion?",id_distrito=?,piso=?,numero=?,referencia=?":"")+" WHERE id_datos_personales=? AND "+f[1]+"=?";
                try(PreparedStatement p=c.prepareStatement(sql)){
                    int i=1;p.setString(i++,x.valor);p.setString(i++,x.tipo);p.setBoolean(i++,x.activo);
                    if(direccion){p.setInt(i++,x.distritoId);p.setString(i++,x.piso);p.setString(i++,x.numero);p.setString(i++,x.referencia);}
                    p.setInt(i++,persona);if(x.id==0)p.setString(i,actor);else p.setInt(i,x.id);
                    if(p.executeUpdate()==0)throw new IllegalArgumentException("El contacto no pertenece al usuario o ya no existe.");
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void eliminar(int usuario,int id,String clase)throws SQLException{
        String[] f=campos(clase);
        String sql="DELETE c FROM "+f[0]+" c JOIN datos_personales d ON d.id_datos_personales=c.id_datos_personales WHERE c."+f[1]+"=? AND d.id_usuario=?";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,id);p.setInt(2,usuario);if(p.executeUpdate()==0)throw new IllegalArgumentException("El contacto no pertenece al usuario o ya no existe.");
        }
    }
}
