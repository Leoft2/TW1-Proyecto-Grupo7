package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOrientacionDeMueblesTest {

  private ServicioOrientacionDeMuebles servicio;

  @BeforeEach
  public void init() {
    servicio = new ServicioOrientacionDeMueblesImpl();
  }

  @Test
  public void deberiaOrientarHaciaAbajoUnMuebleEnElMuroSuperior() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(1.0, 0.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.INFERIOR);
  }

  @Test
  public void deberiaOrientarHaciaArribaUnMuebleEnElMuroInferior() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(1.0, 4.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.SUPERIOR);
  }

  @Test
  public void deberiaOrientarHaciaLaIzquierdaUnMuebleEnElMuroDerecho() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(4.0, 1.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.IZQUIERDO);
  }

  @Test
  public void deberiaOrientarHaciaLaDerechaUnMuebleEnElMuroIzquierdo() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(0.0, 1.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.DERECHO);
  }

  @Test
  public void deberiaUsarElMuroDeMayorContactoEnUnaEsquina() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = new MuebleUbicado(new Mueble("Sofa", 2.0, 1.0), 0.0, 0.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.INFERIOR);
  }

  @Test
  public void deberiaUsarElMuroIzquierdoCuandoTieneMayorContactoEnLaEsquina() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = new MuebleUbicado(new Mueble("Sofa", 1.0, 2.0), 0.0, 0.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.DERECHO);
  }

  @Test
  public void deberiaUsarElMuroSuperiorCuandoElContactoEnLaEsquinaEsIgual() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(0.0, 0.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.INFERIOR);
  }

  @Test
  public void deberiaConsiderarElMargenDeRedondeoAlReconocerLaPared() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(1.0, ZonaNoDisponible.TOLERANCIA / 2);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    thenElFrenteEs(frente, Muro.INFERIOR);
  }

  @Test
  public void deberiaNoDeterminarElFrenteSiNoEstaApoyadoEnUnaPared() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(2.0, 2.0);

    //When
    Optional<Muro> frente = whenSeCalculaElFrente(mueble, ambiente);

    //Then
    assertThat(frente.isEmpty(), is(equalTo(true)));
  }

  @Test
  public void deberiaRechazarUnMuebleFueraDelAmbiente() {
    //Given
    Ambiente ambiente = givenAmbiente();
    MuebleUbicado mueble = givenMueble(-1.0, 1.0);

    //When
    IllegalArgumentException error = assertThrows(
      IllegalArgumentException.class,
      () -> whenSeCalculaElFrente(mueble, ambiente)
    );

    //Then
    assertThat(error.getMessage(), is(equalTo("La ubicacion del mueble no es valida")));
  }

  private Ambiente givenAmbiente() {
    return new Ambiente(5.0, 5.0);
  }

  private MuebleUbicado givenMueble(Double posicionX, Double posicionY) {
    return new MuebleUbicado(new Mueble("Silla", 1.0, 1.0), posicionX, posicionY);
  }

  private Optional<Muro> whenSeCalculaElFrente(MuebleUbicado mueble, Ambiente ambiente) {
    return servicio.calcularFrente(mueble, ambiente);
  }

  private void thenElFrenteEs(Optional<Muro> frente, Muro esperado) {
    assertThat(frente, is(equalTo(Optional.of(esperado))));
  }
}
