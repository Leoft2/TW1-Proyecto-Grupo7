package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEdicionDePlanoTest {

  private ServicioEdicionDePlano servicioEdicionDePlano;
  private Plano plano;
  private Mueble silla;
  private Mueble mesa;

  @BeforeEach
  public void init() {
    servicioEdicionDePlano = new ServicioEdicionDePlanoImpl();
    silla = new Mueble("Silla", 1.0, 1.0);
    mesa = new Mueble("Mesa", 1.0, 1.0);
    plano = givenUnPlanoConDosMuebles();
  }

  @Test
  public void deberiaQuitarElMuebleSeleccionadoDelPlanoYDelAmbiente() {
    //Given
    int cantidadDeMueblesEnElAmbiente = plano.getAmbiente().getMuebles().size();

    //When
    boolean quitado = whenQuitoElMueble(0);

    //Then
    assertThat(cantidadDeMueblesEnElAmbiente, is(equalTo(0)));
    thenSeQuitoLaSillaDelPlanoYDelAmbiente(quitado);
  }

  @Test
  public void noDeberiaQuitarMueblesCuandoElIndiceNoEsValido() {
    //Given
    int cantidadDeMuebles = plano.getMuebles().size();

    //When
    boolean quitado = whenQuitoElMueble(2);

    //Then
    assertThat(quitado, is(equalTo(false)));
    assertThat(plano.getMuebles().size(), is(equalTo(cantidadDeMuebles)));
  }

  @Test
  public void deberiaMantenerElMuebleEnElAmbienteSiTieneOtraInstanciaEnElPlano() {
    //Given
    givenLaSillaApareceDosVecesEnElPlano();

    //When
    boolean quitado = whenQuitoElMueble(0);

    //Then
    assertThat(quitado, is(equalTo(true)));
    assertThat(plano.getMuebles().size(), is(equalTo(2)));
    assertThat(plano.getAmbiente().getMuebles().size(), is(equalTo(2)));
  }

  private Plano givenUnPlanoConDosMuebles() {
    List<MuebleUbicado> mueblesUbicados = new ArrayList<>();
    mueblesUbicados.add(new MuebleUbicado(silla, 0.0, 0.0));
    mueblesUbicados.add(new MuebleUbicado(mesa, 1.0, 0.0));
    return new Plano(new Ambiente(4.0, 3.0), mueblesUbicados, new ArrayList<>(), new ArrayList<>());
  }

  private void givenLaSillaApareceDosVecesEnElPlano() {
    plano.getMuebles().add(new MuebleUbicado(silla, 2.0, 0.0));
    plano.getAmbiente().agregarMueble(silla);
  }

  private boolean whenQuitoElMueble(int indice) {
    return servicioEdicionDePlano.quitarMueble(plano, indice);
  }

  private void thenSeQuitoLaSillaDelPlanoYDelAmbiente(boolean quitado) {
    assertThat(quitado, is(equalTo(true)));
    assertThat(plano.getMuebles().size(), is(equalTo(1)));
    assertThat(plano.getMuebles().get(0).getMueble(), is(equalTo(mesa)));
    assertThat(plano.getAmbiente().getMuebles().size(), is(equalTo(1)));
    assertThat(plano.getAmbiente().getMuebles().get(0), is(equalTo(mesa)));
  }
}
