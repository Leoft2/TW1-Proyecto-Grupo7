package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.LadoDeLaHoja;
import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.ReglaDeApertura;
import com.tallerwebi.dominio.ServicioReglasDeApertura;
import com.tallerwebi.dominio.TipoAbertura;
import com.tallerwebi.dominio.ZonaDeBarrido;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

// -------------------------------------------------------------------------
// Testing de HU-04:Aberturas y HU-34.2:Advertencia de puerta sin pared libre
// -------------------------------------------------------------------------

public class ControladorPlanoAberturasTest {

  private ControladorPlanoAberturas controladorPlanoAberturas;
  private ServicioReglasDeApertura servicioReglasDeApertura;
  private HttpSession sesion;

  @BeforeEach
  public void setUp() {
    servicioReglasDeApertura = mock(ServicioReglasDeApertura.class);
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(true));
    controladorPlanoAberturas = new ControladorPlanoAberturas(servicioReglasDeApertura);
    sesion = mock(HttpSession.class);
  }

  @Test
  public void deberiaMostrarLaVistaDeAberturasConDatosDelPasoAnterior() {
    DatosPlano datosPlano = givenDatosPlanoEnSesion();

    ModelAndView mav = whenVoyAConfigurarAberturas(sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
    thenElModeloContieneClave(mav, "datosPlano");
    thenElModeloContieneClave(mav, "tiposAbertura");
    thenElModeloContieneClave(mav, "muros");
  }

  @Test
  public void deberiaGuardarLasAberturasEnSesionAlEnviarElFormulario() {
    DatosPlano datosPlano = givenDatosPlanoConAberturas();

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
    verify(sesion).setAttribute(eq("datosPlano"), eq(datosPlano));
  }

  @Test
  public void deberiaRedirigirAObstaculosAlConfirmarAberturas() {
    DatosPlano datosPlano = givenDatosPlanoConAberturas();

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "redirect:/plano/obstaculos");
    verify(sesion).setAttribute(eq("datosPlano"), eq(datosPlano));
  }

  @Test
  public void deberiaNoModificarLosDatosDeLaSesionCuandoAbroElPasoDeAberturas() {
    DatosPlano datosPlano = givenDatosPlanoEnSesion();

    whenVoyAConfigurarAberturas(sesion);

    thenLasAberturasSiguenVacias(datosPlano);
  }

  @Test
  public void deberiaVolverAlPasoDeAberturasCuandoUnaAberturaEstaIncompleta() {
    DatosPlano datosPlano = givenDatosPlanoConAberturaIncompleta();

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
  }

  // Una puerta sin pared libre debe agregar un mensaje de advertencia al modelo.
  @Test
  public void deberiaAgregarLaAdvertenciaAlModeloCuandoUnaPuertaNoTieneParedLibre() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenElModeloContieneLaAdvertencia(mav, givenMensajeDeAdvertencia(Muro.SUPERIOR, 0.4));
  }

  // Si el servicio indica que hay pared libre, la lista de advertencias queda vacía.
  @Test
  public void deberiaNoAgregarAdvertenciasCuandoLaPuertaTieneParedLibre() {
    DatosPlano datosPlano = givenDatosPlanoConAberturas();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(true));

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenElModeloNoTieneAdvertencias(mav);
  }

  // Sin el flag de confirmación, la advertencia no bloquea: se vuelve a la vista.
  @Test
  public void deberiaVolverAlPasoDeAberturasCuandoHayUnaAdvertenciaSinConfirmar() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    ModelAndView mav = whenConfirmoAberturas(datosPlano, sesion);

    thenLaVistaEs(mav, "plano-aberturas-config");
  }

  // "Continuar igual" envía confirmarAdvertencias=true y deja avanzar al siguiente paso.
  @Test
  public void deberiaRedirigirAObstaculosCuandoElUsuarioConfirmaLaAdvertencia() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    ModelAndView mav = whenConfirmoAberturasConAdvertenciasConfirmadas(datosPlano, sesion);

    thenLaVistaEs(mav, "redirect:/plano/obstaculos");
  }

  // Aun con advertencia, al confirmar se guardan los datos para el paso de obstáculos.
  @Test
  public void deberiaGuardarLosDatosEnSesionCuandoElUsuarioConfirmaLaAdvertencia() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    whenConfirmoAberturasConAdvertenciasConfirmadas(datosPlano, sesion);

    thenSeGuardoEnSesion(datosPlano);
  }

  // Al reenviar el formulario también se recalculan las advertencias.
  @Test
  public void deberiaAgregarLaAdvertenciaAlGuardarAberturasCuandoUnaPuertaNoTieneParedLibre() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenElModeloContieneLaAdvertencia(mav, givenMensajeDeAdvertencia(Muro.SUPERIOR, 0.4));
  }

  // Si se corrige la posición, el segundo envío ya no debe mostrar advertencias.
  @Test
  public void deberiaQuitarLaAdvertenciaCuandoLaPuertaSeReubicaConParedLibre() {
    DatosPlano sinParedLibre = givenDatosPlanoConPuertaSinParedLibre();
    DatosPlano conParedLibre = givenDatosPlanoConAberturas();
    when(servicioReglasDeApertura.determinarApertura(any(), any()))
      .thenReturn(givenRegla(false), givenRegla(true));

    ModelAndView mav = whenReubicoLaPuertaYGuardoAberturas(sinParedLibre, conParedLibre);

    thenElModeloNoTieneAdvertencias(mav);
  }

  // La regla solo aplica a PUERTA: el servicio no debe consultarse para una VENTANA.
  @Test
  public void deberiaNoAgregarAdvertenciasCuandoLaAberturaEsUnaVentana() {
    DatosPlano datosPlano = givenDatosPlanoConVentana();
    when(servicioReglasDeApertura.determinarApertura(any(), any()))
      .thenThrow(new IllegalArgumentException("No se deben calcular reglas para ventanas"));

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenElModeloNoTieneAdvertencias(mav);
  }

  // Sin ancho o largo no se puede armar el ambiente: se evita calcular y no se advierte.
  @Test
  public void deberiaNoCalcularAdvertenciasCuandoFaltanLasMedidasDelAmbiente() {
    DatosPlano datosPlano = givenDatosPlanoSinAncho();

    ModelAndView mav = whenGuardoAberturas(datosPlano, sesion);

    thenElModeloNoTieneAdvertencias(mav);
  }

  // Al volver a la vista (GET) se recalculan las advertencias desde la sesión.
  @Test
  public void deberiaMantenerLaAdvertenciaAlVolverAlPasoDeAberturas() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibreEnSesion();
    when(servicioReglasDeApertura.determinarApertura(any(), any())).thenReturn(givenRegla(false));

    ModelAndView mav = whenVoyAConfigurarAberturas(sesion);

    thenElModeloContieneLaAdvertencia(
      mav,
      givenMensajeDeAdvertencia(Muro.SUPERIOR, datosPlano.getAberturas().get(0).getPosicion())
    );
  }

  // Given
  // Ambiente 4x5 sin aberturas, ya guardado en la sesión.
  private DatosPlano givenDatosPlanoEnSesion() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    when(sesion.getAttribute("datosPlano")).thenReturn(datosPlano);
    return datosPlano;
  }

  // Ambiente 4x5 con una PUERTA que tiene pared libre a ambos lados.
  private DatosPlano givenDatosPlanoConAberturas() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    datosAbertura.setAncho(0.9);
    datosAbertura.setRadioDeGiro(0.9);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // PUERTA sin ancho: la fila queda incompleta y no debe procesarse.
  private DatosPlano givenDatosPlanoConAberturaIncompleta() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // Ambiente angosto (1.6 m): la puerta no alcanza el radio en ninguno de sus lados.
  private DatosPlano givenDatosPlanoConPuertaSinParedLibre() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(1.6);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(0.4);
    datosAbertura.setAncho(0.9);
    datosAbertura.setRadioDeGiro(0.9);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // La misma puerta sin pared libre, pero ya guardada en la sesión.
  private DatosPlano givenDatosPlanoConPuertaSinParedLibreEnSesion() {
    DatosPlano datosPlano = givenDatosPlanoConPuertaSinParedLibre();
    when(sesion.getAttribute("datosPlano")).thenReturn(datosPlano);
    return datosPlano;
  }

  // Ambiente 4x5 con una VENTANA, que nunca debe generar advertencias.
  private DatosPlano givenDatosPlanoConVentana() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setAncho(4.0);
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.VENTANA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    datosAbertura.setAncho(0.9);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // PUERTA cargada pero sin el ancho del ambiente: faltan medidas para calcular.
  private DatosPlano givenDatosPlanoSinAncho() {
    DatosPlano datosPlano = new DatosPlano();
    datosPlano.setLargo(5.0);
    DatosAbertura datosAbertura = new DatosAbertura();
    datosAbertura.setTipo(TipoAbertura.PUERTA);
    datosAbertura.setMuro(Muro.SUPERIOR);
    datosAbertura.setPosicion(1.0);
    datosAbertura.setAncho(0.9);
    datosAbertura.setRadioDeGiro(0.9);
    List<DatosAbertura> aberturas = new ArrayList<>();
    aberturas.add(datosAbertura);
    datosPlano.setAberturas(aberturas);
    return datosPlano;
  }

  // Simula el resultado que devuelve el servicio de reglas mockeado.
  private ReglaDeApertura givenRegla(boolean hayParedLibre) {
    return new ReglaDeApertura(
      Muro.IZQUIERDO,
      LadoDeLaHoja.INICIO,
      LadoDeLaHoja.FIN,
      new ZonaDeBarrido(0.0, 0.0, 0.9, 0.0, 90.0),
      hayParedLibre
    );
  }

  // Arma el mensaje esperado con el mismo formato que usa el controlador.
  private String givenMensajeDeAdvertencia(Muro muro, Double posicion) {
    return String.format(
      "La puerta del muro %s en la posición %s m no tiene pared libre para abrir; " +
      "la hoja podría chocar con muebles.",
      muro.getNombre(),
      posicion
    );
  }

  // When
  private ModelAndView whenVoyAConfigurarAberturas(HttpSession sesionHttp) {
    return controladorPlanoAberturas.irAConfigurarAberturas(sesionHttp);
  }

  private ModelAndView whenGuardoAberturas(DatosPlano datosPlano, HttpSession sesionHttp) {
    return controladorPlanoAberturas.guardarAberturas(datosPlano, sesionHttp);
  }

  private ModelAndView whenConfirmoAberturas(DatosPlano datosPlano, HttpSession sesionHttp) {
    return controladorPlanoAberturas.confirmarAberturas(datosPlano, sesionHttp, null);
  }

  private ModelAndView whenConfirmoAberturasConAdvertenciasConfirmadas(
    DatosPlano datosPlano,
    HttpSession sesionHttp
  ) {
    return controladorPlanoAberturas.confirmarAberturas(datosPlano, sesionHttp, true);
  }

  private ModelAndView whenReubicoLaPuertaYGuardoAberturas(
    DatosPlano sinParedLibre,
    DatosPlano conParedLibre
  ) {
    whenGuardoAberturas(sinParedLibre, sesion);
    return whenGuardoAberturas(conParedLibre, sesion);
  }

  // Then
  private void thenLaVistaEs(ModelAndView mav, String nombreVista) {
    assertThat(mav.getViewName(), is(nombreVista));
  }

  private void thenElModeloContieneClave(ModelAndView mav, String clave) {
    assertThat(mav.getModel(), hasKey(clave));
  }

  private void thenLasAberturasSiguenVacias(DatosPlano datosPlano) {
    assertThat(datosPlano.getAberturas(), is(empty()));
  }

  @SuppressWarnings("unchecked")
  private void thenElModeloContieneLaAdvertencia(ModelAndView mav, String mensaje) {
    assertThat((List<String>) mav.getModel().get("advertenciasDeApertura"), contains(mensaje));
  }

  private void thenElModeloNoTieneAdvertencias(ModelAndView mav) {
    assertThat((List<?>) mav.getModel().get("advertenciasDeApertura"), is(empty()));
  }

  private void thenSeGuardoEnSesion(DatosPlano datosPlano) {
    verify(sesion).setAttribute("datosPlano", datosPlano);
  }
}
