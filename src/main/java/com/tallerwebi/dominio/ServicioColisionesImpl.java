package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ServicioColisionesImpl implements ServicioColisiones {

  /**
   * Hay colision cuando dos muebles apoyados en el piso ocupan el mismo espacio. Esto incluye al
   * mueble que queda dentro de otro y al que tapa por completo a otro. Los muebles pegados no
   * chocan.
   */
  @Override
  public boolean hayColision(MuebleUbicado candidato, List<MuebleUbicado> otrosMuebles) {
    for (MuebleUbicado otro : otrosMuebles) {
      // HU-28 (apoyo, contencion y montaje) agregara aca las excepciones de solapes validos.
      if (ocupanElMismoEspacio(candidato, otro)) {
        return true;
      }
    }
    return false;
  }

  private boolean ocupanElMismoEspacio(MuebleUbicado primero, MuebleUbicado segundo) {
    double primeroDerecha = primero.getPosicionX() + primero.getMueble().getAncho();
    double primeroAbajo = primero.getPosicionY() + primero.getMueble().getLargo();
    double segundoDerecha = segundo.getPosicionX() + segundo.getMueble().getAncho();
    double segundoAbajo = segundo.getPosicionY() + segundo.getMueble().getLargo();
    return (
      primero.getPosicionX() < segundoDerecha - ZonaNoDisponible.TOLERANCIA &&
      segundo.getPosicionX() < primeroDerecha - ZonaNoDisponible.TOLERANCIA &&
      primero.getPosicionY() < segundoAbajo - ZonaNoDisponible.TOLERANCIA &&
      segundo.getPosicionY() < primeroAbajo - ZonaNoDisponible.TOLERANCIA
    );
  }
}
