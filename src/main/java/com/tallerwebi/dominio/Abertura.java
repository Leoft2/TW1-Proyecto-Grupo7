package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Abertura de un muro del ambiente (puerta o ventana). La posicion se mide desde el inicio del
 * muro, igual que un obstaculo. El radio de giro es propio de las puertas.
 */

// Review 3: se puede inyectar. Quien la consume?
@Embeddable
public class Abertura {

  @Enumerated(EnumType.STRING)
  private TipoAbertura tipo;

  @Enumerated(EnumType.STRING)
  private Muro muro;

  private Double posicion;
  private Double ancho;
  private Double radioDeGiro;

  public Abertura() {}

  public Abertura(TipoAbertura tipo, Muro muro, Double posicion, Double ancho, Double radioDeGiro) {
    this.tipo = tipo;
    this.muro = muro;
    this.posicion = posicion;
    this.ancho = ancho;
    this.radioDeGiro = radioDeGiro;
  }

  public TipoAbertura getTipo() {
    return tipo;
  }

  public Muro getMuro() {
    return muro;
  }

  public Double getPosicion() {
    return posicion;
  }

  public Double getAncho() {
    return ancho;
  }

  public Double getRadioDeGiro() {
    return radioDeGiro;
  }
}
