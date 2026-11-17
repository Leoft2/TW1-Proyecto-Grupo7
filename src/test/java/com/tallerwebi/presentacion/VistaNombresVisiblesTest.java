package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.TipoDeAmbiente;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
 * Verifica que las vistas que listan los muebles de un ambiente muestren la etiqueta
 * corta de la categoria en lugar del nombre comercial del mueble.
 */
public class VistaNombresVisiblesTest {

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
  public void deberiaMostrarLaEtiquetaSimpleEnLaListaDeAmbientesCuandoElMuebleTieneCategoriaConEtiqueta() {
    Ambiente living = givenUnAmbienteConMesa();

    String html = this.cuandoMiroLaVista("ambientes", living);

    assertThat(html, containsString(">Mesa</a>"));
  }

  @Test
  public void deberiaMostrarLaEtiquetaSimpleEnElDetalleDelAmbienteCuandoElMuebleTieneCategoriaConEtiqueta() {
    Ambiente living = givenUnAmbienteConMesa();

    String html = this.cuandoMiroLaVista("ambiente", living);

    assertThat(html, containsString(">Mesa</a>"));
  }

  @Test
  public void deberiaMostrarLaEtiquetaSimpleEnElInicioCuandoElMuebleTieneCategoriaConEtiqueta() {
    Ambiente living = givenUnAmbienteConMesa();

    String html = this.cuandoMiroLaVista("home", living);

    assertThat(html, containsString(">Mesa</a>"));
  }

  private Ambiente givenUnAmbienteConMesa() {
    Ambiente living = new Ambiente(5.0, 4.0, new TipoDeAmbiente("Living"));
    Mueble mesa = new Mueble("Mesa de Comedor Japandi", 1.6, 0.9);
    CategoriaDeMueble categoria = new CategoriaDeMueble("Mesa de comedor", 1.6, 0.9);
    categoria.setEtiqueta("Mesa");
    mesa.setCategoriaDeMueble(categoria);
    living.agregarMueble(mesa);
    return living;
  }

  private String cuandoMiroLaVista(String vista, Ambiente ambiente) {
    List<Ambiente> ambientes = new ArrayList<>();
    ambientes.add(ambiente);
    WebContext contexto = this.unContextoWeb();
    contexto.setVariable("ambientes", ambientes);
    contexto.setVariable("ambiente", ambiente);
    return motorDePlantillas.process(vista, contexto);
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
