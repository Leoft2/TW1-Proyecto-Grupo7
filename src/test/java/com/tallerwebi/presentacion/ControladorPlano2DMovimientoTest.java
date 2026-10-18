package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    muebleUbicado = new MuebleUbicado(new Mueble("Sofa", 1.0, 1.0), 0.0, 0.0);
    Plano plano = new Plano(
      new Ambiente(4.0, 3.0),
      new ArrayList<>(List.of(muebleUbicado)),
      new ArrayList<>(),
      new ArrayList<>()
    );
    sesion.setAttribute("plano", plano);
  }

  @Test
  public void deberiaActualizarYGuardarLaPosicionCuandoElMovimientoEsValido() {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(
      servicioPlano2D.validarMovimiento(eq(muebleUbicado), any(Ambiente.class), eq(1.0), eq(1.0))
    )
      .thenReturn(new ValidacionMovimiento(false));

    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    assertEquals(HttpStatus.OK, respuesta.getStatusCode());
    assertEquals(1.0, muebleUbicado.getPosicionX(), 0.0);
    assertEquals(1.0, muebleUbicado.getPosicionY(), 0.0);
    Plano planoGuardado = (Plano) sesion.getAttribute("plano");
    assertEquals(muebleUbicado, planoGuardado.getMuebles().get(0));
  }

  @Test
  public void deberiaRechazarElMovimientoCuandoElMuebleSaleDelAmbiente() {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(false);

    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 3.5, 0.0, sesion);

    assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
    assertEquals(0.0, muebleUbicado.getPosicionX(), 0.0);
    assertEquals(0.0, muebleUbicado.getPosicionY(), 0.0);
    verify(servicioPlano2D, never())
      .validarMovimiento(any(MuebleUbicado.class), any(Ambiente.class), anyDouble(), anyDouble());
  }

  @Test
  public void deberiaRechazarElMovimientoCuandoInterfiereConUnaZonaRestringida() {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(
      servicioPlano2D.validarMovimiento(eq(muebleUbicado), any(Ambiente.class), eq(1.0), eq(1.0))
    )
      .thenReturn(new ValidacionMovimiento(true));

    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
    assertEquals(0.0, muebleUbicado.getPosicionX(), 0.0);
    assertEquals(0.0, muebleUbicado.getPosicionY(), 0.0);
  }
}
