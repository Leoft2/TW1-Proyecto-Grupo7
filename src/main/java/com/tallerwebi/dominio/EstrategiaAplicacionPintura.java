package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface EstrategiaAplicacionPintura {
  void aplicar(
    RecomendacionPintura recomendacion,
    List<Color> colores,
    Color override,
    ContextoSuperficies ctx
  );
}
