package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CriteriosDeMuebleTest {

  @Test
  public void dadoQueNoHayCriterios_cuandoEvaluaUnMueble_entoncesLoAcepta() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(null, null, null, null, null);

    assertThat(criterios.cumple(unMueble("Sillón", 85000.0, Estilo.Retro, null, null)), is(true));
  }

  @Test
  public void dadoUnEstilo_cuandoEvaluaUnMuebleDeEseEstilo_entoncesLoAcepta() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(null, null, Estilo.Retro, null, null);

    assertThat(criterios.cumple(unMueble("Sillón", 85000.0, Estilo.Retro, null, null)), is(true));
    assertThat(criterios.cumple(unMueble("Mesa", 85000.0, Estilo.Japandi, null, null)), is(false));
  }

  @Test
  public void dadoUnPrecioMinimo_cuandoEvaluaUnMueble_entoncesSoloAceptaLosQueLleganAlPiso() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(50000.0, null, null, null, null);

    assertThat(criterios.cumple(unMueble("Sillón", 50000.0, Estilo.Retro, null, null)), is(true));
    assertThat(criterios.cumple(unMueble("Mesa", 49999.0, Estilo.Retro, null, null)), is(false));
  }

  @Test
  public void dadoUnPrecioMaximo_cuandoEvaluaUnMueble_entoncesSoloAceptaLosQueNoSuperanElTope() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(null, 90000.0, null, null, null);

    assertThat(criterios.cumple(unMueble("Sillón", 90000.0, Estilo.Retro, null, null)), is(true));
    assertThat(criterios.cumple(unMueble("Mesa", 90001.0, Estilo.Retro, null, null)), is(false));
  }

  @Test
  public void dadoUnTipoDeAmbiente_cuandoEvaluaUnMueble_entoncesSoloAceptaLosQueLoAdmiten() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(
      null,
      null,
      null,
      new TipoDeAmbiente("Living"),
      null
    );

    Mueble paraLiving = unMueble(
      "Sillón",
      85000.0,
      Estilo.Retro,
      null,
      new TipoDeAmbiente("Living")
    );
    Mueble paraCocina = unMueble("Mesa", 85000.0, Estilo.Retro, null, new TipoDeAmbiente("Cocina"));

    assertThat(criterios.cumple(paraLiving), is(true));
    assertThat(criterios.cumple(paraCocina), is(false));
  }

  @Test
  public void dadoUnTipoDeAmbienteSinNombre_cuandoEvaluaUnMueble_entoncesLoAcepta() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(
      null,
      null,
      null,
      new TipoDeAmbiente(null),
      null
    );

    assertThat(criterios.cumple(unMueble("Sillón", 85000.0, Estilo.Retro, null, null)), is(true));
  }

  @Test
  public void dadoUnIdDeMaterial_cuandoEvaluaUnMueble_entoncesSoloAceptaLosDeEseMaterial() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(null, null, null, null, 3L);

    Material bambu = new Material("Bambú", true);
    bambu.setId(3L);
    Material metal = new Material("Metal", false);
    metal.setId(5L);

    assertThat(criterios.cumple(unMueble("Silla", 30000.0, Estilo.Retro, bambu, null)), is(true));
    assertThat(criterios.cumple(unMueble("Mesa", 40000.0, Estilo.Retro, metal, null)), is(false));
  }

  @Test
  public void dadoUnIdDeMaterial_cuandoEvaluaUnMuebleSinMaterial_entoncesNoLoAcepta() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(null, null, null, null, 3L);

    assertThat(criterios.cumple(unMueble("Sillón", 85000.0, Estilo.Retro, null, null)), is(false));
  }

  @Test
  public void dadosVariosCriterios_cuandoEvaluaUnMueble_entoncesDebeCumplirlosTodos() {
    CriteriosDeMueble criterios = new CriteriosDeMueble(
      20000.0,
      90000.0,
      Estilo.Retro,
      new TipoDeAmbiente("Living"),
      null
    );

    assertThat(
      criterios.cumple(
        unMueble("Sillón", 85000.0, Estilo.Retro, null, new TipoDeAmbiente("Living"))
      ),
      is(true)
    );
    assertThat(
      criterios.cumple(
        unMueble("Sillón caro", 150000.0, Estilo.Retro, null, new TipoDeAmbiente("Living"))
      ),
      is(false)
    );
  }

  private Mueble unMueble(
    String nombre,
    double precio,
    Estilo estilo,
    Material material,
    TipoDeAmbiente tipoDeAmbiente
  ) {
    Mueble mueble = new Mueble(nombre, 1.0, 1.0);
    mueble.setPrecio(precio);
    mueble.setEstilo(estilo);
    mueble.setMaterial(material);
    List<TipoDeAmbiente> tiposDeAmbiente = new ArrayList<>();
    if (tipoDeAmbiente != null) {
      tiposDeAmbiente.add(tipoDeAmbiente);
    }
    mueble.setTiposDeAmbiente(tiposDeAmbiente);
    return mueble;
  }
}
