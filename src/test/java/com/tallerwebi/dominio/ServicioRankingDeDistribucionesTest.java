package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * HU-33B: Puntaje de ubicacion y ranking de las 3 mejores opciones
 * - Las distribuciones validas suman o restan puntos y se ordenan de mayor a menor.
 * - Se muestran hasta 3 opciones con puntaje y desglose de reglas.
 * - Si no hay distribuciones validas, el ranking queda vacio.
 * - No se repiten distribuciones equivalentes o espejadas con el mismo puntaje.
 */
public class ServicioRankingDeDistribucionesTest {

  private ServicioPlano2D servicioPlano2D;
  private ServicioReglasDeUbicacion servicioReglasDeUbicacion;
  private ServicioRankingDeDistribuciones servicioRankingDeDistribuciones;
  private Mueble cama;
  private Ambiente ambiente;

  @BeforeEach
  public void init() {
    servicioPlano2D = mock(ServicioPlano2D.class);
    servicioReglasDeUbicacion = mock(ServicioReglasDeUbicacion.class);
    servicioRankingDeDistribuciones =
      new ServicioRankingDeDistribucionesImpl(servicioPlano2D, servicioReglasDeUbicacion);
    cama = givenUnaCama();
    ambiente = givenAmbiente();
  }

  @Test
  public void deberiaOrdenarLasOpcionesPorPuntajeDeMayorAMenor() {
    //Given
    givenCuatroOpcionesValidas();
    when(
      servicioReglasDeUbicacion.cumpleReglasDeCategoria(
        any(MuebleUbicado.class),
        any(Ambiente.class)
      )
    )
      .thenReturn(false, true, false, true);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRanking();

    //Then
    assertThat(ranking.size(), is(equalTo(3)));
    assertThat(ranking.get(0).getPuntajeTotal(), is(equalTo(1)));
    assertThat(ranking.get(1).getPuntajeTotal(), is(equalTo(1)));
    assertThat(ranking.get(2).getPuntajeTotal(), is(equalTo(-1)));
    assertThat(
      ranking.get(0).getDesglose().get(0),
      is(equalTo("Cama: cama opuesta a la ventana (+1)"))
    );
  }

  @Test
  public void deberiaDevolverUnRankingVacioSiNingunaDistribucionEsValida() {
    //Given
    Plano conflicto = givenPlanoConConflicto();
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList())).thenReturn(conflicto);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRanking();

    //Then
    assertThat(ranking.size(), is(equalTo(0)));
  }

  @Test
  public void deberiaDescartarUnaDistribucionQueIncumpleUnaReglaEliminatoria() {
    //Given
    givenCuatroOpcionesValidas();
    when(
      servicioReglasDeUbicacion.esUbicacionValida(
        any(MuebleUbicado.class),
        any(Ambiente.class),
        anyList()
      )
    )
      .thenReturn(false);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRanking();

    //Then
    assertThat(ranking.size(), is(equalTo(0)));
  }

  @Test
  public void deberiaEliminarOpcionesEspejadasConElMismoPuntaje() {
    //Given
    givenCuatroOpcionesEspejadas();
    when(
      servicioReglasDeUbicacion.cumpleReglasDeCategoria(
        any(MuebleUbicado.class),
        any(Ambiente.class)
      )
    )
      .thenReturn(true);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRanking();

    //Then
    assertThat(ranking.size(), is(equalTo(1)));
  }

  private Mueble givenUnaCama() {
    Mueble mueble = new Mueble("Cama", 1.0, 1.0);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble("Cama individual", 1.0, 1.0));
    return mueble;
  }

  private Ambiente givenAmbiente() {
    return new Ambiente(4.0, 4.0);
  }

  private void givenCuatroOpcionesValidas() {
    when(
      servicioReglasDeUbicacion.esUbicacionValida(
        any(MuebleUbicado.class),
        any(Ambiente.class),
        anyList()
      )
    )
      .thenReturn(true);
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(
        givenPlano(0.0, 0.0),
        givenPlano(0.5, 0.0),
        givenPlano(0.0, 0.5),
        givenPlano(0.5, 0.5)
      );
  }

  private void givenCuatroOpcionesEspejadas() {
    when(
      servicioReglasDeUbicacion.esUbicacionValida(
        any(MuebleUbicado.class),
        any(Ambiente.class),
        anyList()
      )
    )
      .thenReturn(true);
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(givenPlano(0.5, 0.5));
  }

  private Plano givenPlano(Double x, Double y) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(cama, x, y));
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  private Plano givenPlanoConConflicto() {
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(cama, 0.0, 0.0, true));
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  private List<OpcionDeDistribucion> whenGeneroElRanking() {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(cama);
    return servicioRankingDeDistribuciones.generarRanking(ambiente, muebles);
  }
}
