package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RelacionSofaTelevisorTest {

  @Test
  public void deberiaAceptarLasDistanciasMinimaYMaxima() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofá", 2.0, 1.0, 3.0, 0.0);
    MuebleUbicado rack = givenMueble(plano, "Rack de TV", 1.0, 0.5, 3.5, 2.5);
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();

    //When y Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
    rack.moverA(3.5, 5.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
    rack.moverA(3.5, 2.49);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
    rack.moverA(3.5, 5.01);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
  }

  @Test
  public void deberiaExigirQueElRackEsteDelanteYParcialmenteAlineado() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofa", 2.0, 1.0, 3.0, 0.0);
    MuebleUbicado rack = givenMueble(plano, "Rack de TV", 1.0, 0.5, 4.9, 3.0);
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();

    //When y Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
    rack.moverA(5.0, 3.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
    rack.moverA(3.5, 0.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
  }

  @Test
  public void deberiaRechazarUnMuebleEnLaVisualPeroNoFueraDeElla() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofá", 2.0, 1.0, 3.0, 0.0);
    givenMueble(plano, "Rack de TV", 1.0, 0.5, 3.5, 4.0);
    MuebleUbicado mesa = givenMueble(plano, "Mesa", 0.5, 0.5, 3.7, 2.0);
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();

    //When y Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
    mesa.moverA(4.5, 2.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
  }

  @Test
  public void deberiaValidarLasCuatroDirecciones() {
    //Given
    RelacionSofaTelevisor relacion = new RelacionSofaTelevisor();
    MuebleUbicado sofa = givenMueble("Sofá", 2.0, 1.0, 4.0, 4.0);
    MuebleUbicado rack = givenMueble("Rack de TV", 1.0, 0.5, 4.5, 1.5);
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(sofa);
    muebles.add(rack);

    //When y Then
    assertThat(relacion.esValida(sofa, rack, Muro.SUPERIOR, muebles), is(equalTo(true)));
    rack.moverA(4.5, 6.5);
    assertThat(relacion.esValida(sofa, rack, Muro.INFERIOR, muebles), is(equalTo(true)));
    rack.moverA(1.5, 4.25);
    assertThat(relacion.esValida(sofa, rack, Muro.IZQUIERDO, muebles), is(equalTo(true)));
    rack.moverA(7.5, 4.25);
    assertThat(relacion.esValida(sofa, rack, Muro.DERECHO, muebles), is(equalTo(true)));
  }

  @Test
  public void cadaSofaDebeTenerUnRackValido() {
    //Given
    Plano plano = givenPlano();
    givenMueble(plano, "Sofá", 2.0, 1.0, 1.0, 0.0);
    givenMueble(plano, "Sofá", 2.0, 1.0, 6.0, 0.0);
    MuebleUbicado rack = givenMueble(plano, "Rack de TV", 1.0, 0.5, 1.5, 3.0);
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();

    //When y Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
    givenMueble(plano, "Rack de TV", 1.0, 0.5, 6.5, 3.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
    rack.moverA(4.0, 3.0);
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
  }

  @Test
  public void deberiaGenerarRelacionEnAmbienteMayorAlaDistanciaMaxima() {
    //Given
    Ambiente ambiente = new Ambiente(10.0, 10.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas,
      orientacion
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 2.0, 1.0, 0.0, 0.0).getMueble());
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5, 0.0, 0.0).getMueble());

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
      assertThat(opcion.getPuntajeTotal(), is(equalTo(1)));
    }
  }

  @Test
  public void deberiaEncontrarAlternativaEnLaBusquedaPerimetral() {
    //Given
    Ambiente ambiente = new Ambiente(10.0, 10.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioPlano2D servicioPlano = spy(new ServicioPlano2DImpl(zonas));
    Plano planoSinMuebles = givenPlano();
    doReturn(planoSinMuebles).when(servicioPlano).generarPlano(any(Ambiente.class), anyList());
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      servicioPlano,
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas,
      orientacion
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 2.0, 1.0, 0.0, 0.0).getMueble());
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5, 0.0, 0.0).getMueble());

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  @Test
  public void deberiaGenerarSinUbicarUnMuebleEnLaVisual() {
    //Given
    Ambiente ambiente = new Ambiente(10.0, 10.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas,
      orientacion
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 2.0, 1.0, 0.0, 0.0).getMueble());
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5, 0.0, 0.0).getMueble());
    muebles.add(givenMueble("Biblioteca", 1.0, 0.5, 0.0, 0.0).getMueble());

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
    }
  }

  @Test
  public void deberiaRechazarUnAmbienteSinEspacioParaLaDistanciaMinima() {
    //Given
    Ambiente ambiente = new Ambiente(2.0, 2.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 1.0, 1.0, 0.0, 0.0).getMueble());
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5, 0.0, 0.0).getMueble());

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(true)));
  }

  private Plano givenPlano() {
    return new Plano(
      new Ambiente(10.0, 10.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
  }

  private MuebleUbicado givenMueble(
    Plano plano,
    String categoria,
    double ancho,
    double largo,
    double posicionX,
    double posicionY
  ) {
    MuebleUbicado ubicado = givenMueble(categoria, ancho, largo, posicionX, posicionY);
    plano.getMuebles().add(ubicado);
    return ubicado;
  }

  private MuebleUbicado givenMueble(
    String categoria,
    double ancho,
    double largo,
    double posicionX,
    double posicionY
  ) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    return new MuebleUbicado(mueble, posicionX, posicionY);
  }
}
