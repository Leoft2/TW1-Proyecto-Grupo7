package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;

@Embeddable
public class Dimensiones {

  private Double ancho;
  private Double largo;

  public Dimensiones() {}

  public Dimensiones(Double ancho, Double largo) {
    this.ancho = ancho;
    this.largo = largo;
  }

  public Double getAncho() {
    return ancho;
  }

  public void setAncho(Double ancho) {
    this.ancho = ancho;
  }

  public Double getLargo() {
    return largo;
  }

  public void setLargo(Double largo) {
    this.largo = largo;
  }
}
