package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioAmbiente;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorHome {

  private ServicioAmbiente servicioAmbiente;

  @Autowired
  public ControladorHome(ServicioAmbiente servicioAmbiente) {
    this.servicioAmbiente = servicioAmbiente;
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();
    String email = (String) request.getSession().getAttribute("USUARIO_EMAIL");
    modelo.put("ambientes", servicioAmbiente.obtenerAmbientesDe(email));
    return new ModelAndView("home", modelo);
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("landing-habitat");
  }
}
