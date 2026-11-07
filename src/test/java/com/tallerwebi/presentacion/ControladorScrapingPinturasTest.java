package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.Litros;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import com.tallerwebi.dominio.RepositorioLitros;
import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class ControladorScrapingPinturasTest {

  @Test
  public void deberiaRechazarActualizacionCuandoNoEsAdmin() throws Exception {
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(null);

    ModelAndView mv = controlador.actualizarBaldes(request);

    assertThat(mv.getViewName(), is("redirect:/home?error=sinPermisos"));
    verify(servicio, never()).actualizarBaldesDesdeColorshop();
  }

  @Test
  public void deberiaActualizarBaldesCuandoEsAdmin() throws Exception {
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    HttpSession sesion = mock(HttpSession.class);
    when(sesion.getAttribute("ROL")).thenReturn("ADMIN");
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession()).thenReturn(sesion);

    ModelAndView mv = controlador.actualizarBaldes(request);

    assertThat(mv.getViewName(), is("redirect:/home?actualizado=ok"));
    verify(servicio).actualizarBaldesDesdeColorshop();
  }

  @Test
  public void deberiaMostrarDetalleDeBaldeCuandoExiste() throws Exception {
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    BaldePintura balde = new BaldePintura(
      "Pintura X",
      "Mar Egeo",
      "0191b4",
      "/producto/x",
      4.0,
      9000.0,
      "1,4,10"
    );
    when(repositorio.buscarPorId(1L)).thenReturn(balde);

    ModelAndView mv = controlador.detalleBalde(1L);

    assertThat(mv.getViewName(), is("detalle-balde"));
    ModelMap modelo = mv.getModelMap();
    assertThat(modelo.get("balde"), equalTo(balde));
    assertThat(modeloOpcionesLitros(mv), equalTo(List.of("1", "4", "10")));
    verify(scrapingDetalle, never()).obtenerOpcionesLitros(anyString());
  }

  @Test
  public void deberiaScrapearYGuardarOpcionesCuandoBaldeTieneUrlYEstaVacio() throws Exception {
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    BaldePintura balde = new BaldePintura(
      "Pintura X",
      "Mar Egeo",
      "0191b4",
      "/producto/x",
      4.0,
      9000.0,
      null
    );
    when(repositorio.buscarPorId(1L)).thenReturn(balde);
    when(scrapingDetalle.obtenerOpcionesLitros("/producto/x"))
      .thenReturn(Optional.of(List.of(1.0, 4.0, 10.0)));
    when(repositorioLitros.buscarPorValor(anyDouble())).thenReturn(Optional.empty());

    ModelAndView mv = controlador.detalleBalde(1L);

    assertThat(mv.getViewName(), is("detalle-balde"));
    assertThat(modeloOpcionesLitros(mv), equalTo(List.of("1", "4", "10")));
    verify(scrapingDetalle).obtenerOpcionesLitros("/producto/x");
    verify(repositorioLitros, times(3)).guardar(any(Litros.class));
    verify(repositorio).guardar(balde);
  }

  @Test
  public void deberiaDejarOpcionesLitrosEnBlancoCuandoScrapingDevuelveVacio() throws Exception {
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    BaldePintura balde = new BaldePintura(
      "Pintura X",
      "Mar Egeo",
      "0191b4",
      "/producto/x",
      4.0,
      9000.0,
      null
    );
    when(repositorio.buscarPorId(1L)).thenReturn(balde);
    when(scrapingDetalle.obtenerOpcionesLitros("/producto/x")).thenReturn(Optional.empty());

    ModelAndView mv = controlador.detalleBalde(1L);

    assertThat(mv.getViewName(), is("detalle-balde"));
    assertThat(modeloOpcionesLitros(mv), is(empty()));
    verify(scrapingDetalle).obtenerOpcionesLitros("/producto/x");
    verify(repositorioLitros, never()).guardar(any());
    verify(repositorio, never()).guardar(any());
  }

  @Test
  public void deberiaRedirigirCuandoBaldeNoExiste() throws Exception {
    RepositorioBaldePintura repositorio = mock(RepositorioBaldePintura.class);
    ServicioAdministracionBaldesPintura servicio = mock(ServicioAdministracionBaldesPintura.class);
    ServicioScrapingDetalleBaldePintura scrapingDetalle = mock(
      ServicioScrapingDetalleBaldePintura.class
    );
    RepositorioLitros repositorioLitros = mock(RepositorioLitros.class);
    ControladorScrapingPinturas controlador = new ControladorScrapingPinturas(
      servicio,
      repositorio,
      scrapingDetalle,
      repositorioLitros
    );

    when(repositorio.buscarPorId(99L)).thenReturn(null);

    ModelAndView mv = controlador.detalleBalde(99L);

    assertThat(mv.getViewName(), is("redirect:/home?error=baldeNoEncontrado"));
    verify(scrapingDetalle, never()).obtenerOpcionesLitros(anyString());
  }

  @SuppressWarnings("unchecked")
  private static List<String> modeloOpcionesLitros(ModelAndView mv) {
    ModelMap modelo = mv.getModelMap();
    return (List<String>) modelo.get("opcionesLitros");
  }
}
