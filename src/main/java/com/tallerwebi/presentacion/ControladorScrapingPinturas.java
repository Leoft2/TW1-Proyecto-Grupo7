package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.Litros;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import com.tallerwebi.dominio.RepositorioLitros;
import com.tallerwebi.dominio.ServicioAdministracionBaldesPintura;
import com.tallerwebi.dominio.ServicioScrapingDetalleBaldePintura;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
  private final ServicioScrapingDetalleBaldePintura servicioScrapingDetalleBaldePintura;
  private final RepositorioLitros repositorioLitros;

  @Autowired
  public ControladorScrapingPinturas(
    ServicioAdministracionBaldesPintura servicioAdministracion,
    RepositorioBaldePintura repositorioBaldePintura,
    ServicioScrapingDetalleBaldePintura servicioScrapingDetalleBaldePintura,
    RepositorioLitros repositorioLitros
  ) {
    this.servicioAdministracion = servicioAdministracion;
    this.repositorioBaldePintura = repositorioBaldePintura;
    this.servicioScrapingDetalleBaldePintura = servicioScrapingDetalleBaldePintura;
    this.repositorioLitros = repositorioLitros;
  }

  @GetMapping("/{id}")
  @Transactional
  public ModelAndView detalleBalde(@PathVariable("id") Long id) {
    Optional<BaldePintura> baldeOptional = Optional.ofNullable(
      repositorioBaldePintura.buscarPorId(id)
    );
    if (baldeOptional.isEmpty()) {
      return new ModelAndView(REDIRECT_HOME + "?error=baldeNoEncontrado");
    }
    BaldePintura balde = baldeOptional.get();
    if (balde.getUrl() != null && balde.getLitrosOpciones().isEmpty()) {
      try {
        Optional<List<Double>> opciones = servicioScrapingDetalleBaldePintura.obtenerOpcionesLitros(
          balde.getUrl()
        );
        if (opciones.isPresent() && !opciones.get().isEmpty()) {
          guardarOpcionesLitros(balde, opciones.get());
          repositorioBaldePintura.guardar(balde);
        }
      } catch (Exception e) {
        LOGGER.error("Error obteniendo opciones de litros del balde {}", id, e);
      }
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("balde", balde);
    modelo.put("opcionesLitros", balde.getOpcionesLitrosFormateadas());
    return new ModelAndView(VISTA_DETALLE_BALDE, modelo);
  }

  private void guardarOpcionesLitros(BaldePintura balde, List<Double> opciones) {
    balde.limpiarOpcionesLitros();
    for (Double valor : opciones) {
      Litros litro = repositorioLitros
        .buscarPorValor(valor)
        .orElseGet(() -> {
          Litros nuevo = new Litros(valor);
          repositorioLitros.guardar(nuevo);
          return nuevo;
        });
      balde.agregarOpcionLitros(litro);
    }
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
