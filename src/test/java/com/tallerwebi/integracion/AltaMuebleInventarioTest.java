package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.ServicioMaterial;
import com.tallerwebi.dominio.ServicioPlano2D;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.presentacion.ControladorInventarioMuebles;
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
public class AltaMuebleInventarioTest {

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
  public void deberiaGuardarLaCategoriaCuandoAgregoUnMuebleDesdeElFormulario() throws Exception {
    CategoriaDeMueble sillones = this.dadoQueExisteLaCategoria("Sillón");

    this.cuandoAgregoElMueble("Mesa ratona", sillones.getId());

    this.entoncesElMuebleGuardadoTieneLaCategoria("Mesa ratona", sillones);
  }

  @Test
  public void deberiaAsignarElMaterialCuandoAgregoUnMuebleDesdeElFormulario() {
    ServicioEdicionInventarioMuebles servicioEdicion = mock(ServicioEdicionInventarioMuebles.class);
    ServicioMaterial servicioMaterial = mock(ServicioMaterial.class);
    ControladorInventarioMuebles controlador = new ControladorInventarioMuebles(
      servicioEdicion,
      mock(ServicioPlano2D.class),
      mock(ServicioCategoriaDeMueble.class),
      null,
      servicioMaterial
    );
    Material bambu = new Material("Bambú", true);
    bambu.setId(3L);
    when(servicioMaterial.buscarPorId(3L)).thenReturn(bambu);
    Mueble datos = new Mueble("Mesa ratona", 1.2, 0.6);

    controlador.agregar(datos, null, 3L);

    verify(servicioEdicion).crear(argThat(m -> m.getMaterial() == bambu));
  }

  private CategoriaDeMueble dadoQueExisteLaCategoria(String nombre) {
    CategoriaDeMueble categoria = new CategoriaDeMueble(nombre, 1.0, 0.8);
    this.sessionFactory.getCurrentSession().persist(categoria);
    this.sessionFactory.getCurrentSession().flush();
    return categoria;
  }

  private void cuandoAgregoElMueble(String nombre, Long categoriaId) throws Exception {
    this.mockMvc.perform(
        post("/inventario-muebles/agregar")
          .param("nombre", nombre)
          .param("ancho", "1.2")
          .param("largo", "0.6")
          .param("categoria", String.valueOf(categoriaId))
          .param("estilo", Estilo.Japandi.name())
      )
      .andExpect(status().is3xxRedirection());
  }

  private void entoncesElMuebleGuardadoTieneLaCategoria(
    String nombreDelMueble,
    CategoriaDeMueble categoriaEsperada
  ) {
    Mueble mueble =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Mueble WHERE nombre = :nombre", Mueble.class)
        .setParameter("nombre", nombreDelMueble)
        .uniqueResult();
    assertThat(mueble.getCategoriaDeMueble(), is(categoriaEsperada));
  }
}
