package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SelectorFormaPinturaTest {

  private final SelectorFormaPintura selector = new SelectorFormaPintura();

  @Test
  void dimensionesInvalidasDevuelveCompactar() {
    assertThat(
      seleccionar(ambiente(0.0, 0.0, 2.7)),
      is(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR)
    );
  }

  @Test
  void pasilloDevuelveParedOpuestaALaPuerta() {
    assertThat(
      seleccionar(conTipo(ambiente(3.0, 6.0, 2.7), "Pasillo")),
      is(FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE)
    );
  }

  @Test
  void muyAlargadoDevuelveParedOpuestaALaPuerta() {
    assertThat(
      seleccionar(ambiente(3.0, 8.0, 2.7)),
      is(FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE)
    );
  }

  @Test
  void ambientePequenioDevuelveCompactar() {
    assertThat(
      seleccionar(ambiente(2.5, 2.5, 2.7)),
      is(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR)
    );
  }

  @Test
  void ambienteAlargadoDevuelveTechoYParedOpuesta() {
    assertThat(
      seleccionar(ambiente(3.0, 6.0, 2.7)),
      is(FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO)
    );
  }

  @Test
  void ambienteAnchoMarcadoDevuelveParedesOpuestas() {
    assertThat(
      seleccionar(ambiente(6.0, 3.0, 2.7)),
      is(FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE)
    );
  }

  @Test
  void ambienteAnchoLeveDevuelveTodasLasParedesDeUnColor() {
    assertThat(
      seleccionar(ambiente(4.5, 2.8, 2.7)),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE)
    );
  }

  @Test
  void techoMuyAltoDevuelveParedesPorLaMitad() {
    assertThat(
      seleccionar(ambiente(4.0, 4.0, 3.3)),
      is(FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES)
    );
  }

  @Test
  void techoAltoDevuelveTechoDeUnColor() {
    assertThat(
      seleccionar(ambiente(4.0, 4.0, 3.0)),
      is(FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO)
    );
  }

  @Test
  void ambienteCuadradoEstandarDevuelveDestacarUnaPared() {
    assertThat(
      seleccionar(ambiente(4.0, 4.0, 2.7)),
      is(FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA)
    );
  }

  @Test
  void todasLasFormasSonAlcanzablesConAlgunAmbiente() {
    Set<FormaPintura> formas = EnumSet.noneOf(FormaPintura.class);

    formas.add(seleccionar(ambiente(3.0, 8.0, 2.7)));
    formas.add(seleccionar(ambiente(4.5, 2.8, 2.7)));
    formas.add(seleccionar(ambiente(4.0, 4.0, 3.0)));
    formas.add(seleccionar(ambiente(6.0, 3.0, 2.7)));
    formas.add(seleccionar(ambiente(3.0, 6.0, 2.7)));
    formas.add(seleccionar(ambiente(4.0, 4.0, 3.3)));
    formas.add(seleccionar(ambiente(2.5, 2.5, 2.7)));
    formas.add(seleccionar(ambiente(4.0, 4.0, 2.7)));

    assertThat(formas, is(EnumSet.allOf(FormaPintura.class)));
  }

  private FormaPintura seleccionar(Ambiente ambiente) {
    return selector.seleccionar(AnalisisAmbiente.de(ambiente));
  }

  private Ambiente ambiente(Double ancho, Double largo, Double altura) {
    return new Ambiente(ancho, largo, altura);
  }

  private Ambiente conTipo(Ambiente ambiente, String nombre) {
    ambiente.setTipoDeAmbiente(new TipoDeAmbiente(nombre));
    return ambiente;
  }
}
