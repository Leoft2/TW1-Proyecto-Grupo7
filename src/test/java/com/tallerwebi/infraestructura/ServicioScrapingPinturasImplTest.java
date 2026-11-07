package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.BaldePintura;
import java.util.List;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

public class ServicioScrapingPinturasImplTest {

  @Test
  public void deberiaConstruirUrlFiltroConFormatoEsperado() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserBaldesColorshop parser = mock(ParserBaldesColorshop.class);
    when(conexion.conectar(anyString())).thenReturn(new Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(List.of());

    ServicioScrapingPinturasImpl servicio = new ServicioScrapingPinturasImpl(parser, conexion);

    try {
      servicio.scrapearBaldes();
    } catch (Exception e) {
      // expected: no products
    }

    verify(conexion)
      .conectar(
        "https://www.colorshop.com.ar/pinturas/-0191b4-mar-egeo?map=category-1,colores&page=1"
      );
  }

  @Test
  public void deberiaSetearColorYHexEnCadaBalde() throws Exception {
    BaldeScrapeado balde = new BaldeScrapeado("/producto/p", "Pintura X", 1000.0, 4.0, "1,4,10,20");
    List<BaldeScrapeado> productos = List.of(balde);

    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserBaldesColorshop parser = mock(ParserBaldesColorshop.class);
    when(conexion.conectar(anyString())).thenReturn(new Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(productos);

    ServicioScrapingPinturasImpl servicio = new ServicioScrapingPinturasImpl(parser, conexion);

    List<BaldePintura> resultado = servicio.scrapearBaldes();

    int colores = ColoresConocidos.obtenerColores().size();
    assertThat(resultado, hasSize(colores * 5));
    BaldePintura primero = resultado.get(0);
    assertThat(primero.getColorNombre(), is("Mar Egeo"));
    assertThat(primero.getHex(), is("0191b4"));
    assertThat(primero.getOpcionesLitros(), is("1,4,10,20"));
    for (BaldePintura baldePintura : resultado) {
      assertThat(baldePintura.getColorNombre(), is(notNullValue()));
      assertThat(baldePintura.getHex(), is(notNullValue()));
    }
  }

  @Test
  public void deberiaLimitarProductosPorColor() throws Exception {
    List<BaldeScrapeado> muchosProductos = List.of(
      new BaldeScrapeado("/p1", "P1", 1000.0, 4.0, "4"),
      new BaldeScrapeado("/p2", "P2", 1000.0, 4.0, "4"),
      new BaldeScrapeado("/p3", "P3", 1000.0, 4.0, "4"),
      new BaldeScrapeado("/p4", "P4", 1000.0, 4.0, "4"),
      new BaldeScrapeado("/p5", "P5", 1000.0, 4.0, "4"),
      new BaldeScrapeado("/p6", "P6", 1000.0, 4.0, "4")
    );

    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserBaldesColorshop parser = mock(ParserBaldesColorshop.class);
    when(conexion.conectar(anyString())).thenReturn(new Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(muchosProductos);

    ServicioScrapingPinturasImpl servicio = new ServicioScrapingPinturasImpl(parser, conexion);

    List<BaldePintura> resultado = servicio.scrapearBaldes();

    int colores = ColoresConocidos.obtenerColores().size();
    assertThat(resultado, hasSize(colores * 5));
    for (BaldePintura balde : resultado) {
      assertThat(balde.getColorNombre(), is(notNullValue()));
      assertThat(balde.getHex(), is(notNullValue()));
    }
  }

  @Test
  public void deberiaCortarPaginacionCuandoPaginaEstaVacia() throws Exception {
    BaldeScrapeado balde = new BaldeScrapeado("/producto/p", "Pintura X", 1000.0, 4.0, "4");
    List<BaldeScrapeado> productos = List.of(balde);
    List<BaldeScrapeado> vacio = List.of();

    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserBaldesColorshop parser = mock(ParserBaldesColorshop.class);
    when(
      conexion.conectar(
        "https://www.colorshop.com.ar/pinturas/-0191b4-mar-egeo?map=category-1,colores&page=1"
      )
    )
      .thenReturn(new Document(""));
    when(
      conexion.conectar(
        "https://www.colorshop.com.ar/pinturas/-0191b4-mar-egeo?map=category-1,colores&page=2"
      )
    )
      .thenReturn(new Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(productos).thenReturn(vacio);

    ServicioScrapingPinturasImpl servicio = new ServicioScrapingPinturasImpl(parser, conexion);

    servicio.scrapearBaldes();

    verify(conexion)
      .conectar(
        "https://www.colorshop.com.ar/pinturas/-0191b4-mar-egeo?map=category-1,colores&page=1"
      );
    verify(conexion)
      .conectar(
        "https://www.colorshop.com.ar/pinturas/-0191b4-mar-egeo?map=category-1,colores&page=2"
      );
  }

  @Test
  public void deberiaLanzarExcepcionCuandoNoHayResultados() throws Exception {
    ConexionColorshop conexion = mock(ConexionColorshop.class);
    ParserBaldesColorshop parser = mock(ParserBaldesColorshop.class);
    when(conexion.conectar(anyString())).thenReturn(new Document(""));
    when(parser.parsear(any(Document.class))).thenReturn(List.of());

    ServicioScrapingPinturasImpl servicio = new ServicioScrapingPinturasImpl(parser, conexion);

    try {
      servicio.scrapearBaldes();
      assertThat(true, is(false));
    } catch (Exception e) {
      assertThat(
        e.getMessage(),
        is(
          "No se encontraron baldes en Colorshop. Los selectores de la página pueden haber cambiado o el sitio requiere cookies/JavaScript."
        )
      );
    }
  }
}
