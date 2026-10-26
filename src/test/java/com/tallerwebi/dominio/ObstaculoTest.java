package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;

/**
 * Criterios de aceptación:
 * Dado que registré un obstáculo fijo en un muro, cuando se calcula la distribución,
 * entonces esa zona queda marcada como no disponible.
 * -
 * Dado que un mueble colisiona con un obstáculo fijo, cuando se muestra el plano,
 * entonces el sistema resalta visualmente el conflicto.
 */
public class ObstaculoTest {

  private static final double TOLERANCIA = 1e-9;

  private final Ambiente ambiente = new Ambiente(4.0, 3.0);

  @Test
  public void deberiaUbicarLaZonaNoDisponibleContraElMuroSuperiorDesdeSuPosicion() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 1.0, 0.8, 0.15);

    ZonaNoDisponible zona = whenCalculoLaZonaNoDisponible(radiador);

    thenLaZonaEs(zona, 1.0, 0.0, 0.8, 0.15);
  }

  @Test
  public void deberiaUbicarLaZonaNoDisponibleContraElMuroDerechoDesdeArriba() {
    Obstaculo radiador = givenUnObstaculo(Muro.DERECHO, 0.5, 0.8, 0.15);

    ZonaNoDisponible zona = whenCalculoLaZonaNoDisponible(radiador);

    thenLaZonaEs(zona, 3.85, 0.5, 0.15, 0.8);
  }

  @Test
  public void deberiaUbicarLaZonaNoDisponibleContraElMuroInferiorDesdeLaIzquierda() {
    Obstaculo radiador = givenUnObstaculo(Muro.INFERIOR, 2.0, 0.8, 0.15);

    ZonaNoDisponible zona = whenCalculoLaZonaNoDisponible(radiador);

    thenLaZonaEs(zona, 2.0, 2.85, 0.8, 0.15);
  }

  @Test
  public void deberiaUbicarLaZonaNoDisponibleContraElMuroIzquierdoDesdeArriba() {
    Obstaculo radiador = givenUnObstaculo(Muro.IZQUIERDO, 1.0, 0.8, 0.15);

    ZonaNoDisponible zona = whenCalculoLaZonaNoDisponible(radiador);

    thenLaZonaEs(zona, 0.0, 1.0, 0.15, 0.8);
  }

  @Test
  public void deberiaColisionarCuandoElMuebleSeSuperponeConElObstaculo() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 1.0, 0.8, 0.15);
    MuebleUbicado rack = givenUnMuebleUbicado("Rack", 1.0, 0.5, 0.5, 0.0);

    boolean colisiona = whenVerificoSiColisiona(radiador, rack);

    thenColisiona(colisiona);
  }

  @Test
  public void deberiaNoColisionarCuandoElMuebleTerminaDondeEmpiezaElObstaculo() {
    Obstaculo radiador = givenUnObstaculo(Muro.INFERIOR, 0.7, 0.8, 0.15);
    // Asi calcula el recorrido la X en el muro inferior: da 0.20000000000000018, no 0.2.
    Double posicionX = 4.0 - (4.0 - 0.7) - 0.5;
    MuebleUbicado mesa = givenUnMuebleUbicado("Mesa", 0.5, 0.5, posicionX, 2.5);

    boolean colisiona = whenVerificoSiColisiona(radiador, mesa);

    thenNoColisiona(colisiona);
  }

  @Test
  public void deberiaRechazarUnObstaculoQueSeSaleDelMuro() {
    Obstaculo radiador = givenUnObstaculo(Muro.DERECHO, 2.5, 0.8, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(
      error,
      "Radiador se sale del muro derecho: va de 2,5 m a 3,3 m contando desde arriba y el muro " +
      "mide 3 m. Ubicalo como máximo en la posición 2,2 m."
    );
  }

  @Test
  public void deberiaRechazarUnObstaculoMasAnchoQueElMuro() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 0.0, 4.5, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(
      error,
      "Radiador mide 4,5 m de ancho y no entra en el muro superior, que mide 4 m."
    );
  }

  @Test
  public void deberiaAceptarUnObstaculoQueLlegaJustoAlFinalDelMuro() {
    Obstaculo radiador = givenUnObstaculo(Muro.DERECHO, 2.2, 0.8, 0.15);

    // validarEn no devuelve nada: la ausencia de excepción es el resultado a verificar.
    assertDoesNotThrow(() -> whenValidoElObstaculoSinError(radiador));
  }

  @Test
  public void deberiaRechazarUnObstaculoSinAncho() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 0.5, 0.0, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "Las medidas de Radiador deben ser mayores a 0");
  }

  @Test
  public void deberiaRechazarUnObstaculoSinProfundidadCargada() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 0.5, 0.8, null);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "Las medidas de Radiador deben ser mayores a 0");
  }

  @Test
  public void deberiaRechazarUnObstaculoConPosicionNegativa() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, -0.1, 0.8, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "La posición de Radiador debe ser 0 o mayor");
  }

  @Test
  public void deberiaRechazarUnObstaculoSinPosicionCargada() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, null, 0.8, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "La posición de Radiador debe ser 0 o mayor");
  }

  @Test
  public void deberiaRechazarUnObstaculoSinMuro() {
    Obstaculo radiador = givenUnObstaculo(null, 0.5, 0.8, 0.15);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "Elegí el tipo y el muro de cada obstáculo");
  }

  @Test
  public void deberiaRechazarUnObstaculoMasProfundoQueElAmbiente() {
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 0.0, 0.8, 3.0);

    ValidacionException error = whenValidoElObstaculo(radiador);

    thenElObstaculoSeRechazaCon(error, "Radiador es más profundo que el ambiente");
  }

  @Test
  public void deberiaAceptarUnObstaculoQueLlegaAlFinalDelMuroAunqueHayaErrorDeRedondeo() {
    Ambiente ambienteDeTresTreinta = givenUnAmbiente(3.3, 3.0);
    // 2.2 + 1.1 da 3.3000000000000003, apenas mas que el muro de 3.3.
    Obstaculo radiador = givenUnObstaculo(Muro.SUPERIOR, 2.2, 1.1, 0.15);

    // validarEn no devuelve nada: la ausencia de excepción es el resultado a verificar.
    assertDoesNotThrow(() -> whenValidoElObstaculoSinErrorEn(radiador, ambienteDeTresTreinta));
  }

  private Obstaculo givenUnObstaculo(Muro muro, Double posicion, Double ancho, Double profundidad) {
    return new Obstaculo(TipoDeObstaculo.RADIADOR, muro, posicion, ancho, profundidad);
  }

  private MuebleUbicado givenUnMuebleUbicado(
    String nombre,
    Double ancho,
    Double largo,
    Double posicionX,
    Double posicionY
  ) {
    return new MuebleUbicado(new Mueble(nombre, ancho, largo), posicionX, posicionY);
  }

  private Ambiente givenUnAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private ZonaNoDisponible whenCalculoLaZonaNoDisponible(Obstaculo obstaculo) {
    return obstaculo.zonaNoDisponible(ambiente);
  }

  private boolean whenVerificoSiColisiona(Obstaculo obstaculo, MuebleUbicado ubicado) {
    return obstaculo.colisionaCon(ubicado, ambiente);
  }

  // validarEn no devuelve nada: la excepción es el resultado que luego se valida en el Then.
  private ValidacionException whenValidoElObstaculo(Obstaculo obstaculo) {
    return assertThrows(ValidacionException.class, () -> obstaculo.validarEn(ambiente));
  }

  private void whenValidoElObstaculoSinError(Obstaculo obstaculo) {
    obstaculo.validarEn(ambiente);
  }

  private void whenValidoElObstaculoSinErrorEn(Obstaculo obstaculo, Ambiente ambiente) {
    obstaculo.validarEn(ambiente);
  }

  private void thenColisiona(boolean colisiona) {
    assertTrue(colisiona);
  }

  private void thenNoColisiona(boolean colisiona) {
    assertFalse(colisiona);
  }

  private void thenElObstaculoSeRechazaCon(ValidacionException error, String mensajeEsperado) {
    assertEquals(mensajeEsperado, error.getMessage());
  }

  private void thenLaZonaEs(
    ZonaNoDisponible zona,
    double posicionX,
    double posicionY,
    double ancho,
    double largo
  ) {
    assertEquals(posicionX, zona.getPosicionX(), TOLERANCIA);
    assertEquals(posicionY, zona.getPosicionY(), TOLERANCIA);
    assertEquals(ancho, zona.getAncho(), TOLERANCIA);
    assertEquals(largo, zona.getLargo(), TOLERANCIA);
  }
}
