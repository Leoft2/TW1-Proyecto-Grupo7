package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.OpcionDeDistribucion;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.util.ArrayList;
import java.util.List;
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

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class VistaOrientacionMueblesTest {

  @Autowired
  private WebApplicationContext contexto;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
  }

  @Test
  public void deberiaMostrarElFrenteEnElPlanoInteractivo() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();

    //When
    String html = whenAbroPlano(sesion);

    //Then
    assertThat(html, containsString("Frente hacia INFERIOR"));
    assertThat(html, containsString("rotate(180)"));
  }

  @Test
  public void deberiaMostrarElFrenteEnLaVistaDelRanking() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();
    Plano plano = (Plano) sesion.getAttribute("plano");
    List<OpcionDeDistribucion> opciones = new ArrayList<>();
    opciones.add(new OpcionDeDistribucion(plano, 0, new ArrayList<>(), new ArrayList<>()));
    sesion.setAttribute("opcionesRanking", opciones);

    //When
    String html = mockMvc
      .perform(get("/plano/ranking").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    //Then
    assertThat(html, containsString("Frente hacia INFERIOR"));
    assertThat(html, containsString("rotate(180)"));
  }

  @Test
  public void deberiaActualizarLaOrientacionCuandoSeMueveAParedDerecha() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();

    //When
    MvcResult resultado = mockMvc
      .perform(
        post("/plano/mover-mueble")
          .session(sesion)
          .param("indice", "0")
          .param("posicionX", "4")
          .param("posicionY", "1")
      )
      .andExpect(status().isOk())
      .andReturn();

    //Then
    assertThat(resultado.getResponse().getHeader("X-Orientacion"), is(equalTo("IZQUIERDO")));
  }

  @Test
  public void deberiaQuitarLaOrientacionAlMoverElMuebleLejosDeLaPared() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();

    //When
    MvcResult resultado = mockMvc
      .perform(
        post("/plano/mover-mueble")
          .session(sesion)
          .param("indice", "0")
          .param("posicionX", "2")
          .param("posicionY", "2")
      )
      .andExpect(status().isOk())
      .andReturn();

    //Then
    assertThat(resultado.getResponse().getHeader("X-Orientacion"), is(equalTo("")));
  }

  @Test
  public void deberiaOcultarLaFlechaAlVolverAUnPlanoConElMuebleSinPared() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();
    Plano plano = (Plano) sesion.getAttribute("plano");
    plano.getMuebles().get(0).moverA(2.0, 2.0);

    //When
    String html = whenAbroPlano(sesion);

    //Then
    assertThat(html, containsString("visibility=\"hidden\""));
  }

  @Test
  public void deberiaMostrarElFrenteHaciaLaDerechaTrasRecargarElPlano() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();
    Plano plano = (Plano) sesion.getAttribute("plano");
    plano.getMuebles().get(0).moverA(0.0, 1.0);

    //When
    String html = whenAbroPlano(sesion);

    //Then
    assertThat(html, containsString("Frente hacia DERECHO"));
    assertThat(html, containsString("rotate(90)"));
  }

  @Test
  public void deberiaMostrarElPlanoAntiguoAunqueUnMuebleEsteFueraDelAmbiente() throws Exception {
    //Given
    MockHttpSession sesion = givenSesionConPlano();
    Plano plano = (Plano) sesion.getAttribute("plano");
    plano.getMuebles().get(0).moverA(6.0, 0.0);

    //When
    String html = whenAbroPlano(sesion);

    //Then
    assertThat(html, containsString("visibility=\"hidden\""));
    assertThat(html, containsString("plano-mueble-cuerpo"));
  }

  private MockHttpSession givenSesionConPlano() {
    Mueble sofa = new Mueble("Sofa", 1.0, 1.0);
    sofa.setCategoriaDeMueble(new CategoriaDeMueble("Sofá 3 cuerpos", 1.0, 1.0));
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(sofa, 1.0, 0.0));
    Plano plano = new Plano(new Ambiente(5.0, 5.0), muebles, new ArrayList<>(), new ArrayList<>());
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("plano", plano);
    return sesion;
  }

  private String whenAbroPlano(MockHttpSession sesion) throws Exception {
    return mockMvc
      .perform(get("/plano/actual").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }
}
