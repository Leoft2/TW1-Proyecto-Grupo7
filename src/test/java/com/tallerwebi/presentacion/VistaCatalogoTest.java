package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.OrdenDeMuebles;
import com.tallerwebi.dominio.TipoDeAmbiente;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

/**
 * Verifica que la vista del catálogo se renderice: el panel de filtros a la derecha,
 * los muebles del modelo y el fragmento de resultados que se reemplaza por ajax.
 */
public class VistaCatalogoTest {

  private static final String VISTA_CATALOGO = "catalogo";
  private static final String VISTA_RESULTADOS = "catalogo-resultados";
  private static SpringTemplateEngine motorDePlantillas;

  @BeforeAll
  public static void prepararElMotorDePlantillas() {
    FileTemplateResolver resolutor = new FileTemplateResolver();
    resolutor.setPrefix(rutaDeLasVistas().toString() + File.separator);
    resolutor.setSuffix(".html");
    resolutor.setTemplateMode(TemplateMode.HTML);
    resolutor.setCacheable(false);
    motorDePlantillas = new SpringTemplateEngine();
    motorDePlantillas.setTemplateResolver(resolutor);
  }

  @Test
  public void dadoQueHayMuebles_cuandoMiroLaVista_delCatalogo_entoncesVeoElPanelDeFiltros() {
    String html = this.cuandoMiroLaVistaDelCatalogo();

    assertThat(html, containsString("hb-panel-filtros"));
    assertThat(html, containsString("name=\"estilo\""));
    assertThat(html, containsString("name=\"tipoDeAmbiente\""));
    assertThat(html, containsString("name=\"precioMin\""));
    assertThat(html, containsString("name=\"precioMax\""));
    assertThat(html, containsString("name=\"material\""));
    assertThat(html, containsString("name=\"soloSostenibles\""));
  }

  @Test
  public void dadoQueHayMuebles_cuandoMiroLaVista_delCatalogo_entoncesVeoLosMueblesYCuantosSon() {
    String html = this.cuandoMiroLaVistaDelCatalogo();

    assertThat(html, containsString("Sillón Retró"));
    assertThat(html, containsString("2 muebles"));
    assertThat(html, containsString("id=\"hb-resultados\""));
  }

  @Test
  public void dadoQueFiltroPorEstilo_cuandoMiroLaVista_delCatalogo_entoncesElFiltroQuedaPuesto() {
    String html = this.cuandoMiroLaVistaDelCatalogo();

    assertThat(html, containsString("value=\"Retro\" selected=\"selected\""));
  }

  @Test
  public void dadoQueNoHayMuebles_cuandoMiroLaVista_delCatalogo_entoncesVeoElAvisoDeQueNoHayMuebles() {
    String html = this.cuandoMiroLaVistaDelCatalogo(new ArrayList<>());

    assertThat(html, containsString("No encontramos muebles con esos filtros"));
    assertThat(html, not(containsString("Sillón Retró")));
  }

  @Test
  public void dadoQuePidoElFragmentoDeResultados_cuandoLoMiro_entoncesVeoSoloLosMuebles() {
    String html = this.cuandoMiroElFragmentoDeResultados();

    assertThat(html, containsString("Sillón Retró"));
    assertThat(html, containsString("id=\"hb-resultados\""));
    assertThat(html, not(containsString("hb-panel-filtros")));
  }

  private String cuandoMiroLaVistaDelCatalogo() {
    return this.cuandoMiroLaVistaDelCatalogo(this.mueblesDelCatalogo());
  }

  private String cuandoMiroLaVistaDelCatalogo(List<Mueble> muebles) {
    WebContext contexto = this.unContextoWeb();
    this.cargoElModelo(contexto, muebles);
    return motorDePlantillas.process(VISTA_CATALOGO, contexto);
  }

  private String cuandoMiroElFragmentoDeResultados() {
    WebContext contexto = this.unContextoWeb();
    this.cargoElModelo(contexto, this.mueblesDelCatalogo());
    return motorDePlantillas.process(VISTA_RESULTADOS, contexto);
  }

  private void cargoElModelo(WebContext contexto, List<Mueble> muebles) {
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 0.8, 0.85);
    Material bambu = new Material("Bambú", true);
    bambu.setId(1L);
    contexto.setVariable("estilos", Estilo.values());
    contexto.setVariable("estiloSeleccionado", Estilo.Retro);
    contexto.setVariable("precioMaximo", null);
    contexto.setVariable("precioMinimo", null);
    contexto.setVariable("tipoDeAmbiente", null);
    contexto.setVariable("orden", OrdenDeMuebles.PRECIO_ASC);
    contexto.setVariable("soloSostenibles", false);
    contexto.setVariable("materiales", Arrays.asList(bambu));
    contexto.setVariable("materialSeleccionado", null);
    contexto.setVariable("tiposDeAmbiente", TipoDeAmbiente.nombresConocidos());
    contexto.setVariable("muebles", muebles);
    contexto.setVariable("totalMuebles", muebles.size());
  }

  private List<Mueble> mueblesDelCatalogo() {
    Mueble retro = new Mueble("Sillón Retró", 0.8, 0.85);
    retro.setId(1);
    retro.setNombre("Sillón Retró");
    retro.setDescripcion("Sillón de voluteadas.");
    retro.setPrecio(85000.0);
    retro.setEstilo(Estilo.Retro);
    retro.setCategoriaDeMueble(new CategoriaDeMueble("Sillón", 0.8, 0.85));

    Mueble mesa = new Mueble("Mesa Japandi", 1.2, 0.6);
    mesa.setId(2);
    mesa.setNombre("Mesa Japandi");
    mesa.setPrecio(150000.0);
    mesa.setEstilo(Estilo.Japandi);

    return Arrays.asList(retro, mesa);
  }

  private WebContext unContextoWeb() {
    JakartaServletWebApplication aplicacion =
      (JakartaServletWebApplication) JakartaServletWebApplication.buildApplication(
        new MockServletContext()
      );
    return new WebContext(
      aplicacion.buildExchange(new MockHttpServletRequest(), new MockHttpServletResponse())
    );
  }

  private static Path rutaDeLasVistas() {
    return Paths.get(
      System.getProperty("user.dir"),
      "src",
      "main",
      "webapp",
      "WEB-INF",
      "views",
      "thymeleaf"
    );
  }
}
