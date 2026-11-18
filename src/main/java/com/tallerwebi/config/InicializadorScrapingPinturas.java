package com.tallerwebi.config;

import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class InicializadorScrapingPinturas implements ApplicationListener<ContextRefreshedEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InicializadorScrapingPinturas.class);

  private final ServicioAdministracionBaldesPintura servicioAdministracion;

  public InicializadorScrapingPinturas(ServicioAdministracionBaldesPintura servicioAdministracion) {
    this.servicioAdministracion = servicioAdministracion;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    try {
      servicioAdministracion.actualizarBaldesDesdeColorshop();
      LOGGER.info("Scraping de pinturas ejecutado al iniciar el proyecto");
    } catch (Exception e) {
      LOGGER.error("Error ejecutando scraping de pinturas al iniciar el proyecto", e);
    }
  }
}
