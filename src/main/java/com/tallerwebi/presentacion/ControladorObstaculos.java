package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.ServicioObstaculos;
import com.tallerwebi.dominio.TipoDeObstaculo;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Paso de obstáculos fijos del armado del plano. Recibe las medidas del paso anterior,
 * las conserva en sesión y, si los obstáculos son válidos, sigue al inventario de
 * muebles con el ambiente ya validado.
 */
@Controller
public class ControladorObstaculos {

  private static final String VISTA = "obstaculos-config";
  private static final String ATRIBUTO_DATOS = "datosPlano";
  private static final String ATRIBUTO_AMBIENTE = "ambiente";

  private ServicioObstaculos servicioObstaculos;

  @Autowired
  public ControladorObstaculos(ServicioObstaculos servicioObstaculos) {
    this.servicioObstaculos = servicioObstaculos;
  }

  @RequestMapping(path = "/plano/obstaculos", method = RequestMethod.GET)
  public ModelAndView irAObstaculos(HttpSession sesion) {
    DatosPlano datos = (DatosPlano) sesion.getAttribute(ATRIBUTO_DATOS);
    return new ModelAndView(VISTA, modeloDelPaso(datos != null ? datos : new DatosPlano()));
  }

  @RequestMapping(path = "/plano/obstaculos", method = RequestMethod.POST)
  public ModelAndView irACargarObstaculos(DatosPlano datosDelFormulario, HttpSession sesion) {
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    return new ModelAndView(VISTA, modeloDelPaso(datosDelFormulario));
  }

  @RequestMapping(path = "/plano/obstaculos/confirmar", method = RequestMethod.POST)
  public ModelAndView confirmarObstaculos(DatosPlano datosDelFormulario, HttpSession sesion) {
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    try {
      servicioObstaculos.validarObstaculos(datosDelFormulario.aAmbiente());
    } catch (ValidacionException e) {
      Map<String, Object> modelo = modeloDelPaso(datosDelFormulario);
      modelo.put("error", e.getMessage());
      return new ModelAndView(VISTA, modelo);
    }
    sesion.setAttribute(ATRIBUTO_AMBIENTE, datosDelFormulario.aAmbiente());
    return new ModelAndView(
      "redirect:/inventario-muebles?ancho=" +
      datosDelFormulario.getAncho() +
      "&largo=" +
      datosDelFormulario.getLargo()
    );
  }

  private Map<String, Object> modeloDelPaso(DatosPlano datosDelFormulario) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosPlano", datosDelFormulario);
    modelo.put("tiposDeObstaculo", TipoDeObstaculo.values());
    modelo.put("muros", Muro.values());
    return modelo;
  }
}
