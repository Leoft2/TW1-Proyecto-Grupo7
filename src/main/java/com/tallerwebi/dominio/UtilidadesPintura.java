package com.tallerwebi.dominio;

import java.util.List;

public final class UtilidadesPintura {

  public static final String ZONA_TECHO = "Techo";
  public static final String ZONA_PARED_IZQUIERDA = "Pared izquierda";
  public static final String ZONA_PARED_FONDO = "Pared de fondo";
  public static final String ZONA_PARED_DERECHA = "Pared derecha";
  public static final String ZONA_PARED_OPUESTA = "Pared opuesta a la puerta";
  public static final String ZONA_PARED_DESTACADA = "Pared a destacar";

  public static final int INDICE_TECHO = 0;
  public static final int INDICE_PARED_IZQUIERDA = 1;
  public static final int INDICE_PARED_FONDO = 2;
  public static final int INDICE_PARED_DERECHA = 3;

  public static final String NOMBRE_NEUTRO = "Tono neutro";
  public static final String HEX_NEUTRO_FALLBACK = "E5E5E5";

  public static final Color COLOR_FALLBACK_PARED_OPUESTA = new Color("Salvia", "567c49");
  public static final Color COLOR_FALLBACK_TODAS_LAS_PAREDES = new Color("Arena", "dbc861");
  public static final Color COLOR_FALLBACK_TECHO = new Color("Carbón", "47627d");
  public static final Color COLOR_FALLBACK_PAREDES_OPUESTAS = new Color("Celeste", "5d79c0");
  public static final Color COLOR_FALLBACK_TECHO_Y_PARED_TECHO = new Color("Blanco roto", "ffffff");
  public static final Color COLOR_FALLBACK_TECHO_Y_PARED_PARED = new Color("Salvia", "567c49");
  public static final Color COLOR_FALLBACK_TODO_UN_COLOR = new Color("Blanco roto", "ffffff");
  public static final Color COLOR_FALLBACK_TODO_MENOS_UNA_RESTO = new Color(
    "Blanco roto",
    "ffffff"
  );
  public static final Color COLOR_FALLBACK_TODO_MENOS_UNA_DESTACADA = new Color(
    "Terracota",
    "b6213f"
  );

  private static final String[] NOMBRES_SUPERFICIE = {
    ZONA_TECHO,
    ZONA_PARED_IZQUIERDA,
    ZONA_PARED_FONDO,
    ZONA_PARED_DERECHA,
  };

  private UtilidadesPintura() {}

  public static Color colorDe(List<Color> colores, int indice, Color fallback) {
    if (colores != null && !colores.isEmpty() && indice >= 0 && indice < colores.size()) {
      return colores.get(indice);
    }
    return fallback;
  }

  public static double redondear(double valor) {
    return Math.round(valor * 100.0) / 100.0;
  }

  public static void agregarItem(
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

  public static String nombreSuperficie(int indice) {
    if (indice >= 0 && indice < NOMBRES_SUPERFICIE.length) {
      return NOMBRES_SUPERFICIE[indice];
    }
    return ZONA_TECHO;
  }
}
