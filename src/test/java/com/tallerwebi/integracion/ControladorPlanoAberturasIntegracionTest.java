package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Paso de aberturas del armado del plano. El asistente va del espacio a las aberturas, y de
 * ahi a los obstaculos; las aberturas cargadas tienen que sobrevivir el cambio de paso.
 */

// Tests de integracion para el controlador de aberturas, que es parte del flujo de armado del plano.
//HU-04: Aberturas

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorPlanoAberturasIntegracionTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void deberiaLlevarAlPasoDeAberturasCuandoConfirmoElEspacio() throws Exception {
    // Given: el usuario esta en el primer paso del asistente.

    // When
    String html = whenAbroElFormularioDelAmbiente();

    // Then
    thenElFormularioContinuaEnElPasoDeAberturas(html);
  }

  private String whenAbroElFormularioDelAmbiente() throws Exception {
    return this.mockMvc.perform(get("/plano/ambiente"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenElFormularioContinuaEnElPasoDeAberturas(String html) {
    assertThat(html, containsString("action=\"/plano/aberturas\""));
  }

  @Test
  public void deberiaConservarLasMedidasCuandoConfirmoElEspacio() throws Exception {
    // Given: el usuario cargo el ancho y el largo del ambiente.

    // When
    String html = whenConfirmoElEspacio();

    // Then
    thenElPasoDeAberturasMantieneElAncho(html);
  }

  private String whenConfirmoElEspacio() throws Exception {
    return this.mockMvc.perform(post("/plano/aberturas").param("ancho", "4").param("largo", "3"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenElPasoDeAberturasMantieneElAncho(String html) {
    assertThat(html, containsString("value=\"4.0\""));
  }

  @Test
  public void deberiaRenderizarElPasoDeAberturasSinCamposObligatoriosCuandoConfirmoElEspacio()
    throws Exception {
    // Given: el usuario cargo el ancho y el largo del ambiente.

    // When
    String html = whenConfirmoElEspacio();

    // Then
    thenElPasoDeAberturasNoTieneCamposObligatorios(html);
  }

  private void thenElPasoDeAberturasNoTieneCamposObligatorios(String html) {
    assertThat(html, not(containsString("required")));
  }

  @Test
  public void deberiaMostrarElFormularioSinFilasDeAberturaCuandoConfirmoElEspacio()
    throws Exception {
    // Given: el usuario cargo el ancho y el largo del ambiente.

    // When
    String html = whenConfirmoElEspacio();

    // Then
    thenElPasoDeAberturasNoTieneFilasCargadas(html);
  }

  private void thenElPasoDeAberturasNoTieneFilasCargadas(String html) {
    assertThat(html, not(containsString("name=\"aberturas[0].posicion\"")));
  }

  @Test
  public void deberiaMostrarElPasoDeAberturasCuandoConfirmoElEspacio() throws Exception {
    // Given: el usuario cargo el ancho y el largo del ambiente.

    // When
    String vista = whenConfirmoElEspacioYVeoLaVista();

    // Then
    thenLaVistaEsElPasoDeAberturas(vista);
  }

  private String whenConfirmoElEspacioYVeoLaVista() throws Exception {
    return this.mockMvc.perform(post("/plano/aberturas").param("ancho", "4").param("largo", "3"))
      .andExpect(status().isOk())
      .andReturn()
      .getModelAndView()
      .getViewName();
  }

  private void thenLaVistaEsElPasoDeAberturas(String vista) {
    assertThat(vista, is("plano-aberturas-config"));
  }

  @Test
  public void deberiaAvanzarAlPasoDeObstaculosCuandoConfirmoElEspacioSinAberturas()
    throws Exception {
    // Given: el usuario confirmo el espacio sin cargar ninguna abertura.

    // When
    String redireccion = whenConfirmoElEspacioSinAberturas();

    // Then
    thenElAsistenteAvanzaAlPasoDeObstaculos(redireccion);
  }

  private String whenConfirmoElEspacioSinAberturas() throws Exception {
    return this.mockMvc.perform(
        post("/plano/aberturas/confirmar").param("ancho", "4").param("largo", "3")
      )
      .andExpect(status().is3xxRedirection())
      .andReturn()
      .getResponse()
      .getRedirectedUrl();
  }

  private void thenElAsistenteAvanzaAlPasoDeObstaculos(String redireccion) {
    assertThat(redireccion, is("/plano/obstaculos"));
  }

  @Test
  public void deberiaMostrarElErrorCuandoUnaAberturaEstaIncompleta() throws Exception {
    // Given: el usuario cargo una puerta sin el ancho y confirmo el paso de aberturas.

    // When
    String html = whenConfirmoElPasoDeAberturasConUnaAberturaIncompleta();

    // Then
    thenSeMuestraElErrorDeAberturaIncompleta(html);
  }

  private String whenConfirmoElPasoDeAberturasConUnaAberturaIncompleta() throws Exception {
    return this.mockMvc.perform(
        post("/plano/aberturas/confirmar")
          .param("ancho", "4")
          .param("largo", "3")
          .param("aberturas[0].tipo", "PUERTA")
          .param("aberturas[0].muro", "SUPERIOR")
          .param("aberturas[0].posicion", "1.0")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenSeMuestraElErrorDeAberturaIncompleta(String html) {
    assertThat(html, containsString("Completá la posición y el ancho de cada abertura"));
  }

  @Test
  public void deberiaConservarLaAberturaCuandoConfirmoElPasoDeAberturas() throws Exception {
    // Given: el usuario cargo una puerta y confirmo el paso de aberturas.
    MockHttpSession sesion = whenConfirmoElPasoDeAberturas();

    // When
    String html = whenAbroElPasoDeObstaculos(sesion);

    // Then
    thenElPasoDeObstaculosMantieneLaAbertura(html);
  }

  private MockHttpSession whenConfirmoElPasoDeAberturas() throws Exception {
    MvcResult resultado =
      this.mockMvc.perform(
          post("/plano/aberturas/confirmar")
            .param("ancho", "4")
            .param("largo", "3")
            .param("aberturas[0].tipo", "PUERTA")
            .param("aberturas[0].muro", "SUPERIOR")
            .param("aberturas[0].posicion", "1.0")
            .param("aberturas[0].ancho", "0.9")
            .param("aberturas[0].radioDeGiro", "0.9")
        )
        .andExpect(status().is3xxRedirection())
        .andReturn();
    return (MockHttpSession) resultado.getRequest().getSession(false);
  }

  private String whenAbroElPasoDeObstaculos(MockHttpSession sesion) throws Exception {
    return this.mockMvc.perform(get("/plano/obstaculos").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenElPasoDeObstaculosMantieneLaAbertura(String html) {
    assertThat(html, containsString("name=\"aberturas[0].posicion\""));
  }

  @Test
  public void deberiaGuardarLaAberturaEnElAmbienteCuandoConfirmoElPasoDeObstaculos()
    throws Exception {
    // Given: el usuario cargo una puerta y confirmo el paso de aberturas.
    MockHttpSession sesion = whenConfirmoElPasoDeAberturas();

    // When
    Ambiente ambiente = whenConfirmoElPasoDeObstaculos(sesion);

    // Then
    thenElAmbienteTieneUnaAbertura(ambiente);
  }

  private Ambiente whenConfirmoElPasoDeObstaculos(MockHttpSession sesion) throws Exception {
    this.mockMvc.perform(
        post("/plano/obstaculos/confirmar")
          .session(sesion)
          .param("ancho", "4")
          .param("largo", "3")
          .param("aberturas[0].tipo", "PUERTA")
          .param("aberturas[0].muro", "SUPERIOR")
          .param("aberturas[0].posicion", "1.0")
          .param("aberturas[0].ancho", "0.9")
          .param("aberturas[0].radioDeGiro", "0.9")
      )
      .andExpect(status().is3xxRedirection());
    return (Ambiente) sesion.getAttribute("ambiente");
  }

  private void thenElAmbienteTieneUnaAbertura(Ambiente ambiente) {
    assertThat(ambiente.getAberturas().size(), is(1));
  }

  @Test
  public void deberiaDibujarLaZonaDeExclusionCuandoGeneroElPlanoConUnaAbertura() throws Exception {
    // Given: el ambiente tiene una puerta cargada.

    // When
    String html = whenGeneroElPlanoConUnaPuerta();

    // Then
    thenElPlanoDibujaLaZonaDeExclusion(html);
  }

  private String whenGeneroElPlanoConUnaPuerta() throws Exception {
    return this.mockMvc.perform(
        post("/plano/generar")
          .param("ancho", "4")
          .param("largo", "3")
          .param("aberturas[0].tipo", "PUERTA")
          .param("aberturas[0].muro", "SUPERIOR")
          .param("aberturas[0].posicion", "1.0")
          .param("aberturas[0].ancho", "0.9")
          .param("aberturas[0].radioDeGiro", "0.9")
          .param("muebles[0].nombre", "Rack")
          .param("muebles[0].ancho", "1.0")
          .param("muebles[0].profundidad", "0.5")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenElPlanoDibujaLaZonaDeExclusion(String html) {
    assertThat(html, containsString("class=\"plano-zona-apertura\""));
  }

  @Test
  public void deberiaGenerarElPlanoCuandoLaPuertaNoCargoRadioDeGiro() throws Exception {
    // Given: el ambiente tiene una puerta cargada sin radio de giro.

    // When
    String html = whenGeneroElPlanoConUnaPuertaSinRadioDeGiro();

    // Then
    thenElPlanoDibujaLaZonaDeExclusion(html);
  }

  private String whenGeneroElPlanoConUnaPuertaSinRadioDeGiro() throws Exception {
    return this.mockMvc.perform(
        post("/plano/generar")
          .param("ancho", "4")
          .param("largo", "3")
          .param("aberturas[0].tipo", "PUERTA")
          .param("aberturas[0].muro", "SUPERIOR")
          .param("aberturas[0].posicion", "1.0")
          .param("aberturas[0].ancho", "0.9")
          .param("muebles[0].nombre", "Rack")
          .param("muebles[0].ancho", "1.0")
          .param("muebles[0].profundidad", "0.5")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  @Test
  public void deberiaDibujarLaAberturaSobreElMuroCuandoGeneroElPlanoConUnaAbertura()
    throws Exception {
    // Given: el ambiente tiene una puerta cargada.

    // When
    String html = whenGeneroElPlanoConUnaPuerta();

    // Then
    thenElPlanoDibujaLaAberturaSobreElMuro(html);
  }

  private void thenElPlanoDibujaLaAberturaSobreElMuro(String html) {
    assertThat(html, containsString("class=\"plano-apertura\""));
  }

  @Test
  public void deberiaMostrarElPasoDeAberturasEnElAsistenteCuandoAbroElFormularioDelAmbiente()
    throws Exception {
    // Given: el usuario esta en el primer paso del asistente.

    // When
    String html = whenAbroElFormularioDelAmbiente();

    // Then
    thenElAsistenteMuestraElPasoDeAberturas(html);
  }

  private void thenElAsistenteMuestraElPasoDeAberturas(String html) {
    assertThat(html, containsString("2 · Aberturas"));
  }
}
