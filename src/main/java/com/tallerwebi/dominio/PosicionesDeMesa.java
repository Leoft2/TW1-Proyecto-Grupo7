package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

public class PosicionesDeMesa {

  private final RelacionSillaMesa relacion = new RelacionSillaMesa();

  public List<MuebleUbicado> generar(
    Ambiente ambiente,
    List<Mueble> muebles,
    List<MuebleUbicado> ubicados,
    Mueble mesa
  ) {
    List<MuebleUbicado> posiciones = new ArrayList<>();
    posiciones.add(
      new MuebleUbicado(
        mesa,
        (ambiente.getAncho() - mesa.getAncho()) / 2,
        (ambiente.getLargo() - mesa.getLargo()) / 2
      )
    );
    double espacio = espacioEntreMesas(ambiente, muebles);
    for (MuebleUbicado otra : ubicados) {
      if (!relacion.esMesa(otra.getMueble())) {
        continue;
      }
      posiciones.add(
        new MuebleUbicado(
          mesa,
          otra.getPosicionX() - mesa.getAncho() - espacio,
          otra.getPosicionY()
        )
      );
      posiciones.add(
        new MuebleUbicado(
          mesa,
          otra.getPosicionX() + otra.getMueble().getAncho() + espacio,
          otra.getPosicionY()
        )
      );
      posiciones.add(
        new MuebleUbicado(
          mesa,
          otra.getPosicionX(),
          otra.getPosicionY() - mesa.getLargo() - espacio
        )
      );
      posiciones.add(
        new MuebleUbicado(
          mesa,
          otra.getPosicionX(),
          otra.getPosicionY() + otra.getMueble().getLargo() + espacio
        )
      );
    }
    return posiciones;
  }

  private double espacioEntreMesas(Ambiente ambiente, List<Mueble> muebles) {
    double mayorSilla = 0.0;
    for (Mueble mueble : muebles) {
      if (relacion.esSilla(mueble)) {
        mayorSilla = Math.max(mayorSilla, Math.max(mueble.getAncho(), mueble.getLargo()));
      }
    }
    return 2 * mayorSilla + ambiente.getAnchoMinimoDePasillo();
  }
}
