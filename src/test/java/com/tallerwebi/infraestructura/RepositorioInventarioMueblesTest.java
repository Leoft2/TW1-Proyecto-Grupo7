package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioInventarioMueblesTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioInventarioMueble repositorioInventarioMueble;

  @BeforeEach
  public void init() {
    this.repositorioInventarioMueble = new RepositorioInventarioMuebleImpl(this.sessionFactory);
  }

  // -------------------------------------------------------------------------
  // Test repositorio para HU-01
  // -------------------------------------------------------------------------

  // Alta (guardar un mueble nuevo)

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirLasMedidasCuandoGuardoUnMuebleNuevo() {
    Mueble mesa = givenMuebleNuevo("Mesa", 1.2, 0.8);

    whenGuardoElMueble(mesa);

    thenElMuebleTieneLasMedidas(mesa.getId(), "Mesa", 1.2, 0.8);
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaListarLosMueblesDelCatalogoCuandoListoElInventario() {
    Mueble muebleDeCatalogo = givenMuebleNuevo("Sillón del catálogo", 0.8, 0.85);
    muebleDeCatalogo.setEnCatalogo(true);
    whenGuardoElMueble(muebleDeCatalogo);
    whenGuardoElMueble(givenMuebleNuevo("Mesa", 1.2, 0.8));

    List<Mueble> inventario = whenListoElInventario();

    assertThat(inventario, hasSize(1));
    assertThat(inventario.get(0).getNombre(), is("Mesa"));
  }

  // -------------------------------------------------------------------------
  // Tests repositorio para HU-23
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // Edicion (criterio 1)
  // -------------------------------------------------------------------------

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirElNombreCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setNombre("Mesa ratona"));

    thenElNombrePersiste(mueble.getId(), "Mesa ratona");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirElAnchoCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setAncho(2.0));

    thenElAnchoPersiste(mueble.getId(), 2.0);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirElLargoCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setLargo(1.5));

    thenElLargoPersiste(mueble.getId(), 1.5);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirLaDescripcionCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(
      mueble.getId(),
      modificado -> modificado.setDescripcion("Mesa de madera maciza")
    );

    thenLaDescripcionPersiste(mueble.getId(), "Mesa de madera maciza");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirElPrecioCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setPrecio(12500.0));

    thenElPrecioPersiste(mueble.getId(), 12500.0);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirElEstiloCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setEstilo(Estilo.Japandi));

    thenElEstiloPersiste(mueble.getId(), Estilo.Japandi);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaNoCrearUnRegistroNuevoCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setNombre("Mesa ratona"));

    thenHayUnSoloMuebleEnElInventario();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaNoModificarLosDemasMueblesCuandoModificoUno() {
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    flushYClear();

    whenModificoElMueble(sofa.getId(), modificado -> modificado.setNombre("Sofa nuevo"));

    thenElMuebleConservaSuNombre(mesa.getId(), "Mesa");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverElDatoActualizadoCuandoListoElInventario() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setNombre("Mesa ratona"));

    thenElListadoTraeElNombre("Mesa ratona");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarElIdCuandoModificoUnMuebleGuardado() {
    Mueble mueble = givenMuebleGuardado("Mesa", 1.0, 0.8, Estilo.Minimalista);
    flushYClear();

    whenModificoElMueble(mueble.getId(), modificado -> modificado.setNombre("Mesa ratona"));

    thenElMuebleConservaSuId(mueble.getId());
  }

  // Listado: el inventario solo lista muebles con medidas.
  // Los muebles del catalogo (data.sql) no tienen ancho/largo: no pertenecen
  // al inventario del usuario y hasta pueden romper la generacion del plano.

  @Test
  @Transactional
  @Rollback
  public void deberiaExcluirDelInventarioLosMueblesSinMedidasCuandoListoElInventario() {
    givenMuebleGuardado("Sillon del catalogo", null, null, Estilo.Retro);
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    flushYClear();

    List<Mueble> inventario = whenListoElInventario();

    thenElInventarioContieneSoloElMuebleConMedidas(inventario, mesa);
  }

  // -------------------------------------------------------------------------
  // Eliminacion (criterio 2) - sin distribuciones
  // -------------------------------------------------------------------------

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullCuandoBuscoUnMuebleEliminado() {
    Mueble mueble = givenMuebleGuardado("Sillon", 2.0, 0.9, Estilo.Retro);
    flushYClear();

    whenEliminoElMueble(mueble.getId());

    thenElMuebleNoExiste(mueble.getId());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaReducirElInventarioCuandoEliminoUnMueble() {
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElInventarioTieneUnMueble();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLosDemasMueblesCuandoEliminoUno() {
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElMuebleSigueExistiendo(mesa.getId());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverListaVaciaCuandoEliminoElUnicoMueble() {
    Mueble unico = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    flushYClear();

    whenEliminoElMueble(unico.getId());

    thenElInventarioEstaVacio();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarSusUbicacionesCuandoEliminoUnMuebleUbicadoEnUnPlano() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Ambiente ambiente = givenAmbienteGuardado(4.0, 3.0, living);
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    givenPlanoGuardado(ambiente, new Mueble[] { sofa });
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElMuebleNoTieneUbicaciones(sofa.getId());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarSusUbicacionesEnTodosLosPlanosCuandoEliminoUnMueble() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Ambiente livingAmbiente = givenAmbienteGuardado(4.0, 3.0, living);
    TipoDeAmbiente comedor = givenTipoDeAmbienteGuardado("Comedor");
    Ambiente comedorAmbiente = givenAmbienteGuardado(4.0, 4.0, comedor);
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    givenPlanoGuardado(livingAmbiente, new Mueble[] { sofa });
    givenPlanoGuardado(comedorAmbiente, new Mueble[] { sofa });
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElMuebleNoTieneUbicaciones(sofa.getId());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarElPlanoCuandoEliminoUnMuebleUbicado() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Ambiente ambiente = givenAmbienteGuardado(4.0, 3.0, living);
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    Plano plano = givenPlanoGuardado(ambiente, new Mueble[] { sofa, mesa });
    Long idPlano = (Long) this.sessionFactory.getCurrentSession().getIdentifier(plano);
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElPlanoSigueExistiendo(idPlano);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLasOtrasUbicacionesCuandoEliminoUnMuebleUbicado() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Ambiente ambiente = givenAmbienteGuardado(4.0, 3.0, living);
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    Plano plano = givenPlanoGuardado(ambiente, new Mueble[] { sofa, mesa });
    Long idPlano = (Long) this.sessionFactory.getCurrentSession().getIdentifier(plano);
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElPlanoTieneUnaUbicacion(idPlano);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaQuitarElMuebleDeLosExcluidosDelPlanoCuandoLoElimino() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Ambiente ambiente = givenAmbienteGuardado(4.0, 3.0, living);
    Mueble sofa = givenMuebleGuardado("Sofa", 2.2, 0.9, Estilo.Boho);
    givenPlanoConExcluido(ambiente, sofa);
    flushYClear();

    whenEliminoElMueble(sofa.getId());

    thenElMuebleNoEstaExcluidoEnNingunPlano(sofa.getId());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarLaRelacionConTiposDeAmbienteCuandoEliminoElMueble() {
    TipoDeAmbiente living = givenTipoDeAmbienteGuardado("Living");
    Mueble mesa = givenMuebleGuardado("Mesa", 1.2, 0.8, Estilo.Japandi);
    mesa.agregarTipoDeAmbiente(living);
    flushYClear();

    whenEliminoElMueble(mesa.getId());

    thenElTipoDeAmbienteNoTieneMuebles(living.getId());
  }

  // -------------------------------------------------------------------------
  // Given (preparacion)
  // -------------------------------------------------------------------------

  private Mueble givenMuebleGuardado(String nombre, Double ancho, Double largo, Estilo estilo) {
    Mueble mueble = new Mueble(nombre, ancho, largo);
    mueble.setEstilo(estilo);
    this.sessionFactory.getCurrentSession().persist(mueble);
    return mueble;
  }

  private Mueble givenMuebleNuevo(String nombre, Double ancho, Double largo) {
    return new Mueble(nombre, ancho, largo);
  }

  private TipoDeAmbiente givenTipoDeAmbienteGuardado(String nombre) {
    TipoDeAmbiente tipo = new TipoDeAmbiente(nombre);
    this.sessionFactory.getCurrentSession().persist(tipo);
    return tipo;
  }

  private Ambiente givenAmbienteGuardado(Double ancho, Double largo, TipoDeAmbiente tipo) {
    Ambiente ambiente = new Ambiente(ancho, largo, tipo);
    this.sessionFactory.getCurrentSession().persist(ambiente);
    return ambiente;
  }

  private Plano givenPlanoGuardado(Ambiente ambiente, Mueble[] muebles) {
    List<MuebleUbicado> ubicados = new ArrayList<>();
    for (Mueble mueble : muebles) {
      ubicados.add(new MuebleUbicado(mueble, 0.0, 0.0));
    }
    Plano plano = new Plano(ambiente, ubicados, new ArrayList<>(), new ArrayList<>());
    this.sessionFactory.getCurrentSession().persist(plano);
    return plano;
  }

  private Plano givenPlanoConExcluido(Ambiente ambiente, Mueble mueble) {
    List<Mueble> excluidos = new ArrayList<>();
    excluidos.add(mueble);
    Plano plano = new Plano(ambiente, new ArrayList<>(), excluidos, new ArrayList<>());
    this.sessionFactory.getCurrentSession().persist(plano);
    return plano;
  }

  /**
   * Fuerza el SQL a la base y limpia la cache de 1er nivel para que la proxima lectura vaya a la BD.
   */
  private void flushYClear() {
    Session session = this.sessionFactory.getCurrentSession();
    session.flush();
    session.clear();
  }

  // -------------------------------------------------------------------------
  // When (ejecucion)
  // -------------------------------------------------------------------------

  private void whenGuardoElMueble(Mueble mueble) {
    this.repositorioInventarioMueble.guardar(mueble);
    flushYClear();
  }

  private void whenModificoElMueble(int id, Consumer<Mueble> cambio) {
    Mueble aModificar = this.repositorioInventarioMueble.buscarPorId(id);
    cambio.accept(aModificar);
    this.repositorioInventarioMueble.guardar(aModificar);
    flushYClear();
  }

  private void whenEliminoElMueble(int id) {
    this.repositorioInventarioMueble.eliminar(id);
    flushYClear();
  }

  private List<Mueble> whenListoElInventario() {
    return this.repositorioInventarioMueble.listarTodos();
  }

  // -------------------------------------------------------------------------
  // Then (validacion)
  // -------------------------------------------------------------------------

  private void thenElMuebleTieneLasMedidas(int id, String nombre, Double ancho, Double largo) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id),
      is(equalTo(new Mueble(nombre, ancho, largo)))
    );
  }

  private void thenElNombrePersiste(int id, String nombreEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getNombre(),
      is(equalTo(nombreEsperado))
    );
  }

  private void thenElAnchoPersiste(int id, Double anchoEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getAncho(),
      is(equalTo(anchoEsperado))
    );
  }

  private void thenElLargoPersiste(int id, Double largoEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getLargo(),
      is(equalTo(largoEsperado))
    );
  }

  private void thenLaDescripcionPersiste(int id, String descripcionEsperada) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getDescripcion(),
      is(equalTo(descripcionEsperada))
    );
  }

  private void thenElPrecioPersiste(int id, double precioEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getPrecio(),
      is(equalTo(precioEsperado))
    );
  }

  private void thenElEstiloPersiste(int id, Estilo estiloEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getEstilo(),
      is(equalTo(estiloEsperado))
    );
  }

  private void thenHayUnSoloMuebleEnElInventario() {
    assertThat(this.repositorioInventarioMueble.listarTodos(), hasSize(1));
  }

  private void thenElMuebleConservaSuNombre(int id, String nombreEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(id).getNombre(),
      is(equalTo(nombreEsperado))
    );
  }

  private void thenElListadoTraeElNombre(String nombreEsperado) {
    assertThat(
      this.repositorioInventarioMueble.listarTodos().get(0).getNombre(),
      is(equalTo(nombreEsperado))
    );
  }

  private void thenElMuebleConservaSuId(int idEsperado) {
    assertThat(
      this.repositorioInventarioMueble.buscarPorId(idEsperado).getId(),
      is(equalTo(idEsperado))
    );
  }

  private void thenElMuebleNoExiste(int id) {
    assertThat(this.repositorioInventarioMueble.buscarPorId(id), is(nullValue()));
  }

  private void thenElInventarioTieneUnMueble() {
    assertThat(this.repositorioInventarioMueble.listarTodos(), hasSize(1));
  }

  private void thenElMuebleSigueExistiendo(int id) {
    assertThat(this.repositorioInventarioMueble.buscarPorId(id), is(notNullValue()));
  }

  private void thenElInventarioEstaVacio() {
    assertThat(this.repositorioInventarioMueble.listarTodos(), is(empty()));
  }

  private void thenElInventarioContieneSoloElMuebleConMedidas(
    List<Mueble> inventario,
    Mueble esperado
  ) {
    assertThat(inventario, contains(esperado));
  }

  private void thenElMuebleNoTieneUbicaciones(int id) {
    Long total =
      this.sessionFactory.getCurrentSession()
        .createQuery("select count(u) from MuebleUbicado u where u.mueble.id = :id", Long.class)
        .setParameter("id", id)
        .uniqueResult();
    assertThat(total, is(0L));
  }

  private void thenElPlanoSigueExistiendo(Long id) {
    assertThat(this.sessionFactory.getCurrentSession().get(Plano.class, id), is(notNullValue()));
  }

  private void thenElPlanoTieneUnaUbicacion(Long planoId) {
    Long total =
      this.sessionFactory.getCurrentSession()
        .createQuery("select count(m) from Plano p join p.muebles m where p.id = :id", Long.class)
        .setParameter("id", planoId)
        .uniqueResult();
    assertThat(total, is(1L));
  }

  private void thenElMuebleNoEstaExcluidoEnNingunPlano(int id) {
    Long total =
      this.sessionFactory.getCurrentSession()
        .createQuery(
          "select count(m) from Plano p join p.mueblesExcluidos m where m.id = :id",
          Long.class
        )
        .setParameter("id", id)
        .uniqueResult();
    assertThat(total, is(0L));
  }

  private void thenElTipoDeAmbienteNoTieneMuebles(Long tipoId) {
    Long total =
      this.sessionFactory.getCurrentSession()
        .createQuery(
          "select count(m) from Mueble m join m.tiposDeAmbienteAsociados a join a.tipoDeAmbiente t where t.id = :id",
          Long.class
        )
        .setParameter("id", tipoId)
        .uniqueResult();
    assertThat(total, is(0L));
  }
}
