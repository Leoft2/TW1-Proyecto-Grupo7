package com.tallerwebi.dominio;

/**
 * Tramo de circulacion cuyo ancho libre no alcanza el minimo configurado. Se expresa como un
 * rectangulo en metros, con el mismo origen que el plano.
 */
public class TramoEstrecho {

  private final Double posicionX;
  private final Double posicionY;
  private final Double ancho;
  private final Double largo;
  private final Double anchoLibre;

  public TramoEstrecho(
    Double posicionX,
    Double posicionY,
    Double ancho,
    Double largo,
    Double anchoLibre
  ) {
    this.posicionX = posicionX;
    this.posicionY = posicionY;
    this.ancho = ancho;
    this.largo = largo;
    this.anchoLibre = anchoLibre;
  }

  public Double getPosicionX() {
    return posicionX;
  }

  public Double getPosicionY() {
    return posicionY;
  }

  public Double getAncho() {
    return ancho;
  }

  public Double getLargo() {
    return largo;
  }

  public Double getAnchoLibre() {
    return anchoLibre;
  }
}
