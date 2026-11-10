package com.tallerwebi.dominio;

import java.util.List;

public class RelacionSofaTelevisor {

  public static final double DISTANCIA_MINIMA = 1.5;
  public static final double DISTANCIA_MAXIMA = 4.0;
  public static final String MENSAJE_CONFLICTO =
    " No se encontró una ubicación para el sofá y rack de TV con una distancia de 1,5 a 4 m y la vista libre.";

  public boolean haySofaYTelevisor(List<Mueble> muebles) {
    boolean haySofa = false;
    boolean hayRack = false;
    for (Mueble mueble : muebles) {
      CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
      if (categoria == null || categoria.getNombre() == null) {
        continue;
      }
      String nombre = categoria.getNombre();
      if (nombre.startsWith("Sofá") || nombre.startsWith("Sofa")) {
        haySofa = true;
      }
      if (nombre.startsWith("Rack de TV")) {
        hayRack = true;
      }
    }
    return haySofa && hayRack;
  }

  public boolean esValida(
    MuebleUbicado sofa,
    MuebleUbicado rack,
    Muro frente,
    List<MuebleUbicado> muebles
  ) {
    double distancia = distanciaEntre(sofa, rack, frente);
    if (
      distancia < DISTANCIA_MINIMA - ZonaNoDisponible.TOLERANCIA ||
      distancia > DISTANCIA_MAXIMA + ZonaNoDisponible.TOLERANCIA
    ) {
      return false;
    }
    ZonaNoDisponible visual = zonaVisual(sofa, rack, frente, distancia);
    if (visual == null) {
      return false;
    }
    for (MuebleUbicado otro : muebles) {
      if (!otro.equals(sofa) && !otro.equals(rack) && visual.seSolapaCon(otro)) {
        return false;
      }
    }
    return true;
  }

  private double distanciaEntre(MuebleUbicado sofa, MuebleUbicado rack, Muro frente) {
    return switch (frente) {
      case SUPERIOR -> sofa.getPosicionY() - rack.getPosicionY() - rack.getMueble().getLargo();
      case INFERIOR -> rack.getPosicionY() - sofa.getPosicionY() - sofa.getMueble().getLargo();
      case IZQUIERDO -> sofa.getPosicionX() - rack.getPosicionX() - rack.getMueble().getAncho();
      case DERECHO -> rack.getPosicionX() - sofa.getPosicionX() - sofa.getMueble().getAncho();
    };
  }

  private ZonaNoDisponible zonaVisual(
    MuebleUbicado sofa,
    MuebleUbicado rack,
    Muro frente,
    double distancia
  ) {
    if (frente == Muro.SUPERIOR || frente == Muro.INFERIOR) {
      double inicio = Math.max(sofa.getPosicionX(), rack.getPosicionX());
      double fin = Math.min(
        sofa.getPosicionX() + sofa.getMueble().getAncho(),
        rack.getPosicionX() + rack.getMueble().getAncho()
      );
      if (fin - inicio <= ZonaNoDisponible.TOLERANCIA) {
        return null;
      }
      double posicionY = frente == Muro.SUPERIOR
        ? rack.getPosicionY() + rack.getMueble().getLargo()
        : sofa.getPosicionY() + sofa.getMueble().getLargo();
      return new ZonaNoDisponible(inicio, posicionY, fin - inicio, distancia);
    }
    double inicio = Math.max(sofa.getPosicionY(), rack.getPosicionY());
    double fin = Math.min(
      sofa.getPosicionY() + sofa.getMueble().getLargo(),
      rack.getPosicionY() + rack.getMueble().getLargo()
    );
    if (fin - inicio <= ZonaNoDisponible.TOLERANCIA) {
      return null;
    }
    double posicionX = frente == Muro.IZQUIERDO
      ? rack.getPosicionX() + rack.getMueble().getAncho()
      : sofa.getPosicionX() + sofa.getMueble().getAncho();
    return new ZonaNoDisponible(posicionX, inicio, distancia, fin - inicio);
  }
}
