package com.tallerwebi.dominio;

class SelectorFormaPintura {

  private static final double PROPORCION_MUY_ALARGADO = 2.5;
  private static final double PROPORCION_ALARGADO = 1.5;
  private static final double PROPORCION_ANCHO_MARCADO = 1.8;
  private static final double PROPORCION_ANCHO_LEVE = 1.5;
  private static final double AREA_MINIMA = 9.0;
  private static final double ALTURA_MUY_ALTA = 3.2;
  private static final double ALTURA_ALTA = 2.9;

  FormaPintura seleccionar(AnalisisAmbiente ambiente) {
    if (dimensionesInvalidas(ambiente)) {
      return FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR;
    }

    if (esMuyAlargadoOPasillo(ambiente)) {
      return FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE;
    }

    if (esPequenio(ambiente)) {
      return FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR;
    }

    if (esAlargado(ambiente)) {
      return FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO;
    }

    if (esAnchoMarcado(ambiente)) {
      return FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE;
    }

    if (esAnchoLeve(ambiente)) {
      return FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE;
    }

    return seleccionarPorAltura(ambiente);
  }

  private boolean dimensionesInvalidas(AnalisisAmbiente ambiente) {
    return ambiente.ancho() <= 0 || ambiente.largo() <= 0;
  }

  private boolean esMuyAlargadoOPasillo(AnalisisAmbiente ambiente) {
    return ambiente.esPasillo() || ambiente.largo() >= ambiente.ancho() * PROPORCION_MUY_ALARGADO;
  }

  private boolean esPequenio(AnalisisAmbiente ambiente) {
    return ambiente.area() < AREA_MINIMA;
  }

  private boolean esAlargado(AnalisisAmbiente ambiente) {
    return ambiente.largo() >= ambiente.ancho() * PROPORCION_ALARGADO;
  }

  private boolean esAnchoMarcado(AnalisisAmbiente ambiente) {
    return ambiente.ancho() >= ambiente.largo() * PROPORCION_ANCHO_MARCADO;
  }

  private boolean esAnchoLeve(AnalisisAmbiente ambiente) {
    return ambiente.ancho() >= ambiente.largo() * PROPORCION_ANCHO_LEVE;
  }

  private FormaPintura seleccionarPorAltura(AnalisisAmbiente ambiente) {
    if (ambiente.altura() >= ALTURA_MUY_ALTA) {
      return FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES;
    }

    if (ambiente.altura() >= ALTURA_ALTA) {
      return FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO;
    }

    return FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA;
  }
}
