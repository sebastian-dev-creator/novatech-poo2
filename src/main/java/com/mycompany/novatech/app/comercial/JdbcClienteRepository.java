package com.mycompany.novatech.app.comercial;
public final class JdbcClienteRepository extends JdbcTerceroRepository<Cliente> implements ClienteRepository {
    public JdbcClienteRepository(){super(false);}
    @Override protected Cliente nuevo(){return new Cliente();}
}
