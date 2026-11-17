package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.tallerwebi.dominio.Mueble;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * Verifica que el detalle de un mueble muestre sus medidas en metros, que es la unidad que usa
 * todo el flujo de planos.
 */
public class VistaMuebleTest {

  private static final String VISTA_MUEBLE = "mueble";
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
  public void deberiaMostrarElAnchoYElLargoEnMetrosCuandoMiroElDetalleDeUnMueble() {
    Mueble cama = new Mueble("Cama Boho", 1.4, 1.9);

    String html = this.cuandoMiroElDetalleDelMueble(cama);

    assertThat(html, allOf(containsString("1.4 m"), containsString("1.9 m")));
  }

  @Test
  public void deberiaNoMostrarLasMedidasEnCentimetrosCuandoMiroElDetalleDeUnMueble() {
    Mueble cama = new Mueble("Cama Boho", 1.4, 1.9);

    String html = this.cuandoMiroElDetalleDelMueble(cama);

    assertThat(html, not(containsString("cm")));
  }

  private String cuandoMiroElDetalleDelMueble(Mueble mueble) {
    WebContext contexto = this.unContextoWeb();
    contexto.setVariable("mueble", mueble);
    return motorDePlantillas.process(VISTA_MUEBLE, contexto);
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
