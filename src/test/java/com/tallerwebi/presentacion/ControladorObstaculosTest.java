package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.ServicioObstaculos;
import com.tallerwebi.dominio.TipoDeObstaculo;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorObstaculosTest {

  private ServicioObstaculos servicioObstaculos;
  private ControladorObstaculos controladorObstaculos;
  private DatosPlano formulario;
  private HttpSession sesion;

  @BeforeEach
  public void init() {
    servicioObstaculos = mock(ServicioObstaculos.class);
    controladorObstaculos = new ControladorObstaculos(servicioObstaculos);
    sesion = mock(HttpSession.class);
    formulario = new DatosPlano();
    formulario.setAncho(4.0);
    formulario.setLargo(3.0);
  }

  @Test
  public void deberiaMostrarElPasoDeObstaculosConLosDatosDelPasoAnterior() {
    // ejecucion
    ModelAndView mav = controladorObstaculos.irACargarObstaculos(formulario, sesion);

    // validacion
    assertEquals("obstaculos-config", mav.getViewName());
    assertEquals(formulario, mav.getModel().get("datosPlano"));
  }

  @Test
  public void deberiaOfrecerLosTiposDeObstaculoYLosMuros() {
    // ejecucion
    ModelAndView mav = controladorObstaculos.irACargarObstaculos(formulario, sesion);

    // validacion
    assertArrayEquals(TipoDeObstaculo.values(), (Object[]) mav.getModel().get("tiposDeObstaculo"));
    assertArrayEquals(Muro.values(), (Object[]) mav.getModel().get("muros"));
  }

  @Test
  public void deberiaGuardarElAmbienteEnSesionCuandoLosObstaculosSonValidos() {
    // preparacion
    formulario.setObstaculos(
      new ArrayList<>(
        Arrays.asList(new DatosObstaculo(TipoDeObstaculo.RADIADOR, Muro.DERECHO, 0.5, 0.8, 0.15))
      )
    );

    // ejecucion
    controladorObstaculos.confirmarObstaculos(formulario, sesion);

    // validacion
    ArgumentCaptor<Ambiente> ambiente = ArgumentCaptor.forClass(Ambiente.class);
    verify(servicioObstaculos).validarObstaculos(ambiente.capture());
    assertEquals(Muro.DERECHO, ambiente.getValue().getObstaculos().get(0).getMuro());
  }

  @Test
  public void deberiaRedirigirAlInventarioCuandoLosObstaculosSonValidos() {
    // preparacion
    formulario.setObstaculos(
      new ArrayList<>(
        Arrays.asList(new DatosObstaculo(TipoDeObstaculo.RADIADOR, Muro.DERECHO, 0.5, 0.8, 0.15))
      )
    );

    // ejecucion
    ModelAndView mav = controladorObstaculos.confirmarObstaculos(formulario, sesion);

    // validacion
    assertEquals("redirect:/inventario-muebles?ancho=4.0&largo=3.0", mav.getViewName());
  }

  @Test
  public void deberiaGuardarElFormularioEnSesionCuandoCargoElPasoDeObstaculos() {
    // ejecucion
    controladorObstaculos.irACargarObstaculos(formulario, sesion);

    // validacion
    verify(sesion).setAttribute("datosPlano", formulario);
  }

  @Test
  public void deberiaGuardarElFormularioEnSesionCuandoConfirmoLosObstaculos() {
    // preparacion
    formulario.setObstaculos(
      new ArrayList<>(
        Arrays.asList(new DatosObstaculo(TipoDeObstaculo.RADIADOR, Muro.DERECHO, 0.5, 0.8, 0.15))
      )
    );

    // ejecucion
    controladorObstaculos.confirmarObstaculos(formulario, sesion);

    // validacion
    verify(sesion).setAttribute("datosPlano", formulario);
  }

  @Test
  public void deberiaGuardarElFormularioEnSesionCuandoUnObstaculoNoEsValido() {
    // preparacion
    doThrow(new ValidacionException("Radiador se sale del muro derecho"))
      .when(servicioObstaculos)
      .validarObstaculos(any(Ambiente.class));

    // ejecucion
    controladorObstaculos.confirmarObstaculos(formulario, sesion);

    // validacion
    verify(sesion).setAttribute("datosPlano", formulario);
  }

  @Test
  public void deberiaVolverAlPasoDeObstaculosConElErrorCuandoUnObstaculoNoEsValido() {
    // preparacion
    doThrow(new ValidacionException("Radiador se sale del muro derecho"))
      .when(servicioObstaculos)
      .validarObstaculos(any(Ambiente.class));

    // ejecucion
    ModelAndView mav = controladorObstaculos.confirmarObstaculos(formulario, sesion);

    // validacion
    assertEquals("obstaculos-config", mav.getViewName());
    assertEquals("Radiador se sale del muro derecho", mav.getModel().get("error"));
    assertEquals(formulario, mav.getModel().get("datosPlano"));
    assertArrayEquals(Muro.values(), (Object[]) mav.getModel().get("muros"));
  }
}
