package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.Obstaculo;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.TipoDeObstaculo;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Verifica que los obstáculos fijos se guarden y se recuperen junto con su ambiente. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class AmbienteObstaculosTest {

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  @Transactional
  @Rollback
  public void dadoQueUnAmbienteTieneUnObstaculo_cuandoLoGuardoYLoRecupero_entoncesConservaElObstaculo() {
    Session sesion = this.sessionFactory.getCurrentSession();
    Ambiente ambiente = givenUnAmbientePersistidoConUnRadiador(sesion);

    Ambiente recuperado = whenRecuperoElAmbiente(sesion, ambiente.getId());

    thenElAmbienteConservaElObstaculo(recuperado);
  }

  private TipoDeAmbiente givenUnTipoDeAmbiente(String nombre) {
    return new TipoDeAmbiente(nombre);
  }

  private Ambiente givenUnAmbiente(Double ancho, Double largo, TipoDeAmbiente tipo) {
    return new Ambiente(ancho, largo, tipo);
  }

  private Obstaculo givenUnRadiador(Muro muro, Double posicion, Double ancho, Double profundidad) {
    return new Obstaculo(TipoDeObstaculo.RADIADOR, muro, posicion, ancho, profundidad);
  }

  private Ambiente givenUnAmbientePersistidoConUnRadiador(Session sesion) {
    TipoDeAmbiente living = givenUnTipoDeAmbiente("Living");
    sesion.persist(living);
    Ambiente ambiente = givenUnAmbiente(4.0, 3.0, living);
    ambiente.agregarObstaculo(givenUnRadiador(Muro.DERECHO, 0.5, 0.8, 0.15));
    sesion.persist(ambiente);
    sesion.flush();
    sesion.clear();
    return ambiente;
  }

  private Ambiente whenRecuperoElAmbiente(Session sesion, Long id) {
    return sesion.get(Ambiente.class, id);
  }

  private void thenElAmbienteConservaElObstaculo(Ambiente recuperado) {
    assertThat(recuperado.getObstaculos(), hasSize(1));
    Obstaculo obstaculo = recuperado.getObstaculos().get(0);
    assertThat(obstaculo.getTipo(), is(TipoDeObstaculo.RADIADOR));
    assertThat(obstaculo.getMuro(), is(Muro.DERECHO));
    assertThat(
      Arrays.asList(obstaculo.getPosicion(), obstaculo.getAncho(), obstaculo.getProfundidad()),
      contains(0.5, 0.8, 0.15)
    );
  }
}
