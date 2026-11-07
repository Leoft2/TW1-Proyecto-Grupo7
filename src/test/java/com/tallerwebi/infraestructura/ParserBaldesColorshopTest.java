package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ParserBaldesColorshopTest {

  private static final String HTML_CON_SELECTOR_PRESENTACION =
    "" +
    "<html><body>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/recuplast-interior-colores/p'></a>" +
    "  <h3><span>Recuplast Interior Colores</span></h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>$ 17.193,60</span>" +
    "  <div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>1 Lt.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
    "  </div>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/hidroesmalte-epoxi/p'></a>" +
    "  <h3><span>Hidroesmalte Epoxi</span></h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>10.000,00</span>" +
    "  <div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
    "    <div class='vtex-store-components-3-x-skuSelectorItem'>20 L</div>" +
    "  </div>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/sin-litros/p'></a>" +
    "  <h3><span>Pintura Sin Medida</span></h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>5.000,00</span>" +
    "</div>" +
    "</body></html>";

  @Test
  public void deberiaParsearOpcionesDeLitrosDelSelectorDePresentacion() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_CON_SELECTOR_PRESENTACION);

    List<BaldeScrapeado> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(3));

    BaldeScrapeado primero = baldes.get(0);
    assertThat(primero.getNombre(), is("Recuplast Interior Colores"));
    assertThat(primero.getOpcionesLitros(), is("1,4,10"));
    assertThat(primero.getLitros(), equalTo(1.0));

    BaldeScrapeado segundo = baldes.get(1);
    assertThat(segundo.getNombre(), is("Hidroesmalte Epoxi"));
    assertThat(segundo.getOpcionesLitros(), is("20"));
    assertThat(segundo.getLitros(), equalTo(20.0));

    BaldeScrapeado tercero = baldes.get(2);
    assertThat(tercero.getNombre(), is("Pintura Sin Medida"));
    assertThat(tercero.getOpcionesLitros(), nullValue());
    assertThat(tercero.getLitros(), nullValue());
  }

  @Test
  public void deberiaUsarElTextoDeLaTarjetaCuandoNoHaySelectorDePresentacion() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(
      "" +
      "<html><body>" +
      "<div class='vtex-product-summary-2-x-container'>" +
      "  <a href='/pintura/p'></a>" +
      "  <h3>Pintura Especial</h3>" +
      "  <span class='vtex-product-price-1-x-sellingPrice'>$ 9.000,00</span>" +
      "  <span>Envase de 4L y 10 L disponibles</span>" +
      "</div>" +
      "</body></html>"
    );

    List<BaldeScrapeado> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(1));
    assertThat(baldes.get(0).getOpcionesLitros(), is("4,10"));
    assertThat(baldes.get(0).getLitros(), equalTo(4.0));
  }

  @Test
  public void deberiaOrdenarYDeduplicarLasOpcionesDeLitros() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(
      "" +
      "<html><body>" +
      "<div class='vtex-product-summary-2-x-container'>" +
      "  <a href='/pintura/p'></a>" +
      "  <h3>Pintura Especial</h3>" +
      "  <div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
      "    <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
      "    <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
      "    <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
      "  </div>" +
      "</div>" +
      "</body></html>"
    );

    List<BaldeScrapeado> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(1));
    assertThat(baldes.get(0).getOpcionesLitros(), is("4,10"));
  }
}
