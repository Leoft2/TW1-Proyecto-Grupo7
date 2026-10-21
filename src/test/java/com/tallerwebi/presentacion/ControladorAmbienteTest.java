package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioAmbiente;
import com.tallerwebi.dominio.TipoDeAmbiente;
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

  @SuppressWarnings("unchecked")
  private List<Ambiente> obtenerAmbientesDelModel(ModelAndView modelAndView) {
    return (List<Ambiente>) modelAndView.getModel().get("ambientes");
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
}
