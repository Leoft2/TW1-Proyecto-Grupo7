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
  private final RepositorioLitros repositorioLitros;

  public ServicioAdministracionBaldesPinturaImpl(
    ServicioScrapingPinturas servicioScraping,
    RepositorioBaldePintura repositorioBaldePintura,
    RepositorioLitros repositorioLitros
  ) {
    this.servicioScraping = servicioScraping;
    this.repositorioBaldePintura = repositorioBaldePintura;
    this.repositorioLitros = repositorioLitros;
  }

  @Override
  @Transactional
  public void actualizarBaldesDesdeColorshop() throws Exception {
    List<BaldePintura> baldes = servicioScraping.scrapearBaldes();
    repositorioBaldePintura.eliminarTodos();
    for (BaldePintura balde : baldes) {
      resolverOpcionesLitros(balde);
      repositorioBaldePintura.guardar(balde);
    }
  }

  private void resolverOpcionesLitros(BaldePintura balde) {
    List<Double> valores = balde.getValoresOpcionesLitros();
    balde.limpiarOpcionesLitros();
    for (Double valor : valores) {
      Litros litros = repositorioLitros.buscarPorValor(valor).orElseGet(() -> crearLitros(valor));
      balde.agregarOpcionLitros(litros);
    }
  }

  private Litros crearLitros(Double valor) {
    Litros nuevo = new Litros(valor);
    repositorioLitros.guardar(nuevo);
    return nuevo;
  }
}
