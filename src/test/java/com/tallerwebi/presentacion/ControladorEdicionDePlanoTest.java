package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionDePlano;
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class ControladorEdicionDePlanoTest {

  private ServicioEdicionDePlano servicioEdicionDePlano;
  private ServicioPersistenciaPlano servicioPersistenciaPlano;
  private ControladorEdicionDePlano controlador;
  private MockHttpSession sesion;
  private Plano plano;

  @BeforeEach
  public void init() {
    servicioEdicionDePlano = mock(ServicioEdicionDePlano.class);
    servicioPersistenciaPlano = mock(ServicioPersistenciaPlano.class);
    controlador = new ControladorEdicionDePlano(servicioEdicionDePlano, servicioPersistenciaPlano);
    sesion = new MockHttpSession();
    plano = givenUnPlanoEnSesion();
  }

  @Test
  public void deberiaQuitarYGuardarElMuebleSeleccionado() {
    //Given
    givenElServicioQuitaElMueble(true);
    givenElUsuarioEstaIdentificado();

    //When
    ModelAndView respuesta = whenQuitoElMueble(1);

    //Then
    assertThat(respuesta.getViewName(), is(equalTo("redirect:/plano/actual?muebleQuitado")));
    verify(servicioPersistenciaPlano).guardar(plano, "usuario@unlam.edu.ar");
    verify(servicioPersistenciaPlano).buscarUltimoDe("usuario@unlam.edu.ar");
    assertThat(sesion.getAttribute("plano"), is(equalTo(plano)));
  }

  @Test
  public void deberiaInformarErrorSiNoSePuedeQuitarElMueble() {
    //Given
    givenElServicioQuitaElMueble(false);

    //When
    ModelAndView respuesta = whenQuitoElMueble(8);

    //Then
    assertThat(respuesta.getViewName(), is(equalTo("redirect:/plano/actual?errorQuitarMueble")));
    verify(servicioPersistenciaPlano, never()).guardar(any(Plano.class), anyString());
  }

  private Plano givenUnPlanoEnSesion() {
    Plano planoEnSesion = new Plano(
      new Ambiente(4.0, 3.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
    sesion.setAttribute("plano", planoEnSesion);
    return planoEnSesion;
  }

  private void givenElServicioQuitaElMueble(boolean resultado) {
    when(servicioEdicionDePlano.quitarMueble(plano, 1)).thenReturn(resultado);
    when(servicioEdicionDePlano.quitarMueble(plano, 8)).thenReturn(resultado);
  }

  private void givenElUsuarioEstaIdentificado() {
    sesion.setAttribute("USUARIO_EMAIL", "usuario@unlam.edu.ar");
  }

  private ModelAndView whenQuitoElMueble(int indice) {
    return controlador.quitarMueble(indice, sesion);
  }
}
