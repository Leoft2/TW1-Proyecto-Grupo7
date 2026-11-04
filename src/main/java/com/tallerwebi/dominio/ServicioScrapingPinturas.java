package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioScrapingPinturas {
  List<BaldePintura> scrapearBaldes() throws Exception;
}
