package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.ServicioScrapingPinturas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service("servicioScrapingPinturas")
public class ServicioScrapingPinturasImpl implements ServicioScrapingPinturas {

  private static final String URL_BASE = "https://www.colorshop.com.ar/pinturas";
  private static final String MAP_COLORES = "category-1,colores";
  private static final int MAX_PAGINAS = 5;

  private final ParserBaldesColorshop parser = new ParserBaldesColorshop();

  @Override
  public List<BaldePintura> scrapearBaldes() throws Exception {
    List<BaldeScrapeado> baldesBase = scrapearPaginas(URL_BASE + "?map=" + MAP_COLORES);

    Map<String, String> productosPorColor = new HashMap<>();
    Map<String, String> productosPorHex = new HashMap<>();
    for (Map.Entry<String, String> color : ColoresConocidos.obtenerColores().entrySet()) {
      String hex = color.getKey();
      String nombreColor = color.getValue();
      List<BaldeScrapeado> productosDelColor = scrapearPaginasPorColor(hex, nombreColor);
      for (BaldeScrapeado balde : productosDelColor) {
        productosPorColor.put(balde.getHref(), nombreColor);
        productosPorHex.put(balde.getHref(), hex);
      }
    }

    return construirResultado(baldesBase, productosPorColor, productosPorHex);
  }

  private List<BaldePintura> construirResultado(
    List<BaldeScrapeado> baldesBase,
    Map<String, String> productosPorColor,
    Map<String, String> productosPorHex
  ) throws Exception {
    List<BaldePintura> resultado = new ArrayList<>();
    for (BaldeScrapeado balde : baldesBase) {
      balde.setColorNombre(productosPorColor.get(balde.getHref()));
      balde.setHex(productosPorHex.get(balde.getHref()));
      resultado.add(balde.aBaldePintura());
    }

    if (resultado.isEmpty()) {
      throw new Exception(
        "No se encontraron baldes en Colorshop. Los selectores de la página pueden haber cambiado o el sitio requiere cookies/JavaScript."
      );
    }

    return resultado;
  }

  private List<BaldeScrapeado> scrapearPaginas(String urlBase) throws Exception {
    List<BaldeScrapeado> todos = new ArrayList<>();
    for (int pagina = 1; pagina <= MAX_PAGINAS; pagina++) {
      String urlPagina = urlBase + "&page=" + pagina;
      Document doc = conectarConColorshop(urlPagina);
      List<BaldeScrapeado> productosPagina = parser.parsear(doc);
      if (productosPagina.isEmpty()) {
        break;
      }
      todos.addAll(productosPagina);
    }
    return todos;
  }

  private List<BaldeScrapeado> scrapearPaginasPorColor(String hex, String nombreColor)
    throws Exception {
    List<BaldeScrapeado> todos = new ArrayList<>();
    for (int pagina = 1; pagina <= MAX_PAGINAS; pagina++) {
      String urlColor = construirUrlFiltro(hex, nombreColor, pagina);
      Document doc = conectarConColorshop(urlColor);
      List<BaldeScrapeado> productosDelColor = parser.parsear(doc);
      if (productosDelColor.isEmpty()) {
        break;
      }
      todos.addAll(productosDelColor);
    }
    return todos;
  }

  private String construirUrlFiltro(String hex, String nombreColor, int pagina) {
    String slug =
      hex.toLowerCase(Locale.ROOT) + "-" + nombreColor.toLowerCase(Locale.ROOT).replace(" ", "-");
    return URL_BASE + "/-" + slug + "?map=" + MAP_COLORES + "&page=" + pagina;
  }

  private Document conectarConColorshop(String url) throws Exception {
    try {
      return Jsoup
        .connect(url)
        .userAgent(
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
          "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"
        )
        .timeout(30000)
        .get();
    } catch (HttpStatusException e) {
      throw new Exception(
        "Colorshop respondió con error HTTP " + e.getStatusCode() + ": " + e.getUrl(),
        e
      );
    } catch (UnsupportedMimeTypeException e) {
      throw new Exception("Colorshop devolvió un tipo de contenido no soportado: " + e.getUrl(), e);
    } catch (IOException e) {
      throw new Exception("No se pudo conectar con Colorshop (" + url + "): " + e.getMessage(), e);
    }
  }
}
