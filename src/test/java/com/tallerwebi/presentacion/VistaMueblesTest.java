package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import com.tallerwebi.dominio.Ambiente;
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
 * Verifica que la vista de muebles sea dinámica: tiene que mostrar los muebles del ambiente
 * recibido en el modelo y no una lista fija.
 */
public class VistaMueblesTest {

  private static final String VISTA_MUEBLES = "muebles";
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
  public void dadoQueElAmbienteTieneMuebles_cuandoMiroLaVista_entoncesVeoLosMueblesDeEseAmbiente() {
    Mueble cama = new Mueble("Cama matrimonial", 2.0, 1.6);
    cama.setId(7);
    Ambiente dormitorio = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación"));
    dormitorio.agregarMueble(cama);

    String html = this.cuandoMiroLaVistaDeMuebles(dormitorio);

    assertThat(html, containsString("Cama matrimonial"));
    assertThat(html, containsString("2.0 m x 1.6 m"));
    assertThat(html, containsString("/mueble/7"));
  }

  @Test
  public void dadoQueElAmbienteNoTieneMuebles_cuandoMiroLaVista_entoncesVeoElAvisoDeQueNoHayMuebles() {
    Ambiente pasillo = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Pasillo"));

    String html = this.cuandoMiroLaVistaDeMuebles(pasillo);

    assertThat(html, containsString("todavía no tiene muebles"));
  }

  @Test
  public void dadoQueHayMueblesDeOtroAmbiente_cuandoMiroLaVista_entoncesNoAparecenLosDeOtroAmbiente() {
    Mueble cama = new Mueble("Cama matrimonial", 2.0, 1.6);
    Ambiente dormitorio = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación"));
    dormitorio.agregarMueble(cama);
    Mueble sofa = new Mueble("Sofa de living", 2.1, 0.9);
    Ambiente living = new Ambiente(5.0, 4.0, new TipoDeAmbiente("Living"));
    living.agregarMueble(sofa);

    String html = this.cuandoMiroLaVistaDeMuebles(living);

    assertThat(html, containsString("Sofa de living"));
    assertThat(html, not(containsString("Cama matrimonial")));
  }

  private String cuandoMiroLaVistaDeMuebles(Ambiente ambiente) {
    List<Mueble> muebles = new ArrayList<>(ambiente.getMuebles());
    WebContext contexto = this.unContextoWeb();
    contexto.setVariable("ambiente", ambiente);
    contexto.setVariable("muebles", muebles);
    return motorDePlantillas.process(VISTA_MUEBLES, contexto);
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
