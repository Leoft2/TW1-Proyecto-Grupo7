package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioPlano2D;
import com.tallerwebi.dominio.ValidacionMovimiento;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

public class ControladorPlano2DMovimientoTest {

  private ServicioPlano2D servicioPlano2D;
  private ControladorPlano2D controladorPlano2D;
  private MockHttpSession sesion;
  private MuebleUbicado muebleUbicado;

  @BeforeEach
  public void init() {
    servicioPlano2D = mock(ServicioPlano2D.class);
    controladorPlano2D =
      new ControladorPlano2D(servicioPlano2D, mock(ServicioCategoriaDeMueble.class));
    sesion = new MockHttpSession();
  }

  @Test
  public void deberiaActualizarYGuardarLaPosicionCuandoElMovimientoEsValido() {
    //Given
    givenTengoUnPlanoEnSesion();
    givenElMuebleEstaDentroDelPerimetro(true);
    givenElMovimientoTieneInterferencia(1.0, 1.0, false);

    //When
    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    //Then
    Plano planoGuardado = (Plano) sesion.getAttribute("plano");
    assertThat(respuesta.getStatusCode(), is(equalTo(HttpStatus.OK)));
    assertThat(muebleUbicado.getPosicionX(), is(equalTo(1.0)));
    assertThat(muebleUbicado.getPosicionY(), is(equalTo(1.0)));
    assertThat(planoGuardado.getMuebles().get(0), is(equalTo(muebleUbicado)));
  }

  @Test
  public void deberiaRechazarElMovimientoCuandoElMuebleSaleDelAmbiente() {
    //Given
    givenTengoUnPlanoEnSesion();
    givenElMuebleEstaDentroDelPerimetro(false);

    //When
    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 3.5, 0.0, sesion);

    //Then
    assertThat(respuesta.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
    assertThat(muebleUbicado.getPosicionX(), is(equalTo(0.0)));
    assertThat(muebleUbicado.getPosicionY(), is(equalTo(0.0)));
    verify(servicioPlano2D, never())
      .validarMovimiento(any(MuebleUbicado.class), any(Ambiente.class), anyDouble(), anyDouble());
  }

  @Test
  public void deberiaRechazarElMovimientoCuandoInterfiereConUnaZonaRestringida() {
    //Given
    givenTengoUnPlanoEnSesion();
    givenElMuebleEstaDentroDelPerimetro(true);
    givenElMovimientoTieneInterferencia(1.0, 1.0, true);

    //When
    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    //Then
    assertThat(respuesta.getStatusCode(), is(equalTo(HttpStatus.CONFLICT)));
    assertThat(muebleUbicado.getPosicionX(), is(equalTo(0.0)));
    assertThat(muebleUbicado.getPosicionY(), is(equalTo(0.0)));
  }

  private void givenTengoUnPlanoEnSesion() {
    muebleUbicado = new MuebleUbicado(new Mueble("Sofa", 1.0, 1.0), 0.0, 0.0);
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(muebleUbicado);
    Plano plano = new Plano(new Ambiente(4.0, 3.0), muebles, new ArrayList<>(), new ArrayList<>());
    sesion.setAttribute("plano", plano);
  }

  private void givenElMuebleEstaDentroDelPerimetro(boolean dentro) {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(dentro);
  }

  private void givenElMovimientoTieneInterferencia(double x, double y, boolean interfiere) {
    when(servicioPlano2D.validarMovimiento(eq(muebleUbicado), any(Ambiente.class), eq(x), eq(y)))
      .thenReturn(new ValidacionMovimiento(interfiere));
  }
}
