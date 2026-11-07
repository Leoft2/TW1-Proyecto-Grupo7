package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import java.util.List;
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
  public Optional<List<Double>> obtenerOpcionesLitros(String url) {
    try {
      Document doc = conexion.conectar(url);
      return Optional.of(parser.extraerOpcionesLitros(doc));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
