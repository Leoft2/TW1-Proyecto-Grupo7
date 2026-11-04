package com.tallerwebi.infraestructura;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ColoresConocidos {

  private ColoresConocidos() {}

  /**
   * Colores principales de Colorshop con su valor hexadecimal.
   * La URL de filtro de Colorshop sigue el patron:
   * /pinturas/-{hex}-{nombre-color}?map=category-1,colores
   *
   * Se mantiene una lista reducida para que el scraping sea rapido
   * (una request HTTP por color). Agrega aqui los colores que necesites.
   */
  public static Map<String, String> obtenerColores() {
    Map<String, String> colores = new LinkedHashMap<>();
    colores.put("ffffff", "Blanco");
    colores.put("000000", "Negro");
    colores.put("ff0000", "Rojo");
    colores.put("0000ff", "Azul");
    colores.put("008000", "Verde");
    colores.put("ffff00", "Amarillo");
    colores.put("540000", "Marron");
    return colores;
  }
}
