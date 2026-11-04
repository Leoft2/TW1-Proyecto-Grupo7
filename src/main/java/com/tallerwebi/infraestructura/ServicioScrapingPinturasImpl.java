package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.ServicioScrapingPinturas;
import java.io.IOException;
import java.util.List;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service("servicioScrapingPinturas")
public class ServicioScrapingPinturasImpl implements ServicioScrapingPinturas {

  private static final String URL_PINTURAS = "https://www.colorshop.com.ar/pinturas";

  private final ParserBaldesColorshop parser = new ParserBaldesColorshop();

  @Override
  public List<BaldePintura> scrapearBaldes() throws Exception {
    Document doc = conectarConColorshop();
    String titulo = doc.title();
    int tarjetas = contarTarjetas(doc);

    List<BaldePintura> baldes = parser.parsear(doc);
    if (baldes.isEmpty()) {
      throw new Exception(
        "No se encontraron baldes en Colorshop (título='" +
        titulo +
        "', tarjetas='" +
        tarjetas +
        "'). Los selectores de la página pueden haber cambiado o el sitio requiere cookies/JavaScript."
      );
    }

    return baldes;
  }

  private Document conectarConColorshop() throws Exception {
    try {
      return Jsoup
        .connect(URL_PINTURAS)
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
      throw new Exception(
        "No se pudo conectar con Colorshop (" + URL_PINTURAS + "): " + e.getMessage(),
        e
      );
    }
  }

  private int contarTarjetas(Document doc) {
    List<Element> tarjetas = doc.select(ParserBaldesColorshop.SELECTOR_TARJETA);
    return tarjetas != null ? tarjetas.size() : 0;
  }
}
