package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/pinturas/baldes")
public class ControladorScrapingPinturas {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControladorScrapingPinturas.class);

  private static final String ROL_ADMIN = "ADMIN";
  private static final String REDIRECT_HOME = "redirect:/home";

  private final ServicioAdministracionBaldesPintura servicioAdministracion;

  @Autowired
  public ControladorScrapingPinturas(ServicioAdministracionBaldesPintura servicioAdministracion) {
    this.servicioAdministracion = servicioAdministracion;
  }

  @PostMapping("/actualizar")
  public ModelAndView actualizarBaldes(HttpServletRequest request) {
    if (!esAdministrador(request)) {
      return new ModelAndView(REDIRECT_HOME + "?error=sinPermisos");
    }
    try {
      servicioAdministracion.actualizarBaldesDesdeColorshop();
      return new ModelAndView(REDIRECT_HOME + "?actualizado=ok");
    } catch (Exception e) {
      LOGGER.error("Error scrapeando baldes de pintura desde Colorshop", e);
      String mensaje = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
      String errorCodificado = URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
      return new ModelAndView(REDIRECT_HOME + "?error=scraping:" + errorCodificado);
    }
  }

  private boolean esAdministrador(HttpServletRequest request) {
    HttpSession sesion = request.getSession();
    return sesion != null && ROL_ADMIN.equals(sesion.getAttribute("ROL"));
  }
}
