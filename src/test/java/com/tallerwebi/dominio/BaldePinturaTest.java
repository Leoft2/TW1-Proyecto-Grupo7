package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.Test;

public class BaldePinturaTest {

  @Test
  public void deberiaFormatearLasOpcionesDeLitrosOrdenadasYSinDecimales() {
    BaldePintura balde = new BaldePintura(
      "Pintura X",
      "Mar Egeo",
      "0191b4",
      "/producto/x",
      4.0,
      9000.0,
      "10,1,4,1"
    );

    assertThat(balde.getOpcionesLitrosFormateadas(), equalTo(List.of("1", "4", "10")));
  }

  @Test
  public void deberiaDevolverListaVaciaCuandoNoTieneOpcionesDeLitros() {
    BaldePintura balde = new BaldePintura(
      "Pintura X",
      "Mar Egeo",
      "0191b4",
      "/producto/x",
      4.0,
      9000.0,
      null
    );

    assertThat(balde.getOpcionesLitrosFormateadas(), is(empty()));
  }
}
