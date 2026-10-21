package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPlano2DMovimientoTest {

  private ServicioPlano2D servicioPlano2D;

  @BeforeEach
  public void init() {
    servicioPlano2D = new ServicioPlano2DImpl(new ServicioZonaExclusionImpl());
  }

  @Test
  public void deberiaDetectarInterferenciaCuandoMuebleMovidoInvadeUnObstaculo() {
    //Given
    Ambiente ambiente = givenTengoUnAmbienteConUnRadiador();
    MuebleUbicado mueble = givenTengoUnMuebleUbicado("Mesa", 3.0, 2.0);

    //When
    ValidacionMovimiento validacion = servicioPlano2D.validarMovimiento(
      mueble,
      ambiente,
      1.2,
      0.05
    );

    //Then
    assertThat(validacion.hayInterferencia(), is(equalTo(true)));
  }

  private Ambiente givenTengoUnAmbienteConUnRadiador() {
    Ambiente ambiente = new Ambiente(4.0, 3.0);
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.RADIADOR, Muro.SUPERIOR, 1.0, 0.8, 0.15)
    );
    return ambiente;
  }

  private MuebleUbicado givenTengoUnMuebleUbicado(String nombre, double x, double y) {
    return new MuebleUbicado(new Mueble(nombre, 0.5, 0.5), x, y);
  }
}
