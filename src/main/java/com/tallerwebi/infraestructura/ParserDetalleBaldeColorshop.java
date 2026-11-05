package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DetalleBaldeScrapeado;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ParserDetalleBaldeColorshop {

  private static final String SELECTOR_NOMBRE = "h1";
  private static final Pattern PATRON_LITROS = Pattern.compile("(\\d+)\\s*[Ll](?:itros)?\\b");

  public DetalleBaldeScrapeado parsear(Document doc) {
    String nombre = extraerNombre(doc);
    Double litros = extraerLitros(doc);
    return new DetalleBaldeScrapeado(nombre, litros);
  }

  private String extraerNombre(Document doc) {
    Element el = doc.selectFirst(SELECTOR_NOMBRE);
    if (el == null) {
      return null;
    }
    return el.text().trim();
  }

  private Double extraerLitros(Document doc) {
    String texto = doc.text();
    Matcher matcher = PATRON_LITROS.matcher(texto);
    if (matcher.find()) {
      try {
        return Double.parseDouble(matcher.group(1));
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
