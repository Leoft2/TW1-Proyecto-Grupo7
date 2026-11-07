package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ServicioScrapingDetalleBaldePinturaImplTest {

  private static final String URL = "https://www.colorshop.com.ar/hidroesmalte-epoxi/p";

  @Test
  public void deberiaObtenerTodasLasOpcionesConUnSoloRequest() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();

    Document docBase = Jsoup.parse(
      "" +
      "<html><body>" +
      "<div class='vtex-store-components-3-x-skuSelectorSubcontainer--presentacion'>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>1 Lt.</div>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>4 Lts.</div>" +
      "  <div class='vtex-store-components-3-x-skuSelectorItem'>10 Lts.</div>" +
      "</div>" +
      "</body></html>"
    );
    when(conexion.conectar(URL)).thenReturn(docBase);

    ServicioScrapingDetalleBaldePintura servicio = new ServicioScrapingDetalleBaldePinturaImpl(
      conexion,
      parser
    );

    Optional<List<Double>> resultado = servicio.obtenerOpcionesLitros(URL);

    assertThat(resultado.isPresent(), is(true));
    assertThat(resultado.get(), contains(1.0, 4.0, 10.0));
    verify(conexion, times(1)).conectar(anyString());
    verify(conexion).conectar(URL);
  }

  @Test
  public void deberiaExtraerOpcionesDelTextoDeLaPaginaBase() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();

    Document docBase = Jsoup.parse("<html><body>Envase 20L</body></html>");
    when(conexion.conectar(URL)).thenReturn(docBase);

    ServicioScrapingDetalleBaldePintura servicio = new ServicioScrapingDetalleBaldePinturaImpl(
      conexion,
      parser
    );

    Optional<List<Double>> resultado = servicio.obtenerOpcionesLitros(URL);

    assertThat(resultado.isPresent(), is(true));
    assertThat(resultado.get(), contains(20.0));
  }

  @Test
  public void deberiaDevolverVacioCuandoConexionFalla() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserDetalleBaldeColorshop parser = new ParserDetalleBaldeColorshop();
    when(conexion.conectar(URL)).thenThrow(new RuntimeException("caido"));

    ServicioScrapingDetalleBaldePintura servicio = new ServicioScrapingDetalleBaldePinturaImpl(
      conexion,
      parser
    );

    Optional<List<Double>> resultado = servicio.obtenerOpcionesLitros(URL);

    assertThat(resultado.isPresent(), is(false));
  }
}
