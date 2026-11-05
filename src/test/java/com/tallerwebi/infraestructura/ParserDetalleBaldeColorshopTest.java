package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.DetalleBaldeScrapeado;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ParserDetalleBaldeColorshopTest {

  private static final String HTML_CON_LITROS =
    "" +
    "<html><body>" +
    "<h1>Pintura Recuplast Interior 4L</h1>" +
    "<p>Rendimiento: 4 litros por mano.</p>" +
    "</body></html>";

  private static final String HTML_SIN_LITROS =
    "" +
    "<html><body>" +
    "<h1>Pintura Sin Especificar</h1>" +
    "<p>Producto sin informacion de volumen.</p>" +
    "</body></html>";

  @Test
  public void deberiaExtraerNombreYLitrosDeLaPaginaDeProducto() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse(HTML_CON_LITROS);

    DetalleBaldeScrapeado detalle = parser.parsear(doc);

    assertThat(detalle.getNombre(), is("Pintura Recuplast Interior 4L"));
    assertThat(detalle.getLitros(), equalTo(4.0));
  }

  @Test
  public void deberiaDevolverLitrosNulosCuandoNoHayInformacion() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse(HTML_SIN_LITROS);

    DetalleBaldeScrapeado detalle = parser.parsear(doc);

    assertThat(detalle.getNombre(), is("Pintura Sin Especificar"));
    assertThat(detalle.getLitros(), nullValue());
  }
}
