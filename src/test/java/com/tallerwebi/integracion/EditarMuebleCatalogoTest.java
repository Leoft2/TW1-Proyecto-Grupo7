package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Renderiza el formulario de edicion real del catalogo. Antes fallaba con un error 500 al
 * evaluar la expresion del id en la plantilla; estos tests cubren ese renderizado de punta a
 * punta, que los tests con mocks del controlador no detectan.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class EditarMuebleCatalogoTest {

  private static final String ROL_ADMIN = "ADMIN";

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
  public void dadoQueSoyAdminCuandoAbroElFormularioDeEdicionEntoncesSeRenderizaConSuId()
    throws Exception {
    Mueble mueble = this.dadoUnMuebleEnCatalogo();

    String html = this.cuandoAbroElFormularioDeEdicion(mueble.getId());

    assertThat(html, containsString("/muebles/editar/" + mueble.getId()));
    assertThat(html, containsString("/muebles/eliminar/" + mueble.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueSoyAdminCuandoAbroElFormularioDeEdicionEntoncesVeoSusDatos() throws Exception {
    Mueble mueble = this.dadoUnMuebleEnCatalogo();

    String html = this.cuandoAbroElFormularioDeEdicion(mueble.getId());

    assertThat(html, containsString("Sillón Retró"));
    assertThat(html, containsString("Living"));
  }

  private Mueble dadoUnMuebleEnCatalogo() {
    CategoriaDeMueble categoria = new CategoriaDeMueble("Sillón", 0.80, 0.85);
    this.sessionFactory.getCurrentSession().persist(categoria);
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    this.sessionFactory.getCurrentSession().persist(living);
    Mueble mueble = new Mueble("Sillón Retró", 0.80, 0.85);
    mueble.setPrecio(85000.0);
    mueble.setEstilo(Estilo.Retro);
    mueble.setEnCatalogo(true);
    mueble.setCategoriaDeMueble(categoria);
    mueble.agregarTipoDeAmbiente(living);
    this.sessionFactory.getCurrentSession().persist(mueble);
    this.sessionFactory.getCurrentSession().flush();
    return mueble;
  }

  private String cuandoAbroElFormularioDeEdicion(int id) throws Exception {
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("ROL", ROL_ADMIN);
    return this.mockMvc.perform(get("/muebles/editar/{id}", id).session(sesion))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }
}
