package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.ServicioScrapingPinturas;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioScrapingPinturas")
public class ServicioScrapingPinturasImpl implements ServicioScrapingPinturas {

  private static final String URL_BASE = "https://www.colorshop.com.ar/pinturas";
  private static final String MAP_COLORES = "category-1,colores";
  private static final int MAX_PAGINAS = 5;
  private static final int MAX_PRODUCTOS_POR_COLOR = 5;

  private final ParserBaldesColorshop parser;
  private final ConexionColorshop conexion;

  @Autowired
  public ServicioScrapingPinturasImpl(ParserBaldesColorshop parser, ConexionColorshop conexion) {
    this.parser = parser;
    this.conexion = conexion;
  }

  @Override
  public List<BaldePintura> scrapearBaldes() throws Exception {
    List<BaldePintura> resultado = new ArrayList<>();
    for (ColoresConocidos.ColorConocido color : ColoresConocidos.obtenerColores()) {
      int productosDeEsteColor = 0;
      for (
        int pagina = 1;
        pagina <= MAX_PAGINAS && productosDeEsteColor < MAX_PRODUCTOS_POR_COLOR;
        pagina++
      ) {
        String urlColor = construirUrlFiltro(color, pagina);
        Document doc = conexion.conectar(urlColor);
        List<BaldeScrapeado> productosDelColor = parser.parsear(doc);
        if (productosDelColor.isEmpty()) {
          break;
        }
        for (BaldeScrapeado balde : productosDelColor) {
          if (productosDeEsteColor >= MAX_PRODUCTOS_POR_COLOR) {
            break;
          }
          balde.setColorNombre(color.nombre());
          balde.setHex(color.hex());
          resultado.add(balde.aBaldePintura());
          productosDeEsteColor++;
        }
      }
    }

    if (resultado.isEmpty()) {
      throw new Exception(
        "No se encontraron baldes en Colorshop. Los selectores de la página pueden haber cambiado o el sitio requiere cookies/JavaScript."
      );
    }

    return resultado;
  }

  private String construirUrlFiltro(ColoresConocidos.ColorConocido color, int pagina) {
    String slug =
      color.hex().toLowerCase(Locale.ROOT) +
      "-" +
      color.nombre().toLowerCase(Locale.ROOT).replace(" ", "-");
    return URL_BASE + "/-" + slug + "?map=" + MAP_COLORES + "&page=" + pagina;
  }
}
