package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaTecho implements EstrategiaAplicacionPintura {

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
      color = UtilidadesPintura.colorDe(colores, 1, UtilidadesPintura.COLOR_FALLBACK_TECHO);
    }
    ctx.hexes[UtilidadesPintura.INDICE_TECHO] = color.getHex();
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.ZONA_TECHO,
      ctx.areas[UtilidadesPintura.INDICE_TECHO],
      color.getNombre(),
      color.getHex()
    );
  }
}
