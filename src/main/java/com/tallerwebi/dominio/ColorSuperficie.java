package com.tallerwebi.dominio;

public class ColorSuperficie {

  private final String techo;
  private final String paredIzquierda;
  private final String paredFondo;
  private final String paredDerecha;

  public ColorSuperficie(
    String techo,
    String paredIzquierda,
    String paredFondo,
    String paredDerecha
  ) {
    this.techo = techo;
    this.paredIzquierda = paredIzquierda;
    this.paredFondo = paredFondo;
    this.paredDerecha = paredDerecha;
  }

  public String getTecho() {
    return techo;
  }

  public String getParedIzquierda() {
    return paredIzquierda;
  }

  public String getParedFondo() {
    return paredFondo;
  }

  public String getParedDerecha() {
    return paredDerecha;
  }
}
