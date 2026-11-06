package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPlano2DAdvertenciasTest {

  private ServicioPlano2D servicioPlano2D;
  private ServicioPersistenciaPlano servicioPersistenciaPlano;
  private ServicioReglasDeApertura servicioReglasDeApertura;
  private ControladorPlano2D controladorPlano2D;

  @BeforeEach
  public void init() {
    servicioPlano2D = mock(ServicioPlano2D.class);
    servicioPersistenciaPlano = mock(ServicioPersistenciaPlano.class);
    servicioReglasDeApertura = mock(ServicioReglasDeApertura.class);
    controladorPlano2D =
      new ControladorPlano2D(
        servicioPlano2D,
        mock(ServicioCategoriaDeMueble.class),
        servicioPersistenciaPlano,
        mock(ServicioZonaExclusion.class),
        servicioReglasDeApertura
      );
  }

  @Test
  public void deberiaAgregarLaAdvertenciaAlModeloCuandoUnaPuertaNoTieneParedLibreAlGenerarElPlano() {
    Ambiente ambiente = givenAmbienteConPuertaSinParedLibre();
    givenElServicioIndicaQueNoHayParedLibre();
    givenElPlanoGenerado(ambiente);

    ModelAndView mav = whenGeneroElPlano(ambiente);

    thenElModeloContieneLaAdvertencia(mav, givenMensajeDeAdvertencia(Muro.SUPERIOR, 0.6));
  }

  @Test
  public void deberiaNoAgregarAdvertenciasCuandoLaPuertaTieneParedLibre() {
    Ambiente ambiente = givenAmbienteConPuertaConParedLibre();
    givenElServicioIndicaQueHayParedLibre();
    givenElPlanoGenerado(ambiente);

    ModelAndView mav = whenGeneroElPlano(ambiente);

    thenElModeloNoTieneAdvertencias(mav);
  }

  @Test
  public void deberiaNoAgregarAdvertenciasCuandoLaAberturaEsUnaVentana() {
    Ambiente ambiente = givenAmbienteConVentana();
    givenElServicioFallaSiSeConsulta();
    givenElPlanoGenerado(ambiente);

    ModelAndView mav = whenGeneroElPlano(ambiente);

    thenElModeloNoTieneAdvertencias(mav);
  }

  @Test
  public void deberiaAgregarLaAdvertenciaAlModeloCuandoAbroElPlanoActual() {
    Ambiente ambiente = givenAmbienteConPuertaSinParedLibre();
    givenElServicioIndicaQueNoHayParedLibre();
    HttpSession sesion = givenSesionConElPlano(givenPlanoCon(ambiente));

    ModelAndView mav = whenAbroElPlanoActual(sesion);

    thenElModeloContieneLaAdvertencia(mav, givenMensajeDeAdvertencia(Muro.SUPERIOR, 0.6));
  }

  @Test
  public void deberiaAgregarLaAdvertenciaAlModeloCuandoAbroElUltimoPlano() {
    Ambiente ambiente = givenAmbienteConPuertaSinParedLibre();
    givenElServicioIndicaQueNoHayParedLibre();
    givenHayUnUltimoPlano("test@unlam.edu.ar", givenPlanoCon(ambiente));
    HttpSession sesion = givenSesionConUsuario("test@unlam.edu.ar");

    ModelAndView mav = whenAbroElUltimoPlano(sesion);

    thenElModeloContieneLaAdvertencia(mav, givenMensajeDeAdvertencia(Muro.SUPERIOR, 0.6));
  }

  private Ambiente givenAmbienteConVentana() {
    Ambiente ambiente = new Ambiente(2.0, 5.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 0.6, 0.9, null));
    return ambiente;
  }

  private Ambiente givenAmbienteConPuertaConParedLibre() {
    Ambiente ambiente = new Ambiente(4.0, 3.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.9));
    return ambiente;
  }

  private Ambiente givenAmbienteConPuertaSinParedLibre() {
    Ambiente ambiente = new Ambiente(2.0, 5.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 0.6, 0.9, 0.9));
    return ambiente;
  }

  private void givenElServicioIndicaQueNoHayParedLibre() {
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));
  }

  private void givenElServicioIndicaQueHayParedLibre() {
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(true));
  }

  private void givenElServicioFallaSiSeConsulta() {
    when(servicioReglasDeApertura.determinarApertura(any(), any()))
      .thenThrow(new IllegalArgumentException("No se deben calcular reglas para ventanas"));
  }

  private void givenElPlanoGenerado(Ambiente ambiente) {
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(givenPlanoCon(ambiente));
  }

  private Plano givenPlanoCon(Ambiente ambiente) {
    return new Plano(ambiente, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }

  private HttpSession givenSesionConElPlano(Plano plano) {
    HttpSession sesion = mock(HttpSession.class);
    when(sesion.getAttribute("plano")).thenReturn(plano);
    return sesion;
  }

  private void givenHayUnUltimoPlano(String usuarioEmail, Plano plano) {
    when(servicioPersistenciaPlano.buscarUltimoDe(usuarioEmail)).thenReturn(plano);
  }

  private HttpSession givenSesionConUsuario(String usuarioEmail) {
    HttpSession sesion = mock(HttpSession.class);
    when(sesion.getAttribute("USUARIO_EMAIL")).thenReturn(usuarioEmail);
    return sesion;
  }

  private ReglaDeApertura givenRegla(boolean hayParedLibre) {
    return new ReglaDeApertura(
      Muro.IZQUIERDO,
      LadoDeLaHoja.INICIO,
      LadoDeLaHoja.FIN,
      new ZonaDeBarrido(0.0, 0.0, 0.9, 0.0, 90.0),
      hayParedLibre
    );
  }

  private String givenMensajeDeAdvertencia(Muro muro, Double posicion) {
    return String.format(
      "La puerta del muro %s en la posición %s m no tiene pared libre para abrir; " +
      "la hoja podría chocar con muebles.",
      muro.getNombre(),
      posicion
    );
  }

  private ModelAndView whenGeneroElPlano(Ambiente ambiente) {
    return controladorPlano2D.generarPlano(ambiente, new ArrayList<>());
  }

  private ModelAndView whenAbroElPlanoActual(HttpSession sesion) {
    return controladorPlano2D.planoActual(sesion);
  }

  private ModelAndView whenAbroElUltimoPlano(HttpSession sesion) {
    return controladorPlano2D.ultimoPlano(sesion);
  }

  @SuppressWarnings("unchecked")
  private void thenElModeloContieneLaAdvertencia(ModelAndView mav, String mensaje) {
    assertThat((List<String>) mav.getModel().get("advertenciasDeApertura"), contains(mensaje));
  }

  private void thenElModeloNoTieneAdvertencias(ModelAndView mav) {
    assertThat((List<?>) mav.getModel().get("advertenciasDeApertura"), is(empty()));
  }
}
