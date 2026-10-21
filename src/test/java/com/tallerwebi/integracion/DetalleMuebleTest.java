package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class DetalleMuebleTest {

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
  public void deberiaMostrarSinCategoriaCuandoElMuebleNoTieneCategoria() throws Exception {
    Mueble mueble = this.dadoUnMuebleSinCategoria();

    String html = this.cuandoAbroElDetalleDelMueble(mueble.getId());

    assertThat(html, containsString(">Sin categoría</dd>"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarElMaterialYQueEsSostenibleCuandoElMuebleLoEs() throws Exception {
    Mueble mueble = this.dadoUnMuebleSinCategoria();
    mueble.setMaterial(this.dadoUnMaterial("Bambú", true));
    this.sessionFactory.getCurrentSession().flush();

    String html = this.cuandoAbroElDetalleDelMueble(mueble.getId());

    assertThat(html, containsString(">Bambú</dd>"));
    assertThat(html, containsString(">Sí</span>"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarQueElMaterialNoEsSostenibleCuandoElMuebleNoLoEs() throws Exception {
    Mueble mueble = this.dadoUnMuebleSinCategoria();
    mueble.setMaterial(this.dadoUnMaterial("Melamina", false));
    this.sessionFactory.getCurrentSession().flush();

    String html = this.cuandoAbroElDetalleDelMueble(mueble.getId());

    assertThat(html, containsString(">Melamina</dd>"));
    assertThat(html, containsString(">No</span>"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarSinMaterialCuandoElMuebleNoTieneMaterial() throws Exception {
    Mueble mueble = this.dadoUnMuebleSinCategoria();

    String html = this.cuandoAbroElDetalleDelMueble(mueble.getId());

    assertThat(html, containsString(">Sin material</dd>"));
    assertThat(html, containsString(">No</span>"));
  }

  private Material dadoUnMaterial(String nombre, boolean sostenible) {
    Material material = new Material(nombre, sostenible);
    this.sessionFactory.getCurrentSession().persist(material);
    return material;
  }

  private Mueble dadoUnMuebleSinCategoria() {
    Mueble mueble = new Mueble("Sillón propio", 0.9, 0.8);
    mueble.setPrecio(55000.0);
    mueble.setEstilo(Estilo.Japandi);
    this.sessionFactory.getCurrentSession().persist(mueble);
    this.sessionFactory.getCurrentSession().flush();
    return mueble;
  }

  private String cuandoAbroElDetalleDelMueble(int id) throws Exception {
    return this.mockMvc.perform(get("/mueble/{id}", id))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();
  }
}
