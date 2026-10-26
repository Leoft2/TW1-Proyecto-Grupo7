package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorPlano2DObstaculosTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void deberiaMostrarElFormularioDeObstaculosCuandoAbroElPasoDeObstaculos()
    throws Exception {
    String html = whenAbroElPasoDeObstaculos();

    thenSeMuestraElFormularioDeObstaculos(html);
  }

  @Test
  public void deberiaOcultarLaTablaDeObstaculosCuandoEstoyEnElPasoDeEspacio() throws Exception {
    String html = whenAbroElPasoDeEspacio();

    thenNoSeMuestraLaTablaDeObstaculos(html);
  }

  @Test
  public void dadoQueRegistreUnObstaculo_cuandoGeneroElPlano_entoncesSeDibujaSuZonaNoDisponible()
    throws Exception {
    String[] radiador = givenUnRadiador(0, "SUPERIOR", "0", "0.8");

    String html = whenGeneroElPlanoCon(radiador);

    thenSeDibujaLaZonaNoDisponibleDelRadiador(html);
  }

  @Test
  public void dadoQueUnMuebleQuedaSobreUnObstaculo_cuandoGeneroElPlano_entoncesSeResaltaElConflicto()
    throws Exception {
    String[] mueble = givenUnMueble(0, "Rack", "1.0", "0.5");

    String html = whenGeneroElPlanoCon(
      mueble,
      givenUnRadiador(0, "SUPERIOR", "0", "4"),
      givenUnRadiador(1, "DERECHO", "0", "3"),
      givenUnRadiador(2, "INFERIOR", "0", "4"),
      givenUnRadiador(3, "IZQUIERDO", "0", "3")
    );

    thenSeResaltaElConflicto(html);
  }

  private String[] givenUnRadiador(int indice, String muro, String posicion, String ancho) {
    return new String[] {
      "obstaculos[" + indice + "].tipo",
      "RADIADOR",
      "obstaculos[" + indice + "].muro",
      muro,
      "obstaculos[" + indice + "].posicion",
      posicion,
      "obstaculos[" + indice + "].ancho",
      ancho,
      "obstaculos[" + indice + "].profundidad",
      "0.15",
    };
  }

  private String[] givenUnMueble(int indice, String nombre, String ancho, String profundidad) {
    return new String[] {
      "muebles[" + indice + "].nombre",
      nombre,
      "muebles[" + indice + "].ancho",
      ancho,
      "muebles[" + indice + "].profundidad",
      profundidad,
    };
  }

  private String whenAbroElPasoDeObstaculos() throws Exception {
    return this.mockMvc.perform(get("/plano/obstaculos"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String whenAbroElPasoDeEspacio() throws Exception {
    return this.mockMvc.perform(get("/plano/ambiente"))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String whenGeneroElPlanoCon(String[]... bloques) throws Exception {
    MockHttpServletRequestBuilder pedido = post("/plano/generar")
      .param("ancho", "4")
      .param("largo", "3");
    for (String[] bloque : bloques) {
      for (int indice = 0; indice < bloque.length; indice += 2) {
        pedido = pedido.param(bloque[indice], bloque[indice + 1]);
      }
    }
    return this.mockMvc.perform(pedido)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenSeMuestraElFormularioDeObstaculos(String html) {
    assertThat(html, containsString("action=\"/plano/obstaculos/confirmar\""));
  }

  private void thenNoSeMuestraLaTablaDeObstaculos(String html) {
    assertThat(html, not(containsString("id=\"filas-obstaculos\"")));
  }

  private void thenSeDibujaLaZonaNoDisponibleDelRadiador(String html) {
    assertThat(html, containsString("class=\"plano-obstaculo\""));
    assertThat(html, containsString("Radiador (muro superior)"));
  }

  private void thenSeResaltaElConflicto(String html) {
    assertThat(html, containsString("plano-mueble-conflicto"));
    assertThat(html, containsString("Rack choca con Radiador (muro superior)"));
  }
}
