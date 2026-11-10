package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioOrientacionFuncional {
  boolean cumpleOrientacion(Plano plano);

  default List<String> desgloseOrientacion(Plano plano) {
    return new ServicioOrientacionFuncionalImpl().desgloseOrientacion(plano);
  }

  default int puntajeMaximo(List<Mueble> muebles) {
    return new ServicioOrientacionFuncionalImpl().puntajeMaximo(muebles);
  }
}
