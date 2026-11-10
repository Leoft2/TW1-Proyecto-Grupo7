package com.tallerwebi.dominio;

public class ContextoSuperficies {

  public final String[] hexes = new String[4];
  public final double[] areas;
  public final int indiceParedOpuesta;

  public ContextoSuperficies(
    double techo,
    double izquierda,
    double fondo,
    double derecha,
    int indiceParedOpuesta
  ) {
    this.areas = new double[] { techo, izquierda, fondo, derecha };
    this.indiceParedOpuesta = indiceParedOpuesta;
  }
}
