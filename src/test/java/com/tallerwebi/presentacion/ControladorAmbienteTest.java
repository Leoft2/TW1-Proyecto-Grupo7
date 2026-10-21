package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

import com.tallerwebi.dominio.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorAmbienteTest {

  private ServicioAmbiente servicioAmbiente;
  private ControladorAmbiente controladorAmbiente;
  private HttpServletRequest request;
  private HttpSession session;

  @BeforeEach
  public void init() {
    this.servicioAmbiente = mock(ServicioAmbiente.class);
    this.request = mock(HttpServletRequest.class);
    this.session = mock(HttpSession.class);
    this.controladorAmbiente = new ControladorAmbiente(this.servicioAmbiente);
  }

  @Test
  public void dadoQueEntroAMisAmbientes_entoncesVeoLaListaDeAmbientes() {
    List<Ambiente> ambientes = new ArrayList<>();
    ambientes.add(new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación")));
    when(this.request.getSession()).thenReturn(session);
    when(this.session.getAttribute("USUARIO_EMAIL")).thenReturn("test@unlam.edu.ar");
    when(this.servicioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar")).thenReturn(ambientes);

    ModelAndView modelAndView = this.controladorAmbiente.ambientes(request);

    assertThat(modelAndView.getViewName(), is("ambientes"));
    assertThat(this.obtenerAmbientesDelModel(modelAndView), contains(ambientes.get(0)));
  }

  @Test
  public void dadoQueNoHayUsuarioEnSesion_entoncesVeoLaListaDeAmbientesVacia() {
    when(this.request.getSession()).thenReturn(session);
    when(this.session.getAttribute("USUARIO_EMAIL")).thenReturn(null);

    ModelAndView modelAndView = this.controladorAmbiente.ambientes(request);

    assertThat(modelAndView.getViewName(), is("ambientes"));
    assertThat(this.obtenerAmbientesDelModel(modelAndView), is(new ArrayList<>()));
  }

  @Test
  public void dadoQueEntroAlDetalleDeUnAmbiente_entoncesVeoEseAmbiente() {
    Ambiente ambiente = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación"));
    when(this.servicioAmbiente.buscarAmbiente(1L)).thenReturn(ambiente);

    ModelAndView modelAndView = this.controladorAmbiente.detalleAmbiente(1L);

    assertThat(modelAndView.getViewName(), is("ambiente"));
    assertThat(modelAndView.getModel().get("ambiente"), is(ambiente));
  }

  @Test
  public void dadoQueEntroAMueblesDeUnAmbiente_entoncesVeoLosMueblesDeEseAmbiente() {
    Mueble cama = new Mueble("Cama", 2.0, 1.6);
    Ambiente dormitorio = this.unAmbienteConMuebles(new TipoDeAmbiente("Habitación"), cama);
    when(this.servicioAmbiente.buscarAmbiente(1L)).thenReturn(dormitorio);

    ModelAndView modelAndView = this.controladorAmbiente.mueblesDelAmbiente(1L);

    assertThat(modelAndView.getViewName(), is("muebles"));
    assertThat(modelAndView.getModel().get("ambiente"), is(dormitorio));
    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(cama));
    verify(this.servicioAmbiente).buscarAmbiente(1L);
  }

  @Test
  public void dadoQueHayVariosAmbientes_cuandoEntroAMueblesDeUno_entoncesSoloVeoLosMueblesDeEseAmbiente() {
    Mueble cama = new Mueble("Cama", 2.0, 1.6);
    Mueble sofa = new Mueble("Sofa", 2.1, 0.9);
    when(this.servicioAmbiente.buscarAmbiente(1L))
      .thenReturn(this.unAmbienteConMuebles(new TipoDeAmbiente("Habitación"), cama));
    when(this.servicioAmbiente.buscarAmbiente(2L))
      .thenReturn(this.unAmbienteConMuebles(new TipoDeAmbiente("Living"), sofa));

    ModelAndView modelAndView = this.controladorAmbiente.mueblesDelAmbiente(1L);

    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(cama));
    assertThat(this.obtenerMueblesDelModel(modelAndView), not(contains(sofa)));
  }

  @Test
  public void dadoQueElAmbienteNoTieneMuebles_cuandoEntroAMuebles_entoncesVeoLaListaVacia() {
    Ambiente pasillo = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Pasillo"));
    when(this.servicioAmbiente.buscarAmbiente(1L)).thenReturn(pasillo);

    ModelAndView modelAndView = this.controladorAmbiente.mueblesDelAmbiente(1L);

    assertThat(modelAndView.getViewName(), is("muebles"));
    assertThat(this.obtenerMueblesDelModel(modelAndView), empty());
  }

  @Test
  public void dadoQueElAmbienteNoExiste_cuandoEntroAMuebles_entoncesVuelvoAMisAmbientes() {
    when(this.servicioAmbiente.buscarAmbiente(99L)).thenReturn(null);

    ModelAndView modelAndView = this.controladorAmbiente.mueblesDelAmbiente(99L);

    assertThat(modelAndView.getViewName(), is("redirect:/ambientes"));
  }

  // -------------------------------------------------------------------------
  // Tests HU-04:Aberturas
  // -------------------------------------------------------------------------

  @Test
  public void deberiaMostrarElFormularioDeAberturasCuandoSePidePorId() {
    givenAmbienteDevueltoPorServicio(1L);

    ModelAndView mav = whenVoyAConfigurarAberturas(1L);

    thenLaVistaEs(mav, "ambiente-aberturas");
    thenElModeloContieneClave(mav, "ambiente");
    thenElModeloContieneDatosAberturaVacio(mav);
    thenElModeloContieneTiposAbertura(mav);
    thenElModeloContieneMuros(mav);
  }

  @Test
  public void deberiaAgregarAberturaYRedirigirCuandoElFormularioEsValido() {
    givenAmbienteDevueltoPorServicio(1L);
    DatosAbertura datosAbertura = givenDatosAberturaPuerta();

    ModelAndView mav = whenAgregoAberturaDesdeFormulario(1L, datosAbertura);

    thenLaVistaEs(mav, "redirect:/ambientes/1/aberturas");
    verify(servicioAmbiente).agregarAbertura(eq(1L), any(Abertura.class));
  }

  @Test
  public void deberiaVolverAlFormularioConErrorCuandoElServicioLanzaExcepcionAlNoEncontrarAmbiente() {
    givenAmbienteDevueltoPorServicio(1L);
    givenAgregarAberturaLanzaExcepcion(1L, "Ambiente no encontrado");
    DatosAbertura datosAbertura = givenDatosAberturaPuerta();

    ModelAndView mav = whenAgregoAberturaDesdeFormulario(1L, datosAbertura);

    thenLaVistaEs(mav, "ambiente-aberturas");
    thenElModeloContieneError(mav);
    thenElModeloContieneDatosAberturaOriginal(mav, datosAbertura);
  }

  private Ambiente unAmbienteConMuebles(TipoDeAmbiente tipo, Mueble... muebles) {
    Ambiente ambiente = new Ambiente(4.0, 3.0, tipo);
    for (Mueble mueble : muebles) {
      ambiente.agregarMueble(mueble);
    }
    return ambiente;
  }

  @SuppressWarnings("unchecked")
  private List<Mueble> obtenerMueblesDelModel(ModelAndView modelAndView) {
    return (List<Mueble>) modelAndView.getModel().get("muebles");
  }

  // -------------------------------------------------------------------------
  // Given
  // -------------------------------------------------------------------------

  private Ambiente givenAmbienteDevueltoPorServicio(Long id) {
    Ambiente ambiente = new Ambiente(4.0, 5.0, new TipoDeAmbiente("Living"));
    when(servicioAmbiente.buscarAmbiente(id)).thenReturn(ambiente);
    return ambiente;
  }

  private DatosAbertura givenDatosAberturaPuerta() {
    DatosAbertura datos = new DatosAbertura();
    datos.setTipo(TipoAbertura.PUERTA);
    datos.setMuro(Muro.SUPERIOR);
    datos.setPosicion(1.0);
    datos.setAncho(0.9);
    datos.setRadioDeGiro(0.9);
    return datos;
  }

  private void givenAgregarAberturaLanzaExcepcion(Long id, String mensaje) {
    doThrow(new RuntimeException(mensaje))
      .when(servicioAmbiente)
      .agregarAbertura(eq(id), any(Abertura.class));
  }

  // -------------------------------------------------------------------------
  // When
  // -------------------------------------------------------------------------

  private ModelAndView whenVoyAConfigurarAberturas(Long id) {
    return controladorAmbiente.irAConfigurarAberturas(id);
  }

  private ModelAndView whenAgregoAberturaDesdeFormulario(Long id, DatosAbertura datos) {
    return controladorAmbiente.agregarAbertura(id, datos);
  }

  // -------------------------------------------------------------------------
  // Then
  // -------------------------------------------------------------------------

  private void thenLaVistaEs(ModelAndView mav, String nombreVista) {
    assertThat(mav.getViewName(), is(nombreVista));
  }

  private void thenElModeloContieneClave(ModelAndView mav, String clave) {
    assertThat(mav.getModel(), hasKey(clave));
  }

  private void thenElModeloContieneDatosAberturaVacio(ModelAndView mav) {
    assertThat(mav.getModel().get("datosAbertura"), is(notNullValue()));
    assertThat(mav.getModel().get("datosAbertura"), instanceOf(DatosAbertura.class));
  }

  private void thenElModeloContieneTiposAbertura(ModelAndView mav) {
    assertThat(mav.getModel(), hasKey("tiposAbertura"));
  }

  private void thenElModeloContieneMuros(ModelAndView mav) {
    assertThat(mav.getModel(), hasKey("muros"));
  }

  private void thenElModeloContieneError(ModelAndView mav) {
    assertThat(mav.getModel(), hasKey("error"));
    assertThat(mav.getModel().get("error"), is(notNullValue()));
  }

  private void thenElModeloContieneDatosAberturaOriginal(ModelAndView mav, DatosAbertura datos) {
    assertThat(mav.getModel().get("datosAbertura"), is(datos));
  }

  @SuppressWarnings("unchecked")
  private List<Ambiente> obtenerAmbientesDelModel(ModelAndView modelAndView) {
    return (List<Ambiente>) modelAndView.getModel().get("ambientes");
  }
}
