package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Arrays;
import org.springframework.stereotype.Service;

@Service("servicioRediseño")
public class ServicioRediseñoImpl implements ServicioRediseño {

  private static final String COLOR_BLANCO_ROTO = "Blanco roto";
  private static final String HEX_BLANCO_ROTO = "#F2F0EB";

  public ServicioRediseñoImpl() {}

  @Override
  public Recomendacion recomendar(Estilo estilo) {
    return switch (estilo) {
      case Retro -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Mostaza", "#D4A017"),
        new Color("Oliva", "#6B7B3A"),
        new Color("Terracota", "#C1663F")
      );
      case Minimalista -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        new Color("Blanco", "#F5F5F2"),
        new Color("Gris perla", "#C9C9C4"),
        new Color("Carbón", "#2B2B2B")
      );
      case Japandi -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Arena", "#D9CBB8"),
        new Color("Madera clara", "#C8A87C"),
        new Color("Salvia", "#9CAF94")
      );
      case Industrial -> crearRecomendacion(
        estilo,
        Iluminacion.FRIA,
        new Color("Cemento", "#8C8C86"),
        new Color("Hierro", "#33322E"),
        new Color("Ladrillo", "#A0522D")
      );
      case Boho -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Terracota", "#C1663F"),
        new Color("Mostaza arena", "#E0B252"),
        new Color("Turquesa", "#4FA3A5")
      );
      case Escandinavo -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        new Color(COLOR_BLANCO_ROTO, HEX_BLANCO_ROTO),
        new Color("Celeste", "#A8C3D1"),
        new Color("Gris cálido", "#C4B8A8")
      );
    };
  }

  private Recomendacion crearRecomendacion(
    Estilo estilo,
    Iluminacion iluminacion,
    Color... colores
  ) {
    return new Recomendacion(estilo, new ArrayList<>(Arrays.asList(colores)), iluminacion);
  }
}
