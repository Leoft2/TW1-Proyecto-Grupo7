package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaTodasLasParedes implements EstrategiaAplicacionPintura {

  private final boolean porLaMitad;

  public EstrategiaTodasLasParedes(boolean porLaMitad) {
    this.porLaMitad = porLaMitad;
  }

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
        UtilidadesPintura.colorDe(colores, 0, UtilidadesPintura.COLOR_FALLBACK_TODAS_LAS_PAREDES);
    }
    for (int i : new int[] {
      UtilidadesPintura.INDICE_PARED_IZQUIERDA,
      UtilidadesPintura.INDICE_PARED_FONDO,
      UtilidadesPintura.INDICE_PARED_DERECHA,
    }) {
      ctx.hexes[i] = color.getHex();
      double area;
      if (porLaMitad) {
        area = ctx.areas[i] / 2.0;
      } else {
        area = ctx.areas[i];
      }
      UtilidadesPintura.agregarItem(
        recomendacion,
        UtilidadesPintura.nombreSuperficie(i),
        area,
        color.getNombre(),
        color.getHex()
      );
    }
  }
}
