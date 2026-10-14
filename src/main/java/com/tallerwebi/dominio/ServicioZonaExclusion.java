package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioZonaExclusion {
  List<ZonaExclusion> calcularZonas(Ambiente ambiente);
}
