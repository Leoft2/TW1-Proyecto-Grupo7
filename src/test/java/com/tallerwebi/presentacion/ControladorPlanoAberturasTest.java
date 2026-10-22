package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.TipoAbertura;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

// -------------------------------------------------------------------------
// Testing de HU-04:Aberturas
// -------------------------------------------------------------------------

public class ControladorPlanoAberturasTest {

  private ControladorPlanoAberturas controladorPlanoAberturas;
  private HttpSession sesion;

  @BeforeEach
  public void setUp() {
    controladorPlanoAberturas = new ControladorPlanoAberturas();
    sesion = mock(HttpSession.class);
  }

  @Test
  public void deberiaMostrarLaVistaDeAberturasConDatosDelPasoAnterior() {
    DatosPlano datosPlano = givenDatosPlanoEnSesion();

    ModelAndView mav = whenVoyAConfigurarAberturas(sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
    thenElModeloContieneClave(mav, "datosPlano");
    thenElModeloContieneClave(mav, "tiposAbertura");
    thenElModeloContieneClave(mav, "muros");
  }

  @Test
  public void deberiaGuardarLasAberturasEnSesionAlEnviarElFormulario() {
    DatosPlano datosPlano = givenDatosPlanoConAberturas();

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
    verify(sesion).setAttribute(eq("datosPlano"), eq(datosPlano));
  }

  @Test
  public void deberiaRedirigirAObstaculosAlConfirmarAberturas() {
    DatosPlano datosPlano = givenDatosPlanoConAberturas();

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "redirect:/plano/obstaculos");
    verify(sesion).setAttribute(eq("datosPlano"), eq(datosPlano));
  }

  @Test
  public void deberiaNoModificarLosDatosDeLaSesionCuandoAbroElPasoDeAberturas() {
    DatosPlano datosPlano = givenDatosPlanoEnSesion();

    whenVoyAConfigurarAberturas(sesion);

    thenLasAberturasSiguenVacias(datosPlano);
  }

  @Test
  public void deberiaVolverAlPasoDeAberturasCuandoUnaAberturaEstaIncompleta() {
    DatosPlano datosPlano = givenDatosPlanoConAberturaIncompleta();

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
  }

  // Given
  private DatosPlano givenDatosPlanoEnSesion() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    when(sesion.getAttribute("datosPlano")).thenReturn(datosPlano);
    return datosPlano;
  }

  private DatosPlano givenDatosPlanoConAberturas() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    datosAbertura.setAncho(0.9);
    datosAbertura.setRadioDeGiro(0.9);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  private DatosPlano givenDatosPlanoConAberturaIncompleta() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // When
  private ModelAndView whenVoyAConfigurarAberturas(HttpSession sesionHttp) {
    return controladorPlanoAberturas.irAConfigurarAberturas(sesionHttp);
  }

  private ModelAndView whenGuardoAberturas(DatosPlano datosPlano, HttpSession sesionHttp) {
    return controladorPlanoAberturas.guardarAberturas(datosPlano, sesionHttp);
  }

  private ModelAndView whenConfirmoAberturas(DatosPlano datosPlano, HttpSession sesionHttp) {
    return controladorPlanoAberturas.confirmarAberturas(datosPlano, sesionHttp);
  }

  // Then
  private void thenLaVistaEs(ModelAndView mav, String nombreVista) {
    assertThat(mav.getViewName(), is(nombreVista));
  }

  private void thenElModeloContieneClave(ModelAndView mav, String clave) {
    assertThat(mav.getModel(), hasKey(clave));
  }

  private void thenLasAberturasSiguenVacias(DatosPlano datosPlano) {
    assertThat(datosPlano.getAberturas(), is(empty()));
  }
}
