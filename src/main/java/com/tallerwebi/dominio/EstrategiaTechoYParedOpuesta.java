package com.tallerwebi.dominio;

import java.util.List;

public class EstrategiaTechoYParedOpuesta implements EstrategiaAplicacionPintura {

  @Override
  public void aplicar(
    RecomendacionPintura recomendacion,
    List<Color> colores,
    Color override,
    ContextoSuperficies ctx
  ) {
    int indicePared = ctx.indiceParedOpuesta;
    Color colorTecho;
    if (override != null) {
      colorTecho = override;
    } else {
      colorTecho =
        UtilidadesPintura.colorDe(colores, 0, UtilidadesPintura.COLOR_FALLBACK_TECHO_Y_PARED_TECHO);
    }
    Color colorPared = colorTecho;
    ctx.hexes[UtilidadesPintura.INDICE_TECHO] = colorTecho.getHex();
    ctx.hexes[indicePared] = colorPared.getHex();
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.ZONA_TECHO,
      ctx.areas[UtilidadesPintura.INDICE_TECHO],
      colorTecho.getNombre(),
      colorTecho.getHex()
    );
    UtilidadesPintura.agregarItem(
      recomendacion,
      UtilidadesPintura.nombreSuperficie(indicePared),
      ctx.areas[indicePared],
      colorPared.getNombre(),
      colorPared.getHex()
    );
  }
}
