package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioColisiones {
  boolean hayColision(MuebleUbicado candidato, List<MuebleUbicado> otrosMuebles);
}
