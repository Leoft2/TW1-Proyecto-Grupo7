package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.ServicioPlano2D;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorInventarioMuebles {

  private static final String VISTA_INVENTARIO = "inventario-muebles";
  private static final String PARAM_ANCHO = "ancho";
  private static final String PARAM_LARGO = "largo";
  private static final String ATTR_CATEGORIAS = "categorias";

  private final ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private final ServicioPlano2D servicioPlano2D;
  private final ServicioCategoriaDeMueble servicioCategoriaDeMueble;

  public ControladorInventarioMuebles(
    ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles,
    ServicioPlano2D servicioPlano2D,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble
  ) {
    this.servicioEdicionInventarioMuebles = servicioEdicionInventarioMuebles;
    this.servicioPlano2D = servicioPlano2D;
    this.servicioCategoriaDeMueble = servicioCategoriaDeMueble;
  }

  @GetMapping("/inventario-muebles")
  public ModelAndView irAInventario(
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo
  ) {
    Map<String, Object> model = new ModelMap();
    model.put("muebles", servicioEdicionInventarioMuebles.listar());
    model.put(PARAM_ANCHO, ancho);
    model.put(PARAM_LARGO, largo);
    model.put(ATTR_CATEGORIAS, servicioCategoriaDeMueble.obtenerCategorias());
    return new ModelAndView("inventario-muebles", model);
  }

  @GetMapping("/inventario-muebles/editar/{id}")
  public ModelAndView irAEditar(
    @PathVariable("id") int id,
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo
  ) throws MuebleNoEncontrado {
    Map<String, Object> model = new ModelMap();
    model.put("muebles", servicioEdicionInventarioMuebles.listar());
    model.put(PARAM_ANCHO, ancho);
    model.put(PARAM_LARGO, largo);
    model.put(ATTR_CATEGORIAS, servicioCategoriaDeMueble.obtenerCategorias());
    model.put("mueble", servicioEdicionInventarioMuebles.buscarPorId(id));
    return new ModelAndView(VISTA_INVENTARIO, model);
  }

  public ModelAndView editar(int id, Mueble datos) throws MuebleNoEncontrado {
    return editar(id, datos, null, null);
  }

  @PostMapping("/inventario-muebles/editar/{id}")
  public ModelAndView editar(
    @PathVariable("id") int id,
    Mueble datos,
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo
  ) throws MuebleNoEncontrado {
    try {
      servicioEdicionInventarioMuebles.actualizar(id, datos);
      return new ModelAndView(
        "redirect:/inventario-muebles?" +
        PARAM_ANCHO +
        "=" +
        ancho +
        "&" +
        PARAM_LARGO +
        "=" +
        largo
      );
    } catch (MuebleNoEncontrado e) {
      Map<String, Object> model = new ModelMap();
      model.put("error", e.getMessage());
      model.put(PARAM_ANCHO, ancho);
      model.put(PARAM_LARGO, largo);
      model.put(ATTR_CATEGORIAS, servicioCategoriaDeMueble.obtenerCategorias());
      return new ModelAndView(VISTA_INVENTARIO, model);
    }
  }

  @GetMapping("/inventario-muebles/eliminar/{id}")
  public ModelAndView irAConfirmarEliminacion(
    @PathVariable("id") int id,
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo
  ) throws MuebleNoEncontrado {
    Map<String, Object> model = new ModelMap();
    model.put("muebles", servicioEdicionInventarioMuebles.listar());
    model.put(PARAM_ANCHO, ancho);
    model.put(PARAM_LARGO, largo);
    model.put(ATTR_CATEGORIAS, servicioCategoriaDeMueble.obtenerCategorias());
    model.put("muebleAEliminar", servicioEdicionInventarioMuebles.buscarPorId(id));
    return new ModelAndView(VISTA_INVENTARIO, model);
  }

  @PostMapping("/inventario-muebles/eliminar/{id}/confirmar")
  public ModelAndView eliminar(@PathVariable("id") int id) throws MuebleNoEncontrado {
    try {
      servicioEdicionInventarioMuebles.eliminar(id);
      return new ModelAndView("redirect:/inventario-muebles");
    } catch (MuebleNoEncontrado e) {
      Map<String, Object> model = new ModelMap();
      model.put("error", e.getMessage());
      model.put(ATTR_CATEGORIAS, servicioCategoriaDeMueble.obtenerCategorias());
      return new ModelAndView(VISTA_INVENTARIO, model);
    }
  }

  @GetMapping("/inventario-muebles/eliminar/{id}/cancelar")
  public ModelAndView cancelarEliminacion(@PathVariable("id") int id) {
    return new ModelAndView("redirect:/inventario-muebles");
  }

  @PostMapping("/inventario-muebles/plano")
  public ModelAndView generarPlano(
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo
  ) {
    com.tallerwebi.dominio.Ambiente ambiente = new com.tallerwebi.dominio.Ambiente(ancho, largo);
    Map<String, Object> model = new ModelMap();
    model.put(
      "plano",
      servicioPlano2D.generarPlano(ambiente, servicioEdicionInventarioMuebles.listar())
    );
    return new ModelAndView("plano-interactivo", model);
  }

  @PostMapping("/inventario-muebles/agregar")
  public ModelAndView agregar(Mueble datos) {
    servicioEdicionInventarioMuebles.crear(datos);
    return new ModelAndView("redirect:/inventario-muebles");
  }
}
