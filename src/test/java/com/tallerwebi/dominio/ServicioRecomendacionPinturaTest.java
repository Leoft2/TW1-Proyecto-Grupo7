package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioRecomendacionPinturaTest {

  private static final String BLANCO_ROTO = "#F2F0EB";
  private static final String CELESTE = "#A8C3D1";
  private static final String SALVIA = "#9CAF94";
  private static final String TERRACOTA = "#C1663F";
  private static final String ARENA = "#D9CBB8";
  private static final String CARBON = "#2B2B2B";

  private final ServicioRecomendacionPintura servicio = new ServicioRecomendacionPinturaImpl();

  @Test
  void ambienteNuloDevuelveRecomendacionVacia() {
    RecomendacionPintura recomendacion = servicio.recomendar(null);

    assertThat(recomendacion.getFormaPintura(), is(nullValue()));
    assertThat(recomendacion.getItems(), hasSize(0));
    assertThat(recomendacion.getPrecioTotal(), is(nullValue()));
  }

  @Test
  void paredOpuestaALaPuertaPintaLaParedContrariaAlMuroDeLaPuerta() {
    Ambiente ambiente = conPuerta(ambiente(3.0, 8.0, 2.7), Muro.IZQUIERDO);

    RecomendacionPintura recomendacion = servicio.recomendar(ambiente);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE)
    );
    assertThat(colores.getTecho(), is(nullValue()));
    assertThat(colores.getParedIzquierda(), is(nullValue()));
    assertThat(colores.getParedFondo(), is(nullValue()));
    assertThat(colores.getParedDerecha(), is(SALVIA));
    assertThat(
      itemPorZona(recomendacion, "Pared opuesta a la puerta").getMetrosCuadrados(),
      is(21.6)
    );
  }

  @Test
  void paredOpuestaALaPuertaSinPuertaTomaLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(3.0, 8.0, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getParedFondo(), is(SALVIA));
    assertThat(colores.getParedIzquierda(), is(nullValue()));
    assertThat(colores.getParedDerecha(), is(nullValue()));
    assertThat(
      itemPorZona(recomendacion, "Pared opuesta a la puerta").getMetrosCuadrados(),
      is(8.1)
    );
  }

  @Test
  void todasLasParedesDeUnColorPintaLasTresParedes() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.5, 2.8, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE)
    );
    assertThat(colores.getTecho(), is(nullValue()));
    assertThat(colores.getParedIzquierda(), is(ARENA));
    assertThat(colores.getParedFondo(), is(ARENA));
    assertThat(colores.getParedDerecha(), is(ARENA));
    assertThat(recomendacion.getItems(), hasSize(3));
  }

  @Test
  void techoDeUnColorPintaSoloElTecho() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 3.0));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO)
    );
    assertThat(colores.getTecho(), is(CARBON));
    assertThat(colores.getParedIzquierda(), is(nullValue()));
    assertThat(colores.getParedFondo(), is(nullValue()));
    assertThat(colores.getParedDerecha(), is(nullValue()));
    assertThat(itemPorZona(recomendacion, "Techo").getMetrosCuadrados(), is(16.0));
  }

  @Test
  void paredesOpuestasPintaLasParedesLaterales() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(6.0, 3.0, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE)
    );
    assertThat(colores.getTecho(), is(nullValue()));
    assertThat(colores.getParedIzquierda(), is(CELESTE));
    assertThat(colores.getParedFondo(), is(nullValue()));
    assertThat(colores.getParedDerecha(), is(CELESTE));
    assertThat(recomendacion.getItems(), hasSize(2));
  }

  @Test
  void techoYParedOpuestaPintaElTechoYLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(3.0, 6.0, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is(nullValue()));
    assertThat(colores.getParedFondo(), is(SALVIA));
    assertThat(colores.getParedDerecha(), is(nullValue()));
    assertThat(itemPorZona(recomendacion, "Techo").getMetrosCuadrados(), is(18.0));
    assertThat(itemPorZona(recomendacion, "Pared de fondo").getMetrosCuadrados(), is(8.1));
  }

  @Test
  void paredesPorLaMitadCalculaLaMitadDelAreaDeCadaPared() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 3.3));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES)
    );
    assertThat(colores.getTecho(), is(nullValue()));
    assertThat(colores.getParedIzquierda(), is(ARENA));
    assertThat(colores.getParedFondo(), is(ARENA));
    assertThat(colores.getParedDerecha(), is(ARENA));
    assertThat(itemPorZona(recomendacion, "Pared izquierda").getMetrosCuadrados(), is(6.6));
    assertThat(itemPorZona(recomendacion, "Pared de fondo").getMetrosCuadrados(), is(6.6));
    assertThat(itemPorZona(recomendacion, "Pared derecha").getMetrosCuadrados(), is(6.6));
  }

  @Test
  void todoDeUnColorPintaTodasLasSuperficies() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(2.5, 2.5, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is(BLANCO_ROTO));
    assertThat(colores.getParedDerecha(), is(BLANCO_ROTO));
    assertThat(itemPorZona(recomendacion, "Todo el ambiente").getMetrosCuadrados(), is(33.25));
  }

  @Test
  void todoMenosUnaParedDestacaLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7));
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is(TERRACOTA));
    assertThat(colores.getParedDerecha(), is(BLANCO_ROTO));
    assertThat(itemPorZona(recomendacion, "Resto del ambiente").getMetrosCuadrados(), is(48.4));
    assertThat(itemPorZona(recomendacion, "Pared a destacar").getMetrosCuadrados(), is(10.8));
  }

  @Test
  void todoMenosUnaParedDestacaLaParedContrariaALaPuerta() {
    Ambiente ambiente = conPuerta(ambiente(4.0, 4.0, 2.7), Muro.IZQUIERDO);

    RecomendacionPintura recomendacion = servicio.recomendar(ambiente);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is(BLANCO_ROTO));
    assertThat(colores.getParedDerecha(), is(TERRACOTA));
  }

  @Test
  void cadaItemTieneZonaColorYMetrosCuadrados() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7));

    for (RecomendacionPintura.ItemPintura item : recomendacion.getItems()) {
      assertThat(item.getZona(), is(notNullValue()));
      assertThat(item.getColorNombre(), is(notNullValue()));
      assertThat(item.getHex(), is(notNullValue()));
      assertThat(item.getMetrosCuadrados(), is(notNullValue()));
    }
  }

  @Test
  void laDescripcionSeTomaDeLaFormaPintura() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7));

    assertThat(
      recomendacion.getDescripcion(),
      is(recomendacion.getFormaPintura().getDescripcion())
    );
  }

  @Test
  void sinBaldesCargadosElPrecioTotalQuedaEnNull() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7));

    assertThat(recomendacion.getPrecioTotal(), is(0.0));
  }

  private RecomendacionPintura.ItemPintura itemPorZona(
    RecomendacionPintura recomendacion,
    String zona
  ) {
    List<RecomendacionPintura.ItemPintura> items = recomendacion.getItems();
    for (RecomendacionPintura.ItemPintura item : items) {
      if (zona.equals(item.getZona())) {
        return item;
      }
    }
    throw new AssertionError("No se encontro un item para la zona " + zona);
  }

  private Ambiente ambiente(Double ancho, Double largo, Double altura) {
    return new Ambiente(ancho, largo, altura);
  }

  private Ambiente conPuerta(Ambiente ambiente, Muro muro) {
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, muro, 0.0, 0.9, 0.0));
    return ambiente;
  }
}
