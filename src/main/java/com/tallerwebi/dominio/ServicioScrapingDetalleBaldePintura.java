package com.tallerwebi.dominio;

import java.util.Optional;

@FunctionalInterface
public interface ServicioScrapingDetalleBaldePintura {
  Optional<DetalleBaldeScrapeado> scrapearDetalle(String url) throws Exception;
}
