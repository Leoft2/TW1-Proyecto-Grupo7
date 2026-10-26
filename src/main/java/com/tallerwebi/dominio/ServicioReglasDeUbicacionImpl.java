package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioReglasDeUbicacionImpl implements ServicioReglasDeUbicacion {

  // Valor provisorio hasta que se defina la configuracion del pasillo minimo (FU14)
  private static final double PASILLO_MINIMO = 0.6;
  private static final String CATEGORIA_CAMA = "Cama";

  private final ServicioZonaExclusion servicioZonaExclusion;

  public ServicioReglasDeUbicacionImpl(ServicioZonaExclusion servicioZonaExclusion) {
    this.servicioZonaExclusion = servicioZonaExclusion;
  }

  /**
   * Reglas eliminatorias: la ubicacion se descarta si tapa una puerta o su radio de giro, si choca
   * con un obstaculo fijo o si deja un pasillo menor al minimo. No suman puntaje.
   */
  @Override
  public boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados
  ) {
    return (
      !tapaUnaPuerta(candidato, ambiente) &&
      !tapaUnObstaculo(candidato, ambiente) &&
      !dejaUnPasilloMenorAlMinimo(candidato, ambiente, mueblesUbicados)
    );
  }

  @Override
  public boolean cumpleReglasDeCategoria(MuebleUbicado candidato, Ambiente ambiente) {
    if (!esCama(candidato.getMueble())) {
      return true;
    }
    return estaOpuestaALaVentana(candidato, ambiente);
  }

  @Override
  public String avisoDeCategoria(Mueble mueble) {
    if (mueble.getCategoriaDeMueble() != null) {
      return null;
    }
    return (
      "El mueble " +
      mueble.getNombre() +
      " no tiene categoria. Asignarle una categoria mejora el resultado de la distribucion"
    );
  }

  private boolean tapaUnaPuerta(MuebleUbicado candidato, Ambiente ambiente) {
    for (ZonaExclusion zona : servicioZonaExclusion.calcularZonas(ambiente)) {
      if (zona.getTipo() == TipoAbertura.PUERTA && zona.seSolapaCon(candidato)) {
        return true;
      }
    }
    return false;
  }

  private boolean tapaUnObstaculo(MuebleUbicado candidato, Ambiente ambiente) {
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      if (obstaculo.colisionaCon(candidato, ambiente)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Un mueble pegado a otro mueble o a un muro no deja pasillo. Si queda separado, la separacion
   * es un pasillo y debe alcanzar el minimo.
   */
  private boolean dejaUnPasilloMenorAlMinimo(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados
  ) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();

    if (
      esPasilloEstrecho(izquierda) ||
      esPasilloEstrecho(arriba) ||
      esPasilloEstrecho(ambiente.getAncho() - derecha) ||
      esPasilloEstrecho(ambiente.getLargo() - abajo)
    ) {
      return true;
    }
    for (MuebleUbicado otro : mueblesUbicados) {
      if (dejaUnPasilloEstrechoCon(candidato, otro)) {
        return true;
      }
    }
    return false;
  }

  private boolean dejaUnPasilloEstrechoCon(MuebleUbicado candidato, MuebleUbicado otro) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();
    double otroIzquierda = otro.getPosicionX();
    double otroArriba = otro.getPosicionY();
    double otroDerecha = otroIzquierda + otro.getMueble().getAncho();
    double otroAbajo = otroArriba + otro.getMueble().getLargo();
    if (seSuperponen(izquierda, derecha, otroIzquierda, otroDerecha)) {
      return esPasilloEstrecho(Math.max(otroArriba - abajo, arriba - otroAbajo));
    }
    if (seSuperponen(arriba, abajo, otroArriba, otroAbajo)) {
      return esPasilloEstrecho(Math.max(otroIzquierda - derecha, izquierda - otroDerecha));
    }
    return false;
  }

  private boolean esPasilloEstrecho(double separacion) {
    return (
      separacion > ZonaNoDisponible.TOLERANCIA &&
      separacion < PASILLO_MINIMO - ZonaNoDisponible.TOLERANCIA
    );
  }

  private boolean seSuperponen(double desdeA, double hastaA, double desdeB, double hastaB) {
    return (
      desdeA < hastaB - ZonaNoDisponible.TOLERANCIA && desdeB < hastaA - ZonaNoDisponible.TOLERANCIA
    );
  }

  private boolean esCama(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria != null && categoria.getNombre().startsWith(CATEGORIA_CAMA);
  }

  /**
   * La cama debe apoyarse en el muro opuesto al de una ventana. Si el ambiente no tiene ventanas
   * no hay a que oponerse y la regla se cumple.
   */
  private boolean estaOpuestaALaVentana(MuebleUbicado candidato, Ambiente ambiente) {
    boolean hayVentanas = false;
    for (Abertura abertura : ambiente.getAberturas()) {
      if (abertura.getTipo() == TipoAbertura.VENTANA) {
        hayVentanas = true;
        if (tocaElMuro(candidato, muroOpuestoA(abertura.getMuro()), ambiente)) {
          return true;
        }
      }
    }
    return !hayVentanas;
  }

  private Muro muroOpuestoA(Muro muro) {
    return switch (muro) {
      case SUPERIOR -> Muro.INFERIOR;
      case INFERIOR -> Muro.SUPERIOR;
      case DERECHO -> Muro.IZQUIERDO;
      case IZQUIERDO -> Muro.DERECHO;
    };
  }

  private boolean tocaElMuro(MuebleUbicado candidato, Muro muro, Ambiente ambiente) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();
    return switch (muro) {
      case SUPERIOR -> arriba <= ZonaNoDisponible.TOLERANCIA;
      case INFERIOR -> abajo >= ambiente.getLargo() - ZonaNoDisponible.TOLERANCIA;
      case IZQUIERDO -> izquierda <= ZonaNoDisponible.TOLERANCIA;
      case DERECHO -> derecha >= ambiente.getAncho() - ZonaNoDisponible.TOLERANCIA;
    };
  }
}
