package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCatalogoPinturas {

  private static final String VISTA_CATALOGO_PINTURAS = "catalogo-pinturas";
  private static final String REDIRECT_HOME = "redirect:/home";

  private final RepositorioBaldePintura repositorioBaldePintura;

  @Autowired
  public ControladorCatalogoPinturas(RepositorioBaldePintura repositorioBaldePintura) {
    this.repositorioBaldePintura = repositorioBaldePintura;
  }

  @RequestMapping(path = "/pinturas/baldes", method = RequestMethod.GET)
  @Transactional(readOnly = true)
  public ModelAndView catalogoPinturas(HttpServletRequest request) {
    List<BaldePintura> todos = repositorioBaldePintura.obtenerTodos();

    List<BaldePintura> baldesConColor = new ArrayList<>();
    for (BaldePintura balde : todos) {
      if (balde.getColorNombre() != null) {
        baldesConColor.add(balde);
      }
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("baldes", baldesConColor);
    modelo.put("totalBaldes", baldesConColor.size());
    return new ModelAndView(VISTA_CATALOGO_PINTURAS, modelo);
  }
}
