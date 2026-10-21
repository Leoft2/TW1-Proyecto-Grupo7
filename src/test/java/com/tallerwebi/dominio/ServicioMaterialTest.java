package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMaterialTest {

  private static final Long ID_BAMBU = 3L;

  private RepositorioMaterial repositorioMaterial;
  private ServicioMaterial servicioMaterial;

  @BeforeEach
  public void init() {
    repositorioMaterial = mock(RepositorioMaterial.class);
    servicioMaterial = new ServicioMaterialImpl(repositorioMaterial);
  }

  @Test
  public void dadoQueHayMaterialesCargados_cuandoLosPido_entoncesLosObtengo() {
    List<Material> materiales = new ArrayList<>();
    materiales.add(new Material("Bambú", true));
    materiales.add(new Material("Metal", false));
    when(repositorioMaterial.obtenerTodos()).thenReturn(materiales);

    List<Material> obtenidos = servicioMaterial.obtenerMateriales();

    assertThat(obtenidos, is(materiales));
  }

  @Test
  public void dadoQueExisteUnMaterial_cuandoLoBuscoPorSuId_entoncesLoObtengo() {
    Material bambu = new Material("Bambú", true);
    bambu.setId(ID_BAMBU);
    when(repositorioMaterial.buscarPorId(ID_BAMBU)).thenReturn(bambu);

    Material obtenido = servicioMaterial.buscarPorId(ID_BAMBU);

    assertThat(obtenido, is(bambu));
  }

  @Test
  public void dadoQueNoIndicoUnIdDeMaterial_cuandoLoBusco_entoncesNoObtengoNada() {
    Material obtenido = servicioMaterial.buscarPorId(null);

    assertThat(obtenido, is(nullValue()));
  }
}
