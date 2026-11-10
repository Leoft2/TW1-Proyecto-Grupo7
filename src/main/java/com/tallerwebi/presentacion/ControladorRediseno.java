package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RecomendacionPintura;
import com.tallerwebi.dominio.ServicioAmbiente;
import com.tallerwebi.dominio.ServicioCatalogo;
import com.tallerwebi.dominio.ServicioRecomendacionPintura;
import com.tallerwebi.dominio.ServicioRediseño;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.presentacion.excepcion.FiltroDeMueblesInvalidoException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRediseno {

  private final ServicioCatalogo servicioCatalogo;
  private final ServicioRediseño servicioRediseño;
  private final ServicioRecomendacionPintura servicioRecomendacionPintura;
  private final ServicioAmbiente servicioAmbiente;

  private static final String PRESUPUESTO = "presupuesto";
  private static final String ESTILO = "estilo";
  private static final String AMBIENTE = "ambiente";
  private static final String SOLO_SOSTENIBLES = "soloSostenibles";

  @Autowired
  public ControladorRediseno(
    ServicioCatalogo servicioCatalogo,
    ServicioRediseño servicioRediseño,
    ServicioRecomendacionPintura servicioRecomendacionPintura,
    ServicioAmbiente servicioAmbiente
  ) {
    this.servicioCatalogo = servicioCatalogo;
    this.servicioRediseño = servicioRediseño;
    this.servicioRecomendacionPintura = servicioRecomendacionPintura;
    this.servicioAmbiente = servicioAmbiente;
  }

  @RequestMapping(path = "/ambientes/{id}/redisenar", method = RequestMethod.GET)
  public ModelAndView redisenar(@PathVariable("id") Long id) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("estilos", Estilo.values());
    modelo.put(AMBIENTE, this.servicioAmbiente.buscarAmbiente(id));

    return new ModelAndView("redisenar", modelo);
  }

  @RequestMapping(path = "/ambientes/{id}/redisenar", method = RequestMethod.POST)
  public ModelAndView generarPropuesta(
    @PathVariable("id") Long id,
    @RequestParam(name = ESTILO, required = false) String estilo,
    @RequestParam(name = PRESUPUESTO, required = false) String presupuesto,
    @RequestParam(name = SOLO_SOSTENIBLES, required = false) String soloSostenibles
  ) throws FiltroDeMueblesInvalidoException {
    FiltroDeMuebles propuesta = FiltroDeMuebles.deUnaPropuesta(
      estilo,
      presupuesto,
      soloSostenibles,
      null
    );
    return new ModelAndView("resultadosRediseno", this.modeloDelResultado(id, propuesta, null));
  }

  private ModelAndView redirectAlFormulario(Long id) {
    return new ModelAndView("redirect:/ambientes/" + id + "/redisenar");
  }

  @ExceptionHandler(FiltroDeMueblesInvalidoException.class)
  public ModelAndView manejarFiltroInvalido(
    FiltroDeMueblesInvalidoException excepcion,
    @PathVariable("id") Long id
  ) {
    return this.redirectAlFormulario(id);
  }

  private Map<String, Object> modeloDelResultado(
    Long id,
    FiltroDeMuebles propuesta,
    Integer colorIndex
  ) {
    Ambiente ambiente = this.servicioAmbiente.buscarAmbiente(id);
    TipoDeAmbiente tipo = ambiente.getTipoDeAmbiente();
    Map<String, Object> modelo = new ModelMap();

    modelo.put("propuesta", new DatosPropuesta(propuesta.getEstilo(), propuesta.getPrecioMaximo()));
    modelo.put(AMBIENTE, ambiente);
    modelo.put("tipoDeAmbiente", tipo == null ? null : tipo.getNombre());
    modelo.put("recomendacion", this.servicioRediseño.recomendar(propuesta.getEstilo()));
    modelo.put(
      "coloresOrdenados",
      this.servicioRecomendacionPintura.coloresOrdenadosPorPrecio(propuesta.getEstilo())
    );
    RecomendacionPintura recomendacionPintura =
      this.servicioRecomendacionPintura.recomendar(ambiente, propuesta.getEstilo(), colorIndex);
    modelo.put("recomendacionPintura", recomendacionPintura);
    if (colorIndex != null) {
      modelo.put("colorSeleccionadoIndex", colorIndex);
    }
    modelo.put("soloSostenibles", propuesta.getSoloSostenibles());

    List<Mueble> muebles = null;

    try {
      muebles =
        this.servicioCatalogo.ObtenerMueblesQueCumplan(
            null,
            propuesta.getEstilo(),
            propuesta.getPrecioMaximo(),
            propuesta.getSoloSostenibles(),
            propuesta.getMaterialId()
          );
    } catch (PresupuestoNegativoException e) {
      modelo.put("error", "El presupuesto no puede ser negativo");
    }
    modelo.put("muebles", muebles);

    return modelo;
  }

  @RequestMapping(path = "/ambientes/{id}/redisenar/pintura/litros", method = RequestMethod.POST)
  public ModelAndView actualizarLitros(
    @PathVariable("id") Long id,
    @RequestParam("itemIndex") int itemIndex,
    @RequestParam("litros") Double litros,
    @RequestParam(name = "colorIndex", required = false) Integer colorIndex,
    @RequestParam(name = ESTILO, required = false) String estilo,
    @RequestParam(name = PRESUPUESTO, required = false) String presupuesto,
    @RequestParam(name = SOLO_SOSTENIBLES, required = false) String soloSostenibles
  ) throws FiltroDeMueblesInvalidoException {
    FiltroDeMuebles propuesta = FiltroDeMuebles.deUnaPropuesta(
      estilo,
      presupuesto,
      soloSostenibles,
      null
    );
    Map<String, Object> modelo = this.modeloDelResultado(id, propuesta, colorIndex);

    RecomendacionPintura recomendacionPintura = (RecomendacionPintura) modelo.get(
      "recomendacionPintura"
    );
    List<RecomendacionPintura.ItemPintura> items = recomendacionPintura.getItems();

    if (itemIndex >= 0 && itemIndex < items.size()) {
      RecomendacionPintura.ItemPintura item = items.get(itemIndex);
      item.setLitrosSeleccionados(litros);
      recalcularItem(item);
      recomendacionPintura.calcularTotal();
    }
    return new ModelAndView("resultadosRediseno", modelo);
  }

  private void recalcularItem(RecomendacionPintura.ItemPintura item) {
    BaldePintura balde = item.getBaldePintura();
    if (balde == null) {
      return;
    }
    Double metros = item.getMetrosCuadrados();
    Double litros = item.getLitrosSeleccionados();
    if (litros == null || litros <= 0) {
      litros = balde.getCantidadLitros();
    }
    if (litros == null || litros <= 0 || metros == null) {
      return;
    }
    int cantidad = (int) Math.ceil(metros / litros);
    item.setCantidadBaldes(cantidad);
    Double precio = balde.getPrecio();
    if (precio != null) {
      item.setPrecioTotal(cantidad * precio);
    }
  }

  @RequestMapping(path = "/ambientes/{id}/redisenar/pintura/color", method = RequestMethod.POST)
  public ModelAndView actualizarColor(
    @PathVariable("id") Long id,
    @RequestParam("colorIndex") Integer colorIndex,
    @RequestParam(name = ESTILO, required = false) String estilo,
    @RequestParam(name = PRESUPUESTO, required = false) String presupuesto,
    @RequestParam(name = SOLO_SOSTENIBLES, required = false) String soloSostenibles
  ) throws FiltroDeMueblesInvalidoException {
    FiltroDeMuebles propuesta = FiltroDeMuebles.deUnaPropuesta(
      estilo,
      presupuesto,
      soloSostenibles,
      null
    );
    Map<String, Object> modelo = this.modeloDelResultado(id, propuesta, colorIndex);

    Ambiente ambiente = this.servicioAmbiente.buscarAmbiente(id);
    Estilo estiloElegido = propuesta.getEstilo();
    RecomendacionPintura recomendacionPintura =
      this.servicioRecomendacionPintura.recomendar(ambiente, estiloElegido, colorIndex);
    recomendacionPintura.calcularTotal();
    modelo.put("recomendacionPintura", recomendacionPintura);
    modelo.put("colorSeleccionadoIndex", colorIndex);
    return new ModelAndView("resultadosRediseno", modelo);
  }
}
