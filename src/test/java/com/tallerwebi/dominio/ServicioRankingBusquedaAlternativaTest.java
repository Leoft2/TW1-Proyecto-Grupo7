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

public class ServicioRankingBusquedaAlternativaTest {

  @Test
  public void deberiaBuscarVariantesDelPlanoAlternativoConDosCamas() {
    //Given
    Ambiente ambiente = new Ambiente(10.0, 10.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioPlano2D servicioPlano = spy(new ServicioPlano2DImpl(zonas));
    Plano planoSinMuebles = new Plano(
      ambiente,
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    doReturn(planoSinMuebles).when(servicioPlano).generarPlano(any(Ambiente.class), anyList());
    ServicioReglasDeUbicacion reglas = new ServicioReglasDeUbicacionImpl(zonas);
    ServicioOrientacionFuncional orientacion = new ServicioOrientacionFuncionalImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      servicioPlano,
      reglas,
      zonas,
      orientacion
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenCama("A"));
    muebles.add(givenCama("B"));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.size(), is(equalTo(3)));
    for (OpcionDeDistribucion opcion : opciones) {
      Plano plano = opcion.getPlano();
      assertThat(opcion.getPuntajeTotal(), is(equalTo(4)));
      assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
      for (MuebleUbicado ubicado : plano.getMuebles()) {
        List<MuebleUbicado> otros = new ArrayList<>(plano.getMuebles());
        otros.remove(ubicado);
        assertThat(reglas.esUbicacionValida(ubicado, ambiente, otros), is(equalTo(true)));
      }
    }
    for (int primero = 0; primero < opciones.size(); primero++) {
      for (int segundo = primero + 1; segundo < opciones.size(); segundo++) {
        assertThat(
          mismasPosiciones(opciones.get(primero).getPlano(), opciones.get(segundo).getPlano()),
          is(equalTo(false))
        );
      }
    }
  }

  @Test
  public void noDeberiaAgregarOpcionesCuandoDosCamasNoCabenConAccesoLateral() {
    //Given
    Ambiente ambiente = new Ambiente(2.0, 2.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenCama("A"));
    muebles.add(givenCama("B"));

    //When
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);

    //Then
    assertThat(opciones.size(), is(equalTo(0)));
  }

  private boolean mismasPosiciones(Plano primero, Plano segundo) {
    for (int indice = 0; indice < primero.getMuebles().size(); indice++) {
      MuebleUbicado uno = primero.getMuebles().get(indice);
      MuebleUbicado otro = segundo.getMuebles().get(indice);
      if (
        !uno.getPosicionX().equals(otro.getPosicionX()) ||
        !uno.getPosicionY().equals(otro.getPosicionY())
      ) {
        return false;
      }
    }
    return true;
  }

  private Mueble givenCama(String nombre) {
    Mueble cama = new Mueble(nombre, 1.4, 1.9);
    cama.setCategoriaDeMueble(new CategoriaDeMueble("Cama doble", 1.4, 1.9));
    return cama;
  }
}
