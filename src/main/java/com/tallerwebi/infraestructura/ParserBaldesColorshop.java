package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ParserBaldesColorshop {

  public static final String SELECTOR_TARJETA = ".vtex-product-summary-2-x-container";
  private static final String SELECTOR_NOMBRE = "h3";
  private static final String SELECTOR_PRECIO = ".vtex-product-price-1-x-sellingPrice";

  private static final Pattern PATRON_LITROS = Pattern.compile("(\\d+)\\s*[Ll](?:itros)?\\b");

  public List<BaldePintura> parsear(Document doc) {
    List<Element> tarjetas = doc.select(SELECTOR_TARJETA);
    List<BaldePintura> baldes = new ArrayList<>();

    for (Element tarjeta : tarjetas) {
      String nombre = extraerTexto(tarjeta, SELECTOR_NOMBRE);
      if (nombre == null || nombre.isBlank()) {
        continue;
      }

      Double precio = extraerPrecio(tarjeta, SELECTOR_PRECIO);
      Double litros = extraerLitrosDelNombre(nombre);

      baldes.add(new BaldePintura(nombre, null, null, litros, precio));
    }

    return baldes;
  }

  private String extraerTexto(Element contenedor, String selector) {
    Element el = contenedor.selectFirst(selector);
    return el != null ? el.text().trim() : null;
  }

  private Double extraerPrecio(Element contenedor, String selector) {
    Element el = contenedor.selectFirst(selector);
    if (el == null) {
      return null;
    }
    String texto = el.text().trim();
    if (texto.isBlank()) {
      return null;
    }
    return parsearPrecioARS(texto);
  }

  private Double parsearPrecioARS(String texto) {
    String limpio = texto
      .replace("$", "")
      .replace("ARS", "")
      .replace(" ", "")
      .replace(".", "")
      .replace(",", ".");
    try {
      return Double.parseDouble(limpio);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Double extraerLitrosDelNombre(String nombre) {
    Matcher matcher = PATRON_LITROS.matcher(nombre);
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
