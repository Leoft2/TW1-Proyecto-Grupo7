package com.tallerwebi.dominio;

import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface ServicioScrapingDetalleBaldePintura {
  Optional<List<Double>> obtenerOpcionesLitros(String url) throws Exception;
}
