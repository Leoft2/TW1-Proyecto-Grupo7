package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@SessionAttributes("plano")
public class ControladorPlano2D {

  private static final String ATRIBUTO_PLANO = "plano";

  private ServicioPlano2D servicioPlano2D;
  private ServicioCategoriaDeMueble servicioCategoriaDeMueble;
  private ServicioPersistenciaPlano servicioPersistenciaPlano;

  public ControladorPlano2D(
    ServicioPlano2D servicioPlano2D,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble
  ) {
    this(servicioPlano2D, servicioCategoriaDeMueble, null);
  }

  @Autowired
  public ControladorPlano2D(
    ServicioPlano2D servicioPlano2D,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble,
    ServicioPersistenciaPlano servicioPersistenciaPlano
  ) {
    this.servicioPlano2D = servicioPlano2D;
    this.servicioCategoriaDeMueble = servicioCategoriaDeMueble;
    this.servicioPersistenciaPlano = servicioPersistenciaPlano;
  }

  //Review 2 : Usar model Map que vinimos haciendo en clase.
  @RequestMapping(path = "/plano/ambiente", method = RequestMethod.GET)
  public ModelAndView irAConfigurarAmbiente() {
    return new ModelAndView(
      "ambiente-config",
      "categorias",
      servicioCategoriaDeMueble.obtenerCategorias()
    );
  }

  public ModelAndView generarPlano(Ambiente ambiente, List<Mueble> muebles) {
    try {
      Plano plano = servicioPlano2D.generarPlano(ambiente, muebles);
      return new ModelAndView("plano-interactivo", "plano", plano);
    } catch (ValidacionException e) {
      return planoConError(e);
    }
  }

  private ModelAndView planoConError(ValidacionException error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("error", error.getMessage());
    return new ModelAndView("plano-interactivo", modelo);
  }

  /**
   * Entrada HTTP del formulario. Spring entrega aca el {@link DatosPlano} ya bindeado desde el
   * request; este metodo arma los muebles con el servicio de categorías (HU-08: completa las
   * medidas vacías con el promedio) y delega en {@link #generarPlano}, que es quien decide la
   * vista y el modelo.
   */
  @RequestMapping(path = "/plano/generar", method = RequestMethod.POST)
  public ModelAndView generarPlanoDesdeFormulario(DatosPlano datosDelFormulario) {
    Ambiente ambiente = datosDelFormulario.aAmbiente();
    List<Mueble> muebles = new ArrayList<>();
    try {
      for (DatosMueble datosMueble : datosDelFormulario.getMuebles()) {
        muebles.add(
          servicioCategoriaDeMueble.crearMueble(
            datosMueble.getNombre(),
            datosMueble.getCategoria(),
            datosMueble.getAncho(),
            datosMueble.getProfundidad()
          )
        );
      }
    } catch (ValidacionException e) {
      return planoConError(e);
    }
    return generarPlano(ambiente, muebles);
  }

  @RequestMapping(path = "/plano/mover-mueble", method = RequestMethod.POST)
  public ResponseEntity<String> moverMueble(
    @RequestParam("indice") Integer indice,
    @RequestParam("posicionX") Double posicionX,
    @RequestParam("posicionY") Double posicionY,
    HttpSession sesion
  ) {
    Plano plano = (Plano) sesion.getAttribute(ATRIBUTO_PLANO);
    if (plano == null) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("No hay un plano activo para editar.");
    }
    if (!movimientoEsValido(indice, posicionX, posicionY, plano)) {
      return ResponseEntity.badRequest().body("La posición del mueble no es válida.");
    }

    MuebleUbicado muebleUbicado = plano.getMuebles().get(indice);
    MuebleUbicado candidato = new MuebleUbicado(muebleUbicado.getMueble(), posicionX, posicionY);
    if (!servicioPlano2D.estaDentroDelPerimetro(candidato, plano.getAmbiente())) {
      return ResponseEntity
        .badRequest()
        .body("El mueble debe permanecer dentro de los límites del ambiente.");
    }

    ValidacionMovimiento validacion = servicioPlano2D.validarMovimiento(
      muebleUbicado,
      plano.getAmbiente(),
      posicionX,
      posicionY
    );
    if (validacion.hayInterferencia()) {
      return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(
          "El mueble interfiere con una zona restringida u obstáculo. Se mantuvo su posición anterior."
        );
    }

    muebleUbicado.moverA(posicionX, posicionY);
    String usuarioEmail = (String) sesion.getAttribute("USUARIO_EMAIL");
    if (usuarioEmail != null) {
      servicioPersistenciaPlano.guardar(plano, usuarioEmail);
    }
    sesion.setAttribute(ATRIBUTO_PLANO, plano);
    return ResponseEntity.ok("La posición del mueble se guardó correctamente.");
  }

  @RequestMapping(path = "/plano/ultimo", method = RequestMethod.GET)
  public ModelAndView ultimoPlano(HttpSession sesion) {
    String usuarioEmail = (String) sesion.getAttribute("USUARIO_EMAIL");
    if (usuarioEmail == null) {
      return new ModelAndView("redirect:/login");
    }
    Plano plano = servicioPersistenciaPlano.buscarUltimoDe(usuarioEmail);
    if (plano == null) {
      return new ModelAndView("redirect:/plano/ambiente");
    }
    sesion.setAttribute(ATRIBUTO_PLANO, plano);
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_PLANO, plano);
    return new ModelAndView("plano-interactivo", modelo);
  }

  private boolean movimientoEsValido(
    Integer indice,
    Double posicionX,
    Double posicionY,
    Plano plano
  ) {
    return (
      indice != null &&
      indice >= 0 &&
      indice < plano.getMuebles().size() &&
      posicionX != null &&
      posicionY != null &&
      Double.isFinite(posicionX) &&
      Double.isFinite(posicionY)
    );
  }
}
