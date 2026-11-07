package com.tallerwebi.infraestructura;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ParserBaldesColorshop {

  public static final String SELECTOR_TARJETA = ".vtex-product-summary-2-x-container";
  public static final String SELECTOR_PRESENTACION_ITEM =
    "[class*=skuSelectorSubcontainer--presentacion] [class*=skuSelectorItem]";
  private static final String SELECTOR_NOMBRE = "h3";
  private static final String SELECTOR_PRECIO = ".vtex-product-price-1-x-sellingPrice";
  private static final String SELECTOR_ENLACE = "a[href]";

  public static final Pattern PATRON_LITROS = Pattern.compile(
    "(\\d+(?:[.,]\\d+)?)\\s*(?:Lts|Lt|Litros?|L)\\b",
    Pattern.CASE_INSENSITIVE
  );

  public List<BaldeScrapeado> parsear(Document doc) {
    List<Element> tarjetas = doc.select(SELECTOR_TARJETA);
    List<BaldeScrapeado> baldes = new ArrayList<>();

    for (Element tarjeta : tarjetas) {
      String nombre = extraerTexto(tarjeta, SELECTOR_NOMBRE);
      if (nombre == null || nombre.isBlank()) {
        continue;
      }

      String href = extraerHref(tarjeta, SELECTOR_ENLACE);
      if (href == null) {
        continue;
      }

      Double precio = extraerPrecio(tarjeta, SELECTOR_PRECIO);
      String opcionesLitros = extraerOpcionesLitros(tarjeta);
      Double litros = primerLitro(opcionesLitros);

      baldes.add(new BaldeScrapeado(href, nombre, precio, litros, opcionesLitros));
    }

    return baldes;
  }

  private String extraerTexto(Element contenedor, String selector) {
    Element el = contenedor.selectFirst(selector);
    return el != null ? el.text().trim() : null;
  }

  private String extraerHref(Element contenedor, String selector) {
    Element enlace = contenedor.selectFirst(selector);
    if (enlace == null) {
      return null;
    }
    String href = enlace.attr("href");
    if (href == null || href.isBlank()) {
      return null;
    }
    int queryIndex = href.indexOf('?');
    if (queryIndex >= 0) {
      href = href.substring(0, queryIndex);
    }
    return href;
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

  private String extraerOpcionesLitros(Element tarjeta) {
    List<Double> opciones = new ArrayList<>();
    for (Element item : tarjeta.select(SELECTOR_PRESENTACION_ITEM)) {
      agregarValores(extraerValoresLitros(item.text()), opciones);
    }
    if (opciones.isEmpty()) {
      agregarValores(extraerValoresLitros(tarjeta.text()), opciones);
    }
    return formatearOpciones(opciones);
  }

  private void agregarValores(List<Double> origen, List<Double> destino) {
    for (Double valor : origen) {
      if (!destino.contains(valor)) {
        destino.add(valor);
      }
    }
  }

  public static List<Double> extraerValoresLitros(String texto) {
    List<Double> valores = new ArrayList<>();
    if (texto == null) {
      return valores;
    }
    Matcher matcher = PATRON_LITROS.matcher(texto);
    while (matcher.find()) {
      Double valor = parsearValorLitros(matcher.group(1));
      if (valor != null && !valores.contains(valor)) {
        valores.add(valor);
      }
    }
    return valores;
  }

  public static String formatearOpciones(List<Double> opciones) {
    if (opciones == null || opciones.isEmpty()) {
      return null;
    }
    List<Double> ordenadas = new ArrayList<>(opciones);
    ordenadas.sort(Comparator.naturalOrder());
    StringBuilder sb = new StringBuilder();
    for (Double valor : ordenadas) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      if (valor == Math.floor(valor)) {
        sb.append(valor.intValue());
      } else {
        sb.append(valor);
      }
    }
    return sb.toString();
  }

  private static Double parsearValorLitros(String texto) {
    try {
      return Double.parseDouble(texto.replace(",", "."));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Double primerLitro(String opcionesLitros) {
    if (opcionesLitros == null || opcionesLitros.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(opcionesLitros.split(",")[0]);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
