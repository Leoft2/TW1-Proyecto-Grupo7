package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class RecomendacionPinturaTest {

  @Test
  void calcularTotalSinPreciosDejaElTotalEn0() {
    RecomendacionPintura recomendacion = new RecomendacionPintura();
    recomendacion.agregarItem(unItem("Techo", 10.0, null));

    recomendacion.calcularTotal();

    assertThat(recomendacion.getPrecioTotal(), is(0.0));
  }

  @Test
  void calcularTotalSumaSoloLosPreciosExistentes() {
    RecomendacionPintura recomendacion = new RecomendacionPintura();
    recomendacion.agregarItem(unItem("Techo", 10.0, 100.0));
    recomendacion.agregarItem(unItem("Pared izquierda", 5.0, null));
    recomendacion.agregarItem(unItem("Pared derecha", 5.0, 50.0));

    recomendacion.calcularTotal();

    assertThat(recomendacion.getPrecioTotal(), is(150.0));
  }

  private RecomendacionPintura.ItemPintura unItem(
    String zona,
    Double metrosCuadrados,
    Double precioTotal
  ) {
    RecomendacionPintura.ItemPintura item = new RecomendacionPintura.ItemPintura();
    item.setZona(zona);
    item.setMetrosCuadrados(metrosCuadrados);
    item.setPrecioTotal(precioTotal);
    return item;
  }
}
