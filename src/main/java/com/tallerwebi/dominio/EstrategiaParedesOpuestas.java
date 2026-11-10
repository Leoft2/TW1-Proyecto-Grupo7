package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaParedesOpuestas implements EstrategiaAplicacionPintura {

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
      color =
        UtilidadesPintura.colorDe(colores, 0, UtilidadesPintura.COLOR_FALLBACK_PAREDES_OPUESTAS);
    }
    ctx.hexes[UtilidadesPintura.INDICE_PARED_IZQUIERDA] = color.getHex();
    ctx.hexes[UtilidadesPintura.INDICE_PARED_DERECHA] = color.getHex();
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.ZONA_PARED_IZQUIERDA,
      ctx.areas[UtilidadesPintura.INDICE_PARED_IZQUIERDA],
      color.getNombre(),
      color.getHex()
    );
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.ZONA_PARED_DERECHA,
      ctx.areas[UtilidadesPintura.INDICE_PARED_DERECHA],
      color.getNombre(),
      color.getHex()
    );
  }
}
