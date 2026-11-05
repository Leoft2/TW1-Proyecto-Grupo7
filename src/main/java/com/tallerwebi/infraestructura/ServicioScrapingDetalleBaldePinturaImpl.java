package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DetalleBaldeScrapeado;
import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioScrapingDetalleBaldePintura")
public class ServicioScrapingDetalleBaldePinturaImpl
  implements ServicioScrapingDetalleBaldePintura {

  private final ConexionColorshop conexion;
  private final ParserDetalleBaldeColorshop parser;

  @Autowired
  public ServicioScrapingDetalleBaldePinturaImpl(
    ConexionColorshop conexion,
    ParserDetalleBaldeColorshop parser
  ) {
    this.conexion = conexion;
    this.parser = parser;
  }

  @Override
  public Optional<DetalleBaldeScrapeado> scrapearDetalle(String url) throws Exception {
    Document doc = conexion.conectar(url);
    DetalleBaldeScrapeado detalle = parser.parsear(doc);
    if (detalle == null) {
      return Optional.empty();
    }
    return Optional.of(detalle);
  }
}
