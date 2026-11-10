package com.tallerwebi.dominio;

public class ServicioAccesoLateralCama {

  public boolean hayAcceso(MuebleUbicado cama, Muro frente, Plano plano) {
    double minimo = plano.getAmbiente().getAnchoMinimoDePasillo();
    if (!Double.isFinite(minimo) || minimo <= 0) {
      throw new IllegalArgumentException("El ancho minimo de pasillo debe ser mayor a cero");
    }
    double posicionX = cama.getPosicionX();
    double posicionY = cama.getPosicionY();
    double ancho = cama.getMueble().getAncho();
    double largo = cama.getMueble().getLargo();
    if (frente == Muro.SUPERIOR || frente == Muro.INFERIOR) {
      return (
        libre(posicionX - minimo, posicionY, minimo, largo, plano) &&
        libre(posicionX + ancho, posicionY, minimo, largo, plano)
      );
    }
    return (
      libre(posicionX, posicionY - minimo, ancho, minimo, plano) &&
      libre(posicionX, posicionY + largo, ancho, minimo, plano)
    );
  }

  private boolean libre(
    double posicionX,
    double posicionY,
    double ancho,
    double largo,
    Plano plano
  ) {
    Ambiente ambiente = plano.getAmbiente();
    if (
      posicionX < -ZonaNoDisponible.TOLERANCIA ||
      posicionY < -ZonaNoDisponible.TOLERANCIA ||
      posicionX + ancho > ambiente.getAncho() + ZonaNoDisponible.TOLERANCIA ||
      posicionY + largo > ambiente.getLargo() + ZonaNoDisponible.TOLERANCIA
    ) {
      return false;
    }
    ZonaNoDisponible zona = new ZonaNoDisponible(posicionX, posicionY, ancho, largo);
    for (MuebleUbicado otro : plano.getMuebles()) {
      if (zona.seSolapaCon(otro)) {
        return false;
      }
    }
    MuebleUbicado rectangulo = new MuebleUbicado(
      new Mueble("Acceso", ancho, largo),
      posicionX,
      posicionY
    );
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      if (obstaculo.colisionaCon(rectangulo, ambiente)) {
        return false;
      }
    }
    return true;
  }
}
