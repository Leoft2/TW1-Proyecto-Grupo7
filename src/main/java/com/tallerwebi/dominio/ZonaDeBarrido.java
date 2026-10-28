package com.tallerwebi.dominio;

/**
 * Sector circular de 90 grados que barre la hoja de una puerta al abrirse. Se expresa en las mismas
 * coordenadas en metros que el plano, con el origen en la esquina superior izquierda.
 */
public class ZonaDeBarrido {

  private final Double centroX;
  private final Double centroY;
  private final Double radio;
  private final Double anguloInicial;
  private final Double anguloFinal;

  public ZonaDeBarrido(
    Double centroX,
    Double centroY,
    Double radio,
    Double anguloInicial,
    Double anguloFinal
  ) {
    this.centroX = centroX;
    this.centroY = centroY;
    this.radio = radio;
    this.anguloInicial = anguloInicial;
    this.anguloFinal = anguloFinal;
  }

  public Double getCentroX() {
    return centroX;
  }

  public Double getCentroY() {
    return centroY;
  }

  public Double getRadio() {
    return radio;
  }

  public Double getAnguloInicial() {
    return anguloInicial;
  }

  public Double getAnguloFinal() {
    return anguloFinal;
  }
}
