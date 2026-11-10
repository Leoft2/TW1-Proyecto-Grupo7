package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;

public class ServicioPlano2DTransaccionesTest {

  @Test
  public void deberiaEvaluarElPerimetroSinAbrirConexiones() {
    //Given
    DataSourceContado datos = givenDatos();
    DataSourceTransactionManager transacciones = new DataSourceTransactionManager(datos);
    ServicioPlano2D servicio = givenServicioConTransacciones(transacciones);
    MuebleUbicado mueble = givenMueble();
    Ambiente ambiente = new Ambiente(5.0, 5.0);

    //When
    boolean dentro = true;
    for (int intento = 0; intento < 1000; intento++) {
      dentro = servicio.estaDentroDelPerimetro(mueble, ambiente);
    }

    //Then
    assertThat(dentro, is(equalTo(true)));
    assertThat(datos.conexiones, is(equalTo(0)));
  }

  @Test
  public void deberiaParticiparEnLaTransaccionExistenteSinCrearOtraConexion() {
    //Given
    DataSourceContado datos = givenDatos();
    DataSourceTransactionManager transacciones = new DataSourceTransactionManager(datos);
    ServicioPlano2D servicio = givenServicioConTransacciones(transacciones);
    MuebleUbicado mueble = givenMueble();
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    TransactionTemplate transaccion = new TransactionTemplate(transacciones);

    //When
    Boolean dentro = transaccion.execute(estado -> {
      for (int intento = 0; intento < 100; intento++) {
        servicio.estaDentroDelPerimetro(mueble, ambiente);
      }
      return servicio.estaDentroDelPerimetro(mueble, ambiente);
    });

    //Then
    assertThat(dentro, is(equalTo(true)));
    assertThat(datos.conexiones, is(equalTo(1)));
  }

  private DataSourceContado givenDatos() {
    DataSourceContado datos = new DataSourceContado();
    datos.setDriverClassName("org.hsqldb.jdbcDriver");
    datos.setUrl("jdbc:hsqldb:mem:orientacion_transacciones");
    datos.setUsername("sa");
    datos.setPassword("");
    return datos;
  }

  private ServicioPlano2D givenServicioConTransacciones(
    DataSourceTransactionManager transacciones
  ) {
    ServicioPlano2DImpl servicio = new ServicioPlano2DImpl(new ServicioZonaExclusionImpl());
    ProxyFactory proxy = new ProxyFactory(servicio);
    proxy.addAdvice(
      new TransactionInterceptor(transacciones, new AnnotationTransactionAttributeSource())
    );
    return (ServicioPlano2D) proxy.getProxy();
  }

  private MuebleUbicado givenMueble() {
    return new MuebleUbicado(new Mueble("Silla", 1.0, 1.0), 1.0, 1.0);
  }

  private static class DataSourceContado extends DriverManagerDataSource {

    private int conexiones;

    @Override
    public Connection getConnection() throws SQLException {
      conexiones++;
      return super.getConnection();
    }
  }
}
