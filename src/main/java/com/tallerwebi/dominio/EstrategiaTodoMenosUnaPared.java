package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaTodoMenosUnaPared implements EstrategiaAplicacionPintura {

  @Override
  public void aplicar(
    RecomendacionPintura recomendacion,
    List<Color> colores,
    Color override,
    ContextoSuperficies ctx
  ) {
    int indice = ctx.indiceParedOpuesta;
    Color colorResto;
    if (override != null) {
      colorResto = override;
    } else {
      colorResto =
        UtilidadesPintura.colorDe(
          colores,
          0,
          UtilidadesPintura.COLOR_FALLBACK_TODO_MENOS_UNA_RESTO
        );
    }
    Color colorDestacada;
    if (override != null) {
      colorDestacada = override;
    } else {
      colorDestacada =
        UtilidadesPintura.colorDe(
          colores,
          3,
          UtilidadesPintura.COLOR_FALLBACK_TODO_MENOS_UNA_DESTACADA
        );
    }
    for (int i = 0; i < 4; i++) {
      if (i == indice) {
        ctx.hexes[i] = colorDestacada.getHex();
        UtilidadesPintura.agregarItem(
          recomendacion,
          UtilidadesPintura.ZONA_PARED_DESTACADA,
          ctx.areas[i],
          colorDestacada.getNombre(),
          colorDestacada.getHex()
        );
      } else {
        ctx.hexes[i] = colorResto.getHex();
        UtilidadesPintura.agregarItem(
          recomendacion,
          UtilidadesPintura.nombreSuperficie(i),
          ctx.areas[i],
          colorResto.getNombre(),
          colorResto.getHex()
        );
      }
    }
  }
}
