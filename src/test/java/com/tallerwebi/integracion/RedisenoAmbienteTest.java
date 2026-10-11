package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
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
public class RedisenoAmbienteTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarElResultadoCuandoUnMuebleSugeridoNoTieneCategoria() throws Exception {
    Ambiente ambiente = this.dadoUnAmbienteGuardado();
    this.dadoUnMuebleSinCategoria();

    String html = this.cuandoRedisenoElAmbiente(ambiente, Estilo.Japandi, 60000.0);

    this.entoncesVeoElChip("Sin categoría", html);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLaCategoriaDelMuebleCuandoLaTiene() throws Exception {
    Ambiente ambiente = this.dadoUnAmbienteGuardado();
    this.dadoUnMuebleConCategoria();

    String html = this.cuandoRedisenoElAmbiente(ambiente, Estilo.Japandi, 60000.0);

    this.entoncesVeoElChip("Sillón", html);
  }

  private Ambiente dadoUnAmbienteGuardado() {
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    this.sessionFactory.getCurrentSession().persist(living);
    Ambiente ambiente = new Ambiente(4.0, 3.0, living);
    this.sessionFactory.getCurrentSession().persist(ambiente);
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private void dadoUnMuebleSinCategoria() {
    this.guardarUnMueble(55000.0, null);
  }

  private void dadoUnMuebleConCategoria() {
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 1.0, 0.8);
    this.sessionFactory.getCurrentSession().persist(sillones);
    CategoriaDeMueble categoria =
      this.sessionFactory.getCurrentSession().get(CategoriaDeMueble.class, sillones.getId());
    this.guardarUnMueble(55000.0, categoria);
  }

  private void guardarUnMueble(Double precio, CategoriaDeMueble categoria) {
    Mueble mueble = new Mueble("Sillón propio", 0.9, 0.8);
    mueble.setPrecio(precio);
    mueble.setEstilo(Estilo.Japandi);
    mueble.setCategoriaDeMueble(categoria);
    this.sessionFactory.getCurrentSession().persist(mueble);
    this.sessionFactory.getCurrentSession().flush();
  }

  private String cuandoRedisenoElAmbiente(Ambiente ambiente, Estilo estilo, Double presupuesto)
    throws Exception {
    MockHttpServletRequestBuilder pedido = post("/ambientes/{id}/redisenar", ambiente.getId())
      .param("estilo", estilo.name())
      .param("presupuesto", String.valueOf(presupuesto));
    return this.mockMvc.perform(pedido)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }

  private void entoncesVeoElChip(String texto, String html) {
    assertThat(html, containsString(">" + texto + "</span>"));
  }
}
