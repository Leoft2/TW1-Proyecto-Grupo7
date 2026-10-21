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
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import com.tallerwebi.dominio.ServicioPlano2D;
import com.tallerwebi.dominio.ValidacionMovimiento;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPlano2DAnclajeTest {

  private ServicioPlano2D servicioPlano2D;
  private ControladorPlano2D controladorPlano2D;
  private MockHttpSession sesion;
  private MuebleUbicado muebleUbicado;
  private Model modelo;

  @BeforeEach
  public void init() {
    servicioPlano2D = mock(ServicioPlano2D.class);
    controladorPlano2D =
      new ControladorPlano2D(servicioPlano2D, mock(ServicioCategoriaDeMueble.class));
    sesion = new MockHttpSession();
    modelo = new ExtendedModelMap();
  }

  @Test
  public void deberiaAnclarElMuebleSeleccionado() {
    //Given
    givenTengoUnPlanoEnSesion();

    //When
    ModelAndView vista = controladorPlano2D.anclarMueble(0, sesion, modelo);

    //Then
    assertThat(muebleUbicado.isAnclado(), is(equalTo(true)));
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/actual")));
  }

  @Test
  public void deberiaDesanclarElMuebleSeleccionado() {
    //Given
    givenTengoUnPlanoEnSesion();
    muebleUbicado.anclar();

    //When
    ModelAndView vista = controladorPlano2D.desanclarMueble(0, sesion, modelo);

    //Then
    assertThat(muebleUbicado.isAnclado(), is(equalTo(false)));
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/actual")));
  }

  @Test
  public void deberiaRecalcularElPlanoDeLaSesion() {
    //Given
    givenTengoUnPlanoEnSesion();

    //When
    ModelAndView vista = controladorPlano2D.recalcularPlano(sesion, modelo);

    //Then
    verify(servicioPlano2D).recalcularPlano((Plano) sesion.getAttribute("plano"));
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/actual")));
  }

  @Test
  public void deberiaImpedirMoverUnMuebleAnclado() {
    //Given
    givenTengoUnPlanoEnSesion();
    muebleUbicado.anclar();

    //When
    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    //Then
    assertThat(respuesta.getStatusCode(), is(equalTo(HttpStatus.CONFLICT)));
    assertThat(muebleUbicado.getPosicionX(), is(equalTo(0.0)));
    assertThat(muebleUbicado.getPosicionY(), is(equalTo(0.0)));
  }

  @Test
  public void deberiaPermitirMoverElMuebleDespuesDeDesanclarlo() {
    //Given
    givenTengoUnPlanoEnSesion();
    muebleUbicado.anclar();
    controladorPlano2D.desanclarMueble(0, sesion, modelo);
    givenElMovimientoEsValido(1.0, 1.0);

    //When
    ResponseEntity<String> respuesta = controladorPlano2D.moverMueble(0, 1.0, 1.0, sesion);

    //Then
    assertThat(respuesta.getStatusCode(), is(equalTo(HttpStatus.OK)));
    assertThat(muebleUbicado.getPosicionX(), is(equalTo(1.0)));
  }

  @Test
  public void deberiaReemplazarElPlanoDeLaSesionPorElGuardadoDespuesDeRecalcular() {
    //Given
    givenTengoUnPlanoEnSesion();
    ServicioPersistenciaPlano servicioPersistenciaPlano = mock(ServicioPersistenciaPlano.class);
    controladorPlano2D =
      new ControladorPlano2D(
        servicioPlano2D,
        mock(ServicioCategoriaDeMueble.class),
        servicioPersistenciaPlano
      );
    sesion.setAttribute("USUARIO_EMAIL", "test@unlam.edu.ar");
    Plano planoGuardado = new Plano(
      new Ambiente(4.0, 3.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    when(servicioPersistenciaPlano.buscarUltimoDe("test@unlam.edu.ar")).thenReturn(planoGuardado);

    //When
    controladorPlano2D.recalcularPlano(sesion, modelo);

    //Then
    assertThat(sesion.getAttribute("plano"), is(equalTo(planoGuardado)));
    assertThat(modelo.getAttribute("plano"), is(equalTo(planoGuardado)));
  }

  private void givenTengoUnPlanoEnSesion() {
    muebleUbicado = new MuebleUbicado(new Mueble("Sofa", 1.0, 1.0), 0.0, 0.0);
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(muebleUbicado);
    Plano plano = new Plano(new Ambiente(4.0, 3.0), muebles, new ArrayList<>(), new ArrayList<>());
    sesion.setAttribute("plano", plano);
  }

  private void givenElMovimientoEsValido(double x, double y) {
    when(servicioPlano2D.estaDentroDelPerimetro(any(MuebleUbicado.class), any(Ambiente.class)))
      .thenReturn(true);
    when(servicioPlano2D.validarMovimiento(eq(muebleUbicado), any(Ambiente.class), eq(x), eq(y)))
      .thenReturn(new ValidacionMovimiento(false));
  }
}
