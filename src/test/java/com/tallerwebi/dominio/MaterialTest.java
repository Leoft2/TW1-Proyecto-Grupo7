package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class MaterialTest {

  @Test
  public void dadoUnMaterialSostenible_cuandoConsultoSiEsSostenible_entoncesLoEs() {
    Material maderaCertificada = new Material("Madera certificada", true);

    assertThat(maderaCertificada.isSostenible(), is(true));
    assertThat(maderaCertificada.getNombre(), is("Madera certificada"));
  }

  @Test
  public void dadoUnMaterialNoSostenible_cuandoConsultoSiEsSostenible_entoncesNoLoEs() {
    Material melamina = new Material("Melamina", false);

    assertThat(melamina.isSostenible(), is(false));
    assertThat(melamina.getNombre(), is("Melamina"));
  }
}
