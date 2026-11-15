package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RelacionSillaMesa {

  public static final double DISTANCIA_MAXIMA = 0.30;

  public boolean esSilla(Mueble mueble) {
    return categoria(mueble).startsWith("Silla");
  }

  public boolean esMesa(Mueble mueble) {
    return categoria(mueble).startsWith("Mesa de comedor");
  }

  public boolean hayMesa(List<MuebleUbicado> muebles) {
    for (MuebleUbicado ubicado : muebles) {
      if (esMesa(ubicado.getMueble())) {
        return true;
      }
    }
    return false;
  }

  public boolean haySillaYMesa(List<Mueble> muebles) {
    boolean silla = false;
    boolean mesa = false;
    for (Mueble mueble : muebles) {
      silla |= esSilla(mueble);
      mesa |= esMesa(mueble);
    }
    return silla && mesa;
  }

  public Optional<Muro> frente(MuebleUbicado silla, List<MuebleUbicado> muebles) {
    MuebleUbicado mesa = asignaciones(muebles).get(silla);
    if (mesa == null) {
      return Optional.empty();
    }
    return frenteSiLibre(silla, mesa, muebles);
  }

  public Map<MuebleUbicado, MuebleUbicado> asignaciones(List<MuebleUbicado> muebles) {
    return new AsignacionSillas(this).asignar(muebles);
  }

  public boolean tieneMesaCercana(MuebleUbicado silla, List<MuebleUbicado> muebles) {
    for (MuebleUbicado mesa : muebles) {
      if (esMesa(mesa.getMueble()) && frenteSiLibre(silla, mesa, muebles).isPresent()) {
        return true;
      }
    }
    return false;
  }

  Optional<Muro> frenteSiLibre(
    MuebleUbicado silla,
    MuebleUbicado mesa,
    List<MuebleUbicado> muebles
  ) {
    Optional<Muro> direccion = frente(silla, mesa);
    if (direccion.isPresent() && visualLibre(silla, mesa, direccion.get(), muebles)) {
      return direccion;
    }
    return Optional.empty();
  }

  private boolean visualLibre(
    MuebleUbicado silla,
    MuebleUbicado mesa,
    Muro frente,
    List<MuebleUbicado> muebles
  ) {
    boolean vertical = frente == Muro.SUPERIOR || frente == Muro.INFERIOR;
    double izquierda = Math.max(silla.getPosicionX(), mesa.getPosicionX());
    double arriba = Math.max(silla.getPosicionY(), mesa.getPosicionY());
    double derecha = Math.min(
      silla.getPosicionX() + silla.getMueble().getAncho(),
      mesa.getPosicionX() + mesa.getMueble().getAncho()
    );
    double abajo = Math.min(
      silla.getPosicionY() + silla.getMueble().getLargo(),
      mesa.getPosicionY() + mesa.getMueble().getLargo()
    );
    double distancia = vertical ? arriba - abajo : izquierda - derecha;
    if (distancia <= ZonaNoDisponible.TOLERANCIA) {
      return true;
    }
    ZonaNoDisponible espacio = vertical
      ? new ZonaNoDisponible(izquierda, abajo, derecha - izquierda, distancia)
      : new ZonaNoDisponible(derecha, arriba, distancia, abajo - arriba);
    for (MuebleUbicado otro : muebles) {
      if (!otro.equals(silla) && !otro.equals(mesa) && espacio.seSolapaCon(otro)) {
        return false;
      }
    }
    return true;
  }

  public Optional<Muro> frente(MuebleUbicado silla, MuebleUbicado mesa) {
    double sillaIzquierda = silla.getPosicionX();
    double sillaDerecha = sillaIzquierda + silla.getMueble().getAncho();
    double sillaArriba = silla.getPosicionY();
    double sillaAbajo = sillaArriba + silla.getMueble().getLargo();
    double mesaIzquierda = mesa.getPosicionX();
    double mesaDerecha = mesaIzquierda + mesa.getMueble().getAncho();
    double mesaArriba = mesa.getPosicionY();
    double mesaAbajo = mesaArriba + mesa.getMueble().getLargo();
    if (cruzan(sillaIzquierda, sillaDerecha, mesaIzquierda, mesaDerecha)) {
      if (distanciaValida(mesaArriba - sillaAbajo)) {
        return Optional.of(Muro.INFERIOR);
      }
      if (distanciaValida(sillaArriba - mesaAbajo)) {
        return Optional.of(Muro.SUPERIOR);
      }
    }
    if (cruzan(sillaArriba, sillaAbajo, mesaArriba, mesaAbajo)) {
      if (distanciaValida(mesaIzquierda - sillaDerecha)) {
        return Optional.of(Muro.DERECHO);
      }
      if (distanciaValida(sillaIzquierda - mesaDerecha)) {
        return Optional.of(Muro.IZQUIERDO);
      }
    }
    return Optional.empty();
  }

  public boolean formanPareja(MuebleUbicado primero, MuebleUbicado segundo) {
    if (esSilla(primero.getMueble()) && esMesa(segundo.getMueble())) {
      return frente(primero, segundo).isPresent();
    }
    if (esMesa(primero.getMueble()) && esSilla(segundo.getMueble())) {
      return frente(segundo, primero).isPresent();
    }
    return false;
  }

  private boolean distanciaValida(double distancia) {
    return (
      distancia >= -ZonaNoDisponible.TOLERANCIA &&
      distancia <= DISTANCIA_MAXIMA + ZonaNoDisponible.TOLERANCIA
    );
  }

  private boolean cruzan(double inicio, double fin, double otroInicio, double otroFin) {
    return (
      inicio < otroFin - ZonaNoDisponible.TOLERANCIA &&
      otroInicio < fin - ZonaNoDisponible.TOLERANCIA
    );
  }

  private String categoria(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria == null || categoria.getNombre() == null ? "" : categoria.getNombre();
  }
}
