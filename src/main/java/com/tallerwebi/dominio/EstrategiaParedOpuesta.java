package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaParedOpuesta implements EstrategiaAplicacionPintura {

  @Override
  public void aplicar(
    RecomendacionPintura recomendacion,
    List<Color> colores,
    Color override,
    ContextoSuperficies ctx
  ) {
    int indice = ctx.indiceParedOpuesta;
    Color color;
    if (override != null) {
      color = override;
    } else {
      color = UtilidadesPintura.colorDe(colores, 1, UtilidadesPintura.COLOR_FALLBACK_PARED_OPUESTA);
    }
    ctx.hexes[indice] = color.getHex();
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.ZONA_PARED_OPUESTA,
      ctx.areas[indice],
      color.getNombre(),
      color.getHex()
    );
  }
}
