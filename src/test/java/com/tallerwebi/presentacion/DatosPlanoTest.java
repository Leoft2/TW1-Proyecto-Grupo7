package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.TipoAbertura;
import com.tallerwebi.dominio.TipoDeObstaculo;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

// testing de DTO DatosPlano

public class DatosPlanoTest {

  @Test
  public void deberiaArmarElAmbienteConSusMedidasYSusObstaculos() {
    // given
    DatosPlano formulario = givenUnFormularioConLosObstaculos(
      new DatosObstaculo(TipoDeObstaculo.RADIADOR, Muro.DERECHO, 0.5, 0.8, 0.15)
    );

    // when
    Ambiente ambiente = whenSeConstruyeElAmbiente(formulario);

    // then
    thenElAmbienteTieneMedidas(ambiente, 4.0, 3.0);
    thenElAmbienteTieneUnObstaculoEnMuro(ambiente, Muro.DERECHO);
  }

  @Test
  public void deberiaIgnorarLasFilasDeObstaculoSinMedidas() {
    // given
    DatosPlano formulario = givenUnFormularioConLosObstaculos(
      new DatosObstaculo(TipoDeObstaculo.RADIADOR, Muro.SUPERIOR, null, null, null)
    );

    // when
    Ambiente ambiente = whenSeConstruyeElAmbiente(formulario);

    // then
    thenElAmbienteNoTieneObstaculos(ambiente);
  }

  // -------------------------------------------------------------------------
  // Test HU-04:Aberturas
  // -------------------------------------------------------------------------

  @Test
  public void deberiaAgregarLasAberturasAlAmbienteCuandoExistenEnDatosPlano() {
    // given
    DatosPlano formulario = givenDatosPlanoConPuerta();

    // when
    Ambiente ambiente = whenSeConstruyeElAmbiente(formulario);

    // then
    thenElAmbienteTieneCantidadDeAberturas(ambiente, 1);
    thenElAmbienteTieneUnaAberturaDelTipo(ambiente, TipoAbertura.PUERTA);
  }

  @Test
  public void deberiaDetectarUnaAberturaIncompletaCuandoFaltaElAncho() {
    // given
    DatosPlano formulario = givenDatosPlanoConAberturaIncompleta();

    // when
    boolean hayAberturasIncompletas = whenSeBuscanAberturasIncompletas(formulario);

    // then
    thenHayAberturasIncompletas(hayAberturasIncompletas);
  }

  private DatosPlano givenUnFormularioConLosObstaculos(DatosObstaculo... datosObstaculos) {
    DatosPlano formulario = new DatosPlano();
    formulario.setAncho(4.0);
    formulario.setLargo(3.0);
    List<DatosObstaculo> listaObstaculos = new ArrayList<DatosObstaculo>();
    if (datosObstaculos != null) {
      for (DatosObstaculo dato : datosObstaculos) {
        listaObstaculos.add(dato);
      }
    }
    formulario.setObstaculos(listaObstaculos);
    return formulario;
  }

  private DatosPlano givenDatosPlanoConPuerta() {
    DatosPlano formulario = new DatosPlano();
    formulario.setAncho(4.0);
    formulario.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    datosAbertura.setAncho(0.9);
    datosAbertura.setRadioDeGiro(0.9);
    List<DatosAbertura> aberturas = new ArrayList<DatosAbertura>();
    aberturas.add(datosAbertura);
    formulario.setAberturas(aberturas);
    return formulario;
  }

  private DatosPlano givenDatosPlanoConAberturaIncompleta() {
    DatosPlano formulario = new DatosPlano();
    formulario.setAncho(4.0);
    formulario.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    List<DatosAbertura> aberturas = new ArrayList<DatosAbertura>();
    aberturas.add(datosAbertura);
    formulario.setAberturas(aberturas);
    return formulario;
  }

  private Ambiente whenSeConstruyeElAmbiente(DatosPlano formulario) {
    return formulario.aAmbiente();
  }

  private boolean whenSeBuscanAberturasIncompletas(DatosPlano formulario) {
    return formulario.hayAberturasIncompletas();
  }

  private void thenHayAberturasIncompletas(boolean hayAberturasIncompletas) {
    assertThat(hayAberturasIncompletas, is(true));
  }

  private void thenElAmbienteTieneMedidas(
    Ambiente ambiente,
    Double anchoEsperado,
    Double largoEsperado
  ) {
    assertEquals(anchoEsperado, ambiente.getAncho());
    assertEquals(largoEsperado, ambiente.getLargo());
  }

  private void thenElAmbienteTieneUnObstaculoEnMuro(Ambiente ambiente, Muro muroEsperado) {
    assertEquals(muroEsperado, ambiente.getObstaculos().get(0).getMuro());
  }

  private void thenElAmbienteNoTieneObstaculos(Ambiente ambiente) {
    assertEquals(0, ambiente.getObstaculos().size());
  }

  private void thenElAmbienteTieneCantidadDeAberturas(Ambiente ambiente, int cantidadEsperada) {
    assertThat(ambiente.getAberturas(), hasSize(cantidadEsperada));
  }

  private void thenElAmbienteTieneUnaAberturaDelTipo(Ambiente ambiente, TipoAbertura tipoEsperado) {
    assertThat(ambiente.getAberturas().get(0).getTipo(), is(tipoEsperado));
  }
}
