package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRediseñoTest {

  private final ServicioRediseño servicioRediseño = new ServicioRediseñoImpl();
  private static final String BLANCO_ROTO = "#F2F0EB";
  private static final String CELESTE = "#A8C3D1";
  private static final String SALVIA = "#9CAF94";
  private static final String TERRACOTA = "#C1663F";
  private ServicioRediseño servicio;

  @BeforeEach
  void setUp() {
    servicio = new ServicioRediseñoImpl();
  }

  private Ambiente ambiente(Double ancho, Double largo, Double altura) {
    Ambiente a = new Ambiente();
    a.setAncho(ancho);
    a.setLargo(largo);
    a.setAltura(altura);
    return a;
  }

  @Test
  public void dadoCadaEstilo_cuandoPidoUnaRecomendacion_enternoUnaPaletaYUnaIluminacion() {
    for (Estilo estilo : Estilo.values()) {
      Recomendacion recomendacion = this.servicioRediseño.recomendar(estilo);

      assertThat(
        estilo + ": la recomendacion deberia ser del mismo estilo",
        recomendacion.getEstilo(),
        is(estilo)
      );
      assertThat(
        estilo + ": deberia traer una paleta de 3 colores",
        recomendacion.getColores(),
        hasSize(3)
      );
      assertThat(
        estilo + ": deberia traer una iluminacion",
        recomendacion.getIluminacion(),
        is(notNullValue())
      );
    }
  }

  @Test
  public void dadoDosEstilosDistintos_cuandoPidoSusRecomendaciones_entornoPaletasDistintas() {
    Recomendacion retro = this.servicioRediseño.recomendar(Estilo.Retro);
    Recomendacion industrial = this.servicioRediseño.recomendar(Estilo.Industrial);

    assertThat(retro.getColores(), is(not(industrial.getColores())));
    assertThat(retro.getIluminacion(), is(Iluminacion.CALIDA));
    assertThat(industrial.getIluminacion(), is(Iluminacion.FRIA));
  }

  @Test
  public void dadoUnEstilo_cuandoPidoUnaRecomendacion_entornoTodosLosColoresConNombreYHexValidos() {
    Recomendacion japandi = this.servicioRediseño.recomendar(Estilo.Japandi);

    for (Color color : japandi.getColores()) {
      assertThat(color.getNombre(), is(notNullValue()));
      assertThat(color.getHex().matches("^#[0-9A-Fa-f]{6}$"), is(true));
    }
  }

  @Test
  public void dadoUnaRecomendacion_cuandoIntentoAgregarUnColor_entornoLaPaletaQuedaIntacta() {
    Recomendacion escandinavo = this.servicioRediseño.recomendar(Estilo.Escandinavo);

    assertThrows(
      UnsupportedOperationException.class,
      () -> escandinavo.getColores().add(new Color("Rosa", "#FF00FF"))
    );
    assertThat(escandinavo.getColores(), hasSize(3));
  }

  @Test
  public void dadoUnEstilo_cuandoPidoUnaRecomendacion_entornoLaIluminacionInformaNombreYTemperatura() {
    Recomendacion minimalista = this.servicioRediseño.recomendar(Estilo.Minimalista);
    Recomendacion japandi = this.servicioRediseño.recomendar(Estilo.Japandi);

    assertThat(minimalista.getIluminacion().getDescripcion(), is("Intermedia"));
    assertThat(minimalista.getIluminacion().getKelvin(), is(4000));
    assertThat(japandi.getIluminacion().getDescripcion(), is("Cálida"));
    assertThat(japandi.getIluminacion().getKelvin(), is(2700));
  }

  @Test
  public void dadoLosEstilosQueCompartenColores_cuandoPidoSusPaletas_entornoNoSonLaMismaLista() {
    List<Color> retro = this.servicioRediseño.recomendar(Estilo.Retro).getColores();
    List<Color> boho = this.servicioRediseño.recomendar(Estilo.Boho).getColores();

    assertThat(retro, is(not(boho)));
  }

  @Test
  void ambienteAnchoPintaParedesIzquierdaYDerecha() {
    RecomendacionPintura r = servicio.recomendarPintura(ambiente(6.0, 3.0, 2.7));
    ColorSuperficie c = r.getColorSuperficie();

    assertEquals(FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE, r.getFormaPintura());
    assertNull(c.getTecho());
    assertEquals(CELESTE, c.getParedIzquierda());
    assertNull(c.getParedFondo());
    assertEquals(CELESTE, c.getParedDerecha());
  }

  @Test
  void ambienteAlargadoPintaTechoYFondo() {
    RecomendacionPintura r = servicio.recomendarPintura(ambiente(3.0, 6.0, 2.7));
    ColorSuperficie c = r.getColorSuperficie();

    assertEquals(
      FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO,
      r.getFormaPintura()
    );
    assertEquals(BLANCO_ROTO, c.getTecho());
    assertNull(c.getParedIzquierda());
    assertEquals(SALVIA, c.getParedFondo());
    assertNull(c.getParedDerecha());
  }

  @Test
  void ambienteCuadradoDestacaElFondoYPintaElRestoDeBlancoRoto() {
    RecomendacionPintura r = servicio.recomendarPintura(ambiente(4.0, 4.0, 2.7));
    ColorSuperficie c = r.getColorSuperficie();

    assertEquals(FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA, r.getFormaPintura());
    assertEquals(BLANCO_ROTO, c.getTecho());
    assertEquals(BLANCO_ROTO, c.getParedIzquierda());
    assertEquals(TERRACOTA, c.getParedFondo());
    assertEquals(BLANCO_ROTO, c.getParedDerecha());
  }

  @Test
  void dimensionesInvalidasPintanTodoDeUnColor() {
    RecomendacionPintura r = servicio.recomendarPintura(ambiente(0.0, 0.0, 2.7));
    ColorSuperficie c = r.getColorSuperficie();

    assertEquals(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR, r.getFormaPintura());
    assertEquals(BLANCO_ROTO, c.getTecho());
    assertEquals(BLANCO_ROTO, c.getParedIzquierda());
    assertEquals(BLANCO_ROTO, c.getParedFondo());
    assertEquals(BLANCO_ROTO, c.getParedDerecha());
  }

  @Test
  void ambienteNuloDevuelveRecomendacionVacia() {
    assertNotNull(servicio.recomendarPintura(null));
  }

  @Test
  void dimensionesInvalidasNoGeneranColoresPeroSiLaForma() {
    RecomendacionPintura r = servicio.recomendarPintura(ambiente(0.0, 0.0, 2.7));
    ColorSuperficie c = r.getColorSuperficie();

    assertEquals(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR, r.getFormaPintura());
    assertNull(c.getTecho());
    assertNull(c.getParedIzquierda());
    assertNull(c.getParedFondo());
    assertNull(c.getParedDerecha());
  }

  @Test
  void todaSuperficieActivaEnLaFormaTieneColor() {
    double[][] casos = { { 6, 3 }, { 3, 6 }, { 4, 4 } };

    for (double[] caso : casos) {
      RecomendacionPintura r = servicio.recomendarPintura(ambiente(caso[0], caso[1], 2.7));
      int[] flags = r.getFormaPintura().getSuperficies();
      ColorSuperficie c = r.getColorSuperficie();
      String[] colores = {
        c.getTecho(),
        c.getParedIzquierda(),
        c.getParedFondo(),
        c.getParedDerecha(),
      };

      for (int i = 0; i < 4; i++) {
        if (flags[i] == 1) {
          assertNotNull(
            colores[i],
            "Superficie " + i + " activa sin color en " + r.getFormaPintura()
          );
        }
      }
    }
  }
}
