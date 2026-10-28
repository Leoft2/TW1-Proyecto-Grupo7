package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.OpcionDeDistribucion;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import com.tallerwebi.dominio.ServicioRankingDeDistribuciones;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRankingDeDistribuciones {

  private static final String VISTA_RANKING = "ranking-distribuciones";
  private static final String ATRIBUTO_AMBIENTE = "ambiente";
  private static final String ATRIBUTO_OPCIONES = "opcionesRanking";
  private static final String ATRIBUTO_PLANO = "plano";
  private static final String ATRIBUTO_USUARIO_EMAIL = "USUARIO_EMAIL";

  private final ServicioRankingDeDistribuciones servicioRankingDeDistribuciones;
  private final ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private final ServicioPersistenciaPlano servicioPersistenciaPlano;

  @Autowired
  public ControladorRankingDeDistribuciones(
    ServicioRankingDeDistribuciones servicioRankingDeDistribuciones,
    ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles,
    ServicioPersistenciaPlano servicioPersistenciaPlano
  ) {
    this.servicioRankingDeDistribuciones = servicioRankingDeDistribuciones;
    this.servicioEdicionInventarioMuebles = servicioEdicionInventarioMuebles;
    this.servicioPersistenciaPlano = servicioPersistenciaPlano;
  }

  @GetMapping("/plano/ranking")
  public ModelAndView verRanking(HttpSession sesion) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("opciones", obtenerOpciones(sesion));
    return new ModelAndView(VISTA_RANKING, modelo);
  }

  @PostMapping("/plano/ranking/generar")
  public ModelAndView generarRanking(
    @RequestParam(name = "ancho", required = false) Double ancho,
    @RequestParam(name = "largo", required = false) Double largo,
    HttpSession sesion
  ) {
    Ambiente ambiente = obtenerAmbiente(ancho, largo, sesion);
    List<OpcionDeDistribucion> opciones;
    String mensaje = null;
    try {
      opciones =
        servicioRankingDeDistribuciones.generarRanking(
          ambiente,
          servicioEdicionInventarioMuebles.listar()
        );
      if (opciones.isEmpty()) {
        mensaje =
          "No fue posible generar una distribución válida. Las opciones probadas no pudieron ubicar todos los muebles sin interferir con puertas, obstáculos o el ancho mínimo de circulación.";
      }
    } catch (ValidacionException e) {
      opciones = new ArrayList<>();
      mensaje = "No fue posible generar las distribuciones: " + e.getMessage();
    }
    sesion.setAttribute(ATRIBUTO_OPCIONES, opciones);
    Map<String, Object> modelo = new ModelMap();
    modelo.put("opciones", opciones);
    modelo.put("mensaje", mensaje);
    return new ModelAndView(VISTA_RANKING, modelo);
  }

  @PostMapping("/plano/ranking/confirmar")
  public ModelAndView confirmarOpcion(@RequestParam("indice") Integer indice, HttpSession sesion) {
    String usuarioEmail = (String) sesion.getAttribute(ATRIBUTO_USUARIO_EMAIL);
    if (usuarioEmail == null) {
      return new ModelAndView("redirect:/login");
    }
    List<OpcionDeDistribucion> opciones = obtenerOpciones(sesion);
    if (indice == null || indice < 0 || indice >= opciones.size()) {
      return new ModelAndView("redirect:/plano/ranking");
    }
    Plano planoElegido = opciones.get(indice).getPlano();
    servicioPersistenciaPlano.guardar(planoElegido, usuarioEmail);
    sesion.setAttribute(ATRIBUTO_PLANO, planoElegido);
    sesion.removeAttribute(ATRIBUTO_OPCIONES);
    return new ModelAndView("redirect:/plano/actual");
  }

  private Ambiente obtenerAmbiente(Double ancho, Double largo, HttpSession sesion) {
    Ambiente ambiente = (Ambiente) sesion.getAttribute(ATRIBUTO_AMBIENTE);
    if (ambiente != null) {
      return ambiente;
    }
    return new Ambiente(ancho, largo);
  }

  @SuppressWarnings("unchecked")
  private List<OpcionDeDistribucion> obtenerOpciones(HttpSession sesion) {
    Object atributo = sesion.getAttribute(ATRIBUTO_OPCIONES);
    List<OpcionDeDistribucion> opciones = new ArrayList<>();
    if (atributo instanceof List<?>) {
      for (Object elemento : (List<?>) atributo) {
        if (elemento instanceof OpcionDeDistribucion) {
          opciones.add((OpcionDeDistribucion) elemento);
        }
      }
    }
    return opciones;
  }
}
