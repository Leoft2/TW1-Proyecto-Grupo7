package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioRecomendacionPintura {
  RecomendacionPintura recomendar(Ambiente ambiente, Estilo estilo);
  RecomendacionPintura recomendar(Ambiente ambiente, Estilo estilo, Integer colorOverrideIndex);
  List<Color> coloresOrdenadosPorPrecio(Estilo estilo);
}
