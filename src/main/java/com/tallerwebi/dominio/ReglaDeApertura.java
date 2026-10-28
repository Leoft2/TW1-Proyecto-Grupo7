package com.tallerwebi.dominio;

/** Regla de apertura de una puerta: sentido, bisagra, manilla y zona de barrido. */
public class ReglaDeApertura {

  private final Muro sentidoDeApertura;
  private final LadoDeLaHoja ladoDeLaBisagra;
  private final LadoDeLaHoja ladoDeLaManilla;
  private final ZonaDeBarrido zonaDeExclusion;

  public ReglaDeApertura(
    Muro sentidoDeApertura,
    LadoDeLaHoja ladoDeLaBisagra,
    LadoDeLaHoja ladoDeLaManilla,
    ZonaDeBarrido zonaDeExclusion
  ) {
    this.sentidoDeApertura = sentidoDeApertura;
    this.ladoDeLaBisagra = ladoDeLaBisagra;
    this.ladoDeLaManilla = ladoDeLaManilla;
    this.zonaDeExclusion = zonaDeExclusion;
  }

  public Muro getSentidoDeApertura() {
    return sentidoDeApertura;
  }

  public LadoDeLaHoja getLadoDeLaBisagra() {
    return ladoDeLaBisagra;
  }

  public LadoDeLaHoja getLadoDeLaManilla() {
    return ladoDeLaManilla;
  }

  public ZonaDeBarrido getZonaDeExclusion() {
    return zonaDeExclusion;
  }
}
