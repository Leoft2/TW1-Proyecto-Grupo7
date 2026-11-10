package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioRediseño")
public class ServicioRediseñoImpl implements ServicioRediseño {

  private static final String COLOR_BLANCO_ROTO = "Blanco";
  private static final String HEX_BLANCO_ROTO = "ffffff";
  private static final String COLOR_NEUTRO = "Neutro";
  private static final String HEX_NEUTRO = "E5E5E5";

  public ServicioRediseñoImpl() {}

  @Override
  public Recomendacion recomendar(Estilo estilo) {
    return switch (estilo) {
      case Retro -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color("Limonchello", "fadb49"),
        new Color("El Palmar", "567c49"),
        new Color("Talampaya", "b6213f")
      );
      case Minimalista -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color("Blanco", "ffffff"),
        new Color("El Leoncito", "938d81"),
        new Color("Lago Puelo", "47627d")
      );
      case Japandi -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color("Quebrada del Condorito", "dbc861"),
        new Color("Los Arrayanes", "7f532b"),
        new Color("El Palmar", "567c49")
      );
      case Industrial -> crearRecomendacion(
        estilo,
        Iluminacion.FRIA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color("El Leoncito", "938d81"),
        new Color("Lago Puelo", "47627d"),
        new Color("Talampaya", "b6213f")
      );
      case Boho -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color("Talampaya", "b6213f"),
        new Color("Limonchello", "fadb49"),
        new Color("Mar Egeo", "0191b4")
      );
      case Escandinavo -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        Arrays.asList(new Color(COLOR_NEUTRO, HEX_NEUTRO)),
        new Color(COLOR_BLANCO_ROTO, HEX_BLANCO_ROTO),
        new Color("Danubio", "5d79c0"),
        new Color("El Leoncito", "938d81")
      );
    };
  }

  private Recomendacion crearRecomendacion(
    Estilo estilo,
    Iluminacion iluminacion,
    List<Color> coloresSecundarios,
    Color... colores
  ) {
    return new Recomendacion(
      estilo,
      new ArrayList<>(Arrays.asList(colores)),
      coloresSecundarios,
      iluminacion
    );
  }
}
