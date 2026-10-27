package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioCirculacion {
  List<TramoEstrecho> calcularTramosEstrechos(Plano plano);
}
