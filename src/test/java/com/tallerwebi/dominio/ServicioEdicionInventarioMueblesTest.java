package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
Criterios de aceptación:
-Dado un mueble en el inventario, cuando lo edito y guardo, entonces los cambios
 se reflejan en la tabla y en el plano si ya estaba ubicado.
-Dado un mueble que elimino, cuando confirmo la acción, entonces desaparece del
 inventario y de cualquier distribución guardada.
*/

public class ServicioEdicionInventarioMueblesTest {

  private static final int ID_MUEBLE = 1;
  private static final int ID_INEXISTENTE = 999;

  private RepositorioInventarioMueble repositorioInventarioMueble;
  private ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private List<Mueble> inventario;

  @BeforeEach
  public void init() {
    this.repositorioInventarioMueble = mock(RepositorioInventarioMueble.class);
    this.servicioEdicionInventarioMuebles =
      new ServicioEdicionInventarioMueblesImpl(this.repositorioInventarioMueble);
    this.inventario = new ArrayList<>();
  }

  // -------------------------------------------------------------------------
  // Edicion: copia de cada campo editable sobre el mueble ya guardado
  // -------------------------------------------------------------------------

  @Test
  public void deberiaActualizarElNombreCuandoElMuebleExisteYElNuevoNombreEsValido()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble("Mesa de comedor", mueble.getAncho(), mueble.getLargo());

    whenActualizoElMueble(datosNuevos);

    thenElNombreSeActualiza(mueble, "Mesa de comedor");
  }

  @Test
  public void deberiaActualizarElAnchoCuandoElMuebleExisteYElNuevoAnchoEsValido()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble(mueble.getNombre(), 1.5, mueble.getLargo());

    whenActualizoElMueble(datosNuevos);

    thenElAnchoSeActualiza(mueble, 1.5);
  }

  @Test
  public void deberiaActualizarElLargoCuandoElMuebleExisteYElNuevoLargoEsValido()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble(mueble.getNombre(), mueble.getAncho(), 1.2);

    whenActualizoElMueble(datosNuevos);

    thenElLargoSeActualiza(mueble, 1.2);
  }

  @Test
  public void deberiaActualizarLaDescripcionCuandoElMuebleExisteYLaNuevaDescripcionEsValida()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteConUnaDescripcion("Mesa de madera");
    Mueble datosNuevos = new Mueble(mueble.getNombre(), mueble.getAncho(), mueble.getLargo());
    datosNuevos.setDescripcion("Mesa de comedor de madera maciza");

    whenActualizoElMueble(datosNuevos);

    thenLaDescripcionSeActualiza(mueble, "Mesa de comedor de madera maciza");
  }

  @Test
  public void deberiaActualizarElPrecioCuandoElMuebleExisteYElNuevoPrecioEsValido()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble(mueble.getNombre(), mueble.getAncho(), mueble.getLargo());
    datosNuevos.setPrecio(12500.0);

    whenActualizoElMueble(datosNuevos);

    thenElPrecioSeActualiza(mueble, 12500.0);
  }

  @Test
  public void deberiaActualizarElEstiloCuandoElMuebleExisteYElNuevoEstiloEsValido()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble(mueble.getNombre(), mueble.getAncho(), mueble.getLargo());
    datosNuevos.setEstilo(Estilo.Japandi);

    whenActualizoElMueble(datosNuevos);

    thenElEstiloSeActualiza(mueble, Estilo.Japandi);
  }

  @Test
  public void deberiaActualizarLaCategoriaCuandoElMuebleExisteYLaNuevaCategoriaEsValida()
    throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    CategoriaDeMueble sillones = new CategoriaDeMueble("Sillón", 1.0, 0.8);
    Mueble datosNuevos = new Mueble(mueble.getNombre(), mueble.getAncho(), mueble.getLargo());
    datosNuevos.setCategoriaDeMueble(sillones);

    whenActualizoElMueble(datosNuevos);

    thenLaCategoriaSeActualiza(mueble, sillones);
  }

  @Test
  public void deberiaGuardarElMuebleCuandoLoActualizo() throws MuebleNoEncontrado {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    Mueble datosNuevos = new Mueble("Mesa ratona", mueble.getAncho(), mueble.getLargo());

    whenActualizoElMueble(datosNuevos);

    thenSeGuardoElMueble(mueble);
  }

  @Test
  public void deberiaLanzarMuebleNoEncontradoCuandoElMuebleNoExisteEnElInventario() {
    Mueble datosNuevos = new Mueble("Sillon de dos cuerpos", 2.4, 1.1);
    when(this.repositorioInventarioMueble.buscarPorId(ID_INEXISTENTE)).thenReturn(null);

    assertThrows(
      MuebleNoEncontrado.class,
      () -> this.servicioEdicionInventarioMuebles.actualizar(ID_INEXISTENTE, datosNuevos)
    );
  }

  // -------------------------------------------------------------------------
  // Eliminacion
  // -------------------------------------------------------------------------

  @Test
  public void deberiaEliminarElMuebleCuandoElMuebleExisteEnElInventario()
    throws MuebleNoEncontrado {
    givenMuebleExistenteEnElInventario();

    whenEliminoElMueble(ID_MUEBLE);

    thenSeEliminoElMueble(ID_MUEBLE);
  }

  @Test
  public void deberiaLanzarMuebleNoEncontradoCuandoIntentoEliminarUnMuebleInexistente() {
    when(this.repositorioInventarioMueble.buscarPorId(ID_INEXISTENTE)).thenReturn(null);

    assertThrows(
      MuebleNoEncontrado.class,
      () -> this.servicioEdicionInventarioMuebles.eliminar(ID_INEXISTENTE)
    );
  }

  // -------------------------------------------------------------------------
  // Listado y creacion
  // -------------------------------------------------------------------------

  @Test
  public void deberiaDevolverLosMueblesDelInventario() {
    givenInventarioConDosMuebles();

    List<Mueble> mueblesObtenidos = whenListoElInventario();

    thenElInventarioTieneLosMuebles(mueblesObtenidos);
  }

  @Test
  public void deberiaAgregarElMuebleAlInventarioCuandoLoCreo() {
    Mueble muebleNuevo = new Mueble("Mesa ratona", 1.2, 0.6);

    whenCreoElMueble(muebleNuevo);

    thenSeGuardoElMueble(muebleNuevo);
  }

  // -------------------------------------------------------------------------
  // Given (preparacion)
  // -------------------------------------------------------------------------

  private Mueble givenMuebleExistenteEnElInventario() {
    Mueble mueble = new Mueble("Mesa", 1.0, 0.8);
    mueble.setId(ID_MUEBLE);
    when(this.repositorioInventarioMueble.buscarPorId(ID_MUEBLE)).thenReturn(mueble);
    return mueble;
  }

  private Mueble givenMuebleExistenteConUnaDescripcion(String descripcion) {
    Mueble mueble = givenMuebleExistenteEnElInventario();
    mueble.setDescripcion(descripcion);
    return mueble;
  }

  private void givenInventarioConDosMuebles() {
    Mueble sofa = new Mueble("Sofa", 2.2, 0.9);
    sofa.setId(ID_MUEBLE);
    Mueble mesa = new Mueble("Mesa", 1.2, 0.8);
    mesa.setId(2);
    this.inventario.add(sofa);
    this.inventario.add(mesa);
    when(this.repositorioInventarioMueble.listarTodos()).thenReturn(this.inventario);
  }

  // -------------------------------------------------------------------------
  // When (ejecucion)
  // -------------------------------------------------------------------------

  private void whenActualizoElMueble(Mueble datos) throws MuebleNoEncontrado {
    this.servicioEdicionInventarioMuebles.actualizar(ID_MUEBLE, datos);
  }

  private void whenEliminoElMueble(int id) throws MuebleNoEncontrado {
    this.servicioEdicionInventarioMuebles.eliminar(id);
  }

  private List<Mueble> whenListoElInventario() {
    return this.servicioEdicionInventarioMuebles.listar();
  }

  private void whenCreoElMueble(Mueble mueble) {
    this.servicioEdicionInventarioMuebles.crear(mueble);
  }

  // -------------------------------------------------------------------------
  // Then (validacion)
  // -------------------------------------------------------------------------

  private void thenElNombreSeActualiza(Mueble mueble, String esperado) {
    assertThat(mueble.getNombre(), is(equalTo(esperado)));
  }

  private void thenElAnchoSeActualiza(Mueble mueble, Double esperado) {
    assertThat(mueble.getAncho(), is(equalTo(esperado)));
  }

  private void thenElLargoSeActualiza(Mueble mueble, Double esperado) {
    assertThat(mueble.getLargo(), is(equalTo(esperado)));
  }

  private void thenLaDescripcionSeActualiza(Mueble mueble, String esperado) {
    assertThat(mueble.getDescripcion(), is(equalTo(esperado)));
  }

  private void thenElPrecioSeActualiza(Mueble mueble, double esperado) {
    assertThat(mueble.getPrecio(), is(equalTo(esperado)));
  }

  private void thenElEstiloSeActualiza(Mueble mueble, Estilo esperado) {
    assertThat(mueble.getEstilo(), is(equalTo(esperado)));
  }

  private void thenLaCategoriaSeActualiza(Mueble mueble, CategoriaDeMueble esperada) {
    assertThat(mueble.getCategoriaDeMueble(), is(esperada));
  }

  private void thenElInventarioTieneLosMuebles(List<Mueble> muebles) {
    assertThat(muebles, is(equalTo(this.inventario)));
  }

  private void thenSeGuardoElMueble(Mueble mueble) {
    verify(this.repositorioInventarioMueble).guardar(mueble);
  }

  private void thenSeEliminoElMueble(int id) {
    verify(this.repositorioInventarioMueble).eliminar(id);
  }
}
