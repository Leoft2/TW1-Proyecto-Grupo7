package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.Color;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Iluminacion;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.Recomendacion;
import com.tallerwebi.dominio.RecomendacionPintura;
import com.tallerwebi.dominio.ServicioAmbiente;
import com.tallerwebi.dominio.ServicioCatalogo;
import com.tallerwebi.dominio.ServicioRecomendacionPintura;
import com.tallerwebi.dominio.ServicioRediseño;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.presentacion.ControladorRediseno;
import com.tallerwebi.presentacion.DatosPropuesta;
import com.tallerwebi.presentacion.excepcion.FiltroDeMueblesInvalidoException;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRedisenoTest {

  private static final Long AMBIENTE_ID = 1L;
  private static final String FORMULARIO = "redirect:/ambientes/1/redisenar";
  private static final String RESULTADOS = "resultadosRediseno";
  private static final Double PRESUPUESTO = 60000.0;
  private static final Estilo ESTILO = Estilo.Japandi;

  private ServicioCatalogo servicioCatalogo;
  private ServicioRediseño servicioRediseño;
  private ServicioRecomendacionPintura servicioRecomendacionPintura;
  private ServicioAmbiente servicioAmbiente;
  private ControladorRediseno controladorRediseno;

  @BeforeEach
  public void init() {
    this.servicioCatalogo = mock(ServicioCatalogo.class);
    this.servicioRediseño = mock(ServicioRediseño.class);
    this.servicioRecomendacionPintura = mock(ServicioRecomendacionPintura.class);
    this.servicioAmbiente = mock(ServicioAmbiente.class);
    this.controladorRediseno =
      new ControladorRediseno(
        this.servicioCatalogo,
        this.servicioRediseño,
        this.servicioRecomendacionPintura,
        this.servicioAmbiente
      );
  }

  @Test
  public void dadoQueNavegoAlFormularioDeRediseno_cuandoLoHago_entoncesReciboLosEstilosYElAmbiente()
    throws Exception {
    TipoDeAmbiente habitacion = new TipoDeAmbiente("Habitación");
    Ambiente ambiente = new Ambiente(4.0, 3.0, habitacion);
    when(this.servicioAmbiente.buscarAmbiente(AMBIENTE_ID)).thenReturn(ambiente);

    ModelAndView modelAndView = this.controladorRediseno.redisenar(AMBIENTE_ID);

    assertThat(modelAndView.getViewName(), is("redisenar"));
    assertThat(modelAndView.getModel().get("estilos"), is(Estilo.values()));
    assertThat(modelAndView.getModel().get("ambiente"), is(ambiente));
  }

  @Test
  public void dadoQueCompletoElFormularioDeRediseno_cuandoLoEnvio_entoncesVeoElResultadoConLoElegido()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(modelAndView.getViewName(), is(RESULTADOS));
    DatosPropuesta propuesta = this.obtenerPropuestaDelModel(modelAndView);
    assertThat(propuesta.getEstilo(), is(ESTILO));
    assertThat(propuesta.getPresupuesto(), is(PRESUPUESTO));
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_cuandoLoHago_entoncesLePidoAlCatalogoLosMueblesDeLoElegido()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    verify(this.servicioCatalogo).ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, false, null);
    verify(this.servicioRediseño).recomendar(ESTILO);
  }

  @Test
  public void dadoQueElijoSoloSostenibles_cuandoEnvioElFormularioDeRediseno_entoncesSeLoPidoAsiAlCatalogo()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    when(this.servicioCatalogo.ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, true, null))
      .thenReturn(muebles);

    this.controladorRediseno.generarPropuesta(
        AMBIENTE_ID,
        ESTILO.name(),
        String.valueOf(PRESUPUESTO),
        "true"
      );

    verify(this.servicioCatalogo).ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, true, null);
  }

  @Test
  public void dadoQueNoElijoSoloSostenibles_cuandoEnvioElFormularioDeRediseno_entoncesElModeloNoLoIndica()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(modelAndView.getModel().get("soloSostenibles"), is(false));
  }

  @Test
  public void dadoUnMuebleQueCumpleConLoElegido_cuandoVeoElResultado_entoncesVeoEseMueble()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    Mueble affordable = unMueble("Sillón Japandi", 60000.0, ESTILO);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(affordable);
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(affordable));
  }

  @Test
  public void dadoQueNingunMuebleCumpleConLoElegido_cuandoVeoElResultado_entoncesVeoLaVistaSinMuebles()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(this.obtenerMueblesDelModel(modelAndView), empty());
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_cuandoLoHago_entoncesElModeloTraeLaPaletaYLaIluminacionRecomendadas()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    ArrayList<Color> colores = new ArrayList<>();
    colores.add(new Color("Arena", "#E6D5BC"));
    colores.add(new Color("Madera clara", "#C8A27A"));
    colores.add(new Color("Gris", "#9E9E9E"));
    Recomendacion recomendacion = new Recomendacion(ESTILO, colores, Iluminacion.CALIDA);
    when(this.servicioRediseño.recomendar(ESTILO)).thenReturn(recomendacion);
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    Recomendacion recomendacionObtenida = (Recomendacion) modelAndView
      .getModel()
      .get("recomendacion");
    assertThat(recomendacionObtenida.getEstilo(), is(ESTILO));
    assertThat(recomendacionObtenida.getColores(), hasSize(3));
    assertThat(recomendacionObtenida.getIluminacion(), is(Iluminacion.CALIDA));
  }

  @Test
  public void dadoQueElijoOtroEstilo_cuandoVeoElResultado_entoncesCambiaLaIluminacionRecomendada()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    Recomendacion recomendacion = new Recomendacion(
      Estilo.Industrial,
      new ArrayList<>(),
      Iluminacion.FRIA
    );
    when(this.servicioRediseño.recomendar(Estilo.Industrial)).thenReturn(recomendacion);
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(Estilo.Industrial, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(Estilo.Industrial, PRESUPUESTO);

    Recomendacion recomendacionObtenida = (Recomendacion) modelAndView
      .getModel()
      .get("recomendacion");

    assertThat(recomendacionObtenida.getEstilo(), is(Estilo.Industrial));
    assertThat(recomendacionObtenida.getIluminacion(), is(Iluminacion.FRIA));
  }

  @Test
  public void dadoQueEnvioElFormulario_cuandoLoHago_entoncesElModeloTraeLaRecomendacionDePintura()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    RecomendacionPintura recomendacionPintura = new RecomendacionPintura();
    RecomendacionPintura.ItemPintura item = new RecomendacionPintura.ItemPintura();
    item.setZona("Pared de fondo");
    BaldePintura balde = new BaldePintura(
      "Blanco Roto",
      "Blanco roto",
      "#F2F0EB",
      null,
      10.0,
      5000.0,
      null
    );
    item.setBaldePintura(balde);
    item.setCantidadBaldes(5);
    item.setPrecioTotal(25000.0);
    recomendacionPintura.agregarItem(item);
    when(
      this.servicioRecomendacionPintura.recomendar(any(Ambiente.class), any(Estilo.class), isNull())
    )
      .thenReturn(recomendacionPintura);
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(modelAndView.getModel().get("recomendacionPintura"), is(recomendacionPintura));
    RecomendacionPintura obtenida = (RecomendacionPintura) modelAndView
      .getModel()
      .get("recomendacionPintura");
    assertThat(obtenida.getItems(), hasSize(1));
    assertThat(obtenida.getItems().get(0).getBaldePintura(), is(notNullValue()));
    assertThat(obtenida.getItems().get(0).getCantidadBaldes(), is(5));
    assertThat(obtenida.getItems().get(0).getPrecioTotal(), is(25000.0));
    verify(this.servicioRecomendacionPintura)
      .recomendar(any(Ambiente.class), any(Estilo.class), isNull());
  }

  @Test
  public void dadoQueSeleccionoUnColor_cuandoActualizoElColor_entoncesSeAplicaElOverrideYSeResalta()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);
    when(this.servicioRediseño.recomendar(ESTILO)).thenReturn(this.unaRecomendacion(ESTILO));
    RecomendacionPintura recomendacionPintura = new RecomendacionPintura();
    when(
      this.servicioRecomendacionPintura.recomendar(any(Ambiente.class), any(Estilo.class), eq(1))
    )
      .thenReturn(recomendacionPintura);

    ModelAndView modelAndView =
      this.controladorRediseno.actualizarColor(
          AMBIENTE_ID,
          1,
          ESTILO.name(),
          String.valueOf(PRESUPUESTO),
          null
        );

    assertThat(modelAndView.getViewName(), is(RESULTADOS));
    assertThat(modelAndView.getModel().get("colorSeleccionadoIndex"), is(1));
    assertThat(modelAndView.getModel().get("recomendacionPintura"), is(recomendacionPintura));
    verify(this.servicioRecomendacionPintura, times(2))
      .recomendar(any(Ambiente.class), any(Estilo.class), eq(1));
  }

  @Test
  public void dadoQueSeleccionoUnColor_cuandoActualizoLosLitros_entoncesSePreservaElColorSeleccionado()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(ESTILO, PRESUPUESTO, muebles);
    when(this.servicioRediseño.recomendar(ESTILO)).thenReturn(this.unaRecomendacion(ESTILO));
    RecomendacionPintura recomendacionPintura = new RecomendacionPintura();
    when(
      this.servicioRecomendacionPintura.recomendar(any(Ambiente.class), any(Estilo.class), eq(2))
    )
      .thenReturn(recomendacionPintura);

    ModelAndView modelAndView =
      this.controladorRediseno.actualizarLitros(
          AMBIENTE_ID,
          0,
          5.0,
          2,
          ESTILO.name(),
          String.valueOf(PRESUPUESTO),
          null
        );

    assertThat(modelAndView.getViewName(), is(RESULTADOS));
    assertThat(modelAndView.getModel().get("colorSeleccionadoIndex"), is(2));
    assertThat(modelAndView.getModel().get("recomendacionPintura"), is(recomendacionPintura));
    verify(this.servicioRecomendacionPintura)
      .recomendar(any(Ambiente.class), any(Estilo.class), eq(2));
  }

  @Test
  public void dadoQueElCatalogoRechazaElPresupuesto_cuandoVeoElResultado_entoncesVeoElErrorYNoLosMuebles()
    throws Exception {
    this.dadoQueElAmbienteNoTieneTipo();
    when(this.servicioRediseño.recomendar(ESTILO)).thenReturn(this.unaRecomendacion(ESTILO));
    when(this.servicioCatalogo.ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, false, null))
      .thenThrow(new PresupuestoNegativoException("El presupuesto no puede ser negativo"));

    ModelAndView modelAndView = this.cuandoEnvioElFormulario(ESTILO, PRESUPUESTO);

    assertThat(modelAndView.getViewName(), is(RESULTADOS));
    assertThat(modelAndView.getModel().get("error"), is("El presupuesto no puede ser negativo"));
    assertThat(modelAndView.getModel().get("muebles"), is(nullValue()));
  }

  @Test
  public void dadoQueElAmbienteTieneUnTipo_cuandoEnvioElFormulario_entoncesElTipoNoLimitaLosMuebles()
    throws Exception {
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    this.dadoQueExisteElAmbiente(living);
    List<Mueble> muebles = new ArrayList<>();
    when(this.servicioCatalogo.ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, false, null))
      .thenReturn(muebles);

    ModelAndView modelAndView =
      this.controladorRediseno.generarPropuesta(
          AMBIENTE_ID,
          ESTILO.name(),
          String.valueOf(PRESUPUESTO),
          null
        );

    verify(this.servicioCatalogo).ObtenerMueblesQueCumplan(null, ESTILO, PRESUPUESTO, false, null);
    assertThat(modelAndView.getViewName(), is(RESULTADOS));
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_conUnEstiloQueNoExiste_entoncesVuelvoAlFormulario()
    throws Exception {
    ModelAndView modelAndView = this.cuandoEnvioElFormularioConTexto("NoExiste", "60000");

    this.entoncesVuelvoAlFormulario(modelAndView);
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_sinPresupuesto_entoncesVuelvoAlFormulario()
    throws Exception {
    ModelAndView modelAndView = this.cuandoEnvioElFormularioConTexto("Japandi", null);

    this.entoncesVuelvoAlFormulario(modelAndView);
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_conUnPresupuestoNegativo_entoncesVuelvoAlFormulario()
    throws Exception {
    ModelAndView modelAndView = this.cuandoEnvioElFormularioConTexto("Japandi", "-1");

    this.entoncesVuelvoAlFormulario(modelAndView);
  }

  @Test
  public void dadoQueEnvioElFormularioDeRediseno_sinParametros_entoncesVuelvoAlFormulario()
    throws Exception {
    ModelAndView modelAndView = this.cuandoEnvioElFormularioConTexto(null, null);

    this.entoncesVuelvoAlFormulario(modelAndView);
  }

  private void entoncesVuelvoAlFormulario(ModelAndView modelAndView) {
    assertThat(modelAndView.getViewName(), is(FORMULARIO));
    verifyNoInteractions(
      this.servicioCatalogo,
      this.servicioRediseño,
      this.servicioRecomendacionPintura
    );
  }

  private ModelAndView cuandoEnvioElFormularioConTexto(String estilo, String presupuesto) {
    try {
      return this.controladorRediseno.generarPropuesta(AMBIENTE_ID, estilo, presupuesto, null);
    } catch (FiltroDeMueblesInvalidoException e) {
      return new ModelAndView("redirect:/ambientes/1/redisenar");
    }
  }

  private ModelAndView cuandoEnvioElFormulario(Estilo estilo, Double presupuesto) {
    return this.cuandoEnvioElFormularioConTexto(estilo.name(), String.valueOf(presupuesto));
  }

  private void dadoQueElAmbienteNoTieneTipo() {
    this.dadoQueExisteElAmbiente(null);
  }

  private void dadoQueExisteElAmbiente(TipoDeAmbiente tipo) {
    when(this.servicioAmbiente.buscarAmbiente(AMBIENTE_ID))
      .thenReturn(new Ambiente(4.0, 3.0, tipo));
  }

  private void dadoQueElCatalogoCumpleCon(Estilo estilo, Double presupuesto, List<Mueble> muebles)
    throws Exception {
    when(this.servicioCatalogo.ObtenerMueblesQueCumplan(null, estilo, presupuesto, false, null))
      .thenReturn(muebles);
  }

  private Recomendacion unaRecomendacion(Estilo estilo) {
    return new Recomendacion(estilo, new ArrayList<>(), Iluminacion.CALIDA);
  }

  private static Mueble unMueble(String nombre, double precio, Estilo estilo) {
    Mueble mueble = new Mueble();
    mueble.setNombre(nombre);
    mueble.setPrecio(precio);
    mueble.setEstilo(estilo);
    return mueble;
  }

  @SuppressWarnings("unchecked")
  private List<Mueble> obtenerMueblesDelModel(ModelAndView modelAndView) {
    return (List<Mueble>) modelAndView.getModel().get("muebles");
  }

  private DatosPropuesta obtenerPropuestaDelModel(ModelAndView modelAndView) {
    return (DatosPropuesta) modelAndView.getModel().get("propuesta");
  }
}
