package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.BaldePintura;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ParserBaldesColorshopTest {

  private static final String HTML_CON_DOS_BALDES =
    "" +
    "<html><body>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <h3 class='vtex-product-summary-2-x-name'>Pintura Recuplast Interior 4L</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>$ 17.193,60</span>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <h3 class='vtex-product-summary-2-x-name'>Pintura Sin Litros Especificados</h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>10.000,00</span>" +
    "</div>" +
    "<div class='vtex-product-summary-2-x-container'>" +
    "  <h3 class='vtex-product-summary-2-x-name'>   </h3>" +
    "  <span class='vtex-product-price-1-x-sellingPrice'>5.000,00</span>" +
    "</div>" +
    "</body></html>";

  @Test
  public void deberiaParsearNombrePrecioYLitrosDeCadaTarjeta() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_CON_DOS_BALDES);

    List<BaldePintura> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(2));

    BaldePintura primero = baldes.get(0);
    assertThat(primero.getNombre(), is("Pintura Recuplast Interior 4L"));
    assertThat(primero.getPrecio(), equalTo(17193.60));
    assertThat(primero.getCantidadLitros(), equalTo(4.0));
    assertThat(primero.getColorNombre(), nullValue());
    assertThat(primero.getHex(), nullValue());

    BaldePintura segundo = baldes.get(1);
    assertThat(segundo.getNombre(), is("Pintura Sin Litros Especificados"));
    assertThat(segundo.getPrecio(), equalTo(10000.00));
    assertThat(segundo.getCantidadLitros(), nullValue());
    assertThat(segundo.getColorNombre(), nullValue());
    assertThat(segundo.getHex(), nullValue());
  }

  @Test
  public void deberiaIgnorarTarjetasSinNombre() {
    ParserBaldesColorshop parser = new ParserBaldesColorshop();
    Document doc = Jsoup.parse(HTML_CON_DOS_BALDES);

    List<BaldePintura> baldes = parser.parsear(doc);

    assertThat(baldes, hasSize(2));
    assertThat(baldes.stream().anyMatch(b -> b.getNombre().isBlank()), is(false));
  }
}
