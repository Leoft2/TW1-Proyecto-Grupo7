package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorPlano2DAdvertenciasIntegracionTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void deberiaMostrarLaAdvertenciaCuandoElPlanoTieneUnaPuertaSinParedLibre()
    throws Exception {
    String html = whenGeneroUnPlanoConPuertaSinParedLibre();

    thenElPlanoMuestraLaAdvertencia(html);
  }

  private String whenGeneroUnPlanoConPuertaSinParedLibre() throws Exception {
    return this.mockMvc.perform(
        post("/plano/generar")
          .param("ancho", "2")
          .param("largo", "5")
          .param("aberturas[0].tipo", "PUERTA")
          .param("aberturas[0].muro", "SUPERIOR")
          .param("aberturas[0].posicion", "0.6")
          .param("aberturas[0].ancho", "0.9")
          .param("aberturas[0].radioDeGiro", "0.9")
      )
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void thenElPlanoMuestraLaAdvertencia(String html) {
    assertThat(
      html,
      containsString(
        "La puerta del muro superior en la posición 0.6 m no tiene pared libre para abrir"
      )
    );
  }
}
