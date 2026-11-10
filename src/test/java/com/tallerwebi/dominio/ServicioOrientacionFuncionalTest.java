package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioOrientacionFuncionalTest {

  private ServicioOrientacionFuncional servicio;

  @BeforeEach
  public void init() {
    servicio = new ServicioOrientacionFuncionalImpl();
  }

  @Test
  public void deberiaAceptarSofaConRackAlineadoEnfrente() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofá 3 cuerpos", 2.0, 1.0, 1.0, 0.0);
    givenMueble(plano, "Rack de TV", 1.0, 0.5, 1.5, 4.5);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, true);
  }

  @Test
  public void deberiaRechazarSofaConRackSinAlinear() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofá 3 cuerpos", 2.0, 1.0, 0.0, 0.0);
    givenMueble(plano, "Rack de TV", 1.0, 0.5, 4.0, 4.5);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  @Test
  public void deberiaAceptarSillaHaciaUnaMesa() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Silla", 0.5, 0.5, 1.0, 0.0);
    givenMueble(plano, "Mesa de luz", 1.0, 1.0, 1.0, 2.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, true);
  }

  @Test
  public void deberiaRechazarSillaCuandoLaMesaNoEstaEnfrente() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Silla", 0.5, 0.5, 1.0, 0.0);
    givenMueble(plano, "Mesa de comedor", 1.0, 1.0, 3.0, 2.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  @Test
  public void deberiaAceptarCamaConAmbosLateralesLibres() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Cama doble", 1.4, 1.9, 1.0, 0.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, true);
  }

  @Test
  public void deberiaRechazarCamaConUnLateralContraElMuro() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Cama doble", 1.4, 1.9, 0.0, 0.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  @Test
  public void deberiaRechazarCamaConUnMuebleEnElAccesoLateral() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Cama doble", 1.4, 1.9, 1.0, 0.0);
    givenMueble(plano, "Placard", 0.5, 0.5, 0.5, 0.5);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  @Test
  public void deberiaAceptarElAnchoMinimoExactoEnLosLateralesDeLaCama() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Cama doble", 1.4, 1.9, 0.6, 0.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, true);
  }

  @Test
  public void deberiaRechazarUnLateralMenorAlAnchoMinimo() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Cama doble", 1.4, 1.9, 0.59, 0.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  @Test
  public void deberiaRechazarSillaSinParedDeApoyo() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Silla", 0.5, 0.5, 1.0, 1.0);

    //When
    boolean resultado = whenSeEvalua(plano);

    //Then
    thenResultadoEs(resultado, false);
  }

  private Plano givenPlano() {
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    ambiente.setAnchoMinimoDePasillo(0.6);
    return new Plano(ambiente, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }

  private void givenMueble(
    Plano plano,
    String categoria,
    Double ancho,
    Double largo,
    Double posicionX,
    Double posicionY
  ) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    List<MuebleUbicado> muebles = plano.getMuebles();
    muebles.add(new MuebleUbicado(mueble, posicionX, posicionY));
  }

  private boolean whenSeEvalua(Plano plano) {
    return servicio.cumpleOrientacion(plano);
  }

  private void thenResultadoEs(boolean resultado, boolean esperado) {
    assertThat(resultado, is(equalTo(esperado)));
  }
}
