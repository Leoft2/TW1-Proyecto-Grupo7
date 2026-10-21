package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Abertura;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.ServicioAmbiente;
import com.tallerwebi.dominio.TipoAbertura;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorAmbiente {

  private final ServicioAmbiente servicioAmbiente;
  private static final String AMBIENTE_VIEW = "ambiente";

  @Autowired
  public ControladorAmbiente(ServicioAmbiente servicioAmbiente) {
    this.servicioAmbiente = servicioAmbiente;
  }

  @RequestMapping(path = "/ambientes", method = RequestMethod.GET)
  public ModelAndView ambientes(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();
    String email = (String) request.getSession().getAttribute("USUARIO_EMAIL");
    modelo.put("ambientes", servicioAmbiente.obtenerAmbientesDe(email));
    return new ModelAndView("ambientes", modelo);
  }

  @RequestMapping(path = "/ambientes/crear", method = RequestMethod.GET)
  public ModelAndView crearAmbiente() {
    return new ModelAndView("crearAmbiente");
  }

  @RequestMapping(path = "/ambientes/{id}", method = RequestMethod.GET)
  public ModelAndView detalleAmbiente(@PathVariable("id") Long id) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(AMBIENTE_VIEW, servicioAmbiente.buscarAmbiente(id));
    return new ModelAndView(AMBIENTE_VIEW, modelo);
  }

  @RequestMapping(path = "/ambientes/{id}/aberturas", method = RequestMethod.GET)
  public ModelAndView irAConfigurarAberturas(@PathVariable("id") Long id) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(AMBIENTE_VIEW, servicioAmbiente.buscarAmbiente(id));
    modelo.put("datosAbertura", new DatosAbertura());
    modelo.put("tiposAbertura", TipoAbertura.values());
    modelo.put("muros", Muro.values());
    return new ModelAndView("ambiente-aberturas", modelo);
  }

  @RequestMapping(path = "/ambientes/{id}/aberturas", method = RequestMethod.POST)
  public ModelAndView agregarAbertura(
    @PathVariable("id") Long id,
    @ModelAttribute("datosAbertura") DatosAbertura datos
  ) {
    try {
      Abertura abertura = new Abertura(
        datos.getTipo(),
        datos.getMuro(),
        datos.getPosicion(),
        datos.getAncho(),
        datos.getRadioDeGiro()
      );
      servicioAmbiente.agregarAbertura(id, abertura);
      return new ModelAndView("redirect:/ambientes/" + id + "/aberturas");
    } catch (Exception e) {
      Map<String, Object> modelo = new ModelMap();
      modelo.put(AMBIENTE_VIEW, servicioAmbiente.buscarAmbiente(id));
      modelo.put("datosAbertura", datos);
      modelo.put("tiposAbertura", TipoAbertura.values());
      modelo.put("muros", Muro.values());
      modelo.put("error", e.getMessage());
      return new ModelAndView("ambiente-aberturas", modelo);
    }
  }
}
