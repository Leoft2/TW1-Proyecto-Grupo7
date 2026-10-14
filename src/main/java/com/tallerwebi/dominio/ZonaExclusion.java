package com.tallerwebi.dominio;

import java.util.Objects;

/**
 * Rectangulo del ambiente al que un mueble no puede acceder porque lo ocupa una abertura. Usa las
 * mismas coordenadas en metros que el plano (origen en la esquina superior izquierda).
 */
public class ZonaExclusion {

  private final Double posicionX;
  private final Double posicionY;
  private final Double ancho;
  private final Double profundidad;
  private final TipoAbertura tipo;

  public ZonaExclusion(
    Double posicionX,
    Double posicionY,
    Double ancho,
    Double profundidad,
    TipoAbertura tipo
  ) {
    this.posicionX = posicionX;
    this.posicionY = posicionY;
    this.ancho = ancho;
    this.profundidad = profundidad;
    this.tipo = tipo;
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

  public Double getProfundidad() {
    return profundidad;
  }

  public TipoAbertura getTipo() {
    return tipo;
  }

  public boolean seSolapaCon(MuebleUbicado ubicado) {
    Mueble mueble = ubicado.getMueble();
    return (
      posicionX < ubicado.getPosicionX() + mueble.getAncho() - ZonaNoDisponible.TOLERANCIA &&
      ubicado.getPosicionX() < posicionX + ancho - ZonaNoDisponible.TOLERANCIA &&
      posicionY < ubicado.getPosicionY() + mueble.getLargo() - ZonaNoDisponible.TOLERANCIA &&
      ubicado.getPosicionY() < posicionY + profundidad - ZonaNoDisponible.TOLERANCIA
    );
  }

  @Override
  public boolean equals(Object otro) {
    if (this == otro) {
      return true;
    }
    if (!(otro instanceof ZonaExclusion)) {
      return false;
    }
    ZonaExclusion zona = (ZonaExclusion) otro;
    return (
      Objects.equals(posicionX, zona.posicionX) &&
      Objects.equals(posicionY, zona.posicionY) &&
      Objects.equals(ancho, zona.ancho) &&
      Objects.equals(profundidad, zona.profundidad) &&
      tipo == zona.tipo
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(posicionX, posicionY, ancho, profundidad, tipo);
  }
}
