package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class MuebleTest {

  @Test
  public void deberiaDevolverLaEtiquetaDeLaCategoriaCuandoTieneEtiqueta() {
    Mueble sofa = givenUnMuebleConCategoria("Sofa de tres cuerpos", "Sofá 3 cuerpos", "Sofá");

    String nombreVisible = whenConsultoElNombreVisible(sofa);

    assertThat(nombreVisible, is(equalTo("Sofá")));
  }

  @Test
  public void deberiaDevolverElNombreDeLaCategoriaCuandoNoTieneEtiqueta() {
    Mueble biblioteca = givenUnMuebleConCategoria("Biblioteca Minimalista", "Biblioteca", null);

    String nombreVisible = whenConsultoElNombreVisible(biblioteca);

    assertThat(nombreVisible, is(equalTo("Biblioteca")));
  }

  @Test
  public void deberiaDevolverElNombreDelMuebleCuandoNoTieneCategoria() {
    Mueble muebleSuelto = givenUnMuebleSinCategoria("Sofa de living");

    String nombreVisible = whenConsultoElNombreVisible(muebleSuelto);

    assertThat(nombreVisible, is(equalTo("Sofa de living")));
  }

  private Mueble givenUnMuebleConCategoria(String nombre, String categoria, String etiqueta) {
    Mueble mueble = new Mueble(nombre, 1.0, 1.0);
    CategoriaDeMueble categoriaDeMueble = new CategoriaDeMueble(categoria, 1.0, 1.0);
    categoriaDeMueble.setEtiqueta(etiqueta);
    mueble.setCategoriaDeMueble(categoriaDeMueble);
    return mueble;
  }

  private Mueble givenUnMuebleSinCategoria(String nombre) {
    return new Mueble(nombre, 1.0, 1.0);
  }

  private String whenConsultoElNombreVisible(Mueble mueble) {
    return mueble.getNombreVisible();
  }
}
