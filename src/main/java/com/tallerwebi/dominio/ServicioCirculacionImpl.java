package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioCirculacionImpl implements ServicioCirculacion {

  /**
   * Recorre los espacios libres entre cada mueble y los muros, y entre cada par de muebles. Un
   * espacio mayor a cero pero menor al ancho minimo del ambiente es un tramo que no cumple. Los
   * muebles pegados a un muro o entre si no dejan pasillo, por eso no se marcan.
   */
  @Override
  public List<TramoEstrecho> calcularTramosEstrechos(Plano plano) {
    List<TramoEstrecho> tramos = new ArrayList<>();
    Ambiente ambiente = plano.getAmbiente();
    List<MuebleUbicado> muebles = plano.getMuebles();
    if (muebles == null) {
      return tramos;
    }
    for (int i = 0; i < muebles.size(); i++) {
      agregarTramosConLosMuros(tramos, muebles.get(i), ambiente);
      for (int j = i + 1; j < muebles.size(); j++) {
        agregarTramoEntre(tramos, muebles.get(i), muebles.get(j), ambiente);
      }
    }
    return tramos;
  }

  private void agregarTramosConLosMuros(
    List<TramoEstrecho> tramos,
    MuebleUbicado ubicado,
    Ambiente ambiente
  ) {
    double izquierda = ubicado.getPosicionX();
    double arriba = ubicado.getPosicionY();
    double ancho = ubicado.getMueble().getAncho();
    double largo = ubicado.getMueble().getLargo();
    double minimo = ambiente.getAnchoMinimoDePasillo();
    agregarSiEsEstrecho(tramos, minimo, izquierda, 0, arriba, izquierda, largo);
    agregarSiEsEstrecho(tramos, minimo, arriba, izquierda, 0, ancho, arriba);
    double derecha = ambiente.getAncho() - (izquierda + ancho);
    agregarSiEsEstrecho(tramos, minimo, derecha, izquierda + ancho, arriba, derecha, largo);
    double abajo = ambiente.getLargo() - (arriba + largo);
    agregarSiEsEstrecho(tramos, minimo, abajo, izquierda, arriba + largo, ancho, abajo);
  }

  private void agregarTramoEntre(
    List<TramoEstrecho> tramos,
    MuebleUbicado uno,
    MuebleUbicado otro,
    Ambiente ambiente
  ) {
    double unoDerecha = uno.getPosicionX() + uno.getMueble().getAncho();
    double unoAbajo = uno.getPosicionY() + uno.getMueble().getLargo();
    double otroDerecha = otro.getPosicionX() + otro.getMueble().getAncho();
    double otroAbajo = otro.getPosicionY() + otro.getMueble().getLargo();
    double minimo = ambiente.getAnchoMinimoDePasillo();

    double solapeEnX =
      Math.min(unoDerecha, otroDerecha) - Math.max(uno.getPosicionX(), otro.getPosicionX());
    double solapeEnY =
      Math.min(unoAbajo, otroAbajo) - Math.max(uno.getPosicionY(), otro.getPosicionY());
    if (solapeEnX > ZonaNoDisponible.TOLERANCIA) {
      double separacion = Math.max(otro.getPosicionY() - unoAbajo, uno.getPosicionY() - otroAbajo);
      double inicioEnY = otro.getPosicionY() >= unoAbajo ? unoAbajo : otroAbajo;
      double inicioEnX = Math.max(uno.getPosicionX(), otro.getPosicionX());
      agregarSiEsEstrecho(tramos, minimo, separacion, inicioEnX, inicioEnY, solapeEnX, separacion);
    } else if (solapeEnY > ZonaNoDisponible.TOLERANCIA) {
      double separacion = Math.max(
        otro.getPosicionX() - unoDerecha,
        uno.getPosicionX() - otroDerecha
      );
      double inicioEnX = otro.getPosicionX() >= unoDerecha ? unoDerecha : otroDerecha;
      double inicioEnY = Math.max(uno.getPosicionY(), otro.getPosicionY());
      agregarSiEsEstrecho(tramos, minimo, separacion, inicioEnX, inicioEnY, separacion, solapeEnY);
    }
  }

  private void agregarSiEsEstrecho(
    List<TramoEstrecho> tramos,
    double minimo,
    double separacion,
    double desdeX,
    double desdeY,
    double ancho,
    double largo
  ) {
    if (
      separacion > ZonaNoDisponible.TOLERANCIA && separacion < minimo - ZonaNoDisponible.TOLERANCIA
    ) {
      tramos.add(new TramoEstrecho(desdeX, desdeY, ancho, largo, separacion));
    }
  }
}
