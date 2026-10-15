package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
Criterios de aceptación HU-04:
1-Dado que cargué una puerta con su radio de giro, cuando el sistema
genera la distribución, entonces ningún mueble se ubica dentro de esa zona de exclusión.
2-Dado que un mueble reubicado manualmente invade una zona de abertura, cuando se intenta guardar,
entonces el sistema muestra una advertencia de interferencia.
3-Dado que cargué una ventana, cuando el sistema calcula zonas de exclusión,
entonces aplica una restricción específica para ventanas, distinta a la de radio de giro usada para puertas.
*/

public class ServicioZonaExclusionTest {

  private final ServicioZonaExclusion servicioZonaExclusion = new ServicioZonaExclusionImpl();

  @Test
  public void deberiaDevolverListaVaciaCuandoAmbienteNoTieneAberturas() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLasZonasEstanVacias(zonas);
  }

  @Test
  public void deberiaGenerarUnaZonaCuandoAmbienteTieneUnaPuerta() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaCantidadDeZonasEs(1, zonas);
  }

  @Test
  public void deberiaUsarRadioDeGiroComoProfundidadCuandoAberturaEsPuerta() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaProfundidadDeLaZonaEs(0.9, zonas.get(0));
  }

  @Test
  public void deberiaCambiarProfundidadCuandoPuertaTieneOtroRadioDeGiro() {
    Ambiente ambienteConRadioCorto = givenAmbiente(4.0, 5.0);
    ambienteConRadioCorto.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));
    Ambiente ambienteConRadioLargo = givenAmbiente(4.0, 5.0);
    ambienteConRadioLargo.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 1.2));

    List<Double> profundidades = whenCalculoLasProfundidadesDeLasZonas(
      ambienteConRadioCorto,
      ambienteConRadioLargo
    );

    thenLasProfundidadesSon(0.9, 1.2, profundidades);
  }

  @Test
  public void deberiaUbicarZonaHaciaElInteriorCuandoPuertaEstaEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaPosicionYDeLaZonaEs(4.1, zonas.get(0));
  }

  @Test
  public void deberiaUbicarZonaPegadaAlBordeSuperiorCuandoPuertaEstaEnElMuroSuperior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaPosicionYDeLaZonaEs(0.0, zonas.get(0));
  }

  @Test
  public void deberiaUbicarZonaPegadaAlBordeInferiorCuandoPuertaEstaEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenElBordeInferiorDeLaZonaEs(5.0, zonas.get(0));
  }

  @Test
  public void deberiaUbicarZonaPegadaAlBordeDerechoCuandoPuertaEstaEnElMuroDerecho() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenElBordeDerechoDeLaZonaEs(4.0, zonas.get(0));
  }

  @Test
  public void deberiaUbicarZonaPegadaAlBordeIzquierdoCuandoPuertaEstaEnElMuroIzquierdo() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaPosicionXDeLaZonaEs(0.0, zonas.get(0));
  }

  @Test
  public void deberiaGenerarZonaDeTipoVentanaCuandoAberturaEsVentana() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenVentana(Muro.DERECHO, 0.5, 1.5));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenElTipoDeLaZonaEs(TipoAbertura.VENTANA, zonas.get(0));
  }

  @Test
  public void deberiaIgnorarRadioDeGiroCuandoAberturaEsVentana() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenVentanaConRadio(Muro.SUPERIOR, 1.0, 1.5, 2.0));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaProfundidadDeLaZonaEs(0.3, zonas.get(0));
  }

  @Test
  public void deberiaDiferenciarTipoDeZonaCuandoHayPuertaYVentana() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(givenVentana(Muro.DERECHO, 0.5, 1.5));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLosTiposDeZonaSonPuertaYVentana(zonas);
  }

  @Test
  public void deberiaGenerarUnaZonaPorAberturaCuandoHayVariasAberturas() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(givenVentana(Muro.DERECHO, 0.5, 1.5));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenLaCantidadDeZonasEs(3, zonas);
  }

  @Test
  public void deberiaRecortarZonaAlLimiteDelAmbienteCuandoRadioDeGiroExcedeElAmbiente() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 6.0));

    List<ZonaExclusion> zonas = whenCalculoLasZonas(ambiente);

    thenElBordeInferiorDeLaZonaEs(5.0, zonas.get(0));
  }

  @Test
  public void deberiaDevolverZonasIgualesCuandoSeCalculaDosVecesConLosMismosDatos() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(givenVentana(Muro.DERECHO, 0.5, 1.5));

    List<ZonaExclusion> primerasZonas = whenCalculoLasZonas(ambiente);
    List<ZonaExclusion> segundasZonas = whenCalculoLasZonas(ambiente);

    thenLasZonasSonIguales(primerasZonas, segundasZonas);
  }

  @Test
  public void deberiaConservarLaCantidadDeAberturasCuandoSeCalculanLasZonas() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    ambiente.agregarAbertura(givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(givenVentana(Muro.DERECHO, 0.5, 1.5));

    whenCalculoLasZonasDelAmbiente(ambiente);

    thenLaCantidadDeAberturasDelAmbienteEs(2, ambiente);
  }

  @Test
  public void deberiaConservarLasMismasAberturasCuandoSeCalculanLasZonas() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);
    Abertura ventana = givenVentana(Muro.DERECHO, 0.5, 1.5);
    ambiente.agregarAbertura(puerta);
    ambiente.agregarAbertura(ventana);

    whenCalculoLasZonasDelAmbiente(ambiente);

    thenElAmbienteConservaLasAberturas(ambiente, puerta, ventana);
  }

  @Test
  public void deberiaDetectarInterferenciaCuandoMuebleSolapaConZonaDePuerta() {
    ZonaExclusion zona = givenZonaDePuerta(1.0, 0.0, 0.9, 0.9);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 1.5, 0.5);

    Boolean hayInterferencia = whenVerificoSiElMuebleInterfiereConLaZona(mueble, zona);

    thenHayInterferencia(true, hayInterferencia);
  }

  @Test
  public void deberiaNoDetectarInterferenciaCuandoMuebleEstaFueraDeLaZona() {
    ZonaExclusion zona = givenZonaDePuerta(1.0, 0.0, 0.9, 0.9);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.0, 3.0);

    Boolean hayInterferencia = whenVerificoSiElMuebleInterfiereConLaZona(mueble, zona);

    thenHayInterferencia(false, hayInterferencia);
  }

  @Test
  public void deberiaNoDetectarInterferenciaCuandoMuebleSoloTocaElBordeDeLaZona() {
    ZonaExclusion zona = givenZonaDePuerta(1.0, 0.0, 0.9, 0.9);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 1.9, 0.0);

    Boolean hayInterferencia = whenVerificoSiElMuebleInterfiereConLaZona(mueble, zona);

    thenHayInterferencia(false, hayInterferencia);
  }

  @Test
  public void deberiaDetectarInterferenciaCuandoMuebleSolapaConZonaDeVentana() {
    ZonaExclusion zona = givenZonaDeVentana(3.7, 0.5, 0.3, 1.5);
    MuebleUbicado mueble = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.5, 1.0);

    Boolean hayInterferencia = whenVerificoSiElMuebleInterfiereConLaZona(mueble, zona);

    thenHayInterferencia(true, hayInterferencia);
  }

  @Test
  public void deberiaLanzarExcepcionCuandoAmbienteEsNull() {
    assertThrows(IllegalArgumentException.class, () -> servicioZonaExclusion.calcularZonas(null));
  }

  // -------------------------------------------------------------------------
  // Given (preparacion)
  // -------------------------------------------------------------------------

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private Abertura givenPuerta(Muro muro, Double posicion, Double ancho, Double radioDeGiro) {
    return new Abertura(TipoAbertura.PUERTA, muro, posicion, ancho, radioDeGiro);
  }

  private Abertura givenVentana(Muro muro, Double posicion, Double ancho) {
    return givenVentanaConRadio(muro, posicion, ancho, 0.0);
  }

  private Abertura givenVentanaConRadio(
    Muro muro,
    Double posicion,
    Double ancho,
    Double radioDeGiro
  ) {
    return new Abertura(TipoAbertura.VENTANA, muro, posicion, ancho, radioDeGiro);
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

  private ZonaExclusion givenZonaDeVentana(
    Double posicionX,
    Double posicionY,
    Double ancho,
    Double profundidad
  ) {
    return new ZonaExclusion(posicionX, posicionY, ancho, profundidad, TipoAbertura.VENTANA);
  }

  // -------------------------------------------------------------------------
  // When (ejecucion)
  // -------------------------------------------------------------------------

  private List<ZonaExclusion> whenCalculoLasZonas(Ambiente ambiente) {
    return servicioZonaExclusion.calcularZonas(ambiente);
  }

  private List<Double> whenCalculoLasProfundidadesDeLasZonas(Ambiente... ambientes) {
    List<Double> profundidades = new ArrayList<>();
    for (Ambiente ambiente : ambientes) {
      profundidades.add(servicioZonaExclusion.calcularZonas(ambiente).get(0).getProfundidad());
    }
    return profundidades;
  }

  private void whenCalculoLasZonasDelAmbiente(Ambiente ambiente) {
    servicioZonaExclusion.calcularZonas(ambiente);
  }

  private Boolean whenVerificoSiElMuebleInterfiereConLaZona(
    MuebleUbicado mueble,
    ZonaExclusion zona
  ) {
    return zona.seSolapaCon(mueble);
  }

  // -------------------------------------------------------------------------
  // Then (validacion)
  // -------------------------------------------------------------------------

  private void thenLasZonasEstanVacias(List<ZonaExclusion> zonas) {
    assertThat(zonas, is(empty()));
  }

  private void thenLaCantidadDeZonasEs(int cantidadEsperada, List<ZonaExclusion> zonas) {
    assertThat(zonas, hasSize(cantidadEsperada));
  }

  private void thenLaProfundidadDeLaZonaEs(Double profundidadEsperada, ZonaExclusion zona) {
    assertThat(zona.getProfundidad(), is(closeTo(profundidadEsperada, 0.001)));
  }

  private void thenElTipoDeLaZonaEs(TipoAbertura tipoEsperado, ZonaExclusion zona) {
    assertThat(zona.getTipo(), is(equalTo(tipoEsperado)));
  }

  private void thenLosTiposDeZonaSonPuertaYVentana(List<ZonaExclusion> zonas) {
    assertThat(
      zonas,
      containsInAnyOrder(
        hasProperty("tipo", is(TipoAbertura.PUERTA)),
        hasProperty("tipo", is(TipoAbertura.VENTANA))
      )
    );
  }

  private void thenLasProfundidadesSon(
    Double primeraEsperada,
    Double segundaEsperada,
    List<Double> profundidades
  ) {
    assertThat(
      profundidades,
      contains(closeTo(primeraEsperada, 0.001), closeTo(segundaEsperada, 0.001))
    );
  }

  private void thenLaPosicionYDeLaZonaEs(Double posicionYEsperada, ZonaExclusion zona) {
    assertThat(zona.getPosicionY(), is(closeTo(posicionYEsperada, 0.001)));
  }

  private void thenLaPosicionXDeLaZonaEs(Double posicionXEsperada, ZonaExclusion zona) {
    assertThat(zona.getPosicionX(), is(closeTo(posicionXEsperada, 0.001)));
  }

  private void thenElBordeInferiorDeLaZonaEs(Double bordeEsperado, ZonaExclusion zona) {
    assertThat(zona.getPosicionY() + zona.getProfundidad(), is(closeTo(bordeEsperado, 0.001)));
  }

  private void thenElBordeDerechoDeLaZonaEs(Double bordeEsperado, ZonaExclusion zona) {
    assertThat(zona.getPosicionX() + zona.getAncho(), is(closeTo(bordeEsperado, 0.001)));
  }

  private void thenLasZonasSonIguales(
    List<ZonaExclusion> primerasZonas,
    List<ZonaExclusion> segundasZonas
  ) {
    assertThat(segundasZonas, is(equalTo(primerasZonas)));
  }

  private void thenLaCantidadDeAberturasDelAmbienteEs(int cantidadEsperada, Ambiente ambiente) {
    assertThat(ambiente.getAberturas(), hasSize(cantidadEsperada));
  }

  private void thenElAmbienteConservaLasAberturas(
    Ambiente ambiente,
    Abertura primeraAbertura,
    Abertura segundaAbertura
  ) {
    assertThat(
      ambiente.getAberturas(),
      contains(sameInstance(primeraAbertura), sameInstance(segundaAbertura))
    );
  }

  private void thenHayInterferencia(Boolean interferenciaEsperada, Boolean hayInterferencia) {
    assertThat(hayInterferencia, is(equalTo(interferenciaEsperada)));
  }
}
