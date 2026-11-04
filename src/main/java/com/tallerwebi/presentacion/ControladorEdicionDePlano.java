package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionDePlano;
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEdicionDePlano {

  private static final String ATRIBUTO_PLANO = "plano";
  private static final String ATRIBUTO_USUARIO_EMAIL = "USUARIO_EMAIL";

  private final ServicioEdicionDePlano servicioEdicionDePlano;
  private final ServicioPersistenciaPlano servicioPersistenciaPlano;

  @Autowired
  public ControladorEdicionDePlano(
    ServicioEdicionDePlano servicioEdicionDePlano,
    ServicioPersistenciaPlano servicioPersistenciaPlano
  ) {
    this.servicioEdicionDePlano = servicioEdicionDePlano;
    this.servicioPersistenciaPlano = servicioPersistenciaPlano;
  }

  @PostMapping("/plano/quitar-mueble")
  public ModelAndView quitarMueble(
    @RequestParam(name = "indice", required = false) Integer indice,
    HttpSession sesion
  ) {
    Plano plano = (Plano) sesion.getAttribute(ATRIBUTO_PLANO);
    if (!servicioEdicionDePlano.quitarMueble(plano, indice)) {
      return new ModelAndView("redirect:/plano/actual?errorQuitarMueble");
    }

    String usuarioEmail = (String) sesion.getAttribute(ATRIBUTO_USUARIO_EMAIL);
    if (usuarioEmail != null) {
      servicioPersistenciaPlano.guardar(plano, usuarioEmail);
      Plano planoGuardado = servicioPersistenciaPlano.buscarUltimoDe(usuarioEmail);
      if (planoGuardado != null) {
        plano = planoGuardado;
      }
    }
    sesion.setAttribute(ATRIBUTO_PLANO, plano);
    return new ModelAndView("redirect:/plano/actual?muebleQuitado");
  }
}
