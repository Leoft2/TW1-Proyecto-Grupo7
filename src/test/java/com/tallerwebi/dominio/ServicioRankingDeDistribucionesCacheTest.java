package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * HU-33B (optimizacion): el ranking calcula las zonas de exclusion una sola vez
 * por ambiente (original + 4 simetrias) y las reutiliza en todas las variantes.
 */
public class ServicioRankingDeDistribucionesCacheTest {

  private ServicioZonaExclusion servicioZonaExclusion;
  private ServicioPlano2D servicioPlano2D;
  private ServicioRankingDeDistribuciones servicioRankingDeDistribuciones;
  private Mueble silla;
  private Ambiente ambiente;

  @BeforeEach
  public void init() {
    servicioZonaExclusion = mock(ServicioZonaExclusion.class);
    servicioPlano2D = mock(ServicioPlano2D.class);
    ServicioReglasDeUbicacion servicioReglasDeUbicacion = new ServicioReglasDeUbicacionImpl(
      servicioZonaExclusion
    );
    servicioRankingDeDistribuciones =
      new ServicioRankingDeDistribucionesImpl(
        servicioPlano2D,
        servicioReglasDeUbicacion,
        servicioZonaExclusion
      );
    silla = new Mueble("Silla", 1.0, 1.0);
    ambiente = new Ambiente(5.0, 5.0);
  }

  @Test
  public void deberiaCalcularLasZonasUnaVezPorAmbienteYReutilizarlasEnLasVariantes() {
    //Given
    givenZonasVaciasYPlanoValido();

    //When
    whenGeneroElRanking();

    //Then
    thenLasZonasSeCalcularonUnaVezPorAmbiente();
  }

  @Test
  public void deberiaDevolverOpcionesSinRecalcularZonasPorCadaVariante() {
    //Given
    givenZonasVaciasYPlanoValido();

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRanking();

    //Then
    assertThat(ranking.isEmpty(), is(equalTo(false)));
    thenLasZonasSeCalcularonUnaVezPorAmbiente();
  }

  private void givenZonasVaciasYPlanoValido() {
    MuebleUbicado muebleUbicado = new MuebleUbicado(silla, 0.0, 0.0);
    when(servicioZonaExclusion.calcularZonas(any(Ambiente.class))).thenReturn(new ArrayList<>());
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenAnswer(invocacion -> {
        Ambiente ambienteGenerado = invocacion.getArgument(0);
        List<MuebleUbicado> mueblesUbicados = new ArrayList<>();
        mueblesUbicados.add(muebleUbicado);
        return new Plano(ambienteGenerado, mueblesUbicados, new ArrayList<>(), new ArrayList<>());
      });
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
  }

  private List<OpcionDeDistribucion> whenGeneroElRanking() {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(silla);
    return servicioRankingDeDistribuciones.generarRanking(ambiente, muebles);
  }

  private void thenLasZonasSeCalcularonUnaVezPorAmbiente() {
    verify(servicioZonaExclusion, times(5)).calcularZonas(any(Ambiente.class));
  }
}
