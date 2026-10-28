package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioHistorialDePlanos;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHistorialDePlanos {

  private static final String ATRIBUTO_USUARIO_EMAIL = "USUARIO_EMAIL";
  private static final String REDIRECCION_LOGIN = "redirect:/login";

  private final ServicioHistorialDePlanos servicioHistorialDePlanos;

  @Autowired
  public ControladorHistorialDePlanos(ServicioHistorialDePlanos servicioHistorialDePlanos) {
    this.servicioHistorialDePlanos = servicioHistorialDePlanos;
  }

  @RequestMapping(path = "/plano/historial", method = RequestMethod.GET)
  public ModelAndView verHistorial(HttpSession sesion) {
    String usuarioEmail = (String) sesion.getAttribute(ATRIBUTO_USUARIO_EMAIL);
    if (usuarioEmail == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    List<Plano> versiones = servicioHistorialDePlanos.listarVersiones(usuarioEmail);
    Map<String, Object> modelo = new ModelMap();
    modelo.put("versiones", versiones);
    return new ModelAndView("historial-planos", modelo);
  }

  @RequestMapping(path = "/plano/historial/restaurar", method = RequestMethod.POST)
  public ModelAndView restaurarVersion(@RequestParam("id") Long id, HttpSession sesion) {
    String usuarioEmail = (String) sesion.getAttribute(ATRIBUTO_USUARIO_EMAIL);
    if (usuarioEmail == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    Plano plano = servicioHistorialDePlanos.restaurarVersion(id, usuarioEmail);
    if (plano == null) {
      return new ModelAndView("redirect:/plano/historial");
    }
    sesion.setAttribute("plano", plano);
    return new ModelAndView("redirect:/plano/actual");
  }
}
