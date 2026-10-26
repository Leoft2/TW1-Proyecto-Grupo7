package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Datos que el plano expone para resaltar los conflictos con obstáculos fijos.
 */
public class PlanoTest {

  private final Ambiente ambiente = new Ambiente(4.0, 3.0);
  private final Mueble rack = new Mueble("Rack", 1.0, 0.5);

  @Test
  public void deberiaListarSoloLosMueblesEnConflicto() {
    MuebleUbicado enConflicto = givenUnMuebleUbicado(0.0, 0.0, true);
    MuebleUbicado libre = givenUnMuebleUbicado(1.0, 0.0, false);
    Plano plano = givenUnPlanoCon(Arrays.asList(enConflicto, libre));

    List<MuebleUbicado> conflictos = whenListoLosMueblesEnConflicto(plano);

    thenLosMueblesEnConflictoSon(Arrays.asList(enConflicto), conflictos);
  }

  @Test
  public void deberiaDescribirLosObstaculosConLosQueChocaElMueble() {
    givenUnRadiadorEn(Muro.SUPERIOR, 0.0, 0.8);
    givenUnaTomaDeCorrienteEn(Muro.SUPERIOR, 0.9, 0.15);
    givenUnaTomaDeCorrienteEn(Muro.INFERIOR, 2.0, 0.15);
    MuebleUbicado enConflicto = givenUnMuebleUbicado(0.0, 0.0, true);
    Plano plano = givenUnPlanoCon(Arrays.asList(enConflicto));

    String descripcion = whenDescriboElConflicto(plano, enConflicto);

    thenLaDescripcionDelConflictoEs(
      "Rack choca con Radiador (muro superior), Toma de corriente (muro superior)",
      descripcion
    );
  }

  private MuebleUbicado givenUnMuebleUbicado(
    Double posicionX,
    Double posicionY,
    Boolean enConflicto
  ) {
    return new MuebleUbicado(rack, posicionX, posicionY, enConflicto);
  }

  private void givenUnRadiadorEn(Muro muro, Double posicion, Double ancho) {
    ambiente.agregarObstaculo(new Obstaculo(TipoDeObstaculo.RADIADOR, muro, posicion, ancho, 0.15));
  }

  private void givenUnaTomaDeCorrienteEn(Muro muro, Double posicion, Double ancho) {
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.TOMA_DE_CORRIENTE, muro, posicion, ancho, 0.1)
    );
  }

  private Plano givenUnPlanoCon(List<MuebleUbicado> muebles) {
    return new Plano(ambiente, new ArrayList<>(muebles), new ArrayList<>(), new ArrayList<>());
  }

  private List<MuebleUbicado> whenListoLosMueblesEnConflicto(Plano plano) {
    return plano.getMueblesEnConflicto();
  }

  private String whenDescriboElConflicto(Plano plano, MuebleUbicado ubicado) {
    return plano.descripcionDelConflicto(ubicado);
  }

  private void thenLosMueblesEnConflictoSon(
    List<MuebleUbicado> esperados,
    List<MuebleUbicado> conflictos
  ) {
    assertEquals(new ArrayList<>(esperados), conflictos);
  }

  private void thenLaDescripcionDelConflictoEs(String esperada, String descripcion) {
    assertEquals(esperada, descripcion);
  }
}
