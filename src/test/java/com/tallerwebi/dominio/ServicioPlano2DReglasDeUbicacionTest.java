package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * HU-33A: la generacion del plano aplica las reglas de ubicacion segun la categoria
 * - Un mueble sin categoria se ubica y se avisa que asignar una categoria mejora el resultado.
 * - Una cama que no queda opuesta a la ventana genera un aviso.
 */
public class ServicioPlano2DReglasDeUbicacionTest {

  private ServicioPlano2D servicioPlano2D;

  @BeforeEach
  public void init() {
    servicioPlano2D = new ServicioPlano2DImpl(new ServicioZonaExclusionImpl());
  }

  @Test
  public void deberiaAvisarQueAsignarUnaCategoriaMejoraElResultadoSiElMuebleNoTieneCategoria() {
    //Given
    Ambiente ambiente = new Ambiente(4.0, 4.0);
    List<Mueble> muebles = givenMuebles(new Mueble("Silla", 1.0, 1.0));

    //When
    Plano plano = servicioPlano2D.generarPlano(ambiente, muebles);

    //Then
    assertThat(plano.getMuebles().size(), is(1));
    assertThat(plano.getMensaje(), containsString("Asignarle una categoria"));
  }

  @Test
  public void noDeberiaAvisarNadaSiElMuebleTieneCategoria() {
    //Given
    Ambiente ambiente = new Ambiente(4.0, 4.0);
    List<Mueble> muebles = givenMuebles(givenMuebleConCategoria("Mesa", "Mesa ratona"));

    //When
    Plano plano = servicioPlano2D.generarPlano(ambiente, muebles);

    //Then
    assertThat(plano.getMensaje(), is(nullValue()));
  }

  @Test
  public void deberiaAvisarSiLaCamaNoQuedaOpuestaALaVentana() {
    //Given
    Ambiente ambiente = new Ambiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 1.0, 1.0, null));
    List<Mueble> muebles = givenMuebles(givenMuebleConCategoria("Cama", "Cama individual"));

    //When
    Plano plano = servicioPlano2D.generarPlano(ambiente, muebles);

    //Then
    assertThat(plano.getMensaje(), containsString("no quedo opuesto a la ventana"));
  }

  private Mueble givenMuebleConCategoria(String nombre, String categoria) {
    Mueble mueble = new Mueble(nombre, 1.0, 2.0);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, 1.0, 2.0));
    return mueble;
  }

  private List<Mueble> givenMuebles(Mueble mueble) {
    List<Mueble> muebles = new ArrayList<Mueble>();
    muebles.add(mueble);
    return muebles;
  }
}
