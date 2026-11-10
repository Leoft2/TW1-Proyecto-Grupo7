package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaTodoUnColor implements EstrategiaAplicacionPintura {

  @Override
  public void aplicar(
    RecomendacionPintura recomendacion,
    List<Color> colores,
    Color override,
    ContextoSuperficies ctx
  ) {
    Color color;
    if (override != null) {
      color = override;
    } else {
      color = UtilidadesPintura.colorDe(colores, 0, UtilidadesPintura.COLOR_FALLBACK_TODO_UN_COLOR);
    }
    for (int i = 0; i < 4; i++) {
      ctx.hexes[i] = color.getHex();
      UtilidadesPintura.agregarItem(
        recomendacion,
        UtilidadesPintura.nombreSuperficie(i),
        ctx.areas[i],
        color.getNombre(),
        color.getHex()
      );
    }
  }
}
