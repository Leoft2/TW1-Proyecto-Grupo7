package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

/*
Criterios de aceptación:
-Dado un mueble en el inventario, cuando lo edito y guardo, entonces los cambios se reflejan en la tabla y en el plano si ya estaba ubicado.
-Dado un mueble que elimino, cuando confirmo la acción, entonces desaparece del inventario y de cualquier distribución guardada.
*/

public class ControladorInventarioMueblesTest {

  private ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private ServicioPlano2D servicioPlano2D;
  private ServicioCategoriaDeMueble servicioCategoriaDeMueble;
  private ServicioMaterial servicioMaterial;
  private ServicioZonaExclusion servicioZonaExclusion;
  private HttpSession sesion;
  private ControladorInventarioMuebles controladorInventarioMuebles;

  @BeforeEach
  public void init() {
    servicioEdicionInventarioMuebles = mock(ServicioEdicionInventarioMuebles.class);
    servicioPlano2D = mock(ServicioPlano2D.class);
    sesion = mock(HttpSession.class);
    servicioCategoriaDeMueble = mock(ServicioCategoriaDeMueble.class);
    servicioMaterial = mock(ServicioMaterial.class);
    servicioZonaExclusion = mock(ServicioZonaExclusion.class);
    when(servicioCategoriaDeMueble.obtenerCategorias()).thenReturn(new java.util.ArrayList<>());
    when(servicioMaterial.obtenerMateriales()).thenReturn(new java.util.ArrayList<>());
    controladorInventarioMuebles =
      new ControladorInventarioMuebles(
        servicioEdicionInventarioMuebles,
        servicioPlano2D,
        servicioCategoriaDeMueble,
        null,
        servicioMaterial,
        servicioZonaExclusion
      );
  }

  @Test
  public void deberiaCargarElMuebleEnElModelCuandoSolicitoElFormularioDeEdicion()
    throws MuebleNoEncontrado {
    // Given
    Mueble muebleEsperado = new Mueble("Mesa", 1.0, 0.8);
    muebleEsperado.setId(1);
    when(servicioEdicionInventarioMuebles.buscarPorId(1)).thenReturn(muebleEsperado);

    // When
    ModelAndView mav = whenSolicitoElFormularioDeEdicion();

    // Then
    thenElControllerCargaElMuebleEnElModel(muebleEsperado, mav);
  }

  @Test
  public void deberiaDelegarLaActualizacionAlServiceCuandoEditoUnMuebleValido()
    throws MuebleNoEncontrado {
    // Given
    Mueble datosNuevos = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    whenActualizoElMuebleDesdeElController(datosNuevos);

    // Then
    thenElControllerDelegaLaActualizacion(datosNuevos);
  }

  @Test
  public void deberiaEnviarTodosLosDatosAlServiceCuandoRealizoUnaEdicionCompleta()
    throws MuebleNoEncontrado {
    // Given
    Mueble datosCompletos = new Mueble("Sillon", 2.0, 0.9);
    datosCompletos.setDescripcion("Tapizado de lino");
    datosCompletos.setPrecio(1200.0);
    datosCompletos.setEstilo(Estilo.Japandi);

    // When
    whenEnviaLaEdicionCompleta(datosCompletos);

    // Then
    thenElControllerEntregaTodosLosCamposAlService(datosCompletos);
  }

  @Test
  public void deberiaManejarElErrorCuandoIntentoEditarUnMuebleInexistente()
    throws MuebleNoEncontrado {
    // Given
    Mueble datos = new Mueble("Mesa", 1.0, 0.8);
    doThrow(new MuebleNoEncontrado("No se encontro un mueble con el id 999"))
      .when(servicioEdicionInventarioMuebles)
      .actualizar(eq(999), eq(datos));

    // When
    ModelAndView mav = whenIntentoEditarUnMuebleInexistente(datos);

    // Then
    thenElControllerManejaElError(mav);
  }

  @Test
  public void deberiaMostrarLaConfirmacionSinEliminarCuandoSolicitoLaEliminacion()
    throws MuebleNoEncontrado {
    // Given
    Mueble mueble = new Mueble("Mesa", 1.0, 0.8);
    mueble.setId(1);
    when(servicioEdicionInventarioMuebles.buscarPorId(1)).thenReturn(mueble);

    // When
    whenMuestroLaConfirmacionDeEliminacion();

    // Then
    thenNoSeInvocaEliminacionTodavia();
  }

  @Test
  public void deberiaMostrarElPlanoCuandoConfirmoElInventarioDesdeElFormulario() {
    // Given
    givenAmbienteYInventarioListos();

    // When
    ModelAndView mav = whenConfirmoElInventario();

    // Then
    thenLaVistaEsPlanoInteractivo(mav);
  }

  @Test
  public void deberiaDelegarElPlanoAlServicioCuandoConfirmoElInventario() {
    // Given
    givenAmbienteYInventarioListos();

    // When
    whenConfirmoElInventario();

    // Then
    thenElServicioRecibeElAmbienteYLosMuebles();
  }

  private void givenAmbienteYInventarioListos() {
    Mueble silla = new Mueble("Silla", 0.5, 0.5);
    java.util.List<Mueble> muebles = new java.util.ArrayList<>();
    muebles.add(silla);
    when(servicioEdicionInventarioMuebles.listar()).thenReturn(muebles);
    when(
      servicioPlano2D.generarPlano(
        org.mockito.ArgumentMatchers.any(com.tallerwebi.dominio.Ambiente.class),
        org.mockito.ArgumentMatchers.anyList()
      )
    )
      .thenReturn(
        new com.tallerwebi.dominio.Plano(
          new com.tallerwebi.dominio.Ambiente(4.0, 3.0),
          new java.util.ArrayList<>(),
          new java.util.ArrayList<>(),
          new java.util.ArrayList<>()
        )
      );
  }

  private ModelAndView whenConfirmoElInventario() {
    return controladorInventarioMuebles.generarPlano(4.0, 3.0, sesion);
  }

  private void thenLaVistaEsPlanoInteractivo(ModelAndView mav) {
    assertEquals("plano-interactivo", mav.getViewName());
  }

  @Test
  public void deberiaPasarLasZonasDeExclusionAlModeloCuandoGeneroElPlano() {
    // Given
    givenAmbienteYInventarioListos();
    java.util.List<ZonaExclusion> zonas = new java.util.ArrayList<>();
    zonas.add(new ZonaExclusion(1.0, 0.0, 0.9, 0.9, TipoAbertura.PUERTA));
    when(servicioZonaExclusion.calcularZonas(any(Ambiente.class))).thenReturn(zonas);

    // When
    ModelAndView mav = whenConfirmoElInventario();

    // Then
    thenElModeloTieneLasZonasDeExclusion(zonas, mav);
  }

  private void thenElModeloTieneLasZonasDeExclusion(
    java.util.List<ZonaExclusion> zonasEsperadas,
    ModelAndView mav
  ) {
    assertEquals(zonasEsperadas, mav.getModel().get("zonasDeExclusion"));
  }

  @Test
  public void deberiaUsarElAmbienteDeSesionCuandoExiste() {
    // Given
    Ambiente ambienteDeSesion = new Ambiente(5.0, 4.0);
    when(sesion.getAttribute("ambiente")).thenReturn(ambienteDeSesion);
    when(servicioPlano2D.generarPlano(any(Ambiente.class), anyList()))
      .thenReturn(
        new Plano(
          ambienteDeSesion,
          new java.util.ArrayList<>(),
          new java.util.ArrayList<>(),
          new java.util.ArrayList<>()
        )
      );

    // When
    controladorInventarioMuebles.generarPlano(4.0, 3.0, sesion);

    // Then
    thenElServicioRecibeElAmbiente(ambienteDeSesion);
  }

  @Test
  public void deberiaRedirigirAConfigurarElAmbienteCuandoFaltanLasMedidasParaGenerarElPlano() {
    // Given
    when(sesion.getAttribute("ambiente")).thenReturn(null);
    when(servicioEdicionInventarioMuebles.listar()).thenReturn(new java.util.ArrayList<>());
    doThrow(new ValidacionException("El ancho del ambiente debe ser mayor a 0"))
      .when(servicioPlano2D)
      .generarPlano(any(Ambiente.class), anyList());

    // When
    ModelAndView mav = whenGeneroElPlanoSinMedidas();

    // Then
    thenRedirigeAConfigurarElAmbiente(mav);
  }

  private ModelAndView whenGeneroElPlanoSinMedidas() {
    return controladorInventarioMuebles.generarPlano(null, null, sesion);
  }

  private void thenRedirigeAConfigurarElAmbiente(ModelAndView mav) {
    assertEquals("redirect:/plano/ambiente", mav.getViewName());
  }

  private void thenElServicioRecibeElAmbiente(Ambiente ambienteEsperado) {
    verify(servicioPlano2D).generarPlano(eq(ambienteEsperado), anyList());
  }

  private void thenElServicioRecibeElAmbienteYLosMuebles() {
    org.mockito.ArgumentCaptor<com.tallerwebi.dominio.Ambiente> captor =
      org.mockito.ArgumentCaptor.forClass(com.tallerwebi.dominio.Ambiente.class);
    verify(servicioPlano2D).generarPlano(captor.capture(), org.mockito.ArgumentMatchers.anyList());
    assertEquals(4.0, captor.getValue().getAncho());
  }

  @Test
  public void deberiaAsignarLaCategoriaAlMuebleCuandoLaIndicoEnElAlta() {
    // Given
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 1.0, 0.8);
    when(servicioCategoriaDeMueble.buscarPorId(2L)).thenReturn(sillones);
    Mueble muebleNuevo = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    whenAgregoElMuebleConCategoria(muebleNuevo, 2L);

    // Then
    thenElControllerCreaElMuebleConCategoria(sillones);
  }

  private void whenAgregoElMuebleConCategoria(Mueble mueble, Long categoriaId) {
    controladorInventarioMuebles.agregar(mueble, categoriaId, null);
  }

  private void thenElControllerCreaElMuebleConCategoria(CategoriaDeMueble categoria) {
    verify(servicioEdicionInventarioMuebles)
      .crear(argThat(m -> m.getCategoriaDeMueble() == categoria));
  }

  @Test
  public void deberiaAsignarLaCategoriaAlMuebleCuandoLaIndicoEnLaEdicion()
    throws MuebleNoEncontrado {
    // Given
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 1.0, 0.8);
    when(servicioCategoriaDeMueble.buscarPorId(2L)).thenReturn(sillones);
    Mueble datos = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    whenActualizoElMuebleConCategoria(datos, 2L);

    // Then
    thenElControllerActualizaElMuebleConCategoria(sillones);
  }

  private void whenActualizoElMuebleConCategoria(Mueble datos, Long categoriaId)
    throws MuebleNoEncontrado {
    controladorInventarioMuebles.editar(1, datos, null, null, categoriaId, null);
  }

  private void thenElControllerActualizaElMuebleConCategoria(CategoriaDeMueble categoria)
    throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles)
      .actualizar(eq(1), argThat(m -> m.getCategoriaDeMueble() == categoria));
  }

  @Test
  public void deberiaAsignarElMaterialAlMuebleCuandoLoIndicoEnElAlta() {
    // Given
    Material bambu = new Material("Bambú", true);
    when(servicioMaterial.buscarPorId(3L)).thenReturn(bambu);
    Mueble muebleNuevo = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    controladorInventarioMuebles.agregar(muebleNuevo, null, 3L);

    // Then
    verify(servicioEdicionInventarioMuebles).crear(argThat(m -> m.getMaterial() == bambu));
  }

  @Test
  public void deberiaAsignarElMaterialAlMuebleCuandoLoIndicoEnLaEdicion()
    throws MuebleNoEncontrado {
    // Given
    Material bambu = new Material("Bambú", true);
    when(servicioMaterial.buscarPorId(3L)).thenReturn(bambu);
    Mueble datos = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    controladorInventarioMuebles.editar(1, datos, null, null, null, 3L);

    // Then
    verify(servicioEdicionInventarioMuebles)
      .actualizar(eq(1), argThat(m -> m.getMaterial() == bambu));
  }

  @Test
  public void deberiaCargarLosMaterialesEnElModelCuandoSolicitoElInventario() {
    // When
    ModelAndView mav = controladorInventarioMuebles.irAInventario(null, null);

    // Then
    assertEquals(servicioMaterial.obtenerMateriales(), mav.getModel().get("materiales"));
  }

  @Test
  public void deberiaCargarLosMaterialesEnElModelCuandoSolicitoElFormularioDeEdicion()
    throws MuebleNoEncontrado {
    // When
    ModelAndView mav = controladorInventarioMuebles.irAEditar(1, null, null);

    // Then
    assertEquals(servicioMaterial.obtenerMateriales(), mav.getModel().get("materiales"));
  }

  @Test
  public void deberiaCrearElMuebleCuandoLoAgregoDesdeElFormulario() {
    // Given
    Mueble muebleNuevo = new Mueble("Mesa ratona", 1.2, 0.6);

    // When
    whenAgregoElMueble(muebleNuevo);

    // Then
    thenElControllerCreaElMueble(muebleNuevo);
  }

  @Test
  public void deberiaCrearElMuebleCuandoLaDescripcionVieneVacia() {
    // Given: la descripcion es opcional en el form de alta, llega como cadena vacia.
    Mueble muebleNuevo = new Mueble("Mesa ratona", 1.2, 0.6);
    muebleNuevo.setDescripcion("");

    // When
    whenAgregoElMueble(muebleNuevo);

    // Then
    thenElControllerCreaElMueble(muebleNuevo);
  }

  private void whenAgregoElMueble(Mueble mueble) {
    controladorInventarioMuebles.agregar(mueble);
  }

  private void thenElControllerCreaElMueble(Mueble mueble) {
    verify(servicioEdicionInventarioMuebles).crear(mueble);
  }

  @Test
  public void deberiaMostrarLaVistaInventarioCuandoSolicitoLaPagina() {
    // Given
    // Sin datos de entrada: abrir el inventario no necesita ningun mueble cargado.

    // When
    ModelAndView mav = whenSolicitoElInventario();

    // Then
    thenLaVistaEsInventario(mav);
  }

  @Test
  public void deberiaPasarLosMueblesAlModeloCuandoSolicitoElInventario() {
    // Given
    givenInventarioConDosMuebles();

    // When
    ModelAndView mav = whenSolicitoElInventario();

    // Then
    thenElModeloTieneLosDosMuebles(mav);
  }

  private void givenInventarioConDosMuebles() {
    Mueble mesa = new Mueble("Mesa", 1.0, 0.8);
    Mueble silla = new Mueble("Silla", 0.5, 0.5);
    java.util.List<Mueble> dos = new java.util.ArrayList<>();
    dos.add(mesa);
    dos.add(silla);
    when(servicioEdicionInventarioMuebles.listar()).thenReturn(dos);
  }

  private ModelAndView whenSolicitoElInventario() {
    return controladorInventarioMuebles.irAInventario(4.0, 3.0);
  }

  private void thenLaVistaEsInventario(ModelAndView mav) {
    assertEquals("inventario-muebles", mav.getViewName());
  }

  private void thenElModeloTieneLosDosMuebles(ModelAndView mav) {
    assertEquals(2, ((java.util.List<?>) mav.getModel().get("muebles")).size());
  }

  @Test
  public void deberiaEliminarElMuebleCuandoConfirmoLaEliminacion() throws MuebleNoEncontrado {
    // Given
    // El mueble existe en el inventario; el servicio no lanza error al eliminar.

    // When
    whenConfirmoLaEliminacionDesdeElController();

    // Then
    thenElControllerEliminaElMueble();
  }

  @Test
  public void deberiaNoEliminarElMuebleCuandoCanceloLaEliminacion() throws MuebleNoEncontrado {
    // Given
    // El usuario llego al dialogo de confirmacion y decide cancelar.

    // When
    whenCanceloLaEliminacionDesdeElController();

    // Then
    thenElControllerNoEliminaElMueble();
  }

  @Test
  public void deberiaManejarElErrorCuandoIntentoEliminarUnMuebleInexistente()
    throws MuebleNoEncontrado {
    // Given
    doThrow(new MuebleNoEncontrado("No se encontro un mueble con el id 999"))
      .when(servicioEdicionInventarioMuebles)
      .eliminar(eq(999));

    // When
    ModelAndView mav = whenIntentoEliminarUnMuebleInexistente();

    // Then
    thenElControllerManejaElError(mav);
  }

  @Test
  public void deberiaRedirigirAlInventarioCuandoLaEliminacionEsExitosa() throws MuebleNoEncontrado {
    // Given
    // El servicio confirma la eliminacion: no lanza MuebleNoEncontrado.

    // When
    ModelAndView mav = whenEliminoElMuebleDesdeElController();

    // Then
    thenElControllerRedirigeAlInventario(mav);
  }

  private ModelAndView whenIntentoEditarUnMuebleInexistente(Mueble datos)
    throws MuebleNoEncontrado {
    return controladorInventarioMuebles.editar(999, datos);
  }

  private void whenConfirmoLaEliminacionDesdeElController() throws MuebleNoEncontrado {
    controladorInventarioMuebles.eliminar(1);
  }

  private void whenCanceloLaEliminacionDesdeElController() {
    controladorInventarioMuebles.cancelarEliminacion(1);
  }

  private ModelAndView whenIntentoEliminarUnMuebleInexistente() throws MuebleNoEncontrado {
    return controladorInventarioMuebles.eliminar(999);
  }

  private void whenMuestroLaConfirmacionDeEliminacion() throws MuebleNoEncontrado {
    controladorInventarioMuebles.irAConfirmarEliminacion(1, 4.0, 3.0);
  }

  private ModelAndView whenEliminoElMuebleDesdeElController() throws MuebleNoEncontrado {
    return controladorInventarioMuebles.eliminar(1);
  }

  private ModelAndView whenSolicitoElFormularioDeEdicion() throws MuebleNoEncontrado {
    return controladorInventarioMuebles.irAEditar(1, 4.0, 3.0);
  }

  private void whenActualizoElMuebleDesdeElController(Mueble datos) throws MuebleNoEncontrado {
    controladorInventarioMuebles.editar(1, datos);
  }

  private void whenEnviaLaEdicionCompleta(Mueble datos) throws MuebleNoEncontrado {
    controladorInventarioMuebles.editar(1, datos);
  }

  private void thenElControllerEliminaElMueble() throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles, times(1)).eliminar(1);
  }

  private void thenElControllerNoEliminaElMueble() throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles, never()).eliminar(eq(1));
  }

  private void thenNoSeInvocaEliminacionTodavia() throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles, never()).eliminar(eq(1));
  }

  private void thenElControllerRedirigeAlInventario(ModelAndView mav) {
    assertEquals("redirect:/inventario-muebles", mav.getViewName());
  }

  private void thenElControllerCargaElMuebleEnElModel(Mueble muebleEsperado, ModelAndView mav) {
    assertEquals(muebleEsperado, mav.getModel().get("mueble"));
  }

  private void thenElControllerDelegaLaActualizacion(Mueble datos) throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles).actualizar(1, datos);
  }

  private void thenElControllerManejaElError(ModelAndView mav) {
    assertEquals("No se encontro un mueble con el id 999", mav.getModel().get("error"));
  }

  private void thenElControllerEntregaTodosLosCamposAlService(Mueble datos)
    throws MuebleNoEncontrado {
    verify(servicioEdicionInventarioMuebles)
      .actualizar(
        eq(1),
        argThat(m ->
          m.getNombre().equals(datos.getNombre()) &&
          m.getAncho().equals(datos.getAncho()) &&
          m.getLargo().equals(datos.getLargo()) &&
          m.getDescripcion().equals(datos.getDescripcion()) &&
          m.getPrecio() == datos.getPrecio() &&
          m.getEstilo() == datos.getEstilo()
        )
      );
  }
}
