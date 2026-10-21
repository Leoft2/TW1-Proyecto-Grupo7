package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.TipoAbertura;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPlanoAberturas {

  private static final String VISTA = "plano-aberturas-config";
  private static final String ATRIBUTO_DATOS = "datosPlano";

  @RequestMapping(path = "/plano/aberturas", method = RequestMethod.GET)
  public ModelAndView irAConfigurarAberturas(HttpSession sesion) {
    DatosPlano datos = (DatosPlano) sesion.getAttribute(ATRIBUTO_DATOS);
    if (datos == null) {
      datos = new DatosPlano();
    }
    return new ModelAndView(VISTA, modelo(datos));
  }

  @RequestMapping(path = "/plano/aberturas", method = RequestMethod.POST)
  public ModelAndView guardarAberturas(DatosPlano datosDelFormulario, HttpSession sesion) {
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    return new ModelAndView(VISTA, modelo(datosDelFormulario));
  }

  @RequestMapping(path = "/plano/aberturas/confirmar", method = RequestMethod.POST)
  public ModelAndView confirmarAberturas(DatosPlano datosDelFormulario, HttpSession sesion) {
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    return new ModelAndView("redirect:/plano/obstaculos");
  }

  private Map<String, Object> modelo(DatosPlano datos) {
    Map<String, Object> modelo = new ModelMap();
    if (datos.getAberturas() == null || datos.getAberturas().isEmpty()) {
      datos.getAberturas().add(new DatosAbertura());
    }
    modelo.put("datosPlano", datos);
    modelo.put("tiposAbertura", TipoAbertura.values());
    modelo.put("muros", Muro.values());
    return modelo;
  }
}
