package com.tallerwebi.infraestructura;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ParserDetalleBaldeColorshop {

  private static final String SELECTOR_ENLACE_SKU = "a[href*='skuId=']";
  private static final Pattern PATRON_SKU = Pattern.compile("[?&]skuId=(\\d+)");

  public List<String> extraerSkuIds(Document doc) {
    List<String> skuIds = new ArrayList<>();
    for (Element enlace : doc.select(SELECTOR_ENLACE_SKU)) {
      String href = enlace.attr("href");
      Matcher matcher = PATRON_SKU.matcher(href);
      if (matcher.find()) {
        String skuId = matcher.group(1);
        if (!skuIds.contains(skuId)) {
          skuIds.add(skuId);
        }
      }
    }
    return skuIds;
  }

  public List<Double> extraerOpcionesLitros(Document doc) {
    List<Double> opciones = new ArrayList<>();
    for (Element item : doc.select(ParserBaldesColorshop.SELECTOR_PRESENTACION_ITEM)) {
      for (Double valor : ParserBaldesColorshop.extraerValoresLitros(item.text())) {
        if (!opciones.contains(valor)) {
          opciones.add(valor);
        }
      }
    }
    if (opciones.isEmpty()) {
      for (Double valor : ParserBaldesColorshop.extraerValoresLitros(doc.text())) {
        if (!opciones.contains(valor)) {
          opciones.add(valor);
        }
      }
    }
    opciones.sort(Comparator.naturalOrder());
    return opciones;
  }

  public Optional<Double> extraerLitros(Document doc) {
    List<Double> opciones = extraerOpcionesLitros(doc);
    if (opciones.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(opciones.get(0));
  }
}
