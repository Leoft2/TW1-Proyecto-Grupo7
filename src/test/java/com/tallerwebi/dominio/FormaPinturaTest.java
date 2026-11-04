package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

public class FormaPinturaTest {

  @Test
  void todasLasFormasTienenEtiquetaYDescripcionNoVacias() {
    for (FormaPintura forma : FormaPintura.values()) {
      assertThat(forma.name() + " deberia tener etiqueta", forma.getEtiqueta(), is(notNullValue()));
      assertThat(
        forma.name() + " deberia tener etiqueta no vacia",
        forma.getEtiqueta().isBlank(),
        is(false)
      );
      assertThat(
        forma.name() + " deberia tener descripcion",
        forma.getDescripcion(),
        is(notNullValue())
      );
      assertThat(
        forma.name() + " deberia tener descripcion no vacia",
        forma.getDescripcion().isBlank(),
        is(false)
      );
    }
  }

  @Test
  void todasLasFormasTienenCuatroSuperficies() {
    for (FormaPintura forma : FormaPintura.values()) {
      assertThat(
        forma.name() + " deberia tener 4 superficies",
        forma.getSuperficies().length,
        is(4)
      );
    }
  }
}
