package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

public class PosicionesDeSilla {

  private static final double PASO = 0.25;
  private static final int MAXIMO_POSICIONES = 20000;

  public List<MuebleUbicado> generar(Mueble silla, MuebleUbicado mesa, Muro lado) {
    boolean horizontal = lado == Muro.SUPERIOR || lado == Muro.INFERIOR;
    double largoMesa = horizontal ? mesa.getMueble().getAncho() : mesa.getMueble().getLargo();
    double largoSilla = horizontal ? silla.getAncho() : silla.getLargo();
    double maximo = Math.max(0, largoMesa - largoSilla);
    List<MuebleUbicado> posiciones = new ArrayList<>();
    agregarPosiciones(posiciones, silla, mesa, lado, maximo, Math.max(largoSilla, PASO));
    agregarPosiciones(posiciones, silla, mesa, lado, maximo, PASO);
    posiciones.add(candidato(silla, mesa, lado, maximo));
    return posiciones;
  }

  private void agregarPosiciones(
    List<MuebleUbicado> posiciones,
    Mueble silla,
    MuebleUbicado mesa,
    Muro lado,
    double maximo,
    double paso
  ) {
    for (
      double avance = 0;
      avance <= maximo + ZonaNoDisponible.TOLERANCIA && posiciones.size() < MAXIMO_POSICIONES;
      avance += paso
    ) {
      posiciones.add(candidato(silla, mesa, lado, avance));
    }
  }

  private MuebleUbicado candidato(Mueble silla, MuebleUbicado mesa, Muro lado, double avance) {
    double posicionX =
      switch (lado) {
        case IZQUIERDO -> mesa.getPosicionX() - silla.getAncho();
        case DERECHO -> mesa.getPosicionX() + mesa.getMueble().getAncho();
        default -> mesa.getPosicionX() + avance;
      };
    double posicionY =
      switch (lado) {
        case SUPERIOR -> mesa.getPosicionY() - silla.getLargo();
        case INFERIOR -> mesa.getPosicionY() + mesa.getMueble().getLargo();
        default -> mesa.getPosicionY() + avance;
      };
    return new MuebleUbicado(silla, posicionX, posicionY);
  }
}
