package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRankingOrientacionTest {

  private ServicioRankingDeDistribuciones ranking;
  private ServicioOrientacionFuncional orientacion;

  @BeforeEach
  public void init() {
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ranking =
      new ServicioRankingDeDistribucionesImpl(
        new ServicioPlano2DImpl(zonas),
        new ServicioReglasDeUbicacionImpl(zonas),
        zonas
      );
    orientacion = new ServicioOrientacionFuncionalImpl();
  }

  @Test
  public void deberiaBuscarUnaCamaConAccesoEnAmbosLaterales() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Cama doble", 1.4, 1.9));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  @Test
  public void deberiaBuscarUnSofaAlineadoConElRack() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá 3 cuerpos", 2.0, 1.0));
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  @Test
  public void deberiaBuscarUnaSillaAlineadaConLaMesa() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Mesa de comedor", 1.6, 0.9));
    muebles.add(givenMueble("Silla", 0.5, 0.5));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  @Test
  public void deberiaBuscarUnaCamaYUnSofaConSuRack() {
    //Given
    Ambiente ambiente = new Ambiente(8.0, 8.0);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Cama doble", 1.4, 1.9));
    muebles.add(givenMueble("Sofá 3 cuerpos", 2.0, 1.0));
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  private Mueble givenMueble(String categoria, Double ancho, Double largo) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    return mueble;
  }
}
