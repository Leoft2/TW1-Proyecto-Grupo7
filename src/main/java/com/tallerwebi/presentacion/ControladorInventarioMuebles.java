package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
  private static final String PARAM_CATEGORIA = "categoria";
  private static final String ATTR_CATEGORIAS = "categorias";
  private static final String ATRIBUTO_AMBIENTE = "ambiente";
  private static final String ATRIBUTO_PLANO = "plano";

  private final ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private final ServicioPlano2D servicioPlano2D;
  private final ServicioCategoriaDeMueble servicioCategoriaDeMueble;
  private final ServicioPersistenciaPlano servicioPersistenciaPlano;

  public ControladorInventarioMuebles(
    ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles,
    ServicioPlano2D servicioPlano2D,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble
  ) {
    this(servicioEdicionInventarioMuebles, servicioPlano2D, servicioCategoriaDeMueble, null);
  }

  @Autowired
  public ControladorInventarioMuebles(
    ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles,
    ServicioPlano2D servicioPlano2D,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble,
    ServicioPersistenciaPlano servicioPersistenciaPlano
  ) {
    this.servicioEdicionInventarioMuebles = servicioEdicionInventarioMuebles;
    this.servicioPlano2D = servicioPlano2D;
    this.servicioCategoriaDeMueble = servicioCategoriaDeMueble;
    this.servicioPersistenciaPlano = servicioPersistenciaPlano;
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
    return editar(id, datos, null, null, null);
  }

  @PostMapping("/inventario-muebles/editar/{id}")
  public ModelAndView editar(
    @PathVariable("id") int id,
    Mueble datos,
    @RequestParam(name = PARAM_ANCHO, required = false) Double ancho,
    @RequestParam(name = PARAM_LARGO, required = false) Double largo,
    @RequestParam(name = PARAM_CATEGORIA, required = false) Long categoriaId
  ) throws MuebleNoEncontrado {
    asignarCategoria(datos, categoriaId);
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
    @RequestParam(name = PARAM_LARGO, required = false) Double largo,
    HttpSession sesion
  ) {
    // Si el usuario llego sin pasar por el asistente, no hay medidas del ambiente en la
    // sesion ni en el formulario: el servicio valida y este catch lo devuelve al primer
    // paso del asistente en vez de dejar que termine en un 500.
    Ambiente ambiente = (Ambiente) sesion.getAttribute(ATRIBUTO_AMBIENTE);
    if (ambiente == null) {
      ambiente = new Ambiente(ancho, largo);
    }
    try {
      Map<String, Object> model = new ModelMap();
      Plano plano = servicioPlano2D.generarPlano(
        ambiente,
        servicioEdicionInventarioMuebles.listar()
      );
      String usuarioEmail = (String) sesion.getAttribute("USUARIO_EMAIL");
      if (usuarioEmail != null) {
        servicioPersistenciaPlano.guardar(plano, usuarioEmail);
      }
      sesion.setAttribute(ATRIBUTO_PLANO, plano);
      model.put(ATRIBUTO_PLANO, plano);
      return new ModelAndView("plano-interactivo", model);
    } catch (ValidacionException e) {
      return new ModelAndView("redirect:/plano/ambiente");
    }
  }

  public ModelAndView agregar(Mueble datos) {
    return agregar(datos, null);
  }

  @PostMapping("/inventario-muebles/agregar")
  public ModelAndView agregar(
    Mueble datos,
    @RequestParam(name = PARAM_CATEGORIA, required = false) Long categoriaId
  ) {
    asignarCategoria(datos, categoriaId);
    servicioEdicionInventarioMuebles.crear(datos);
    return new ModelAndView("redirect:/inventario-muebles");
  }

  private void asignarCategoria(Mueble datos, Long categoriaId) {
    if (categoriaId != null) {
      datos.setCategoriaDeMueble(servicioCategoriaDeMueble.buscarPorId(categoriaId));
    }
  }
}
