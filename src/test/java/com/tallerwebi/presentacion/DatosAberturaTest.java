package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.TipoAbertura;
import org.junit.jupiter.api.Test;

// testing del DTO DatosAbertura

public class DatosAberturaTest {

  @Test
  public void deberiaConsiderarIncompletaUnaAberturaSinElAncho() {
    // given
    DatosAbertura abertura = givenUnaAberturaSinElAncho();

    // when
    boolean estaIncompleta = whenSeEvaluaLaCompletitudDeLaAbertura(abertura);

    // then
    thenLaAberturaEstaIncompleta(estaIncompleta);
  }

  @Test
  public void deberiaConsiderarCompletaUnaAberturaConTodosSusDatos() {
    // given
    DatosAbertura abertura = givenUnaAberturaCompleta();

    // when
    boolean estaIncompleta = whenSeEvaluaLaCompletitudDeLaAbertura(abertura);

    // then
    thenLaAberturaEstaCompleta(estaIncompleta);
  }

  private DatosAbertura givenUnaAberturaSinElAncho() {
    DatosAbertura abertura = new DatosAbertura();
    abertura.setTipo(TipoAbertura.PUERTA);
    abertura.setMuro(Muro.SUPERIOR);
    abertura.setPosicion(1.0);
    return abertura;
  }

  private DatosAbertura givenUnaAberturaCompleta() {
    DatosAbertura abertura = new DatosAbertura();
    abertura.setTipo(TipoAbertura.PUERTA);
    abertura.setMuro(Muro.SUPERIOR);
    abertura.setPosicion(1.0);
    abertura.setAncho(0.9);
    return abertura;
  }

  private boolean whenSeEvaluaLaCompletitudDeLaAbertura(DatosAbertura abertura) {
    return abertura.estaIncompleta();
  }

  private void thenLaAberturaEstaIncompleta(boolean estaIncompleta) {
    assertThat(estaIncompleta, is(true));
  }

  private void thenLaAberturaEstaCompleta(boolean estaIncompleta) {
    assertThat(estaIncompleta, is(false));
  }
}
