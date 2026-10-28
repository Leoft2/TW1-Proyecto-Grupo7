package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ServicioReglasDeAperturaTest {

  private static final Double TOLERANCIA = 0.001;

  private final ServicioReglasDeApertura servicioReglasDeApertura =
    new ServicioReglasDeAperturaImpl();

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoHayUnaUnicaParedLibre() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.DERECHO, regla);
  }

  @Test
  public void deberiaDefinirElLadoDeLaBisagraCuandoHayUnaUnicaParedLibre() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElLadoDeLaBisagraEs(LadoDeLaHoja.FIN, regla);
  }

  @Test
  public void deberiaElegirLaParedMasCercanaCuandoHayParedesLibresEnAmbosLados() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.IZQUIERDO, regla);
  }

  @Test
  public void deberiaUbicarLaBisagraDelLadoDeLaParedMasCercanaCuandoHayParedesLibresEnAmbosLados() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElLadoDeLaBisagraEs(LadoDeLaHoja.INICIO, regla);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroInferiorYLaBisagraQuedaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.IZQUIERDO, regla);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroInferiorYLaBisagraQuedaEnFin() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 2.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.DERECHO, regla);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroInferiorCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(5.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElExtremoFinCuandoLaPuertaEstaEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 2.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(2.9, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnDoscientosSetentaGradosCuandoLaBisagraEstaEnInicioEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(270.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnTrescientosSesentaGradosCuandoLaBisagraEstaEnInicioEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(360.0, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnCientoOchentaGradosCuandoLaBisagraEstaEnFinEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 2.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(180.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnDoscientosSetentaGradosCuandoLaBisagraEstaEnFinEnElMuroInferior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 2.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(270.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElMuroInferiorCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(1.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroInferiorCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.INFERIOR, 2.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(5.0, zona);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroDerechoYLaBisagraQuedaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.SUPERIOR, regla);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroDerechoYLaBisagraQuedaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.8, 1.0, 1.0);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.INFERIOR, regla);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElMuroDerechoCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(4.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroDerechoCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(1.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElMuroDerechoCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(6.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroDerechoCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(2.8, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnNoventaGradosCuandoLaBisagraEstaEnInicioEnElMuroDerecho() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(90.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnCientoOchentaGradosCuandoLaBisagraEstaEnInicioEnElMuroDerecho() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(180.0, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnCientoOchentaGradosCuandoLaBisagraEstaEnFinEnElMuroDerecho() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(180.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnDoscientosSetentaGradosCuandoLaBisagraEstaEnFinEnElMuroDerecho() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.DERECHO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(270.0, zona);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroIzquierdoYLaBisagraQuedaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.SUPERIOR, regla);
  }

  @Test
  public void deberiaDeterminarElSentidoDeAperturaCuandoLaPuertaEstaEnElMuroIzquierdoYLaBisagraQuedaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.8, 1.0, 1.0);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.INFERIOR, regla);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElMuroIzquierdoCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroIzquierdoCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(1.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElMuroIzquierdoCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroIzquierdoCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(2.8, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnCeroGradosCuandoLaBisagraEstaEnInicioEnElMuroIzquierdo() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnNoventaGradosCuandoLaBisagraEstaEnInicioEnElMuroIzquierdo() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(90.0, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnDoscientosSetentaGradosCuandoLaBisagraEstaEnFinEnElMuroIzquierdo() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(270.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnTrescientosSesentaGradosCuandoLaBisagraEstaEnFinEnElMuroIzquierdo() {
    Ambiente ambiente = givenAmbiente(6.0, 4.0);
    Abertura puerta = givenPuerta(Muro.IZQUIERDO, 1.8, 1.0, 1.0);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(360.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnElExtremoFinCuandoLaPuertaEstaEnElMuroSuperior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(1.1, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnElMuroSuperiorCuandoLaBisagraEstaEnFin() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnNoventaGradosCuandoLaBisagraEstaEnFinEnElMuroSuperior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(90.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnCientoOchentaGradosCuandoLaBisagraEstaEnFinEnElMuroSuperior() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(180.0, zona);
  }

  @Test
  public void deberiaDefinirElLadoDeLaManillaCuandoHayUnaUnicaParedLibre() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.2, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElLadoDeLaManillaEs(LadoDeLaHoja.INICIO, regla);
  }

  @Test
  public void deberiaConsiderarLibreLaParedCuandoLaDistanciaEsIgualAlRadioEfectivo() {
    Ambiente ambiente = givenAmbiente(2.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.9, 0.9, 0.9);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.IZQUIERDO, regla);
  }

  @Test
  public void deberiaElegirInicioCuandoLasDosParedesLibresEstanALaMismaDistancia() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.5, 1.0, 1.0);

    ReglaDeApertura regla = whenDeterminoLaApertura(puerta, ambiente);

    thenElSentidoDeAperturaEs(Muro.IZQUIERDO, regla);
  }

  @Test
  public void deberiaUbicarElCentroXDeLaZonaDeExclusionEnLaBisagraCuandoElSentidoDeAperturaEstaDefinido() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroXDeLaZonaEs(1.0, zona);
  }

  @Test
  public void deberiaUbicarElCentroYDeLaZonaDeExclusionEnLaBisagraCuandoElSentidoDeAperturaEstaDefinido() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElCentroYDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaCalcularElRadioDeLaZonaDeExclusionDesdeLaBisagraCuandoElSentidoDeAperturaEstaDefinido() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElRadioDeLaZonaEs(0.9, zona);
  }

  @Test
  public void deberiaIniciarElBarridoDeLaZonaDeExclusionEnCeroGradosCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloInicialDeLaZonaEs(0.0, zona);
  }

  @Test
  public void deberiaFinalizarElBarridoDeLaZonaDeExclusionEnNoventaGradosCuandoLaBisagraEstaEnInicio() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 1.0, 0.9, 0.9);

    ZonaDeBarrido zona = whenDeterminoLaZona(puerta, ambiente);

    thenElAnguloFinalDeLaZonaEs(90.0, zona);
  }

  @Test
  public void deberiaLanzarExcepcionCuandoNoHayNingunaParedLibre() {
    Ambiente ambiente = givenAmbiente(1.6, 5.0);
    Abertura puerta = givenPuerta(Muro.SUPERIOR, 0.4, 0.9, 0.9);

    // assertThrows envuelve el When y el Then porque el resultado esperado es la excepcion.
    assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglasDeApertura.determinarApertura(puerta, ambiente)
    );
  }

  @Test
  public void deberiaLanzarExcepcionCuandoLaAberturaNoEsUnaPuerta() {
    Ambiente ambiente = givenAmbiente(4.0, 5.0);
    Abertura ventana = givenVentana(Muro.SUPERIOR, 1.0, 0.9);

    // assertThrows envuelve el When y el Then porque el resultado esperado es la excepcion.
    assertThrows(
      IllegalArgumentException.class,
      () -> servicioReglasDeApertura.determinarApertura(ventana, ambiente)
    );
  }

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private Abertura givenPuerta(Muro muro, Double posicion, Double ancho, Double radioDeGiro) {
    return new Abertura(TipoAbertura.PUERTA, muro, posicion, ancho, radioDeGiro);
  }

  private Abertura givenVentana(Muro muro, Double posicion, Double ancho) {
    return new Abertura(TipoAbertura.VENTANA, muro, posicion, ancho, null);
  }

  private ReglaDeApertura whenDeterminoLaApertura(Abertura puerta, Ambiente ambiente) {
    return servicioReglasDeApertura.determinarApertura(puerta, ambiente);
  }

  private ZonaDeBarrido whenDeterminoLaZona(Abertura puerta, Ambiente ambiente) {
    return servicioReglasDeApertura.determinarApertura(puerta, ambiente).getZonaDeExclusion();
  }

  private void thenElSentidoDeAperturaEs(Muro sentidoEsperado, ReglaDeApertura regla) {
    assertThat(regla.getSentidoDeApertura(), is(equalTo(sentidoEsperado)));
  }

  private void thenElLadoDeLaBisagraEs(LadoDeLaHoja ladoEsperado, ReglaDeApertura regla) {
    assertThat(regla.getLadoDeLaBisagra(), is(equalTo(ladoEsperado)));
  }

  private void thenElLadoDeLaManillaEs(LadoDeLaHoja ladoEsperado, ReglaDeApertura regla) {
    assertThat(regla.getLadoDeLaManilla(), is(equalTo(ladoEsperado)));
  }

  private void thenElCentroXDeLaZonaEs(Double centroXEsperado, ZonaDeBarrido zona) {
    assertThat(zona.getCentroX(), is(closeTo(centroXEsperado, TOLERANCIA)));
  }

  private void thenElCentroYDeLaZonaEs(Double centroYEsperado, ZonaDeBarrido zona) {
    assertThat(zona.getCentroY(), is(closeTo(centroYEsperado, TOLERANCIA)));
  }

  private void thenElRadioDeLaZonaEs(Double radioEsperado, ZonaDeBarrido zona) {
    assertThat(zona.getRadio(), is(closeTo(radioEsperado, TOLERANCIA)));
  }

  private void thenElAnguloInicialDeLaZonaEs(Double anguloEsperado, ZonaDeBarrido zona) {
    assertThat(zona.getAnguloInicial(), is(closeTo(anguloEsperado, TOLERANCIA)));
  }

  private void thenElAnguloFinalDeLaZonaEs(Double anguloEsperado, ZonaDeBarrido zona) {
    assertThat(zona.getAnguloFinal(), is(closeTo(anguloEsperado, TOLERANCIA)));
  }
}
