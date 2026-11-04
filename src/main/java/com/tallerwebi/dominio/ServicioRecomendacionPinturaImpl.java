package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

@Service("servicioRecomendacionPintura")
public class ServicioRecomendacionPinturaImpl implements ServicioRecomendacionPintura {

  private static final String COLOR_BLANCO_ROTO = "Blanco roto";
  private static final String HEX_BLANCO_ROTO = "#F2F0EB";
  private static final String COLOR_SALVIA = "Salvia";
  private static final String HEX_SALVIA = "#9CAF94";
  private static final String COLOR_CELESTE = "Celeste";
  private static final String HEX_CELESTE = "#A8C3D1";
  private static final String COLOR_ARENA = "Arena";
  private static final String HEX_ARENA = "#D9CBB8";
  private static final String COLOR_CARBON = "Carbón";
  private static final String HEX_CARBON = "#2B2B2B";
  private static final String COLOR_TERRACOTA = "Terracota";
  private static final String HEX_TERRACOTA = "#C1663F";

  private static final String ZONA_TECHO = "Techo";
  private static final String ZONA_PARED_IZQUIERDA = "Pared izquierda";
  private static final String ZONA_PARED_FONDO = "Pared de fondo";
  private static final String ZONA_PARED_DERECHA = "Pared derecha";
  private static final String ZONA_TODO_AMBIENTE = "Todo el ambiente";
  private static final String ZONA_PARED_OPUESTA = "Pared opuesta a la puerta";
  private static final String ZONA_PARED_DESTACADA = "Pared a destacar";
  private static final String ZONA_RESTO_AMBIENTE = "Resto del ambiente";

  private static final int INDICE_TECHO = 0;
  private static final int INDICE_PARED_IZQUIERDA = 1;
  private static final int INDICE_PARED_FONDO = 2;
  private static final int INDICE_PARED_DERECHA = 3;

  private final SelectorFormaPintura selector = new SelectorFormaPintura();

  @Override
  public RecomendacionPintura recomendar(Ambiente ambiente) {
    return ambiente == null
      ? new RecomendacionPintura()
      : recomendarPara(AnalisisAmbiente.de(ambiente));
  }

  private RecomendacionPintura recomendarPara(AnalisisAmbiente analisis) {
    FormaPintura forma = selector.seleccionar(analisis);

    RecomendacionPintura recomendacion = new RecomendacionPintura();
    recomendacion.setFormaPintura(forma);
    recomendacion.setDescripcion(forma.getDescripcion());

    aplicarForma(recomendacion, analisis, forma);

    recomendacion.calcularTotal();
    return recomendacion;
  }

  private void aplicarForma(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis,
    FormaPintura forma
  ) {
    if (forma == FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE) {
      recomendarParedOpuesta(recomendacion, analisis);
    } else if (
      forma == FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE
    ) {
      recomendarTodasLasParedes(recomendacion, analisis, false);
    } else if (forma == FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO) {
      recomendarTecho(recomendacion, analisis);
    } else if (forma == FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE) {
      recomendarParedesOpuestas(recomendacion, analisis);
    } else if (forma == FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO) {
      recomendarTechoYParedOpuesta(recomendacion, analisis);
    } else if (
      forma == FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES
    ) {
      recomendarTodasLasParedes(recomendacion, analisis, true);
    } else if (forma == FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR) {
      recomendarTodoDeUnColor(recomendacion, analisis);
    } else if (forma == FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA) {
      recomendarTodoMenosUnaPared(recomendacion, analisis);
    } else {
      recomendarTodoDeUnColor(recomendacion, analisis);
    }
  }

  private void recomendarParedOpuesta(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis
  ) {
    int indice = indiceParedOpuesta(analisis.muroPuerta());
    String[] colores = new String[4];
    colores[indice] = HEX_SALVIA;

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_PARED_OPUESTA,
      areaDePared(analisis, indice),
      COLOR_SALVIA,
      HEX_SALVIA
    );
  }

  private void recomendarTodasLasParedes(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis,
    boolean porLaMitad
  ) {
    String[] colores = new String[4];
    colores[INDICE_PARED_IZQUIERDA] = HEX_ARENA;
    colores[INDICE_PARED_FONDO] = HEX_ARENA;
    colores[INDICE_PARED_DERECHA] = HEX_ARENA;

    double divisor = 1.0;

    if (porLaMitad) {
      divisor = 2.0;
    }

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_PARED_IZQUIERDA,
      analisis.areaParedIzquierda() / divisor,
      COLOR_ARENA,
      HEX_ARENA
    );
    agregarItem(
      recomendacion,
      ZONA_PARED_FONDO,
      analisis.areaParedFondo() / divisor,
      COLOR_ARENA,
      HEX_ARENA
    );
    agregarItem(
      recomendacion,
      ZONA_PARED_DERECHA,
      analisis.areaParedDerecha() / divisor,
      COLOR_ARENA,
      HEX_ARENA
    );
  }

  private void recomendarTecho(RecomendacionPintura recomendacion, AnalisisAmbiente analisis) {
    String[] colores = new String[4];
    colores[INDICE_TECHO] = HEX_CARBON;

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(recomendacion, ZONA_TECHO, analisis.areaTecho(), COLOR_CARBON, HEX_CARBON);
  }

  private void recomendarParedesOpuestas(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis
  ) {
    String[] colores = new String[4];
    colores[INDICE_PARED_IZQUIERDA] = HEX_CELESTE;
    colores[INDICE_PARED_DERECHA] = HEX_CELESTE;

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_PARED_IZQUIERDA,
      analisis.areaParedIzquierda(),
      COLOR_CELESTE,
      HEX_CELESTE
    );
    agregarItem(
      recomendacion,
      ZONA_PARED_DERECHA,
      analisis.areaParedDerecha(),
      COLOR_CELESTE,
      HEX_CELESTE
    );
  }

  private void recomendarTechoYParedOpuesta(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis
  ) {
    String[] colores = new String[4];
    colores[INDICE_TECHO] = HEX_BLANCO_ROTO;
    colores[INDICE_PARED_FONDO] = HEX_SALVIA;

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_TECHO,
      analisis.areaTecho(),
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );
    agregarItem(
      recomendacion,
      ZONA_PARED_FONDO,
      analisis.areaParedFondo(),
      COLOR_SALVIA,
      HEX_SALVIA
    );
  }

  private void recomendarTodoDeUnColor(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis
  ) {
    String[] colores = new String[4];
    for (int i = 0; i < colores.length; i++) {
      colores[i] = HEX_BLANCO_ROTO;
    }

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_TODO_AMBIENTE,
      analisis.areaTotal(),
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );
  }

  private void recomendarTodoMenosUnaPared(
    RecomendacionPintura recomendacion,
    AnalisisAmbiente analisis
  ) {
    int indice = indiceParedOpuesta(analisis.muroPuerta());
    double areaDestacada = areaDePared(analisis, indice);
    String[] colores = new String[4];
    for (int i = 0; i < colores.length; i++) {
      colores[i] = HEX_BLANCO_ROTO;
    }
    colores[indice] = HEX_TERRACOTA;

    recomendacion.setColorSuperficie(colorSuperficie(colores));
    agregarItem(
      recomendacion,
      ZONA_RESTO_AMBIENTE,
      analisis.areaTotal() - areaDestacada,
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );
    agregarItem(recomendacion, ZONA_PARED_DESTACADA, areaDestacada, COLOR_TERRACOTA, HEX_TERRACOTA);
  }

  private int indiceParedOpuesta(Muro muroPuerta) {
    if (muroPuerta == null) {
      return INDICE_PARED_FONDO;
    }

    return switch (muroPuerta) {
      case IZQUIERDO -> INDICE_PARED_DERECHA;
      case DERECHO -> INDICE_PARED_IZQUIERDA;
      case SUPERIOR, INFERIOR -> INDICE_PARED_FONDO;
    };
  }

  private double areaDePared(AnalisisAmbiente analisis, int indice) {
    return switch (indice) {
      case INDICE_PARED_IZQUIERDA, INDICE_PARED_DERECHA -> analisis.areaParedIzquierda();
      case INDICE_PARED_FONDO -> analisis.areaParedFondo();
      default -> 0.0;
    };
  }

  private ColorSuperficie colorSuperficie(String... colores) {
    return new ColorSuperficie(colores[0], colores[1], colores[2], colores[3]);
  }

  private void agregarItem(
    RecomendacionPintura recomendacion,
    String zona,
    double metrosCuadrados,
    String colorNombre,
    String hex
  ) {
    if (metrosCuadrados <= 0) {
      return;
    }

    RecomendacionPintura.ItemPintura item = new RecomendacionPintura.ItemPintura();
    item.setZona(zona);
    item.setColorNombre(colorNombre);
    item.setHex(hex);
    item.setMetrosCuadrados(redondear(metrosCuadrados));
    recomendacion.agregarItem(item);
  }

  private double redondear(double valor) {
    return Math.round(valor * 100.0) / 100.0;
  }
}
