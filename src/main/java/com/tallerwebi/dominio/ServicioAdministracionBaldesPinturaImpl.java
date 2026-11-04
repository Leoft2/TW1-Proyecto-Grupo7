package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioAdministracionBaldesPintura")
@Transactional
public class ServicioAdministracionBaldesPinturaImpl
  implements ServicioAdministracionBaldesPintura {

  private final ServicioScrapingPinturas servicioScraping;
  private final RepositorioBaldePintura repositorioBaldePintura;

  public ServicioAdministracionBaldesPinturaImpl(
    ServicioScrapingPinturas servicioScraping,
    RepositorioBaldePintura repositorioBaldePintura
  ) {
    this.servicioScraping = servicioScraping;
    this.repositorioBaldePintura = repositorioBaldePintura;
  }

  @Override
  @Transactional
  public void actualizarBaldesDesdeColorshop() throws Exception {
    List<BaldePintura> baldes = servicioScraping.scrapearBaldes();
    repositorioBaldePintura.eliminarTodos();
    for (BaldePintura balde : baldes) {
      repositorioBaldePintura.guardar(balde);
    }
  }
}
