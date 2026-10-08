package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
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
  private ControladorInventarioMuebles controladorInventarioMuebles;

  @BeforeEach
  public void init() {
    servicioEdicionInventarioMuebles = mock(ServicioEdicionInventarioMuebles.class);
    controladorInventarioMuebles =
      new ControladorInventarioMuebles(servicioEdicionInventarioMuebles);
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
    controladorInventarioMuebles.irAConfirmarEliminacion(1);
  }

  private ModelAndView whenEliminoElMuebleDesdeElController() throws MuebleNoEncontrado {
    return controladorInventarioMuebles.eliminar(1);
  }

  private ModelAndView whenSolicitoElFormularioDeEdicion() throws MuebleNoEncontrado {
    return controladorInventarioMuebles.irAEditar(1);
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
