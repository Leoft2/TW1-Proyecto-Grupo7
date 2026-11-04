package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorScrapingPinturasTest {

  @Test
  public void deberiaRechazarActualizacionCuandoNoEsAdmin() throws Exception {
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(servicio);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(null);

    ModelAndView mv = controlador.actualizarBaldes(request);

    assertThat(mv.getViewName(), is("redirect:/home?error=sinPermisos"));
    verify(servicio, never()).actualizarBaldesDesdeColorshop();
  }

  @Test
  public void deberiaActualizarBaldesCuandoEsAdmin() throws Exception {
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(servicio);

    HttpSession sesion = mock(HttpSession.class);
    when(sesion.getAttribute("ROL")).thenReturn("ADMIN");
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(sesion);

    ModelAndView mv = controlador.actualizarBaldes(request);

    assertThat(mv.getViewName(), is("redirect:/home?actualizado=ok"));
    verify(servicio).actualizarBaldesDesdeColorshop();
  }
}
