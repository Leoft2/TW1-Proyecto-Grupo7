package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ServicioPlano2DMovimientoTest {

  private final ServicioPlano2D servicioPlano2D = new ServicioPlano2DImpl(
    new ServicioZonaExclusionImpl()
  );

  @Test
  public void deberiaDetectarInterferenciaCuandoMuebleMovidoInvadeUnObstaculo() {
    Ambiente ambiente = new Ambiente(4.0, 3.0);
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.RADIADOR, Muro.SUPERIOR, 1.0, 0.8, 0.15)
    );
    MuebleUbicado mueble = new MuebleUbicado(new Mueble("Mesa", 0.5, 0.5), 3.0, 2.0);

    ValidacionMovimiento validacion = servicioPlano2D.validarMovimiento(
      mueble,
      ambiente,
      1.2,
      0.05
    );

    assertTrue(validacion.hayInterferencia());
  }
}
