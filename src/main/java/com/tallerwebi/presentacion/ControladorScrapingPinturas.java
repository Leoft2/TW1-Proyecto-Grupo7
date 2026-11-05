package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.DetalleBaldeScrapeado;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/pinturas/baldes")
public class ControladorScrapingPinturas {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControladorScrapingPinturas.class);

  private static final String ROL_ADMIN = "ADMIN";
  private static final String REDIRECT_HOME = "redirect:/home";
  private static final String VISTA_DETALLE_BALDE = "detalle-balde";

  private final ServicioAdministracionBaldesPintura servicioAdministracion;
  private final RepositorioBaldePintura repositorioBaldePintura;
  private final ServicioScrapingDetalleBaldePintura servicioScrapingDetalle;

  @Autowired
  public ControladorScrapingPinturas(
    ServicioAdministracionBaldesPintura servicioAdministracion,
    RepositorioBaldePintura repositorioBaldePintura,
    ServicioScrapingDetalleBaldePintura servicioScrapingDetalle
  ) {
    this.servicioAdministracion = servicioAdministracion;
    this.repositorioBaldePintura = repositorioBaldePintura;
    this.servicioScrapingDetalle = servicioScrapingDetalle;
  }

  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ModelAndView detalleBalde(@PathVariable("id") Long id) {
    Optional<BaldePintura> baldeOptional = Optional.ofNullable(
      repositorioBaldePintura.buscarPorId(id)
    );
    if (baldeOptional.isEmpty()) {
      return new ModelAndView(REDIRECT_HOME + "?error=baldeNoEncontrado");
    }
    BaldePintura balde = baldeOptional.get();
    Map<String, Object> modelo = new ModelMap();
    modelo.put("balde", balde);
    if (balde.getUrl() != null) {
      try {
        Optional<DetalleBaldeScrapeado> detalle = servicioScrapingDetalle.scrapearDetalle(
          balde.getUrl()
        );
        if (detalle.isPresent()) {
          DetalleBaldeScrapeado detalleScrapeado = detalle.get();
          if (detalleScrapeado.getLitros() != null) {
            balde.setCantidadLitros(detalleScrapeado.getLitros());
          }
          if (detalleScrapeado.getNombre() != null) {
            balde.setNombre(detalleScrapeado.getNombre());
          }
        }
      } catch (Exception e) {
        LOGGER.warn("No se pudieron obtener los litros del balde {}", id, e);
      }
    }
    return new ModelAndView(VISTA_DETALLE_BALDE, modelo);
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
