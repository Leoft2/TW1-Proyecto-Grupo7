package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class VistaNombresVisiblesEnElPlanoTest {

  @Autowired
  private WebApplicationContext contexto;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
  }

  @Test
  public void deberiaMostrarLaEtiquetaSimpleEnElPlanoInteractivoCuandoElMuebleTieneCategoriaConEtiqueta()
    throws Exception {
    MockHttpSession sesion = givenSesionConPlanoConEtiqueta();

    String html = whenAbroElPlano(sesion);

    assertThat(html, containsString(">Mesa<"));
  }

  @Test
  public void deberiaMostrarLaEtiquetaSimpleEnElRankingCuandoElMuebleTieneCategoriaConEtiqueta()
    throws Exception {
    MockHttpSession sesion = givenSesionConRankingConEtiqueta();

    String html = whenAbroElRanking(sesion);

    assertThat(html, containsString(">Mesa<"));
  }

  private MockHttpSession givenSesionConPlanoConEtiqueta() {
    Mueble mesa = givenUnMuebleMesa();
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(mesa, 1.0, 1.0));
    Plano plano = new Plano(new Ambiente(5.0, 5.0), muebles, new ArrayList<>(), new ArrayList<>());
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("plano", plano);
    return sesion;
  }

  private MockHttpSession givenSesionConRankingConEtiqueta() {
    MockHttpSession sesion = givenSesionConPlanoConEtiqueta();
    Plano plano = (Plano) sesion.getAttribute("plano");
    List<OpcionDeDistribucion> opciones = new ArrayList<>();
    opciones.add(new OpcionDeDistribucion(plano, 0, new ArrayList<>(), new ArrayList<>()));
    sesion.setAttribute("opcionesRanking", opciones);
    return sesion;
  }

  private Mueble givenUnMuebleMesa() {
    Mueble mesa = new Mueble("Mesa de Comedor Japandi", 1.0, 1.0);
    CategoriaDeMueble categoria = new CategoriaDeMueble("Mesa de comedor", 1.0, 1.0);
    categoria.setEtiqueta("Mesa");
    mesa.setCategoriaDeMueble(categoria);
    return mesa;
  }

  private String whenAbroElPlano(MockHttpSession sesion) throws Exception {
    return mockMvc
      .perform(get("/plano/actual").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private String whenAbroElRanking(MockHttpSession sesion) throws Exception {
    return mockMvc
      .perform(get("/plano/ranking").session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }
}
