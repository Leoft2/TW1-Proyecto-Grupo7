package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * HU-14: Validacion de accesibilidad y circulacion minima
 * - Dado un ancho minimo de pasillo configurado, cuando se genera la distribucion, se valida que se cumpla en todo el recorrido.
 * - Dado un tramo que no cumple el minimo, cuando se muestra el plano, ese tramo queda marcado visualmente.
 */
public class ServicioCirculacionTest {

  private ServicioCirculacion servicioCirculacion;

  @BeforeEach
  public void init() {
    servicioCirculacion = new ServicioCirculacionImpl();
  }

  @Test
  public void deberiaUsarElAnchoMinimoPorDefectoSiElAmbienteNoLoConfigura() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);

    //When
    Double minimo = ambiente.getAnchoMinimoDePasillo();

    //Then
    assertThat(minimo, is(equalTo(0.6)));
  }

  @Test
  public void deberiaNoMarcarNadaSiLosMueblesEstanPegadosAlMuro() {
    //Given
    Plano plano = givenPlano(givenAmbiente(4.0, 4.0), givenMuebleUbicado(1.0, 1.0, 0.0, 0.0));

    //When
    List<TramoEstrecho> tramos = whenCalculoLosTramosEstrechos(plano);

    //Then
    thenHayTramosEstrechos(tramos, 0);
  }

  @Test
  public void deberiaNoMarcarNadaSiTodosLosPasillosCumplenElMinimo() {
    //Given
    Plano plano = givenPlano(givenAmbiente(4.0, 4.0), givenMuebleUbicado(1.0, 1.0, 1.0, 1.0));

    //When
    List<TramoEstrecho> tramos = whenCalculoLosTramosEstrechos(plano);

    //Then
    thenHayTramosEstrechos(tramos, 0);
  }

  @Test
  public void deberiaMarcarElTramoEntreUnMuebleYElMuroSiNoAlcanzaElMinimo() {
    //Given
    Plano plano = givenPlano(givenAmbiente(4.0, 4.0), givenMuebleUbicado(1.0, 1.0, 0.3, 1.5));

    //When
    List<TramoEstrecho> tramos = whenCalculoLosTramosEstrechos(plano);

    //Then
    thenHayTramosEstrechos(tramos, 1);
    thenElTramoTieneAnchoLibre(tramos.get(0), 0.3);
  }

  @Test
  public void deberiaMarcarElTramoEntreDosMueblesSiNoAlcanzaElMinimo() {
    //Given
    Plano plano = givenPlano(givenAmbiente(6.0, 6.0), givenMuebleUbicado(1.0, 1.0, 2.0, 2.0));
    plano.getMuebles().add(givenMuebleUbicado(1.0, 1.0, 3.4, 2.0));

    //When
    List<TramoEstrecho> tramos = whenCalculoLosTramosEstrechos(plano);

    //Then
    thenHayTramosEstrechos(tramos, 1);
    thenElTramoTieneAnchoLibre(tramos.get(0), 0.4);
  }

  @Test
  public void deberiaRespetarElAnchoMinimoConfiguradoEnElAmbiente() {
    //Given
    Ambiente ambiente = givenAmbiente(6.0, 6.0);
    ambiente.setAnchoMinimoDePasillo(0.3);
    Plano plano = givenPlano(ambiente, givenMuebleUbicado(1.0, 1.0, 0.4, 2.0));

    //When
    List<TramoEstrecho> tramos = whenCalculoLosTramosEstrechos(plano);

    //Then
    thenHayTramosEstrechos(tramos, 0);
  }

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private MuebleUbicado givenMuebleUbicado(
    Double ancho,
    Double largo,
    Double posicionX,
    Double posicionY
  ) {
    return new MuebleUbicado(new Mueble("Mueble", ancho, largo), posicionX, posicionY);
  }

  private Plano givenPlano(Ambiente ambiente, MuebleUbicado mueble) {
    Plano plano = new Plano(
      ambiente,
      new ArrayList<MuebleUbicado>(),
      new ArrayList<Mueble>(),
      new ArrayList<String>()
    );
    plano.getMuebles().add(mueble);
    return plano;
  }

  private List<TramoEstrecho> whenCalculoLosTramosEstrechos(Plano plano) {
    return servicioCirculacion.calcularTramosEstrechos(plano);
  }

  private void thenHayTramosEstrechos(List<TramoEstrecho> tramos, int cantidad) {
    assertThat(tramos.size(), is(equalTo(cantidad)));
  }

  private void thenElTramoTieneAnchoLibre(TramoEstrecho tramo, Double anchoLibre) {
    assertThat(tramo.getAnchoLibre(), is(closeTo(anchoLibre, 0.0001)));
  }
}
