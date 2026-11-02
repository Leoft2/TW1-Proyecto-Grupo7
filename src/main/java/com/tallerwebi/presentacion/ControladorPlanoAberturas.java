package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Abertura;
import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.ReglaDeApertura;
import com.tallerwebi.dominio.ServicioReglasDeApertura;
import com.tallerwebi.dominio.ServicioReglasDeAperturaImpl;
import com.tallerwebi.dominio.TipoAbertura;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPlanoAberturas {

  private static final String VISTA = "plano-aberturas-config";
  private static final String ATRIBUTO_DATOS = "datosPlano";
  // Clave del modelo que la vista usa para mostrar las advertencias de apertura.
  private static final String ATRIBUTO_ADVERTENCIAS = "advertenciasDeApertura";
  private static final String ERROR_DE_ABERTURA_INCOMPLETA =
    "Completá la posición y el ancho de cada abertura, o eliminá la fila.";
  // Mensaje que se agrega por cada puerta que no tiene espacio para abrir.
  private static final String MENSAJE_ADVERTENCIA =
    "La puerta del muro %s en la posición %s m no tiene pared libre para abrir; " +
    "la hoja podría chocar con muebles.";

  // Servicio que decide si una puerta tiene pared libre para abrir.
  private final ServicioReglasDeApertura servicioReglasDeApertura;

  // Constructor sin argumentos que conserva el uso directo del controlador.
  public ControladorPlanoAberturas() {
    this(new ServicioReglasDeAperturaImpl());
  }

  @Autowired
  public ControladorPlanoAberturas(ServicioReglasDeApertura servicioReglasDeApertura) {
    this.servicioReglasDeApertura = servicioReglasDeApertura;
  }

  @RequestMapping(path = "/plano/aberturas", method = RequestMethod.GET)
  public ModelAndView irAConfigurarAberturas(HttpSession sesion) {
    DatosPlano datos = (DatosPlano) sesion.getAttribute(ATRIBUTO_DATOS);
    if (datos == null) {
      datos = new DatosPlano();
    }
    return new ModelAndView(VISTA, modelo(datos));
  }

  @RequestMapping(path = "/plano/aberturas", method = RequestMethod.POST)
  public ModelAndView guardarAberturas(DatosPlano datosDelFormulario, HttpSession sesion) {
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    return new ModelAndView(VISTA, modelo(datosDelFormulario));
  }

  @RequestMapping(path = "/plano/aberturas/confirmar", method = RequestMethod.POST)
  public ModelAndView confirmarAberturas(
    DatosPlano datosDelFormulario,
    HttpSession sesion,
    // El boton "Continuar igual" reenvia el formulario con este flag en true.
    @RequestParam(name = "confirmarAdvertencias", required = false) Boolean confirmarAdvertencias
  ) {
    if (datosDelFormulario.hayAberturasIncompletas()) {
      Map<String, Object> modelo = modelo(datosDelFormulario);
      modelo.put("error", ERROR_DE_ABERTURA_INCOMPLETA);
      return new ModelAndView(VISTA, modelo);
    }
    // Si hay puertas sin pared libre y el usuario no confirmo, se vuelve a la
    // vista con las advertencias sin impedir que pueda continuar.
    List<String> advertencias = advertenciasDeApertura(datosDelFormulario);
    if (!advertencias.isEmpty() && !Boolean.TRUE.equals(confirmarAdvertencias)) {
      return new ModelAndView(VISTA, modelo(datosDelFormulario, advertencias));
    }
    sesion.setAttribute(ATRIBUTO_DATOS, datosDelFormulario);
    return new ModelAndView("redirect:/plano/obstaculos");
  }

  // Arma el modelo recalculando las advertencias cada vez que se vuelve a la vista.
  private Map<String, Object> modelo(DatosPlano datos) {
    return modelo(datos, advertenciasDeApertura(datos));
  }

  // Variante que recibe las advertencias ya calculadas para no repetir el calculo.
  private Map<String, Object> modelo(DatosPlano datos, List<String> advertencias) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_DATOS, datos);
    modelo.put("tiposAbertura", TipoAbertura.values());
    modelo.put("muros", Muro.values());
    modelo.put(ATRIBUTO_ADVERTENCIAS, advertencias);
    return modelo;
  }

  // Calcula una advertencia por cada PUERTA sin pared libre. Las ventanas no
  // se consultan al servicio y, si faltan las medidas, no se intenta calcular.
  private List<String> advertenciasDeApertura(DatosPlano datos) {
    List<String> advertencias = new ArrayList<>();
    if (datos.getAncho() == null || datos.getLargo() == null) {
      return advertencias;
    }
    Ambiente ambiente = datos.aAmbiente();
    for (Abertura abertura : ambiente.getAberturas()) {
      if (abertura.getTipo() == TipoAbertura.PUERTA) {
        ReglaDeApertura regla = servicioReglasDeApertura.determinarApertura(abertura, ambiente);
        if (!regla.getHayParedLibre()) {
          advertencias.add(
            String.format(
              MENSAJE_ADVERTENCIA,
              abertura.getMuro().getNombre(),
              abertura.getPosicion()
            )
          );
        }
      }
    }
    return advertencias;
  }
}
