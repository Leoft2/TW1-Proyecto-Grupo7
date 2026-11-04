package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioAmbiente;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHome {

  private static final String ROL_ADMIN = "ADMIN";
  private static final String PARAM_ACTUALIZADO = "actualizado";
  private static final String PARAM_ERROR = "error";

  private ServicioAmbiente servicioAmbiente;

  @Autowired
  public ControladorHome(ServicioAmbiente servicioAmbiente) {
    this.servicioAmbiente = servicioAmbiente;
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome(
    HttpServletRequest request,
    @RequestParam(name = PARAM_ACTUALIZADO, required = false) String actualizado,
    @RequestParam(name = PARAM_ERROR, required = false) String error
  ) {
    return irAHomeCompleto(request, actualizado, error);
  }

  public ModelAndView irAHome(HttpServletRequest request) {
    return irAHomeCompleto(request, null, null);
  }

  private ModelAndView irAHomeCompleto(
    HttpServletRequest request,
    String actualizado,
    String error
  ) {
    Map<String, Object> modelo = new ModelMap();
    String email = (String) request.getSession().getAttribute("USUARIO_EMAIL");
    modelo.put("ambientes", servicioAmbiente.obtenerAmbientesDe(email));
    modelo.put("esAdmin", ROL_ADMIN.equals(request.getSession().getAttribute("ROL")));
    if (actualizado != null) {
      modelo.put("actualizado", actualizado);
    }
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView("home", modelo);
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("landing-habitat");
  }
}
