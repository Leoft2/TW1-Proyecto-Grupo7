package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
Criterios de aceptación HU-04 (integración con el algoritmo del plano):
1-Dado que cargué una puerta con su radio de giro, cuando el sistema
genera la distribución, entonces ningún mueble se ubica dentro de esa zona de exclusión.
2-Dado que un mueble reubicado manualmente invade una zona de abertura, cuando se intenta guardar,
entonces el sistema muestra una advertencia de interferencia.
*/

public class ServicioPlano2DZonasExclusionTest {

  private final ServicioZonaExclusion servicioZonaExclusion = mock(ServicioZonaExclusion.class);
  private final ServicioPlano2D servicioPlano2D = new ServicioPlano2DImpl(servicioZonaExclusion);

  @Test
  public void deberiaConsultarZonasDeExclusionCuandoSeGeneraLaDistribucion() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    List<Mueble> muebles = givenUnSofa();

    whenGeneroLaDistribucion(ambiente, muebles);

    thenConsultaLasZonasDeExclusion(ambiente);
  }

  @Test
  public void deberiaNoUbicarMuebleDentroDeLaZonaCuandoSeGeneraLaDistribucion() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ZonaExclusion zona = givenZonaDePuerta(0.0, 0.0, 1.0, 1.0);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    List<Mueble> muebles = givenUnSofa();

    Plano plano = whenGeneroLaDistribucion(ambiente, muebles);
    List<Boolean> solapamientos = whenVerificoSiCadaMuebleSolapaLaZona(plano, zona);

    thenNingunMuebleSolapaLaZona(solapamientos);
  }

  @Test
  public void deberiaExcluirMuebleCuandoUnicaPosicionValidaEstaDentroDeLaZona() {
    Ambiente ambiente = givenAmbiente(2.0, 2.0);
    ZonaExclusion zona = givenZonaDePuerta(0.0, 0.0, 2.0, 2.0);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    List<Mueble> muebles = givenUnSofa();

    Plano plano = whenGeneroLaDistribucion(ambiente, muebles);

    thenElMuebleEstaExcluido(plano);
  }

  @Test
  public void deberiaRegistrarElMotivoCuandoUnicaPosicionValidaEstaDentroDeLaZona() {
    Ambiente ambiente = givenAmbiente(2.0, 2.0);
    ZonaExclusion zona = givenZonaDePuerta(0.0, 0.0, 2.0, 2.0);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    List<Mueble> muebles = givenUnSofa();

    Plano plano = whenGeneroLaDistribucion(ambiente, muebles);

    thenElMotivoDeExclusionNoEsVacio(plano);
  }

  @Test
  public void deberiaMantenerResultadoDeFU01CuandoAmbienteNoTieneZonasDeExclusion() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    dadoQueElServicioNoCalculaZonas(ambiente);
    List<Mueble> muebles = givenMueblesDeFU01();

    Plano plano = whenGeneroLaDistribucion(ambiente, muebles);

    thenLasPosicionesSon(givenPosicionesEsperadasDeFU01(), plano);
  }

  @Test
  public void deberiaDevolverMismaDistribucionCuandoSeGeneraDosVecesConLasMismasZonas() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ZonaExclusion zona = givenZonaDePuerta(0.0, 0.0, 1.0, 1.0);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    List<Mueble> muebles = givenUnSofa();

    Plano primerPlano = whenGeneroLaDistribucion(ambiente, muebles);
    Plano segundoPlano = whenGeneroLaDistribucion(ambiente, muebles);

    thenLasPosicionesSon(givenPosicionesDe(primerPlano), segundoPlano);
  }

  @Test
  public void deberiaAdvertirInterferenciaCuandoMuebleMovidoManualmenteInvadeZonaDeAbertura() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ZonaExclusion zona = givenZonaDePuerta(1.0, 0.0, 0.9, 0.9);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.0, 3.0);

    ValidacionMovimiento validacion = whenValidoElMovimiento(mueble, ambiente, 1.5, 0.5);

    thenHayInterferencia(true, validacion);
  }

  @Test
  public void deberiaPermitirMovimientoCuandoMuebleMovidoNoInvadeZonaDeAbertura() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ZonaExclusion zona = givenZonaDePuerta(1.0, 0.0, 0.9, 0.9);
    dadoQueElServicioCalculaLaZona(ambiente, zona);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.0, 3.0);

    ValidacionMovimiento validacion = whenValidoElMovimiento(mueble, ambiente, 3.0, 3.0);

    thenHayInterferencia(false, validacion);
  }

  // -------------------------------------------------------------------------
  // Given (preparacion)
  // -------------------------------------------------------------------------

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private Mueble givenMueble(String nombre, Double ancho, Double largo) {
    return new Mueble(nombre, ancho, largo);
  }

  private MuebleUbicado givenMuebleUbicado(Mueble mueble, Double posicionX, Double posicionY) {
    return new MuebleUbicado(mueble, posicionX, posicionY);
  }

  private ZonaExclusion givenZonaDePuerta(
    Double posicionX,
    Double posicionY,
    Double ancho,
    Double profundidad
  ) {
    return new ZonaExclusion(posicionX, posicionY, ancho, profundidad, TipoAbertura.PUERTA);
  }

  private List<Mueble> givenUnSofa() {
    return new ArrayList<>(Arrays.asList(givenMueble("Sofa", 1.0, 1.0)));
  }

  private List<Mueble> givenMueblesDeFU01() {
    return new ArrayList<>(
      Arrays.asList(givenMueble("Silla", 0.5, 0.5), givenMueble("Mesa", 1.0, 1.0))
    );
  }

  private List<List<Double>> givenPosicionesEsperadasDeFU01() {
    return new ArrayList<>(
      Arrays.asList(
        new ArrayList<>(Arrays.asList(0.0, 0.0)),
        new ArrayList<>(Arrays.asList(1.0, 0.0))
      )
    );
  }

  private List<List<Double>> givenPosicionesDe(Plano plano) {
    List<List<Double>> posiciones = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      posiciones.add(
        new ArrayList<>(Arrays.asList(ubicado.getPosicionX(), ubicado.getPosicionY()))
      );
    }
    return posiciones;
  }

  private void dadoQueElServicioCalculaLaZona(Ambiente ambiente, ZonaExclusion zona) {
    when(servicioZonaExclusion.calcularZonas(ambiente))
      .thenReturn(new ArrayList<>(Arrays.asList(zona)));
  }

  private void dadoQueElServicioNoCalculaZonas(Ambiente ambiente) {
    when(servicioZonaExclusion.calcularZonas(ambiente)).thenReturn(new ArrayList<>());
  }

  // -------------------------------------------------------------------------
  // When (ejecucion)
  // -------------------------------------------------------------------------

  private Plano whenGeneroLaDistribucion(Ambiente ambiente, List<Mueble> muebles) {
    return servicioPlano2D.generarPlano(ambiente, muebles);
  }

  private List<Boolean> whenVerificoSiCadaMuebleSolapaLaZona(Plano plano, ZonaExclusion zona) {
    List<Boolean> solapamientos = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      solapamientos.add(zona.seSolapaCon(ubicado));
    }
    return solapamientos;
  }

  private ValidacionMovimiento whenValidoElMovimiento(
    MuebleUbicado mueble,
    Ambiente ambiente,
    Double posicionXNueva,
    Double posicionYNueva
  ) {
    return servicioPlano2D.validarMovimiento(mueble, ambiente, posicionXNueva, posicionYNueva);
  }

  // -------------------------------------------------------------------------
  // Then (validacion)
  // -------------------------------------------------------------------------

  private void thenConsultaLasZonasDeExclusion(Ambiente ambiente) {
    verify(servicioZonaExclusion).calcularZonas(ambiente);
  }

  private void thenNingunMuebleSolapaLaZona(List<Boolean> solapamientos) {
    assertThat(solapamientos, everyItem(is(false)));
  }

  private void thenElMuebleEstaExcluido(Plano plano) {
    assertThat(plano.getMueblesExcluidos(), hasSize(1));
  }

  private void thenElMotivoDeExclusionNoEsVacio(Plano plano) {
    assertThat(plano.getMotivosDeExclusion(), hasSize(1));
  }

  private void thenLasPosicionesSon(List<List<Double>> posicionesEsperadas, Plano plano) {
    assertThat(givenPosicionesDe(plano), is(equalTo(posicionesEsperadas)));
  }

  private void thenHayInterferencia(
    Boolean interferenciaEsperada,
    ValidacionMovimiento validacion
  ) {
    assertThat(validacion.hayInterferencia(), is(equalTo(interferenciaEsperada)));
  }
}
