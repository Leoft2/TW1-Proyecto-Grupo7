package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;

public class ServicioObstaculosTest {

  private final ServicioObstaculos servicioObstaculos = new ServicioObstaculosImpl();
  private final Ambiente ambiente = new Ambiente(4.0, 3.0);

  @Test
  public void deberiaAceptarLosObstaculosQueEntranEnSusMuros() {
    ambiente.agregarObstaculo(givenUnRadiador(Muro.SUPERIOR, 0.0, 0.8));

    // validarObstaculos no devuelve nada: la ausencia de excepción es el resultado a verificar.
    assertDoesNotThrow(() -> whenValidoLosObstaculos(ambiente));
  }

  @Test
  public void deberiaRechazarElAmbienteCuandoUnObstaculoSeSaleDeSuMuro() {
    ambiente.agregarObstaculo(givenUnRadiador(Muro.SUPERIOR, 0.0, 0.8));
    ambiente.agregarObstaculo(givenUnaTomaDeCorriente(Muro.IZQUIERDO, 3.0, 0.15));

    ValidacionException error = whenValidoLosObstaculosConError(ambiente);

    thenSeRechazaElAmbienteCon(
      error,
      "Toma de corriente se sale del muro izquierdo: va de 3 m a 3,15 m contando desde arriba " +
      "y el muro mide 3 m. Ubicalo como máximo en la posición 2,85 m."
    );
  }

  private Obstaculo givenUnRadiador(Muro muro, Double posicion, Double ancho) {
    return new Obstaculo(TipoDeObstaculo.RADIADOR, muro, posicion, ancho, 0.15);
  }

  private Obstaculo givenUnaTomaDeCorriente(Muro muro, Double posicion, Double ancho) {
    return new Obstaculo(TipoDeObstaculo.TOMA_DE_CORRIENTE, muro, posicion, ancho, 0.1);
  }

  private void whenValidoLosObstaculos(Ambiente ambiente) {
    servicioObstaculos.validarObstaculos(ambiente);
  }

  // validarObstaculos no devuelve nada: la excepción es el resultado que luego se valida.
  private ValidacionException whenValidoLosObstaculosConError(Ambiente ambiente) {
    return assertThrows(
      ValidacionException.class,
      () -> servicioObstaculos.validarObstaculos(ambiente)
    );
  }

  private void thenSeRechazaElAmbienteCon(ValidacionException error, String mensajeEsperado) {
    assertEquals(mensajeEsperado, error.getMessage());
  }
}
