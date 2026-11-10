package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ServicioRecomendacionPinturaTest {

  private static final String BLANCO_ROTO = "ffffff";
  private static final String CELESTE = "5d79c0";
  private static final String SALVIA = "567c49";
  private static final String TERRACOTA = "b6213f";
  private static final String ARENA = "dbc861";
  private static final String CARBON = "47627d";

  private final ServicioRecomendacionPintura servicio = new ServicioRecomendacionPinturaImpl();

  @Test
  void ambienteNuloDevuelveRecomendacionVacia() {
    RecomendacionPintura recomendacion = servicio.recomendar(null, null);

    assertThat(recomendacion.getFormaPintura(), is(nullValue()));
    assertThat(recomendacion.getItems(), hasSize(0));
    assertThat(recomendacion.getPrecioTotal(), is(nullValue()));
  }

  @Test
  void paredOpuestaALaPuertaPintaLaParedContrariaAlMuroDeLaPuerta() {
    Ambiente ambiente = conPuerta(ambiente(3.0, 8.0, 2.7), Muro.IZQUIERDO);

    RecomendacionPintura recomendacion = servicio.recomendar(ambiente, null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE)
    );
    assertThat(colores.getTecho(), is("E5E5E5"));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedFondo(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is(SALVIA));
    assertThat(
      itemPorZona(recomendacion, "Pared opuesta a la puerta").getMetrosCuadrados(),
      is(21.6)
    );
  }

  @Test
  void paredOpuestaALaPuertaSinPuertaTomaLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(3.0, 8.0, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getParedFondo(), is(SALVIA));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is("E5E5E5"));
    assertThat(
      itemPorZona(recomendacion, "Pared opuesta a la puerta").getMetrosCuadrados(),
      is(8.1)
    );
  }

  @Test
  void todasLasParedesDeUnColorPintaLasTresParedes() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.5, 2.8, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE)
    );
    assertThat(colores.getTecho(), is("E5E5E5"));
    assertThat(colores.getParedIzquierda(), is(ARENA));
    assertThat(colores.getParedFondo(), is(ARENA));
    assertThat(colores.getParedDerecha(), is(ARENA));
    assertThat(recomendacion.getItems(), hasSize(2));
    assertThat(
      itemPorZona(recomendacion, "Pared izquierda + Pared de fondo + Pared derecha")
        .getMetrosCuadrados(),
      is(27.27)
    );
  }

  @Test
  void techoDeUnColorPintaSoloElTecho() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 3.0), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO)
    );
    assertThat(colores.getTecho(), is(CARBON));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedFondo(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is("E5E5E5"));
    assertThat(itemPorZona(recomendacion, "Techo").getMetrosCuadrados(), is(16.0));
  }

  @Test
  void paredesOpuestasPintaLasParedesLaterales() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(6.0, 3.0, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE)
    );
    assertThat(colores.getTecho(), is("E5E5E5"));
    assertThat(colores.getParedIzquierda(), is(CELESTE));
    assertThat(colores.getParedFondo(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is(CELESTE));
    assertThat(recomendacion.getItems(), hasSize(2));
    assertThat(
      itemPorZona(recomendacion, "Pared izquierda + Pared derecha").getMetrosCuadrados(),
      is(16.2)
    );
  }

  @Test
  void techoYParedOpuestaPintaElTechoYLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(3.0, 6.0, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedFondo(), is(BLANCO_ROTO));
    assertThat(colores.getParedDerecha(), is("E5E5E5"));
    assertThat(itemPorZona(recomendacion, "Techo + Pared de fondo").getMetrosCuadrados(), is(26.1));
  }

  @Test
  void paredesPorLaMitadCalculaLaMitadDelAreaDeCadaPared() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 3.3), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES)
    );
    assertThat(colores.getTecho(), is("E5E5E5"));
    assertThat(colores.getParedIzquierda(), is(ARENA));
    assertThat(colores.getParedFondo(), is(ARENA));
    assertThat(colores.getParedDerecha(), is(ARENA));
    assertThat(recomendacion.getItems(), hasSize(2));
    assertThat(
      itemPorZona(recomendacion, "Pared izquierda + Pared de fondo + Pared derecha")
        .getMetrosCuadrados(),
      is(19.8)
    );
  }

  @Test
  void todoDeUnColorPintaTodasLasSuperficies() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(2.5, 2.5, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is(BLANCO_ROTO));
    assertThat(colores.getParedDerecha(), is(BLANCO_ROTO));
    assertThat(recomendacion.getItems(), hasSize(1));
    assertThat(
      itemPorZona(recomendacion, "Techo + Pared izquierda + Pared de fondo + Pared derecha")
        .getMetrosCuadrados(),
      is(26.5)
    );
  }

  @Test
  void todoMenosUnaParedDestacaLaParedDeFondo() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7), null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(
      recomendacion.getFormaPintura(),
      is(FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA)
    );
    assertThat(colores.getTecho(), is(BLANCO_ROTO));
    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is(TERRACOTA));
    assertThat(colores.getParedDerecha(), is(BLANCO_ROTO));
    assertThat(recomendacion.getItems(), hasSize(2));
    assertThat(
      itemPorZona(recomendacion, "Techo + Pared izquierda + Pared derecha").getMetrosCuadrados(),
      is(37.6)
    );
    assertThat(itemPorZona(recomendacion, "Pared a destacar").getMetrosCuadrados(), is(10.8));
  }

  @Test
  void todoMenosUnaParedDestacaLaParedContrariaALaPuerta() {
    Ambiente ambiente = conPuerta(ambiente(4.0, 4.0, 2.7), Muro.IZQUIERDO);

    RecomendacionPintura recomendacion = servicio.recomendar(ambiente, null);
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getParedIzquierda(), is(BLANCO_ROTO));
    assertThat(colores.getParedFondo(), is("ffffff"));
    assertThat(colores.getParedDerecha(), is(TERRACOTA));
  }

  @Test
  void cadaItemTieneZonaColorYMetrosCuadrados() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7), null);

    for (RecomendacionPintura.ItemPintura item : recomendacion.getItems()) {
      assertThat(item.getZona(), is(notNullValue()));
      assertThat(item.getColorNombre(), is(notNullValue()));
      assertThat(item.getHex(), is(notNullValue()));
      assertThat(item.getMetrosCuadrados(), is(notNullValue()));
    }
  }

  @Test
  void laDescripcionSeTomaDeLaFormaPintura() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7), null);

    assertThat(
      recomendacion.getDescripcion(),
      is(recomendacion.getFormaPintura().getDescripcion())
    );
  }

  @Test
  void sinBaldesCargadosElPrecioTotalQuedaEnCero() {
    RecomendacionPintura recomendacion = servicio.recomendar(ambiente(4.0, 4.0, 2.7), null);

    assertThat(recomendacion.getPrecioTotal(), is(0.0));
  }

  @Test
  void resuelveBaldeCantidadYPrecioCuandoElCatalogoTieneCoincidenciaDeHex() {
    BaldePintura balde = new BaldePintura(
      "Blanco Roto",
      "Blanco roto",
      BLANCO_ROTO,
      null,
      10.0,
      5000.0,
      null
    );
    ServicioRecomendacionPintura servicioConBaldes = new ServicioRecomendacionPinturaImpl(
      null,
      new RepositorioBaldePinturaStub(balde)
    );

    // Ambiente pequeno (area < 9) -> PINTAR_TODO_DE_UN_COLOR, una sola zona agrupada.
    RecomendacionPintura recomendacion = servicioConBaldes.recomendar(
      ambiente(2.0, 2.0, 2.7),
      null
    );

    assertThat(recomendacion.getItems(), hasSize(1));
    RecomendacionPintura.ItemPintura item = recomendacion.getItems().get(0);
    assertThat(item.getHex(), is(BLANCO_ROTO));
    assertThat(item.getBaldePintura(), is(notNullValue()));
    assertThat(item.getBaldePintura().getNombre(), is("Blanco Roto"));
    // 20.2 m2 / 10 L = 2.02 -> 3 baldes -> $15000
    assertThat(item.getCantidadBaldes(), is(3));
    assertThat(item.getPrecioTotal(), is(15000.0));
    assertThat(recomendacion.getPrecioTotal(), is(15000.0));
  }

  @Test
  void losColoresSeOrdenanPorPrecioElMasBaratoPrimero() {
    // Estilo Retro: [Limonchello fadb49, El Palmar 567c49, Talampaya b6213f]
    BaldePintura barato = new BaldePintura(
      "Limon",
      "Limonchello",
      "fadb49",
      null,
      10.0,
      1000.0,
      null
    );
    BaldePintura medio = new BaldePintura("Palma", "El Palmar", "567c49", null, 10.0, 5000.0, null);
    BaldePintura caro = new BaldePintura("Tala", "Talampaya", "b6213f", null, 10.0, 9000.0, null);

    RepositorioBaldePinturaStub repo = new RepositorioBaldePinturaStub(null);
    repo.agregar(caro);
    repo.agregar(barato);
    repo.agregar(medio);

    ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
    ServicioRecomendacionPintura servicioConEstilo = new ServicioRecomendacionPinturaImpl(
      servicioRediseño,
      repo
    );

    // Ambiente pequeno (area < 9) -> PINTAR_TODO_DE_UN_COLOR, usa colores[0] para todo.
    RecomendacionPintura recomendacion = servicioConEstilo.recomendar(
      ambiente(2.0, 2.0, 2.7),
      Estilo.Retro
    );

    // El primer color (mas barato, fadb49 a $1000) debe usarse en todas las superficies.
    assertThat(recomendacion.getItems(), hasSize(1));
    assertThat(recomendacion.getItems().get(0).getHex(), is("fadb49"));
    assertThat(recomendacion.getItems().get(0).getBaldePintura().getNombre(), is("Limon"));
    assertThat(recomendacion.getItems().get(0).getColorNombre(), is("Limonchello"));
  }

  @Test
  void eligeLitroMasBaratoAutomaticamente() {
    // Ambiente pequeno (area < 9) -> PINTAR_TODO_DE_UN_COLOR, una sola zona de 20.2 m2.
    // Balde 10L a $5000, opciones 4, 8, 10:
    //   4L -> 6 baldes -> $30000
    //   8L -> 3 baldes -> $15000
    //   10L -> 3 baldes -> $15000 (empate, elige mas litros = 10)
    BaldePintura balde = new BaldePintura(
      "Blanco",
      "Blanco roto",
      BLANCO_ROTO,
      null,
      10.0,
      5000.0,
      "4,8,10"
    );
    ServicioRecomendacionPintura servicioConBaldes = new ServicioRecomendacionPinturaImpl(
      null,
      new RepositorioBaldePinturaStub(balde)
    );

    RecomendacionPintura recomendacion = servicioConBaldes.recomendar(
      ambiente(2.0, 2.0, 2.7),
      null
    );

    assertThat(recomendacion.getItems(), hasSize(1));
    RecomendacionPintura.ItemPintura item = recomendacion.getItems().get(0);
    assertThat(item.getLitrosSeleccionados(), is(10.0));
    assertThat(item.getCantidadBaldes(), is(3));
    assertThat(item.getPrecioTotal(), is(15000.0));
  }

  @Test
  void colorOverrideSeAplicaATodasLasSuperficiesPintadas() {
    ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
    ServicioRecomendacionPintura servicioConEstilo = new ServicioRecomendacionPinturaImpl(
      servicioRediseño,
      new RepositorioBaldePinturaStub(null)
    );

    RecomendacionPintura recomendacion = servicioConEstilo.recomendar(
      ambiente(2.5, 2.5, 2.7),
      Estilo.Escandinavo,
      1
    );
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getTecho(), is("5d79c0"));
    assertThat(colores.getParedIzquierda(), is("5d79c0"));
    assertThat(colores.getParedFondo(), is("5d79c0"));
    assertThat(colores.getParedDerecha(), is("5d79c0"));
  }

  @Test
  void colorOverrideFueraDeRangoUsaColorBaseDeLaForma() {
    ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
    ServicioRecomendacionPintura servicioConEstilo = new ServicioRecomendacionPinturaImpl(
      servicioRediseño,
      new RepositorioBaldePinturaStub(null)
    );

    RecomendacionPintura recomendacion = servicioConEstilo.recomendar(
      ambiente(2.5, 2.5, 2.7),
      Estilo.Escandinavo,
      99
    );
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getTecho(), is("ffffff"));
    assertThat(colores.getParedIzquierda(), is("ffffff"));
    assertThat(colores.getParedFondo(), is("ffffff"));
    assertThat(colores.getParedDerecha(), is("ffffff"));
  }

  @Test
  void superficiesNoPintadasUsanTonoNeutro() {
    ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
    ServicioRecomendacionPintura servicioConEstilo = new ServicioRecomendacionPinturaImpl(
      servicioRediseño,
      new RepositorioBaldePinturaStub(null)
    );

    RecomendacionPintura recomendacion = servicioConEstilo.recomendar(
      ambiente(4.0, 4.0, 3.0),
      Estilo.Japandi,
      null
    );
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getTecho(), is("7f532b"));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedFondo(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is("E5E5E5"));
    assertThat(recomendacion.getItems(), hasSize(2));
  }

  @Test
  void colorOverridePintaTechoYRestoQuedaNeutro() {
    ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
    ServicioRecomendacionPintura servicioConEstilo = new ServicioRecomendacionPinturaImpl(
      servicioRediseño,
      new RepositorioBaldePinturaStub(null)
    );

    RecomendacionPintura recomendacion = servicioConEstilo.recomendar(
      ambiente(4.0, 4.0, 3.0),
      Estilo.Japandi,
      2
    );
    ColorSuperficie colores = recomendacion.getColorSuperficie();

    assertThat(colores.getTecho(), is("567c49"));
    assertThat(colores.getParedIzquierda(), is("E5E5E5"));
    assertThat(colores.getParedFondo(), is("E5E5E5"));
    assertThat(colores.getParedDerecha(), is("E5E5E5"));
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

  private static class RepositorioBaldePinturaStub implements RepositorioBaldePintura {

    private final List<BaldePintura> baldes;

    RepositorioBaldePinturaStub(BaldePintura balde) {
      this.baldes = balde == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(balde));
    }

    @Override
    public List<BaldePintura> obtenerTodos() {
      return baldes;
    }

    @Override
    public BaldePintura buscarPorId(Long id) {
      return null;
    }

    @Override
    public BaldePintura buscarPorHex(String hex) {
      if (hex == null) {
        return null;
      }
      for (BaldePintura b : baldes) {
        if (b != null && hex.equals(b.getHex())) {
          return b;
        }
      }
      return null;
    }

    @Override
    public BaldePintura buscarPorColorNombre(String nombre) {
      return null;
    }

    public void agregar(BaldePintura balde) {
      if (balde != null) {
        baldes.add(balde);
      }
    }

    @Override
    public void guardar(BaldePintura baldePintura) {}

    @Override
    public void eliminarTodos() {}
  }
}
