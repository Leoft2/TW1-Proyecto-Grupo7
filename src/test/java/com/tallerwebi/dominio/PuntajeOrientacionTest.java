package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class PuntajeOrientacionTest {

  private ServicioOrientacionFuncional orientacion;
  private ServicioRankingDeDistribuciones ranking;

  @BeforeEach
  public void init() {
    orientacion = new ServicioOrientacionFuncionalImpl();
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ranking =
      new ServicioRankingDeDistribucionesImpl(
        new ServicioPlano2DImpl(zonas),
        new ServicioReglasDeUbicacionImpl(zonas),
        zonas
      );
  }

  @Test
  public void deberiaSumarPuntoPorCamaConAccesoAdemasDelPuntajeExistente() {
    //Given
    List<Mueble> muebles = givenMuebles("Cama doble");
    Ambiente ambiente = new Ambiente(5.0, 5.0);

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    assertThat(opciones.get(0).getPuntajeTotal(), is(equalTo(2)));
    assertThat(
      opciones.get(0).getDesglose().toString(),
      containsString("acceso libre a ambos laterales (+1)")
    );
  }

  @Test
  public void deberiaSumarPuntoPorSofaHaciaRack() {
    //Given
    List<Mueble> muebles = givenMuebles("Sofá 3 cuerpos", "Rack de TV");
    Ambiente ambiente = new Ambiente(5.0, 5.0);

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    assertThat(opciones.get(0).getPuntajeTotal(), is(equalTo(1)));
    assertThat(
      opciones.get(0).getDesglose().toString(),
      containsString("sofa orientado hacia el rack de TV (+1)")
    );
  }

  @Test
  public void deberiaSumarPuntoPorSillaHaciaMesa() {
    //Given
    List<Mueble> muebles = givenMuebles("Mesa de comedor", "Silla");
    Ambiente ambiente = new Ambiente(5.0, 5.0);

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    assertThat(opciones.get(0).getPuntajeTotal(), is(equalTo(1)));
    assertThat(
      opciones.get(0).getDesglose().toString(),
      containsString("silla orientada hacia la mesa (+1)")
    );
  }

  @Test
  public void deberiaNoPuntuarRelacionesSinReferencia() {
    //Given
    List<Mueble> muebles = givenMuebles("Sofá 3 cuerpos", "Silla");
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(new MuebleUbicado(muebles.get(0), 0.0, 0.0));
    ubicados.add(new MuebleUbicado(muebles.get(1), 2.0, 0.0));
    Plano plano = new Plano(new Ambiente(5.0, 5.0), ubicados, new ArrayList<>(), new ArrayList<>());

    //When
    List<String> desglose = orientacion.desgloseOrientacion(plano);

    //Then
    assertThat(desglose.size(), is(equalTo(0)));
    assertThat(orientacion.puntajeMaximo(muebles), is(equalTo(0)));
  }

  @Test
  public void deberiaContarCadaSillaYUnaCamaEnElMaximo() {
    //Given
    List<Mueble> muebles = givenMuebles("Mesa de comedor", "Silla", "Silla", "Cama doble");

    //When
    int maximo = orientacion.puntajeMaximo(muebles);

    //Then
    assertThat(maximo, is(equalTo(3)));
  }

  @Test
  public void deberiaSumarUnaVezPorCadaSillaAunqueHayaDosMesas() {
    //Given
    Plano plano = givenPlano();
    givenMuebleUbicado(plano, "Silla", 0.5, 0.5, 1.0, 0.0);
    givenMuebleUbicado(plano, "Silla", 0.5, 0.5, 3.0, 0.0);
    givenMuebleUbicado(plano, "Mesa de comedor", 2.5, 1.0, 1.0, 2.0);
    givenMuebleUbicado(plano, "Mesa de luz", 2.5, 0.5, 1.0, 4.5);

    //When
    List<String> desglose = orientacion.desgloseOrientacion(plano);

    //Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
    assertThat(desglose.size(), is(equalTo(2)));
    assertThat(desglose.get(0), containsString("silla orientada hacia la mesa (+1)"));
    assertThat(desglose.get(1), containsString("silla orientada hacia la mesa (+1)"));
  }

  @Test
  public void noDeberiaPuntuarOrientacionesInvalidas() {
    //Given
    Plano plano = givenPlano();
    givenMuebleUbicado(plano, "Sofá", 1.0, 1.0, 1.0, 0.0);
    givenMuebleUbicado(plano, "Rack de TV", 1.0, 0.5, 4.0, 4.5);
    givenMuebleUbicado(plano, "Cama", 1.0, 1.0, 0.0, 0.0);

    //When
    List<String> desglose = orientacion.desgloseOrientacion(plano);

    //Then
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(false)));
    assertThat(desglose.size(), is(equalTo(0)));
  }

  @Test
  public void deberiaConservarLaRestaAnteriorSiLaCamaNoCumpleLaReglaDeVentana() {
    //Given
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioReglasDeUbicacion reglas = spy(new ServicioReglasDeUbicacionImpl(zonas));
    doReturn(false)
      .when(reglas)
      .cumpleReglasDeCategoria(any(MuebleUbicado.class), any(Ambiente.class));
    ServicioRankingDeDistribuciones rankingConReglaIncumplida =
      new ServicioRankingDeDistribucionesImpl(new ServicioPlano2DImpl(zonas), reglas, zonas);
    List<Mueble> muebles = givenMuebles("Cama doble");

    //When
    List<OpcionDeDistribucion> opciones = rankingConReglaIncumplida.generarRanking(
      new Ambiente(5.0, 5.0),
      muebles
    );

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    assertThat(opciones.get(0).getPuntajeTotal(), is(equalTo(0)));
    assertThat(
      opciones.get(0).getDesglose().toString(),
      containsString("no opuesta a la ventana (-1)")
    );
    assertThat(
      opciones.get(0).getDesglose().toString(),
      containsString("acceso libre a ambos laterales (+1)")
    );
  }

  @Test
  public void deberiaConservarElPuntajeDeVentanaConUnaVentanaReal() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 3.5, 1.0, 0.0));
    Plano camaOpuesta = new Plano(
      ambiente,
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    Plano camaNoOpuesta = new Plano(
      ambiente,
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    givenMuebleUbicado(camaOpuesta, "Cama", 1.0, 1.0, 1.0, 4.0);
    givenMuebleUbicado(camaNoOpuesta, "Cama", 1.0, 1.0, 1.0, 0.0);

    //When
    OpcionDeDistribucion opcionOpuesta = ReflectionTestUtils.invokeMethod(
      ranking,
      "puntuar",
      camaOpuesta,
      ambiente,
      new ArrayList<>()
    );
    OpcionDeDistribucion opcionNoOpuesta = ReflectionTestUtils.invokeMethod(
      ranking,
      "puntuar",
      camaNoOpuesta,
      ambiente,
      new ArrayList<>()
    );

    //Then
    assertThat(opcionOpuesta.getPuntajeTotal(), is(equalTo(2)));
    assertThat(opcionNoOpuesta.getPuntajeTotal(), is(equalTo(0)));
    assertThat(opcionOpuesta.getDesglose().toString(), containsString("opuesta a la ventana (+1)"));
    assertThat(
      opcionNoOpuesta.getDesglose().toString(),
      containsString("no opuesta a la ventana (-1)")
    );
  }

  @Test
  public void deberiaSumarLasReglasDeCamaYSofaEnElRanking() {
    //Given
    List<Mueble> muebles = givenMuebles("Cama doble", "Sofá", "Rack de TV");

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(new Ambiente(8.0, 8.0), muebles);

    //Then
    assertThat(opciones.isEmpty(), is(equalTo(false)));
    for (OpcionDeDistribucion opcion : opciones) {
      assertThat(orientacion.cumpleOrientacion(opcion.getPlano()), is(equalTo(true)));
      assertThat(opcion.getPuntajeTotal(), is(equalTo(3)));
      assertThat(opcion.getDesglose().size(), is(equalTo(3)));
    }
  }

  @Test
  public void deberiaMantenerLaInterfazCompatibleConUnaLambda() {
    //Given
    ServicioOrientacionFuncional orientacionConLambda = plano -> true;
    Plano plano = givenPlano();
    givenMuebleUbicado(plano, "Cama", 1.0, 1.0, 1.0, 0.0);

    //When
    List<String> desglose = orientacionConLambda.desgloseOrientacion(plano);

    //Then
    assertThat(desglose.size(), is(equalTo(1)));
    assertThat(orientacionConLambda.puntajeMaximo(givenMuebles("Cama")), is(equalTo(1)));
  }

  @Test
  public void deberiaIncluirLosPuntosDeOrientacionEnElMaximoDelRanking() {
    //Given
    List<Mueble> muebles = givenMuebles(
      "Cama",
      "Sofá",
      "Sofa",
      "Rack de TV",
      "Silla",
      "Silla",
      "Mesa",
      "Mesa de luz"
    );

    //When
    Integer maximo = ReflectionTestUtils.invokeMethod(ranking, "calcularPuntajeMaximo", muebles);

    //Then
    assertThat(maximo, is(equalTo(6)));
  }

  private Plano givenPlano() {
    return new Plano(
      new Ambiente(5.0, 5.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
  }

  private void givenMuebleUbicado(
    Plano plano,
    String categoria,
    Double ancho,
    Double largo,
    Double x,
    Double y
  ) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    plano.getMuebles().add(new MuebleUbicado(mueble, x, y));
  }

  private List<Mueble> givenMuebles(String... categorias) {
    List<Mueble> muebles = new ArrayList<>();
    for (String categoria : categorias) {
      Mueble mueble = new Mueble(categoria, 1.0, 1.0);
      mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, 1.0, 1.0));
      muebles.add(mueble);
    }
    return muebles;
  }
}
