package com.tallerwebi.infraestructura;

import java.io.IOException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class JsoupConexionColorshop implements ConexionColorshop {

  private static final String USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

  private static final int TIMEOUT_MS = 30000;

  @Override
  public Document conectar(String url) throws Exception {
    try {
      return Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT_MS).get();
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
