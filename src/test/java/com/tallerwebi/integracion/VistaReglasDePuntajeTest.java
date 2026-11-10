package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Ambiente;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class VistaReglasDePuntajeTest {

  @Autowired
  private WebApplicationContext contexto;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
  }

  @Test
  public void deberiaMostrarLasReglasAntesDeGenerarElPlano() throws Exception {
    //Given
    MockHttpSession sesion = new MockHttpSession();

    //When
    String html = mockMvc
      .perform(get("/inventario-muebles").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    //Then
    thenSeMuestranLasReglas(html);
    assertThat(html, containsString("Generar plano"));
  }

  @Test
  public void deberiaMostrarLasReglasYElDesgloseEnElRanking() throws Exception {
    //Given
    MockHttpSession sesion = new MockHttpSession();
    Plano plano = new Plano(
      new Ambiente(5.0, 5.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    List<String> desglose = new ArrayList<>();
    desglose.add("Cama: acceso libre a ambos laterales (+1)");
    List<OpcionDeDistribucion> opciones = new ArrayList<>();
    opciones.add(new OpcionDeDistribucion(plano, 1, desglose, new ArrayList<>()));
    sesion.setAttribute("opcionesRanking", opciones);

    //When
    String html = mockMvc
      .perform(get("/plano/ranking").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    //Then
    thenSeMuestranLasReglas(html);
    assertThat(html, containsString("Cama: acceso libre a ambos laterales (+1)"));
    assertThat(html, containsString("Elegir esta distribución"));
  }

  @Test
  public void deberiaMostrarLasReglasCuandoNoHayOpciones() throws Exception {
    //Given
    MockHttpSession sesion = new MockHttpSession();

    //When
    String html = mockMvc
      .perform(get("/plano/ranking").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    //Then
    thenSeMuestranLasReglas(html);
  }

  private void thenSeMuestranLasReglas(String html) {
    assertThat(html, containsString("<details"));
    assertThat(html, containsString("Ver reglas de puntaje"));
    assertThat(html, containsString("Si no hay ventanas: +1"));
    assertThat(html, containsString("Cama con acceso libre en ambos laterales: +1"));
    assertThat(html, containsString("Sofá orientado hacia un rack de TV: +1"));
    assertThat(html, containsString("Silla orientada hacia una mesa: +1"));
    assertThat(
      html,
      containsString(
        "Placard con despeje frontal de 0.60 m o más: +2. Con despeje entre 0.40 y 0.59 m: +1."
      )
    );
    assertThat(html, containsString("el despeje frontal de 0.40 m del placard"));
    assertThat(html, containsString("no se ofrecen opciones que los incumplan"));
  }
}
