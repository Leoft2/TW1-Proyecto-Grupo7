package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.ServicioAmbiente;
import com.tallerwebi.dominio.TipoDeAmbiente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorHomeTest {

  private static final String EMAIL = "dami@unlam.com";

  private ControladorHome controladorHome;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioAmbiente servicioAmbienteMock;

  @BeforeEach
  public void init() {
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioAmbienteMock = mock(ServicioAmbiente.class);
    controladorHome = new ControladorHome(servicioAmbienteMock);
  }

  @Test
  public void irAHomeDeberiaRetornarVistaHome() {
    givenSesionConUsuario(EMAIL);
    givenAmbientesDelUsuario(EMAIL, new ArrayList<>());

    ModelAndView modelAndView = whenIrAHome();

    thenSeMuestraLaVistaHome(modelAndView);
  }

  @Test
  public void irAHomeDeberiaIncluirLosAmbientesDelUsuarioEnElModelo() {
    List<Ambiente> ambientes = new ArrayList<>();
    ambientes.add(new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación")));
    givenSesionConUsuario(EMAIL);
    givenAmbientesDelUsuario(EMAIL, ambientes);

    ModelAndView modelAndView = whenIrAHome();

    thenElModeloContieneLosAmbientes(modelAndView, ambientes);
  }

  @Test
  public void deberiaMostrarLaLandingDeHabitatCuandoSeSolicitaLaPaginaDeInicio() {
    ModelAndView modelAndView = whenInicio();

    thenSeMuestraLaLandingDeHabitat(modelAndView);
  }

  private void givenSesionConUsuario(String email) {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO_EMAIL")).thenReturn(email);
  }

  private void givenAmbientesDelUsuario(String email, List<Ambiente> ambientes) {
    when(servicioAmbienteMock.obtenerAmbientesDe(email)).thenReturn(ambientes);
  }

  private ModelAndView whenIrAHome() {
    return controladorHome.irAHome(requestMock);
  }

  private ModelAndView whenInicio() {
    return controladorHome.inicio();
  }

  private void thenSeMuestraLaVistaHome(ModelAndView modelAndView) {
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
  }

  private void thenElModeloContieneLosAmbientes(
    ModelAndView modelAndView,
    List<Ambiente> ambientes
  ) {
    assertThat(modelAndView.getModel().get("ambientes"), is(ambientes));
  }

  private void thenSeMuestraLaLandingDeHabitat(ModelAndView modelAndView) {
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("landing-habitat"));
  }
}
