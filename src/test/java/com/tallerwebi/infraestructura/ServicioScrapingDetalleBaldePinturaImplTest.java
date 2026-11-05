package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.DetalleBaldeScrapeado;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ServicioScrapingDetalleBaldePinturaImplTest {

  @Test
  public void deberiaDevolverDetalleConLitrosCuandoLaConexionDevuelvePagina() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserDetalleBaldeColorshop parser = mock(ParserDetalleBaldeColorshop.class);
    Document doc = new org.jsoup.nodes.Document("");
    when(conexion.conectar(anyString())).thenReturn(doc);
    DetalleBaldeScrapeado esperado = new DetalleBaldeScrapeado("Pintura X", 4.0);
    when(parser.parsear(doc)).thenReturn(esperado);

    ServicioScrapingDetalleBaldePinturaImpl servicio = new ServicioScrapingDetalleBaldePinturaImpl(
      conexion,
      parser
    );

    Optional<DetalleBaldeScrapeado> resultado = servicio.scrapearDetalle(
      "https://www.colorshop.com.ar/producto/p"
    );

    assertThat(resultado.isPresent(), is(true));
    assertThat(resultado.get().getNombre(), is("Pintura X"));
    assertThat(resultado.get().getLitros(), equalTo(4.0));
  }

  @Test
  public void deberiaDevolverVacioCuandoElParserNoDevuelveDetalle() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserDetalleBaldeColorshop parser = mock(ParserDetalleBaldeColorshop.class);
    when(conexion.conectar(anyString())).thenReturn(new org.jsoup.nodes.Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(null);

    ServicioScrapingDetalleBaldePinturaImpl servicio = new ServicioScrapingDetalleBaldePinturaImpl(
      conexion,
      parser
    );

    Optional<DetalleBaldeScrapeado> resultado = servicio.scrapearDetalle(
      "https://www.colorshop.com.ar/producto/p"
    );

    assertThat(resultado.isPresent(), is(false));
  }
}
