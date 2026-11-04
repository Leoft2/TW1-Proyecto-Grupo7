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

  private static final String HTML_CON_DOS_BALDES =
    "" +
    "<html><body>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/recuplast-interior-mate/p?skuId=84520993'></a>" +
    "  <h3>Pintura Recuplast Interior 4L</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>$ 17.193,60</span>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/hidroesmalte-epoxi/p?skuId=89441003'></a>" +
    "  <h3>Pintura Sin Litros Especificados</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>10.000,00</span>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <a href='/sin-enlace/p?skuId=999'></a>" +
    "  <h3>   </h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>5.000,00</span>" +
    "</div>" +
    "</body></html>";

  @Test
  public void deberiaParsearNombrePrecioYLitrosDeCadaTarjeta() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_CON_DOS_BALDES);

    List<BaldeScrapeado> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(2));

    BaldeScrapeado primero = baldes.get(0);
    assertThat(primero.getNombre(), is("Pintura Recuplast Interior 4L"));
    assertThat(primero.getHref(), is("/recuplast-interior-mate/p"));
    assertThat(primero.getPrecio(), equalTo(17193.60));
    assertThat(primero.getLitros(), equalTo(4.0));
    assertThat(primero.getColorNombre(), nullValue());
    assertThat(primero.getHex(), nullValue());

    BaldeScrapeado segundo = baldes.get(1);
    assertThat(segundo.getNombre(), is("Pintura Sin Litros Especificados"));
    assertThat(segundo.getHref(), is("/hidroesmalte-epoxi/p"));
    assertThat(segundo.getPrecio(), equalTo(10000.00));
    assertThat(segundo.getLitros(), nullValue());
    assertThat(segundo.getColorNombre(), nullValue());
    assertThat(segundo.getHex(), nullValue());
  }

  @Test
  public void deberiaIgnorarTarjetasSinNombre() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_CON_DOS_BALDES);

    List<BaldeScrapeado> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(2));
    assertThat(baldes.stream().anyMatch(b -> b.getNombre().isBlank()), is(false));
  }
}
