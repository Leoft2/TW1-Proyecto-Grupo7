package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPlano2DRecalculoTest {

  private ServicioPlano2D servicioPlano2D;

  @BeforeEach
  public void init() {
    servicioPlano2D = new ServicioPlano2DImpl(new ServicioZonaExclusionImpl());
  }

  @Test
  public void deberiaMantenerElMuebleAncladoEnSuPosicionYRedistribuirElResto() {
    //Given
    MuebleUbicado anclado = givenTengoUnMuebleUbicado("Mesa", 2.0, 1.0);
    MuebleUbicado libre = givenTengoUnMuebleUbicado("Silla", 4.0, 2.0);
    anclado.anclar();
    Plano plano = givenTengoUnPlanoCon(anclado, libre);

    //When
    Plano recalculado = servicioPlano2D.recalcularPlano(plano);

    //Then
    assertThat(recalculado.getMuebles().size(), is(equalTo(2)));
    assertThat(anclado.getPosicionX(), is(equalTo(2.0)));
    assertThat(anclado.getPosicionY(), is(equalTo(1.0)));
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionX(), is(equalTo(0.0)));
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionY(), is(equalTo(0.0)));
  }

  @Test
  public void deberiaMantenerFijosTodosLosMueblesAncladosAlRecalcular() {
    //Given
    MuebleUbicado primerAnclado = givenTengoUnMuebleUbicado("Mesa", 2.0, 1.0);
    MuebleUbicado segundoAnclado = givenTengoUnMuebleUbicado("Escritorio", 3.0, 1.0);
    MuebleUbicado libre = givenTengoUnMuebleUbicado("Silla", 4.0, 2.0);
    primerAnclado.anclar();
    segundoAnclado.anclar();
    Plano plano = givenTengoUnPlanoCon(primerAnclado, segundoAnclado, libre);

    //When
    Plano recalculado = servicioPlano2D.recalcularPlano(plano);

    //Then
    assertThat(buscarUbicadoDe("Mesa", recalculado).getPosicionX(), is(equalTo(2.0)));
    assertThat(buscarUbicadoDe("Mesa", recalculado).getPosicionY(), is(equalTo(1.0)));
    assertThat(buscarUbicadoDe("Escritorio", recalculado).getPosicionX(), is(equalTo(3.0)));
    assertThat(buscarUbicadoDe("Escritorio", recalculado).getPosicionY(), is(equalTo(1.0)));
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionX(), is(equalTo(0.0)));
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionY(), is(equalTo(0.0)));
  }

  @Test
  public void deberiaNoUbicarElRestoSobreLaZonaDeUnMuebleAnclado() {
    //Given
    MuebleUbicado anclado = givenTengoUnMuebleUbicado("Mesa", 0.0, 0.0);
    MuebleUbicado libre = givenTengoUnMuebleUbicado("Silla", 4.0, 2.0);
    anclado.anclar();
    Plano plano = givenTengoUnPlanoCon(anclado, libre);

    //When
    Plano recalculado = servicioPlano2D.recalcularPlano(plano);

    //Then
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionX(), is(equalTo(1.0)));
    assertThat(buscarUbicadoDe("Silla", recalculado).getPosicionY(), is(equalTo(0.0)));
  }

  private MuebleUbicado givenTengoUnMuebleUbicado(String nombre, double x, double y) {
    return new MuebleUbicado(new Mueble(nombre, 1.0, 1.0), x, y);
  }

  private Plano givenTengoUnPlanoCon(MuebleUbicado... mueblesUbicados) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    for (MuebleUbicado muebleUbicado : mueblesUbicados) {
      muebles.add(muebleUbicado);
    }
    return new Plano(new Ambiente(6.0, 4.0), muebles, new ArrayList<>(), new ArrayList<>());
  }

  private MuebleUbicado buscarUbicadoDe(String nombre, Plano plano) {
    for (MuebleUbicado muebleUbicado : plano.getMuebles()) {
      if (muebleUbicado.getMueble().getNombre().equals(nombre)) {
        return muebleUbicado;
      }
    }
    return null;
  }
}
