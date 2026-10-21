package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioCatalogo;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.presentacion.ControladorCatalogoDeMuebles;
import com.tallerwebi.presentacion.DTONuevoMuebleCatalogo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorCatalogoDeMueblesTest {

  private static final String CATALOGO = "catalogo";
  private static final String CREAR_MUEBLE = "crearMueble";
  private static final String EDITAR_MUEBLE = "editarMueble";
  private static final String REDIRECCION_AL_CATALOGO = "redirect:/catalogo";
  private static final String ROL_ADMIN = "ADMIN";
  private static final String ROL_USUARIO_COMUN = "USER";

  private ServicioCatalogo servicioCatalogo;
  private ServicioCategoriaDeMueble servicioCategoriaDeMueble;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ControladorCatalogoDeMuebles controladorCatalogoDeMuebles;

  @BeforeEach
  public void init() {
    this.servicioCatalogo = mock(ServicioCatalogo.class);
    this.servicioCategoriaDeMueble = mock(ServicioCategoriaDeMueble.class);
    this.requestMock = mock(HttpServletRequest.class);
    this.sessionMock = mock(HttpSession.class);
    this.controladorCatalogoDeMuebles =
      new ControladorCatalogoDeMuebles(this.servicioCatalogo, this.servicioCategoriaDeMueble);
  }

  @Test
  public void dadoQueNoAplicoFiltros_cuandoNavegoAlCatalogo_entoncesVeoTodosLosMueblesDelCatalogo()
    throws Exception {
    Mueble retro = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    Mueble japandi = unMueble("Mesa Japandi", 150000.0, Estilo.Japandi);
    this.dadoQueElCatalogoCumpleCon(null, null, Arrays.asList(retro, japandi));

    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, null);

    assertThat(modelAndView.getViewName(), is(CATALOGO));
    assertThat(this.obtenerMueblesDelModel(modelAndView), containsInAnyOrder(retro, japandi));
  }

  @Test
  public void dadoQueNavegoAlCatalogo_cuandoLoHago_entoncesReciboLosEstilosParaPoderFiltrar()
    throws Exception {
    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, null);

    assertThat(modelAndView.getModel().get("estilos"), is(Estilo.values()));
  }

  @Test
  public void dadoQueNoAplicoFiltros_cuandoNavegoAlCatalogo_entoncesElModeloNoTraeFiltroDeEstiloNiDePrecio()
    throws Exception {
    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, null);

    assertThat(modelAndView.getModel().get("estiloSeleccionado"), is(nullValue()));
    assertThat(modelAndView.getModel().get("precioMaximo"), is(nullValue()));
    assertThat(modelAndView.getModel().get("precioMinimo"), is(nullValue()));
    assertThat(modelAndView.getModel().get("tipoDeAmbiente"), is(nullValue()));
    assertThat(modelAndView.getModel().get("orden"), is(nullValue()));
  }

  @Test
  public void dadoQueFiltroPorEstilo_cuandoNavegoAlCatalogo_entoncesLePidoAlCatalogoSoloEseEstilo()
    throws Exception {
    Mueble retro = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    this.dadoQueElCatalogoCumpleCon(Estilo.Retro, null, Arrays.asList(retro));

    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo("Retro", null);

    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(retro));
    assertThat(modelAndView.getModel().get("estiloSeleccionado"), is(Estilo.Retro));
    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(null, null, Estilo.Retro, null, null);
  }

  @Test
  public void dadoQueFiltroPorPrecioMaximo_cuandoNavegoAlCatalogo_entoncesLePidoSoloLosMueblesQueNoLoSuperan()
    throws Exception {
    Mueble barato = unMueble("Lámpara de Pie Retro", 25000.0, Estilo.Retro);
    ArrayList<Mueble> muebles = new ArrayList<>();
    muebles.add(barato);
    this.dadoQueElCatalogoCumpleCon(null, 50000.0, muebles);

    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, "50000");

    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(barato));
    assertThat(modelAndView.getModel().get("precioMaximo"), is(50000.0));
    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(null, 50000.0, null, null, null);
  }

  @Test
  public void dadoQueFiltroPorEstiloYPrecioMaximo_cuandoNavegoAlCatalogo_entoncesLePidoLosQueCumplenAmbos()
    throws Exception {
    ArrayList<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(Estilo.Retro, 50000.0, muebles);

    this.cuandoNavegoAlCatalogo("Retro", "50000");

    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(null, 50000.0, Estilo.Retro, null, null);
  }

  @Test
  public void dadoQueElijoTodosLosEstilos_cuandoFiltroPorElCatalogo_entoncesNoFiltroPorEstilo()
    throws Exception {
    ArrayList<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(null, 50000.0, muebles);

    this.cuandoNavegoAlCatalogo("", "50000");

    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(null, 50000.0, null, null, null);
  }

  @Test
  public void dadoQueElCatalogoEstaVacio_cuandoNavegoAlCatalogo_entoncesVeoLaVistaSinMuebles()
    throws Exception {
    List<Mueble> muebles = new ArrayList<>();
    this.dadoQueElCatalogoCumpleCon(null, null, muebles);

    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, null);

    assertThat(this.obtenerMueblesDelModel(modelAndView), empty());
  }

  @Test
  public void dadoQueElCatalogoRechazaElPrecioMaximo_cuandoNavegoAlCatalogo_entoncesVeoElErrorYNoLosMuebles()
    throws Exception {
    when(this.servicioCatalogo.ObtenerMueblesFiltrados(null, 50000.0, Estilo.Retro, null, null))
      .thenThrow(new PresupuestoNegativoException("El presupuesto no puede ser negativo"));

    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo("Retro", "50000");

    assertThat(modelAndView.getViewName(), is(CATALOGO));
    assertThat(modelAndView.getModel().get("error"), is("El presupuesto no puede ser negativo"));
    assertThat(modelAndView.getModel().get("muebles"), is(nullValue()));
  }

  @Test
  public void dadoQueNavegoAlCatalogo_conUnEstiloQueNoExiste_entoncesVuelvoAlCatalogoSinFiltros()
    throws Exception {
    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo("NoExiste", null);

    this.entoncesVuelvoAlCatalogoSinConsultarElServicio(modelAndView);
  }

  @Test
  public void dadoQueNavegoAlCatalogo_conUnPrecioMaximoNegativo_entoncesVuelvoAlCatalogoSinFiltros()
    throws Exception {
    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, "-1");

    this.entoncesVuelvoAlCatalogoSinConsultarElServicio(modelAndView);
  }

  @Test
  public void dadoQueNavegoAlCatalogo_conUnPrecioMaximoQueNoEsNumero_entoncesVuelvoAlCatalogoSinFiltros()
    throws Exception {
    ModelAndView modelAndView = this.cuandoNavegoAlCatalogo(null, "caro");

    this.entoncesVuelvoAlCatalogoSinConsultarElServicio(modelAndView);
  }

  @Test
  public void dadoQueFiltroPorPrecioMinimo_cuandoNavegoAlCatalogo_entoncesLePidoSoloLosQueLoSuperan()
    throws Exception {
    Mueble caro = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    ArrayList<Mueble> muebles = new ArrayList<>();
    muebles.add(caro);
    when(this.servicioCatalogo.ObtenerMueblesFiltrados(50000.0, null, null, null, null))
      .thenReturn(muebles);

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.catalogo(null, null, "50000", null, null);

    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(caro));
    assertThat(modelAndView.getModel().get("precioMinimo"), is(50000.0));
    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(50000.0, null, null, null, null);
  }

  @Test
  public void dadoQueFiltroPorTipoDeAmbiente_cuandoNavegoAlCatalogo_entoncesLePidoSoloLosDeEseTipo()
    throws Exception {
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    Mueble paraLiving = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    ArrayList<Mueble> muebles = new ArrayList<>();
    muebles.add(paraLiving);
    when(this.servicioCatalogo.ObtenerMueblesFiltrados(null, null, null, living, null))
      .thenReturn(muebles);

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.catalogo(null, null, null, "Living", null);

    verify(this.servicioCatalogo).ObtenerMueblesFiltrados(null, null, null, living, null);
    assertThat(modelAndView.getModel().get("tipoDeAmbiente"), is("Living"));
    assertThat(this.obtenerMueblesDelModel(modelAndView), contains(paraLiving));
  }

  @Test
  public void dadoQueNavegoAMuebles_cuandoLoHago_entoncesVeoLaVistaDeMuebles() throws Exception {
    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.muebles();
    assertThat(modelAndView.getViewName(), is("muebles"));
  }

  @Test
  public void dadoQueSoyAdmin_cuandoNavegoACrearMueble_entoncesVeoElFormularioDeCreacion()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.crearMueble(this.requestMock);

    assertThat(modelAndView.getViewName(), is(CREAR_MUEBLE));
    assertThat(modelAndView.getModel().get("estilos"), is(Estilo.values()));
    assertThat(
      modelAndView.getModel().get("tiposDeAmbiente"),
      is(TipoDeAmbiente.nombresConocidos())
    );
  }

  @Test
  public void dadoQueSoyAdmin_cuandoNavegoACrearMueble_entoncesVeoLasCategoriasParaElegir()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    List<CategoriaDeMueble> categorias = new ArrayList<>();
    categorias.add(new CategoriaDeMueble("Sillón", 0.80, 0.85));
    when(this.servicioCategoriaDeMueble.obtenerCategorias()).thenReturn(categorias);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.crearMueble(this.requestMock);

    assertThat(modelAndView.getModel().get("categorias"), is(categorias));
  }

  @Test
  public void dadoQueNoInicieSesion_cuandoNavegoACrearMueble_entoncesVuelvoAlCatalogo()
    throws Exception {
    when(this.requestMock.getSession()).thenReturn(this.sessionMock);
    when(this.sessionMock.getAttribute("ROL")).thenReturn(null);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.crearMueble(this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verifyNoInteractions(this.servicioCatalogo, this.servicioCategoriaDeMueble);
  }

  @Test
  public void dadoQueSoyUnUsuarioComun_cuandoNavegoACrearMueble_entoncesVuelvoAlCatalogo()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_USUARIO_COMUN);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.crearMueble(this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verifyNoInteractions(this.servicioCatalogo, this.servicioCategoriaDeMueble);
  }

  @Test
  public void dadoUnFormularioValido_cuandoCreoUnMuebleDelCatalogo_entoncesSeRegistraEnElCatalogo()
    throws Exception {
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 0.80, 0.85);
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    TipoDeAmbiente comedor = new TipoDeAmbiente("Comedor");
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    when(this.servicioCategoriaDeMueble.buscarPorId(1L)).thenReturn(sillones);
    when(this.servicioCatalogo.buscarTipoDeAmbientePorNombre("Living")).thenReturn(living);
    when(this.servicioCatalogo.buscarTipoDeAmbientePorNombre("Comedor")).thenReturn(comedor);
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.crearMuebleDelCatalogo(datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    Mueble muebleGuardado = this.cuandoCapturoElMuebleRegistrado();
    assertThat(muebleGuardado.getNombre(), is("Sillón Retró"));
    assertThat(muebleGuardado.getDescripcion(), is("Sillón de voluteadas."));
    assertThat(muebleGuardado.getPrecio(), is(85000.0));
    assertThat(muebleGuardado.getEstilo(), is(Estilo.Retro));
    assertThat(muebleGuardado.getCategoriaDeMueble(), is(sillones));
    assertThat(muebleGuardado.getAncho(), is(0.80));
    assertThat(muebleGuardado.getLargo(), is(0.85));
    assertThat(muebleGuardado.getTiposDeAmbiente(), containsInAnyOrder(living, comedor));
  }

  @Test
  public void dadoUnFormularioConDatosInvalidos_cuandoCreoUnMuebleDelCatalogo_entoncesVeoElError()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    doThrow(new ValidacionException("El precio del mueble debe ser mayor a cero"))
      .when(this.servicioCatalogo)
      .RegistrarMuebleDelCatalogo(any(Mueble.class));
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.crearMuebleDelCatalogo(datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(CREAR_MUEBLE));
    assertThat(
      modelAndView.getModel().get("error"),
      is("El precio del mueble debe ser mayor a cero")
    );
    verify(this.servicioCatalogo).RegistrarMuebleDelCatalogo(any(Mueble.class));
  }

  @Test
  public void dadoQueNoInicieSesion_cuandoCreoUnMuebleDelCatalogo_entoncesNoSeRegistraNada()
    throws Exception {
    when(this.requestMock.getSession()).thenReturn(this.sessionMock);
    when(this.sessionMock.getAttribute("ROL")).thenReturn(null);
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.crearMuebleDelCatalogo(datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verify(this.servicioCatalogo, never()).RegistrarMuebleDelCatalogo(any(Mueble.class));
  }

  private void dadoQueLaSesionTieneElRol(String rol) {
    when(this.requestMock.getSession()).thenReturn(this.sessionMock);
    when(this.sessionMock.getAttribute("ROL")).thenReturn(rol);
  }

  private DTONuevoMuebleCatalogo dadoQueCompleteElFormularioDeMueble() {
    DTONuevoMuebleCatalogo datos = new DTONuevoMuebleCatalogo();
    datos.setNombre("Sillón Retró");
    datos.setDescripcion("Sillón de voluteadas.");
    datos.setPrecio(85000.0);
    datos.setEstilo("Retro");
    datos.setCategoria(1L);
    datos.setAncho(0.80);
    datos.setLargo(0.85);
    List<String> tiposDeAmbiente = new ArrayList<>();
    tiposDeAmbiente.add("Living");
    tiposDeAmbiente.add("Comedor");
    datos.setTiposDeAmbiente(tiposDeAmbiente);
    return datos;
  }

  private Mueble cuandoCapturoElMuebleRegistrado() {
    ArgumentCaptor<Mueble> captor = ArgumentCaptor.forClass(Mueble.class);
    verify(this.servicioCatalogo).RegistrarMuebleDelCatalogo(captor.capture());
    return captor.getValue();
  }

  @Test
  public void dadoQueNavegoAlDetalleDeUnMueble_cuandoLoHago_entoncesVeoLosDatosDeEseMueble()
    throws Exception {
    Mueble retro = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    retro.setId(7);
    retro.setDescripcion("Sillón de voluteadas tapizado en terciopelo.");
    when(this.servicioCatalogo.ObtenerMueblePorId(7)).thenReturn(retro);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.detalleMueble(7);

    assertThat(modelAndView.getViewName(), is("mueble"));
    assertThat(modelAndView.getModel().get("mueble"), is(retro));
  }

  @Test
  public void dadoQueNavegoAlDetalleDeUnMuebleQueNoExiste_cuandoLoHago_entoncesVuelvoAlCatalogo()
    throws Exception {
    when(this.servicioCatalogo.ObtenerMueblePorId(999))
      .thenThrow(new MuebleNoEncontrado("No existe el mueble 999"));

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.detalleMueble(999);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
  }

  private void entoncesVuelvoAlCatalogoSinConsultarElServicio(ModelAndView modelAndView)
    throws Exception {
    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verifyNoInteractions(this.servicioCatalogo);
  }

  @Test
  public void dadoQueSoyAdmin_cuandoNavegoAEditarUnMueble_entoncesVeoElFormularioConSusDatos()
    throws Exception {
    Mueble retro = unMueble("Sillón Retró", 85000.0, Estilo.Retro);
    retro.setId(7);
    retro.setAncho(0.80);
    retro.setLargo(0.85);
    retro.setTiposDeAmbiente(Arrays.asList(new TipoDeAmbiente("Living")));
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    when(this.servicioCatalogo.ObtenerMueblePorId(7)).thenReturn(retro);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.editarMueble(7, this.requestMock);

    assertThat(modelAndView.getViewName(), is(EDITAR_MUEBLE));
    assertThat(modelAndView.getModel().get("muebleAEliminar"), is(retro));
  }

  @Test
  public void dadoQueNoSoyAdmin_cuandoNavegoAEditarUnMueble_entoncesVuelvoAlCatalogo()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_USUARIO_COMUN);

    ModelAndView modelAndView = this.controladorCatalogoDeMuebles.editarMueble(7, this.requestMock);

    this.entoncesVuelvoAlCatalogoSinConsultarElServicio(modelAndView);
  }

  @Test
  public void dadoUnFormularioValido_cuandoEditoUnMuebleDelCatalogo_entoncesSeGuarda()
    throws Exception {
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 0.80, 0.85);
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    when(this.servicioCategoriaDeMueble.buscarPorId(1L)).thenReturn(sillones);
    when(this.servicioCatalogo.buscarTipoDeAmbientePorNombre("Living")).thenReturn(living);
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.modificarMuebleDelCatalogo(7, datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    ArgumentCaptor<Mueble> captor = ArgumentCaptor.forClass(Mueble.class);
    verify(this.servicioCatalogo).modificarMuebleDelCatalogo(eq(7), captor.capture());
    assertThat(captor.getValue().getNombre(), is("Sillón Retró"));
    assertThat(captor.getValue().getAncho(), is(0.80));
  }

  @Test
  public void dadoUnFormularioConDatosInvalidos_cuandoEditoUnMuebleDelCatalogo_entoncesVeoElError()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    doThrow(new ValidacionException("El precio del mueble debe ser mayor a cero"))
      .when(this.servicioCatalogo)
      .modificarMuebleDelCatalogo(eq(7), any(Mueble.class));
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.modificarMuebleDelCatalogo(7, datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(EDITAR_MUEBLE));
    assertThat(modelAndView.getModel().get("idMueble"), is(7));
    assertThat(
      modelAndView.getModel().get("error"),
      is("El precio del mueble debe ser mayor a cero")
    );
  }

  @Test
  public void dadoUnMuebleInexistente_cuandoLoEdito_entoncesVuelvoAlCatalogo() throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);
    doThrow(new MuebleNoEncontrado("No existe el mueble 7"))
      .when(this.servicioCatalogo)
      .modificarMuebleDelCatalogo(eq(7), any(Mueble.class));
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.modificarMuebleDelCatalogo(7, datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
  }

  @Test
  public void dadoQueNoSoyAdmin_cuandoEditoUnMuebleDelCatalogo_entoncesNoSeGuardaNada()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_USUARIO_COMUN);
    DTONuevoMuebleCatalogo datos = this.dadoQueCompleteElFormularioDeMueble();

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.modificarMuebleDelCatalogo(7, datos, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verify(this.servicioCatalogo, never()).modificarMuebleDelCatalogo(eq(7), any(Mueble.class));
  }

  @Test
  public void dadoQueSoyAdmin_cuandoEliminoUnMuebleDelCatalogo_entoncesSeElimina()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_ADMIN);

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.eliminarMuebleDelCatalogo(7, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verify(this.servicioCatalogo).eliminarMuebleDelCatalogo(7);
  }

  @Test
  public void dadoQueNoSoyAdmin_cuandoEliminoUnMuebleDelCatalogo_entoncesNoSeEliminaNada()
    throws Exception {
    this.dadoQueLaSesionTieneElRol(ROL_USUARIO_COMUN);

    ModelAndView modelAndView =
      this.controladorCatalogoDeMuebles.eliminarMuebleDelCatalogo(7, this.requestMock);

    assertThat(modelAndView.getViewName(), is(REDIRECCION_AL_CATALOGO));
    verify(this.servicioCatalogo, never()).eliminarMuebleDelCatalogo(7);
  }

  private ModelAndView cuandoNavegoAlCatalogo(String estilo, String precioMaximo) {
    return this.controladorCatalogoDeMuebles.catalogo(estilo, precioMaximo, null, null, null);
  }

  private void dadoQueElCatalogoCumpleCon(Estilo estilo, Double precioMaximo, List<Mueble> muebles)
    throws Exception {
    when(this.servicioCatalogo.ObtenerMueblesFiltrados(null, precioMaximo, estilo, null, null))
      .thenReturn(muebles);
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
}
