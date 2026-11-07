package com.tallerwebi.infraestructura;

import java.util.List;

public final class ColoresConocidos {

  private ColoresConocidos() {}

  public record ColorConocido(String hex, String nombre) {}

  /**
   * Colores de interiores de Colorshop con su valor hexadecimal y el nombre tal como aparece en el sitio.
   * La URL de filtro de Colorshop sigue el patron:
   * /pinturas/-{hex}-{nombre-color}?map=category-1,colores
   *
   * Son los colores que devuelve el filtro de interiores; usar estos evita traer Enduido, esmalte, etc.
   * Se mantiene una lista reducida para que el scraping sea rapido (una request HTTP por color).
   */
  public static List<ColorConocido> obtenerColores() {
    return List.of(
      new ColorConocido("0191b4", "Mar Egeo"),
      new ColorConocido("47627d", "Lago Puelo"),
      new ColorConocido("567c49", "El Palmar"),
      new ColorConocido("5d79c0", "Danubio"),
      new ColorConocido("7acf4b", "Pradera"),
      new ColorConocido("7f532b", "Los Arrayanes"),
      new ColorConocido("8364b3", "Uva"),
      new ColorConocido("938d81", "El Leoncito"),
      new ColorConocido("b6213f", "Talampaya"),
      new ColorConocido("da68a4", "Rosa"),
      new ColorConocido("dbc861", "Quebrada del Condorito"),
      new ColorConocido("f32329", "Carmin"),
      new ColorConocido("fadb49", "Limonchello"),
      new ColorConocido("ff8f21", "Mango"),
      new ColorConocido("ffffff", "Blanco")
    );
  }
}
