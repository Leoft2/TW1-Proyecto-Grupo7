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

  @Test
  public void dadoUnMaterial_cuandoLeAsignoUnId_entoncesLoConserva() {
    Material bambu = new Material("Bambú", true);

    bambu.setId(3L);

    assertThat(bambu.getId(), is(3L));
  }

  @Test
  public void dadosDosMaterialesConElMismoNombre_cuandoLosComparo_entoncesSonIguales() {
    Material uno = new Material("Bambú", true);
    Material otro = new Material("Bambú", false);

    assertThat(uno.equals(otro), is(true));
    assertThat(uno.hashCode(), is(otro.hashCode()));
  }
}
