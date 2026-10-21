package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.RepositorioMaterial;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioMaterialTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioMaterial repositorioMaterial;

  @BeforeEach
  public void init() {
    repositorioMaterial = new RepositorioMaterialImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueHayMaterialesGuardados_cuandoLosPido_entoncesLosObtengoOrdenadosPorNombre() {
    this.dadoQueExisteElMaterial("Metal", false);
    this.dadoQueExisteElMaterial("Bambú", true);
    this.dadoQueExisteElMaterial("Melamina", false);

    List<Material> materiales = repositorioMaterial.obtenerTodos();

    assertThat(
      materiales.stream().map(Material::getNombre).toList(),
      contains("Bambú", "Melamina", "Metal")
    );
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueGuardeUnMaterial_cuandoLoPido_entoncesConservaSiEsSostenible() {
    this.dadoQueExisteElMaterial("Bambú", true);

    Material bambu = repositorioMaterial.obtenerTodos().get(0);

    assertThat(bambu.isSostenible(), is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoHayMateriales_cuandoLosPido_entoncesObtengoUnaListaVacia() {
    assertThat(repositorioMaterial.obtenerTodos(), is(empty()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueGuardeUnMaterial_cuandoLoBuscoPorSuId_entoncesLoObtengo() {
    this.dadoQueExisteElMaterial("Metal", false);
    Material bambu = this.dadoQueExisteElMaterial("Bambú", true);

    Material encontrado = repositorioMaterial.buscarPorId(bambu.getId());

    assertThat(encontrado.getNombre(), is("Bambú"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoExisteElMaterial_cuandoLoBuscoPorSuId_entoncesNoObtengoNada() {
    assertThat(repositorioMaterial.buscarPorId(999L), is(nullValue()));
  }

  private Material dadoQueExisteElMaterial(String nombre, boolean sostenible) {
    Material material = new Material(nombre, sostenible);
    this.sessionFactory.getCurrentSession().persist(material);
    return material;
  }
}
