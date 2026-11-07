package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ParserDetalleBaldeColorshopTest {

  @Test
  public void deberiaExtraerSkuIdsDeEnlacesDelDetalle() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse(
      "" +
      "<html><body>" +
      "  <a href='/hidroesmalte-epoxi/p?skuId=89441003'>Uno</a>" +
      "  <a href='/hidroesmalte-epoxi/p?skuId=89441004'>Dos</a>" +
      "  <a href='/hidroesmalte-epoxi/p?skuId=89441003'>Duplicado</a>" +
      "  <a href='/otro/p'>Sin sku</a>" +
      "</body></html>"
    );

    List<String> skuIds = parser.extraerSkuIds(doc);

    assertThat(skuIds, containsInAnyOrder("89441003", "89441004"));
  }

  @Test
  public void deberiaExtraerTodasLasOpcionesDelSelectorDePresentacion() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse(
      "" +
      "<html><body>" +
      "<div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>1 Lt.</div>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
      "</div>" +
      "</body></html>"
    );

    List<Double> opciones = parser.extraerOpcionesLitros(doc);

    assertThat(opciones, contains(1.0, 4.0, 10.0));
  }

  @Test
  public void deberiaExtraerOpcionesDelTextoCuandoNoHaySelector() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse("<html><body>Envase de 4L y 10 L disponibles</body></html>");

    List<Double> opciones = parser.extraerOpcionesLitros(doc);

    assertThat(opciones, contains(4.0, 10.0));
  }

  @Test
  public void deberiaDevolverListaVaciaCuandoNoHayLitros() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse("<html><body>Pintura sin medida</body></html>");

    assertThat(parser.extraerOpcionesLitros(doc), is(empty()));
  }

  @Test
  public void deberiaExtraerElPrimerLitroDelTextoDeLaPagina() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse("<html><body>Envase de 4L y 10 L disponibles</body></html>");

    Optional<Double> litros = parser.extraerLitros(doc);

    assertThat(litros.isPresent(), is(true));
    assertThat(litros.get(), is(4.0));
  }

  @Test
  public void deberiaDevolverVacioCuandoNoHayLitrosParaExtraerElPrimero() {
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    Document doc = Jsoup.parse("<html><body>Pintura sin medida</body></html>");

    assertThat(parser.extraerLitros(doc).isPresent(), is(false));
  }
}
