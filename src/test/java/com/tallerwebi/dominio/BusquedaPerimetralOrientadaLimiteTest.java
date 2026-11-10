package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class BusquedaPerimetralOrientadaLimiteTest {

  @Test
  public void deberiaDetenerLaBusquedaAlLlegarAlLimiteDeIntentos() {
    //Given
    ServicioPlano2D servicioPlano = mock(ServicioPlano2D.class);
    ServicioReglasDeUbicacion reglas = mock(ServicioReglasDeUbicacion.class);
    ServicioOrientacionFuncional orientacion = mock(ServicioOrientacionFuncional.class);
    when(servicioPlano.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(
      reglas.esUbicacionValida(any(MuebleUbicado.class), any(Ambiente.class), anyList(), anyList())
    )
      .thenReturn(true);
    BusquedaPerimetralOrientada busqueda = new BusquedaPerimetralOrientada(
      servicioPlano,
      reglas,
      orientacion
    );
    List<Mueble> muebles = givenMuebles();

    //When
    Optional<Plano> resultado = busqueda.buscar(new Ambiente(8.0, 8.0), muebles, new ArrayList<>());

    //Then
    assertThat(resultado.isEmpty(), is(equalTo(true)));
    verify(servicioPlano, times(20000))
      .estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class));
  }

  private List<Mueble> givenMuebles() {
    List<Mueble> muebles = new ArrayList<>();
    for (int indice = 0; indice < 4; indice++) {
      Mueble silla = new Mueble("Silla", 0.5, 0.5);
      silla.setCategoriaDeMueble(new CategoriaDeMueble("Silla", 0.5, 0.5));
      muebles.add(silla);
    }
    return muebles;
  }
}
