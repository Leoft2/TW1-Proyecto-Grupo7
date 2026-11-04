package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioColisionesTest {

  private ServicioColisiones servicioColisiones;
  private ServicioReglasDeUbicacion servicioReglasDeUbicacion;

  @BeforeEach
  public void init() {
    servicioColisiones = new ServicioColisionesImpl();
    servicioReglasDeUbicacion = new ServicioReglasDeUbicacionImpl(new ServicioZonaExclusionImpl());
  }

  @Test
  public void deberiaDetectarColisionEntreDosSofasEnElMismoEspacio() {
    //Given
    MuebleUbicado sofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 1.0, 1.0);
    MuebleUbicado otroSofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 1.5, 1.2);
    List<MuebleUbicado> ubicados = givenLista(sofa);

    //When
    boolean hayColision = whenSeEvaluaLaColision(otroSofa, ubicados);

    //Then
    thenHayColision(hayColision, true);
  }

  @Test
  public void deberiaDetectarColisionCuandoUnaMesaTapaPorCompletoAOtra() {
    //Given
    MuebleUbicado mesaGrande = givenMuebleUbicado("Mesa", 2.0, 2.0, 1.0, 1.0);
    MuebleUbicado mesaChica = givenMuebleUbicado("Mesa", 1.0, 1.0, 1.0, 1.0);
    List<MuebleUbicado> ubicados = givenLista(mesaGrande);

    //When
    boolean hayColision = whenSeEvaluaLaColision(mesaChica, ubicados);

    //Then
    thenHayColision(hayColision, true);
  }

  @Test
  public void deberiaDetectarColisionCuandoUnMuebleQuedaDentroDeOtro() {
    //Given
    MuebleUbicado placard = givenMuebleUbicado("Placard", 3.0, 1.0, 0.0, 0.0);
    MuebleUbicado silla = givenMuebleUbicado("Silla", 0.5, 0.5, 1.0, 0.2);
    List<MuebleUbicado> ubicados = givenLista(placard);

    //When
    boolean hayColision = whenSeEvaluaLaColision(silla, ubicados);

    //Then
    thenHayColision(hayColision, true);
  }

  @Test
  public void deberiaNoDetectarColisionCuandoLosMueblesEstanPegados() {
    //Given
    MuebleUbicado sofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 0.0, 0.0);
    MuebleUbicado otroSofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 2.0, 0.0);
    List<MuebleUbicado> ubicados = givenLista(sofa);

    //When
    boolean hayColision = whenSeEvaluaLaColision(otroSofa, ubicados);

    //Then
    thenHayColision(hayColision, false);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueColisionaConOtroMueble() {
    //Given
    Ambiente ambiente = new Ambiente(6.0, 6.0);
    MuebleUbicado sofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 2.0, 2.0);
    MuebleUbicado candidato = givenMuebleUbicado("Mesa", 1.0, 1.0, 2.5, 2.0);
    List<MuebleUbicado> ubicados = givenLista(sofa);

    //When
    boolean esValida = whenSeEvaluaLaUbicacion(candidato, ambiente, ubicados);

    //Then
    thenHayColision(esValida, false);
  }

  @Test
  public void deberiaAceptarUnaUbicacionAlternativaSinColision() {
    //Given
    Ambiente ambiente = new Ambiente(6.0, 6.0);
    MuebleUbicado sofa = givenMuebleUbicado("Sofa", 2.0, 1.0, 2.0, 2.0);
    MuebleUbicado alternativa = givenMuebleUbicado("Mesa", 1.0, 1.0, 2.0, 4.0);
    List<MuebleUbicado> ubicados = givenLista(sofa);

    //When
    boolean esValida = whenSeEvaluaLaUbicacion(alternativa, ambiente, ubicados);

    //Then
    thenHayColision(esValida, true);
  }

  private MuebleUbicado givenMuebleUbicado(
    String nombre,
    Double ancho,
    Double largo,
    Double posicionX,
    Double posicionY
  ) {
    return new MuebleUbicado(new Mueble(nombre, ancho, largo), posicionX, posicionY);
  }

  private List<MuebleUbicado> givenLista(MuebleUbicado mueble) {
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(mueble);
    return ubicados;
  }

  private boolean whenSeEvaluaLaColision(MuebleUbicado candidato, List<MuebleUbicado> ubicados) {
    return servicioColisiones.hayColision(candidato, ubicados);
  }

  private boolean whenSeEvaluaLaUbicacion(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> ubicados
  ) {
    return servicioReglasDeUbicacion.esUbicacionValida(candidato, ambiente, ubicados);
  }

  private void thenHayColision(boolean resultado, boolean esperado) {
    assertThat(resultado, is(equalTo(esperado)));
  }
}
