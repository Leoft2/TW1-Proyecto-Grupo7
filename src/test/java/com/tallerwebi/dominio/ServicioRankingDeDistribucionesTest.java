package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    ServicioOrientacionFuncional orientacion = mock(ServicioOrientacionFuncional.class);
    when(orientacion.cumpleOrientacion(any(Plano.class))).thenReturn(true);
    servicioRankingDeDistribuciones =
      new ServicioRankingDeDistribucionesImpl(
        servicioPlano2D,
        servicioReglasDeUbicacion,
        ambiente -> null,
        orientacion
      );
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
    thenRankingOrdenado(ranking);
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
  public void deberiaMostrarVariasOpcionesDistintasCuandoHayEspacioSuficiente() {
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
    assertThat(ranking.size(), is(equalTo(3)));
    assertThat(ranking.get(0).getPuntajeTotal(), is(equalTo(ranking.get(1).getPuntajeTotal())));
    assertThat(ranking.get(1).getPuntajeTotal(), is(equalTo(ranking.get(2).getPuntajeTotal())));
    thenNoHayOpcionesEspejadasConElMismoPuntaje(ranking);
  }

  @Test
  public void deberiaGenerarTresOpcionesConElAlgoritmoRealCuandoHayEspacioSuficiente() {
    //Given
    ServicioZonaExclusion servicioZonaExclusion = new ServicioZonaExclusionImpl();
    ServicioPlano2D servicioPlanoReal = new ServicioPlano2DImpl(servicioZonaExclusion);
    ServicioReglasDeUbicacion reglasReales = new ServicioReglasDeUbicacionImpl(
      servicioZonaExclusion
    );
    ServicioRankingDeDistribuciones servicioReal = new ServicioRankingDeDistribucionesImpl(
      servicioPlanoReal,
      reglasReales
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(cama);

    //When
    List<OpcionDeDistribucion> ranking = servicioReal.generarRanking(ambiente, muebles);

    //Then
    assertThat(ranking.size(), is(equalTo(3)));
    thenRankingOrdenado(ranking);
    thenNoHayOpcionesEspejadasConElMismoPuntaje(ranking);
  }

  // -------------------------------------------------------------------------
  // HU35.1: Piso eliminatorio de despeje frontal del placard
  // -------------------------------------------------------------------------

  @Test
  public void deberiaExcluirDelRankingUnaDistribucionConPlacardSinDespejeFrontal() {
    //Given
    ServicioZonaExclusion servicioZonaExclusion = new ServicioZonaExclusionImpl();
    ServicioReglasDeUbicacion reglasReales = new ServicioReglasDeUbicacionImpl(
      servicioZonaExclusion
    );
    ServicioRankingDeDistribuciones rankingReal = new ServicioRankingDeDistribucionesImpl(
      servicioPlano2D,
      reglasReales
    );
    Ambiente ambienteConPlacard = new Ambiente(4.0, 2.0);
    Mueble placard = givenUnPlacard();
    Mueble bloqueo = givenUnaMesa();
    List<MuebleUbicado> mueblesUbicados = new ArrayList<>();
    mueblesUbicados.add(new MuebleUbicado(placard, 1.0, 0.0));
    mueblesUbicados.add(new MuebleUbicado(bloqueo, 1.5, 0.6));
    Plano plano = new Plano(
      ambienteConPlacard,
      mueblesUbicados,
      new ArrayList<>(),
      new ArrayList<>()
    );
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList())).thenReturn(plano);
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    List<Mueble> mueblesDelAmbiente = new ArrayList<>();
    mueblesDelAmbiente.add(placard);
    mueblesDelAmbiente.add(bloqueo);

    //When
    List<OpcionDeDistribucion> ranking = rankingReal.generarRanking(
      ambienteConPlacard,
      mueblesDelAmbiente
    );

    //Then
    assertThat(ranking.size(), is(equalTo(0)));
  }

  @Test
  public void deberiaNoIncluirEnElRankingUnaDistribucionDescartadaPorDespejeFrontal() {
    //Given
    ServicioZonaExclusion servicioZonaExclusion = new ServicioZonaExclusionImpl();
    ServicioReglasDeUbicacion reglasReales = new ServicioReglasDeUbicacionImpl(
      servicioZonaExclusion
    );
    ServicioRankingDeDistribuciones rankingReal = new ServicioRankingDeDistribucionesImpl(
      servicioPlano2D,
      reglasReales
    );
    Ambiente ambienteConPlacard = new Ambiente(4.0, 3.0);
    Mueble placard = givenUnPlacard();
    Mueble bloqueo = givenUnaMesa();
    Plano planoBloqueado = givenPlanoCon(
      ambienteConPlacard,
      new MuebleUbicado(placard, 1.0, 0.0),
      new MuebleUbicado(bloqueo, 1.5, 0.6)
    );
    Plano planoValido = givenPlanoCon(
      ambienteConPlacard,
      new MuebleUbicado(placard, 1.0, 0.0),
      new MuebleUbicado(bloqueo, 1.5, 1.6)
    );
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(planoBloqueado, planoValido, planoBloqueado, planoValido);
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenAnswer(invocacion -> {
        MuebleUbicado ubicado = invocacion.getArgument(0);
        Ambiente ambienteDelPlano = invocacion.getArgument(1);
        return (
          ubicado.getPosicionX() >= -ZonaNoDisponible.TOLERANCIA &&
          ubicado.getPosicionY() >= -ZonaNoDisponible.TOLERANCIA &&
          ubicado.getPosicionX() + ubicado.getMueble().getAncho() <=
            ambienteDelPlano.getAncho() + ZonaNoDisponible.TOLERANCIA &&
          ubicado.getPosicionY() + ubicado.getMueble().getLargo() <=
            ambienteDelPlano.getLargo() + ZonaNoDisponible.TOLERANCIA
        );
      });
    List<Mueble> mueblesDelAmbiente = new ArrayList<>();
    mueblesDelAmbiente.add(placard);
    mueblesDelAmbiente.add(bloqueo);

    //When
    List<OpcionDeDistribucion> ranking = rankingReal.generarRanking(
      ambienteConPlacard,
      mueblesDelAmbiente
    );

    //Then
    assertThat(
      thenElRankingDescartaLaBloqueadaYMantieneLaValida(ranking, bloqueo),
      is(equalTo(true))
    );
  }

  // -------------------------------------------------------------------------
  // HU35.2: Puntaje por despeje frontal del placard
  // -------------------------------------------------------------------------

  @Test
  public void deberiaSumarDosPuntosCuandoElDespejeFrontalEsMayorOIgualA60Centimetros() {
    //Given
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(0.60);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElPuntajeTotalEs(ranking, 2);
  }

  @Test
  public void deberiaSumarUnPuntoCuandoElDespejeFrontalEstaEntre40Y59Centimetros() {
    //Given
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(0.59);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElPuntajeTotalEs(ranking, 1);
  }

  @Test
  public void deberiaSumarUnPuntoCuandoElDespejeFrontalEsExactamente40Centimetros() {
    //Given
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(0.40);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElPuntajeTotalEs(ranking, 1);
  }

  @Test
  public void deberiaSumarCeroPuntosCuandoElDespejeFrontalEsMenorA40Centimetros() {
    //Given
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(0.39);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElPuntajeTotalEs(ranking, 0);
  }

  @Test
  public void deberiaMostrarCeroPuntosEnElDesgloseCuandoElPlacardNoTieneDespejeFrontalAplicable() {
    //Given
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(null);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElDesgloseContiene(ranking, "Placard: despeje frontal (+0)");
  }

  @Test
  public void deberiaNoMostrarDesgloseDeDespejeFrontalCuandoElMuebleNoEsUnPlacard() {
    //Given
    Mueble mesa = givenUnaMesa();
    givenPlanoValidoCon(mesa, 1.0, 0.5);

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(mesa);

    //Then
    thenElDesgloseEstaVacio(ranking);
  }

  @Test
  public void deberiaUsarLasZonasCalculadasCuandoPuntuaElDespejeFrontal() {
    //Given
    ServicioZonaExclusion servicioZonaExclusion = mock(ServicioZonaExclusion.class);
    List<ZonaExclusion> zonasOriginales = new ArrayList<>();
    zonasOriginales.add(new ZonaExclusion(1.0, 0.8, 1.0, 0.4, TipoAbertura.PUERTA));
    List<ZonaExclusion> zonasTransformadas = new ArrayList<>();
    when(servicioZonaExclusion.calcularZonas(any(Ambiente.class)))
      .thenReturn(
        zonasOriginales,
        zonasTransformadas,
        zonasTransformadas,
        zonasTransformadas,
        zonasTransformadas
      );
    servicioRankingDeDistribuciones =
      new ServicioRankingDeDistribucionesImpl(
        servicioPlano2D,
        servicioReglasDeUbicacion,
        servicioZonaExclusion
      );
    Mueble placard = givenUnPlacard();
    givenPlanoValidoConZonas(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenReturn(0.60);

    //When
    whenGeneroElRankingCon(placard);

    //Then
    thenSeUsoLaZonaCalculada(zonasOriginales);
  }

  @Test
  public void deberiaSeguirExplorandoVariantesCuandoLasPrimerasOpcionesNoAlcanzanElPuntajeMaximoDelPlacard() {
    //Given
    ambiente = new Ambiente(4.0, 1.7);
    Mueble placard = givenUnPlacard();
    givenPlanoValidoCon(placard, 1.0, 0.5);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenAnswer(invocacion -> {
        MuebleUbicado ubicado = invocacion.getArgument(0);
        return ubicado.getPosicionY() > 0.7 ? 0.60 : 0.59;
      });

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingCon(placard);

    //Then
    thenElPuntajeTotalEs(ranking, 2);
  }

  @Test
  public void deberiaSumarElPuntajeDeCadaPlacardCuandoHayMasDeUnPlacard() {
    //Given
    ambiente = new Ambiente(4.0, 2.5);
    Mueble primerPlacard = givenUnPlacard();
    Mueble segundoPlacard = givenUnPlacard();
    givenPlanoValidoConDosMuebles(primerPlacard, 0.0, 0.0, segundoPlacard, 2.0, 0.0);
    when(servicioReglasDeUbicacion.despejeFrontalDe(any(), any(), anyList(), any()))
      .thenAnswer(invocacion -> {
        MuebleUbicado ubicado = invocacion.getArgument(0);
        return ubicado.getMueble() == primerPlacard ? 0.60 : 0.40;
      });

    //When
    List<OpcionDeDistribucion> ranking = whenGeneroElRankingConDosMuebles(
      primerPlacard,
      segundoPlacard
    );

    //Then
    thenElPuntajeTotalEs(ranking, 3);
  }

  // -------------------------------------------------------------------------
  // GIVEN
  // -------------------------------------------------------------------------

  private Mueble givenUnaCama() {
    Mueble mueble = new Mueble("Cama", 1.0, 1.0);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble("Cama individual", 1.0, 1.0));
    return mueble;
  }

  private Mueble givenUnPlacard() {
    Mueble placard = new Mueble("Placard", 1.8, 0.6);
    placard.setCategoriaDeMueble(new CategoriaDeMueble("Placard", 1.8, 0.6));
    return placard;
  }

  private Mueble givenUnaMesa() {
    Mueble mesa = new Mueble("Mesa", 1.0, 0.5);
    mesa.setCategoriaDeMueble(new CategoriaDeMueble("Mesa", 1.0, 0.5));
    return mesa;
  }

  private Ambiente givenAmbiente() {
    return new Ambiente(4.0, 4.0);
  }

  private void givenCuatroOpcionesValidas() {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
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
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
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

  private Plano givenPlanoCon(Ambiente ambiente, MuebleUbicado primero, MuebleUbicado segundo) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(primero);
    muebles.add(segundo);
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  private Plano givenPlanoConConflicto() {
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(cama, 0.0, 0.0, true));
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  private void givenPlanoValidoCon(Mueble mueble, Double x, Double y) {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(
      servicioReglasDeUbicacion.esUbicacionValida(
        any(MuebleUbicado.class),
        any(Ambiente.class),
        anyList()
      )
    )
      .thenReturn(true);
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(givenPlanoConMueble(mueble, x, y));
  }

  private void givenPlanoValidoConDosMuebles(
    Mueble primero,
    Double xPrimero,
    Double yPrimero,
    Mueble segundo,
    Double xSegundo,
    Double ySegundo
  ) {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
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
        givenPlanoCon(
          ambiente,
          new MuebleUbicado(primero, xPrimero, yPrimero),
          new MuebleUbicado(segundo, xSegundo, ySegundo)
        )
      );
  }

  private void givenPlanoValidoConZonas(Mueble mueble, Double x, Double y) {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(
      servicioReglasDeUbicacion.esUbicacionValida(
        any(MuebleUbicado.class),
        any(Ambiente.class),
        anyList(),
        anyList()
      )
    )
      .thenReturn(true);
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(givenPlanoConMueble(mueble, x, y));
  }

  private Plano givenPlanoConMueble(Mueble mueble, Double x, Double y) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(mueble, x, y));
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  // -------------------------------------------------------------------------
  // WHEN
  // -------------------------------------------------------------------------

  private List<OpcionDeDistribucion> whenGeneroElRanking() {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(cama);
    return servicioRankingDeDistribuciones.generarRanking(ambiente, muebles);
  }

  private List<OpcionDeDistribucion> whenGeneroElRankingCon(Mueble mueble) {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(mueble);
    return servicioRankingDeDistribuciones.generarRanking(ambiente, muebles);
  }

  private List<OpcionDeDistribucion> whenGeneroElRankingConDosMuebles(
    Mueble primero,
    Mueble segundo
  ) {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(primero);
    muebles.add(segundo);
    return servicioRankingDeDistribuciones.generarRanking(ambiente, muebles);
  }

  // -------------------------------------------------------------------------
  // THEN
  // -------------------------------------------------------------------------

  private void thenRankingOrdenado(List<OpcionDeDistribucion> ranking) {
    for (int indice = 1; indice < ranking.size(); indice++) {
      assertThat(
        ranking.get(indice - 1).getPuntajeTotal() >= ranking.get(indice).getPuntajeTotal(),
        is(equalTo(true))
      );
    }
  }

  private boolean thenElMuebleEstaEnY(
    List<OpcionDeDistribucion> ranking,
    Mueble mueble,
    Double posicionY
  ) {
    for (OpcionDeDistribucion opcion : ranking) {
      for (MuebleUbicado ubicado : opcion.getPlano().getMuebles()) {
        if (
          ubicado.getMueble().equals(mueble) &&
          Math.abs(ubicado.getPosicionY() - posicionY) < ZonaNoDisponible.TOLERANCIA
        ) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean thenElRankingDescartaLaBloqueadaYMantieneLaValida(
    List<OpcionDeDistribucion> ranking,
    Mueble bloqueo
  ) {
    return (
      !thenElMuebleEstaEnY(ranking, bloqueo, 0.6) && thenElMuebleEstaEnY(ranking, bloqueo, 1.6)
    );
  }

  private void thenNoHayOpcionesEspejadasConElMismoPuntaje(List<OpcionDeDistribucion> ranking) {
    for (int primero = 0; primero < ranking.size(); primero++) {
      for (int segundo = primero + 1; segundo < ranking.size(); segundo++) {
        OpcionDeDistribucion una = ranking.get(primero);
        OpcionDeDistribucion otra = ranking.get(segundo);
        if (una.getPuntajeTotal() == otra.getPuntajeTotal()) {
          assertThat(sonEspejadas(una.getPlano(), otra.getPlano()), is(equalTo(false)));
        }
      }
    }
  }

  private boolean sonEspejadas(Plano uno, Plano otro) {
    MuebleUbicado muebleUno = uno.getMuebles().get(0);
    MuebleUbicado muebleOtro = otro.getMuebles().get(0);
    double ancho = ambiente.getAncho();
    double largo = ambiente.getLargo();
    boolean espejoHorizontal =
      Math.abs(
          ancho -
          muebleUno.getPosicionX() -
          muebleUno.getMueble().getAncho() -
          muebleOtro.getPosicionX()
        ) <
        0.0001 &&
      Math.abs(muebleUno.getPosicionY() - muebleOtro.getPosicionY()) < 0.0001;
    boolean espejoVertical =
      Math.abs(
          largo -
          muebleUno.getPosicionY() -
          muebleUno.getMueble().getLargo() -
          muebleOtro.getPosicionY()
        ) <
        0.0001 &&
      Math.abs(muebleUno.getPosicionX() - muebleOtro.getPosicionX()) < 0.0001;
    boolean espejoAmbos =
      Math.abs(
          ancho -
          muebleUno.getPosicionX() -
          muebleUno.getMueble().getAncho() -
          muebleOtro.getPosicionX()
        ) <
        0.0001 &&
      Math.abs(
          largo -
          muebleUno.getPosicionY() -
          muebleUno.getMueble().getLargo() -
          muebleOtro.getPosicionY()
        ) <
        0.0001;
    return espejoHorizontal || espejoVertical || espejoAmbos;
  }

  private void thenElPuntajeTotalEs(List<OpcionDeDistribucion> ranking, int esperado) {
    assertThat(ranking.get(0).getPuntajeTotal(), is(equalTo(esperado)));
  }

  private void thenElDesgloseContiene(List<OpcionDeDistribucion> ranking, String linea) {
    assertThat(ranking.get(0).getDesglose(), hasItem(linea));
  }

  private void thenElDesgloseEstaVacio(List<OpcionDeDistribucion> ranking) {
    assertThat(ranking.get(0).getDesglose().isEmpty(), is(equalTo(true)));
  }

  private void thenSeUsoLaZonaCalculada(List<ZonaExclusion> zonas) {
    verify(servicioReglasDeUbicacion, atLeastOnce())
      .despejeFrontalDe(any(MuebleUbicado.class), any(Ambiente.class), anyList(), eq(zonas));
  }
}
