package com.tallerwebi.dominio;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServicioOrientacionDeMueblesImpl implements ServicioOrientacionDeMuebles {

  @Override
  public Optional<Muro> calcularFrente(MuebleUbicado ubicado, Ambiente ambiente) {
    validarPosicion(ubicado, ambiente);
    Muro apoyo = null;
    double mayorContacto = 0.0;
    for (Muro muro : Muro.values()) {
      if (tocaMuro(ubicado, ambiente, muro)) {
        double contacto = contactoConMuro(ubicado, muro);
        if (apoyo == null || contacto > mayorContacto + ZonaNoDisponible.TOLERANCIA) {
          apoyo = muro;
          mayorContacto = contacto;
        }
      }
    }
    if (apoyo == null) {
      return Optional.empty();
    }
    return Optional.of(muroOpuesto(apoyo));
  }

  private void validarPosicion(MuebleUbicado ubicado, Ambiente ambiente) {
    double posicionX = ubicado.getPosicionX();
    double posicionY = ubicado.getPosicionY();
    double ancho = ubicado.getMueble().getAncho();
    double largo = ubicado.getMueble().getLargo();
    double anchoAmbiente = ambiente.getAncho();
    double largoAmbiente = ambiente.getLargo();
    if (
      !medidaValida(ancho) ||
      !medidaValida(largo) ||
      !medidaValida(anchoAmbiente) ||
      !medidaValida(largoAmbiente) ||
      !posicionValida(posicionX, ancho, anchoAmbiente) ||
      !posicionValida(posicionY, largo, largoAmbiente)
    ) {
      throw new IllegalArgumentException("La ubicacion del mueble no es valida");
    }
  }

  private boolean medidaValida(double medida) {
    return Double.isFinite(medida) && medida > 0;
  }

  private boolean posicionValida(double posicion, double medida, double medidaAmbiente) {
    return (
      Double.isFinite(posicion) &&
      posicion >= -ZonaNoDisponible.TOLERANCIA &&
      posicion + medida <= medidaAmbiente + ZonaNoDisponible.TOLERANCIA
    );
  }

  private boolean tocaMuro(MuebleUbicado ubicado, Ambiente ambiente, Muro muro) {
    double distancia =
      switch (muro) {
        case SUPERIOR -> ubicado.getPosicionY();
        case DERECHO -> ambiente.getAncho() -
        ubicado.getPosicionX() -
        ubicado.getMueble().getAncho();
        case INFERIOR -> ambiente.getLargo() -
        ubicado.getPosicionY() -
        ubicado.getMueble().getLargo();
        case IZQUIERDO -> ubicado.getPosicionX();
      };
    return Math.abs(distancia) <= ZonaNoDisponible.TOLERANCIA;
  }

  private double contactoConMuro(MuebleUbicado ubicado, Muro muro) {
    if (muro == Muro.SUPERIOR || muro == Muro.INFERIOR) {
      return ubicado.getMueble().getAncho();
    }
    return ubicado.getMueble().getLargo();
  }

  private Muro muroOpuesto(Muro muro) {
    return switch (muro) {
      case SUPERIOR -> Muro.INFERIOR;
      case DERECHO -> Muro.IZQUIERDO;
      case INFERIOR -> Muro.SUPERIOR;
      case IZQUIERDO -> Muro.DERECHO;
    };
  }
}
