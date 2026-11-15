package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class RelacionSillaMesaTest {

  @Test
  public void deberiaReconocerLosCuatroLadosDeLaMesa() {
    //Given
    RelacionSillaMesa relacion = new RelacionSillaMesa();
    MuebleUbicado mesa = ubicado("Mesa de comedor", 2.0, 1.0, 2.0, 2.0);
    MuebleUbicado arriba = ubicado("Silla", 0.5, 0.5, 2.0, 1.2);
    MuebleUbicado abajo = ubicado("Silla", 0.5, 0.5, 2.0, 3.3);
    MuebleUbicado izquierda = ubicado("Silla", 0.5, 0.5, 1.2, 2.0);
    MuebleUbicado derecha = ubicado("Silla", 0.5, 0.5, 4.3, 2.0);

    //When
    Muro frenteArriba = relacion.frente(arriba, mesa).orElse(null);
    Muro frenteAbajo = relacion.frente(abajo, mesa).orElse(null);
    Muro frenteIzquierda = relacion.frente(izquierda, mesa).orElse(null);
    Muro frenteDerecha = relacion.frente(derecha, mesa).orElse(null);

    //Then
    assertThat(frenteArriba, is(equalTo(Muro.INFERIOR)));
    assertThat(frenteAbajo, is(equalTo(Muro.SUPERIOR)));
    assertThat(frenteIzquierda, is(equalTo(Muro.DERECHO)));
    assertThat(frenteDerecha, is(equalTo(Muro.IZQUIERDO)));
  }

  @Test
  public void deberiaRechazarSeparacionMayorAlLimiteYFaltaDeAlineacion() {
    //Given
    RelacionSillaMesa relacion = new RelacionSillaMesa();
    MuebleUbicado mesa = ubicado("Mesa de comedor", 2.0, 1.0, 2.0, 2.0);
    MuebleUbicado lejos = ubicado("Silla", 0.5, 0.5, 2.0, 1.19);
    MuebleUbicado alCostado = ubicado("Silla", 0.5, 0.5, 1.0, 1.5);

    //When
    boolean lejosValida = relacion.frente(lejos, mesa).isPresent();
    boolean costadoValida = relacion.frente(alCostado, mesa).isPresent();

    //Then
    assertThat(lejosValida, is(equalTo(false)));
    assertThat(costadoValida, is(equalTo(false)));
  }

  @Test
  public void deberiaRechazarUnMuebleEntreLaSillaYLaMesa() {
    //Given
    RelacionSillaMesa relacion = new RelacionSillaMesa();
    MuebleUbicado silla = ubicado("Silla", 0.5, 0.5, 2.0, 1.2);
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(silla);
    muebles.add(ubicado("Mesa de comedor", 2.0, 1.0, 2.0, 2.0));
    muebles.add(ubicado("Otro", 0.5, 0.3, 2.0, 1.7));

    //When
    boolean valida = relacion.frente(silla, muebles).isPresent();

    //Then
    assertThat(valida, is(equalTo(false)));
  }

  @Test
  public void deberiaPermitirElEspacioReducidoSoloConMesaDeComedor() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    ServicioReglasDeUbicacion reglas = new ServicioReglasDeUbicacionImpl(
      new ServicioZonaExclusionImpl()
    );
    MuebleUbicado silla = ubicado("Silla", 0.5, 0.5, 2.0, 1.2);
    List<MuebleUbicado> otros = new ArrayList<>();
    otros.add(ubicado("Mesa de comedor", 2.0, 1.0, 2.0, 2.0));

    //When
    boolean validaConMesaComedor = reglas.esUbicacionValida(silla, ambiente, otros);
    otros.clear();
    otros.add(ubicado("Mesa de luz", 2.0, 1.0, 2.0, 2.0));
    boolean validaConMesaDeLuz = reglas.esUbicacionValida(silla, ambiente, otros);

    //Then
    assertThat(validaConMesaComedor, is(equalTo(true)));
    assertThat(validaConMesaDeLuz, is(equalTo(false)));
  }

  @Test
  public void deberiaGenerarVariasSillasAlrededorDeLaMesa() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
    List<Mueble> muebles = new ArrayList<>();
    for (int indice = 0; indice < 4; indice++) {
      muebles.add(mueble("Silla", 0.45, 0.5));
    }
    muebles.add(mueble("Mesa de comedor", 1.6, 0.9));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      Plano plano = opcion.getPlano();
      assertThat(plano.getMuebles().size(), is(equalTo(5)));
      Set<Muro> frentes = new HashSet<>();
      for (MuebleUbicado ubicado : plano.getMuebles()) {
        RelacionSillaMesa relacion = new RelacionSillaMesa();
        if (relacion.esSilla(ubicado.getMueble())) {
          assertThat(relacion.frente(ubicado, plano.getMuebles()).isPresent(), is(equalTo(true)));
          frentes.add(relacion.frente(ubicado, plano.getMuebles()).get());
        }
      }
      assertThat(frentes.size() >= 2, is(equalTo(true)));
    }
  }

  @Test
  public void deberiaDistribuirCincoSillasEntreDosMesas() {
    //Given
    List<Mueble> muebles = givenMesasYSillas(2, 5);
    ServicioRankingDeDistribuciones ranking = givenRanking();

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(new Ambiente(8.0, 8.0), muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      Map<MuebleUbicado, MuebleUbicado> asignaciones = new RelacionSillaMesa()
        .asignaciones(opcion.getPlano().getMuebles());
      assertThat(asignaciones.size(), is(equalTo(5)));
      Set<MuebleUbicado> mesas = new HashSet<>(asignaciones.values());
      assertThat(mesas.size(), is(equalTo(2)));
      for (MuebleUbicado mesa : mesas) {
        assertThat(contarAsignaciones(asignaciones, mesa) <= 4, is(equalTo(true)));
      }
    }
  }

  @Test
  public void deberiaUbicarLaSillaExcedenteSinAsignarla() {
    //Given
    List<Mueble> muebles = givenMesasYSillas(1, 5);
    ServicioRankingDeDistribuciones ranking = givenRanking();

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(new Ambiente(8.0, 8.0), muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      Plano plano = opcion.getPlano();
      assertThat(plano.getMuebles().size(), is(equalTo(6)));
      assertThat(new RelacionSillaMesa().asignaciones(plano.getMuebles()).size(), is(equalTo(4)));
      assertThat(
        new ServicioOrientacionFuncionalImpl().desgloseOrientacion(plano).size(),
        is(equalTo(4))
      );
    }
    assertThat(new ServicioOrientacionFuncionalImpl().puntajeMaximo(muebles), is(equalTo(4)));
  }

  @Test
  public void deberiaRepartirOchoSillasYDejarUnaSinMesa() {
    //Given
    List<Mueble> muebles = givenMesasYSillas(2, 9);
    ServicioRankingDeDistribuciones ranking = givenRanking();

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(new Ambiente(10.0, 10.0), muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      Map<MuebleUbicado, MuebleUbicado> asignaciones = new RelacionSillaMesa()
        .asignaciones(opcion.getPlano().getMuebles());
      assertThat(opcion.getPlano().getMuebles().size(), is(equalTo(11)));
      assertThat(asignaciones.size(), is(equalTo(8)));
      for (MuebleUbicado mesa : new HashSet<>(asignaciones.values())) {
        assertThat(contarAsignaciones(asignaciones, mesa), is(equalTo(4)));
      }
    }
  }

  @Test
  public void deberiaRechazarCincoSillasEnUnaMesaAunqueHayaOtraLibre() {
    //Given
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(ubicado("Mesa de comedor", 3.0, 1.0, 1.0, 2.0));
    muebles.add(ubicado("Mesa de comedor", 1.0, 1.0, 6.0, 2.0));
    for (int indice = 0; indice < 5; indice++) {
      muebles.add(ubicado("Silla", 0.5, 0.5, 1.0 + indice * 0.5, 1.5));
    }
    Plano plano = new Plano(
      new Ambiente(10.0, 10.0),
      muebles,
      new ArrayList<>(),
      new ArrayList<>()
    );

    //When
    boolean valida = new ServicioOrientacionFuncionalImpl().cumpleOrientacion(plano);

    //Then
    assertThat(valida, is(equalTo(false)));
  }

  @Test
  public void deberiaReasignarUnaSillaQuePuedeEstarEnAmbasMesas() {
    //Given
    List<MuebleUbicado> muebles = new ArrayList<>();
    MuebleUbicado primera = ubicado("Mesa de comedor", 2.5, 1.0, 1.0, 2.0);
    MuebleUbicado segunda = ubicado("Mesa de comedor", 2.5, 1.0, 3.4, 2.0);
    muebles.add(primera);
    muebles.add(segunda);
    MuebleUbicado compartida = ubicado("Silla", 0.5, 0.5, 3.2, 1.5);
    muebles.add(compartida);
    for (int indice = 0; indice < 4; indice++) {
      muebles.add(ubicado("Silla", 0.5, 0.5, 1.0 + indice * 0.5, 1.5));
    }

    //When
    Map<MuebleUbicado, MuebleUbicado> asignaciones = new RelacionSillaMesa().asignaciones(muebles);

    //Then
    assertThat(asignaciones.size(), is(equalTo(5)));
    assertThat(asignaciones.get(compartida), is(equalTo(segunda)));
    assertThat(contarAsignaciones(asignaciones, primera), is(equalTo(4)));
  }

  private List<Mueble> givenMesasYSillas(int cantidadMesas, int cantidadSillas) {
    List<Mueble> muebles = new ArrayList<>();
    Mueble silla = mueble("Silla", 0.45, 0.5);
    Mueble mesa = mueble("Mesa de comedor", 1.6, 0.9);
    for (int indice = 0; indice < cantidadSillas; indice++) {
      muebles.add(silla);
    }
    for (int indice = 0; indice < cantidadMesas; indice++) {
      muebles.add(mesa);
    }
    return muebles;
  }

  private ServicioRankingDeDistribuciones givenRanking() {
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    return new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
  }

  private int contarAsignaciones(
    Map<MuebleUbicado, MuebleUbicado> asignaciones,
    MuebleUbicado mesa
  ) {
    int total = 0;
    for (MuebleUbicado asignada : asignaciones.values()) {
      if (mesa.equals(asignada)) {
        total++;
      }
    }
    return total;
  }

  private MuebleUbicado ubicado(String categoria, double ancho, double largo, double x, double y) {
    return new MuebleUbicado(mueble(categoria, ancho, largo), x, y);
  }

  private Mueble mueble(String categoria, double ancho, double largo) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    return mueble;
  }
}
