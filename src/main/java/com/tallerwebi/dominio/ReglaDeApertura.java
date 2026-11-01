package com.tallerwebi.dominio;

/** Regla de apertura de una puerta: sentido, bisagra, manilla, zona de barrido y pared libre. */
public class ReglaDeApertura {

  private final Muro sentidoDeApertura;
  private final LadoDeLaHoja ladoDeLaBisagra;
  private final LadoDeLaHoja ladoDeLaManilla;
  private final ZonaDeBarrido zonaDeExclusion;
  private final boolean hayParedLibre;

  public ReglaDeApertura(
    Muro sentidoDeApertura,
    LadoDeLaHoja ladoDeLaBisagra,
    LadoDeLaHoja ladoDeLaManilla,
    ZonaDeBarrido zonaDeExclusion,
    boolean hayParedLibre
  ) {
    this.sentidoDeApertura = sentidoDeApertura;
    this.ladoDeLaBisagra = ladoDeLaBisagra;
    this.ladoDeLaManilla = ladoDeLaManilla;
    this.zonaDeExclusion = zonaDeExclusion;
    this.hayParedLibre = hayParedLibre;
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

  public boolean getHayParedLibre() {
    return hayParedLibre;
  }
}
