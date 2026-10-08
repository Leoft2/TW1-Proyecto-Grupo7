package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.Map;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

public class ControladorInventarioMuebles {

  private static final String VISTA_EDITAR_MUEBLE = "editar-mueble";

  private final ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;

  public ControladorInventarioMuebles(
    ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles
  ) {
    this.servicioEdicionInventarioMuebles = servicioEdicionInventarioMuebles;
  }

  public ModelAndView irAEditar(int id) throws MuebleNoEncontrado {
    Map<String, Object> model = new ModelMap();
    model.put("mueble", servicioEdicionInventarioMuebles.buscarPorId(id));
    return new ModelAndView(VISTA_EDITAR_MUEBLE, model);
  }

  public ModelAndView editar(int id, Mueble datos) throws MuebleNoEncontrado {
    try {
      servicioEdicionInventarioMuebles.actualizar(id, datos);
      return new ModelAndView(VISTA_EDITAR_MUEBLE);
    } catch (MuebleNoEncontrado e) {
      Map<String, Object> model = new ModelMap();
      model.put("error", e.getMessage());
      return new ModelAndView(VISTA_EDITAR_MUEBLE, model);
    }
  }

  public ModelAndView irAConfirmarEliminacion(int id) throws MuebleNoEncontrado {
    Map<String, Object> model = new ModelMap();
    model.put("mueble", servicioEdicionInventarioMuebles.buscarPorId(id));
    return new ModelAndView("confirmar-eliminacion", model);
  }

  public ModelAndView eliminar(int id) throws MuebleNoEncontrado {
    try {
      servicioEdicionInventarioMuebles.eliminar(id);
      return new ModelAndView("redirect:/inventario-muebles");
    } catch (MuebleNoEncontrado e) {
      Map<String, Object> model = new ModelMap();
      model.put("error", e.getMessage());
      return new ModelAndView(VISTA_EDITAR_MUEBLE, model);
    }
  }

  public ModelAndView cancelarEliminacion(int id) {
    return new ModelAndView(VISTA_EDITAR_MUEBLE);
  }
}
