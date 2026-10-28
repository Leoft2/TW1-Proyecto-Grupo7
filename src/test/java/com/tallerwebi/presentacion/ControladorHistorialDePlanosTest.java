package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioHistorialDePlanos;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/*
 * HU-12: Historial de versiones de distribucion
 * - Dado un historial de versiones, cuando selecciono una anterior, el sistema la restaura como la activa.
 */
public class ControladorHistorialDePlanosTest {

  private ServicioHistorialDePlanos servicioHistorialDePlanos;
  private ControladorHistorialDePlanos controladorHistorialDePlanos;
  private MockHttpSession sesion;

  @BeforeEach
  public void init() {
    servicioHistorialDePlanos = mock(ServicioHistorialDePlanos.class);
    controladorHistorialDePlanos = new ControladorHistorialDePlanos(servicioHistorialDePlanos);
    sesion = new MockHttpSession();
  }

  @Test
  public void deberiaMostrarLasVersionesDelUsuario() {
    //Given
    sesion.setAttribute("USUARIO_EMAIL", "test@unlam.edu.ar");
    when(servicioHistorialDePlanos.listarVersiones("test@unlam.edu.ar"))
      .thenReturn(new ArrayList<Plano>());

    //When
    ModelAndView vista = controladorHistorialDePlanos.verHistorial(sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("historial-planos")));
    assertThat(vista.getModel().containsKey("versiones"), is(equalTo(true)));
  }

  @Test
  public void deberiaRedirigirAlLoginSiNoHayUsuarioEnSesion() {
    //Given
    givenSesionSinUsuario();

    //When
    ModelAndView vista = controladorHistorialDePlanos.verHistorial(sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("redirect:/login")));
  }

  @Test
  public void deberiaPonerLaVersionRestauradaComoPlanoActivoDeLaSesion() {
    //Given
    sesion.setAttribute("USUARIO_EMAIL", "test@unlam.edu.ar");
    Plano version = givenPlano();
    when(servicioHistorialDePlanos.restaurarVersion(5L, "test@unlam.edu.ar")).thenReturn(version);

    //When
    ModelAndView vista = controladorHistorialDePlanos.restaurarVersion(5L, sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/actual")));
    assertThat((Plano) sesion.getAttribute("plano"), is(sameInstance(version)));
  }

  @Test
  public void deberiaVolverAlHistorialSiLaVersionNoExiste() {
    //Given
    sesion.setAttribute("USUARIO_EMAIL", "test@unlam.edu.ar");
    when(servicioHistorialDePlanos.restaurarVersion(9L, "test@unlam.edu.ar")).thenReturn(null);

    //When
    ModelAndView vista = controladorHistorialDePlanos.restaurarVersion(9L, sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/historial")));
  }

  private void givenSesionSinUsuario() {
    sesion.removeAttribute("USUARIO_EMAIL");
  }

  private Plano givenPlano() {
    return new Plano(
      new Ambiente(4.0, 3.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
  }
}
