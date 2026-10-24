package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
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
public class RepositorioAmbienteTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioAmbiente repositorioAmbiente;

  @BeforeEach
  public void init() {
    repositorioAmbiente = new RepositorioAmbienteImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnAmbienteGuardado_cuandoLoBuscoPorId_entoncesTraigoSuTipoDeAmbiente() {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(4.0, 3.0, "Habitación");

    Ambiente obtenido = this.repositorioAmbiente.buscarPorId(ambiente.getId());

    assertThat(obtenido, is(notNullValue()));
    assertThat(obtenido.getAncho(), is(4.0));
    assertThat(obtenido.getLargo(), is(3.0));
    assertThat(obtenido.getTipoDeAmbiente().getNombre(), is("Habitación"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoHayAmbienteConEseId_cuandoLoBusco_entoncesObtengoNull() {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(4.0, 3.0, "Living");

    Ambiente obtenido = this.repositorioAmbiente.buscarPorId(ambiente.getId() + 1000);

    assertThat(obtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadosVariosAmbientesGuardados_cuandoPidoTodos_entoncesObtengoTodos() {
    Ambiente habitacion = this.dadoQueExisteElAmbiente(4.0, 3.0, "Habitación");
    Ambiente cocina = this.dadoQueExisteElAmbiente(5.0, 3.0, "Cocina");

    List<Ambiente> ambientes = this.repositorioAmbiente.obtenerTodos();

    assertThat(ambientes, hasSize(2));
    assertThat(ambientes, hasSize(2));
    assertThat(ambientes.get(0).getId(), is(equalTo(habitacion.getId())));
    assertThat(ambientes.get(1).getId(), is(equalTo(cocina.getId())));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnUsuarioConAmbientesConMuebles_cuandoPidoLosDeSuEmail_entoncesTraigoLosAmbientesYSusMuebles() {
    Usuario usuario = new Usuario();
    usuario.setEmail("test@unlam.edu.ar");
    this.sessionFactory.getCurrentSession().persist(usuario);

    Ambiente habitacion = this.dadoQueExisteElAmbientePara(4.0, 3.0, "Habitación", usuario);
    Mueble cama = this.dadoQueExisteElMueble("Cama");
    Mueble escritorio = this.dadoQueExisteElMueble("Escritorio");
    habitacion.agregarMueble(cama);
    habitacion.agregarMueble(escritorio);
    this.sessionFactory.getCurrentSession().persist(habitacion);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    List<Ambiente> ambientes = this.repositorioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar");

    assertThat(ambientes, hasSize(1));
    assertThat(ambientes.get(0).getMuebles(), hasSize(2));
    assertThat(ambientes.get(0).getMuebles().get(0).getId(), is(equalTo(cama.getId())));
    assertThat(ambientes.get(0).getMuebles().get(1).getId(), is(equalTo(escritorio.getId())));
  }

  // -------------------------------------------------------------------------
  // Tests para HU-04
  // -------------------------------------------------------------------------

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarAberturaCuandoSeAgregaPuertaAlAmbiente() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuerta(4.0, 5.0, "Living");
    this.flushYClear();

    List<Abertura> aberturas = this.whenBuscoLasAberturasDelAmbiente(ambiente.getId());

    this.thenLaCantidadDeAberturasEs(aberturas, 1);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarElMuroCuandoSeGuardaAbertura() {
    Ambiente ambiente =
      this.dadoQueExisteElAmbienteConPuertaEnMuro(4.0, 5.0, "Living", Muro.INFERIOR);
    this.flushYClear();

    Abertura abertura = this.whenBuscoLaPrimeraAberturaDelAmbiente(ambiente.getId());

    this.thenElMuroEs(abertura, Muro.INFERIOR);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarElAnchoDeLaAberturaCuandoSeGuardaAbertura() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConAberturaDeAncho(4.0, 5.0, "Living", 1.2);
    this.flushYClear();

    Abertura abertura = this.whenBuscoLaPrimeraAberturaDelAmbiente(ambiente.getId());

    this.thenElAnchoDeLaAberturaEs(abertura, 1.2);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLaPosicionDeLaAberturaCuandoSeGuardaAbertura() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConAberturaEnPosicion(4.0, 5.0, "Living", 2.3);
    this.flushYClear();

    Abertura abertura = this.whenBuscoLaPrimeraAberturaDelAmbiente(ambiente.getId());

    this.thenLaPosicionDeLaAberturaEs(abertura, 2.3);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverLaListaDeAberturasVaciaCuandoElAmbienteNoTieneAberturas() {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(4.0, 5.0, "Living");
    this.flushYClear();

    List<Abertura> aberturas = this.whenBuscoLasAberturasDelAmbiente(ambiente.getId());

    this.thenLaCantidadDeAberturasEs(aberturas, 0);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLaAberturaDelPrimerAmbienteCuandoHayDosAmbientes() {
    Ambiente ambienteA = this.dadoQueExisteElAmbienteConPuerta(4.0, 5.0, "Living");
    this.dadoQueExisteElAmbienteConPuerta(3.0, 3.5, "Dormitorio");
    this.flushYClear();

    List<Abertura> aberturasA = this.whenBuscoLasAberturasDelAmbiente(ambienteA.getId());

    this.thenLaCantidadDeAberturasEs(aberturasA, 1);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLasAberturasDelSegundoAmbienteCuandoHayDosAmbientes() {
    this.dadoQueExisteElAmbienteConPuerta(4.0, 5.0, "Living");
    Ambiente ambienteB = this.dadoQueExisteElAmbienteConDosVentanas(3.0, 3.5, "Dormitorio");
    this.flushYClear();

    List<Abertura> aberturasB = this.whenBuscoLasAberturasDelAmbiente(ambienteB.getId());

    this.thenContieneDosVentanas(aberturasB);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLasAberturasCuandoElAmbienteTieneTambienObstaculos() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuertaYObstaculo(4.0, 5.0, "Living");
    this.flushYClear();

    List<Abertura> aberturas = this.whenBuscoLasAberturasDelAmbiente(ambiente.getId());

    this.thenLaCantidadDeAberturasEs(aberturas, 1);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLosObstaculosCuandoElAmbienteTieneTambienAberturas() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuertaYObstaculo(4.0, 5.0, "Living");
    this.flushYClear();

    List<Obstaculo> obstaculos = this.whenBuscoLosObstaculosDelAmbiente(ambiente.getId());

    this.thenLaCantidadDeObstaculosEs(obstaculos, 1);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaActualizarAberturaCuandoSeModificaYSeGuardaElAmbiente() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuertaConRadio(4.0, 5.0, "Living", 0.9);
    this.flushYClear();

    Ambiente ambienteRecargado = this.whenBuscoElAmbiente(ambiente.getId());
    ambienteRecargado
      .getAberturas()
      .set(0, new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.85));
    this.flushYClear();

    Abertura abertura = this.whenBuscoLaPrimeraAberturaDelAmbiente(ambiente.getId());

    this.thenElRadioDeGiroEs(abertura, 0.85);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEliminarAberturaCuandoSeRemueveDeLaListaDelAmbiente() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuertaYVentana(4.0, 5.0, "Living");
    this.flushYClear();

    Ambiente ambienteRecargado = this.whenBuscoElAmbiente(ambiente.getId());
    ambienteRecargado.getAberturas().remove(0);
    this.flushYClear();

    List<Abertura> aberturas = this.whenBuscoLasAberturasDelAmbiente(ambiente.getId());

    this.thenQuedaSoloLaVentana(aberturas);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarVentanaSinRadioDeGiroCuandoSeAgregaVentanaAlAmbiente() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConVentana(4.0, 5.0, "Living");
    this.flushYClear();

    Abertura abertura = this.whenBuscoLaPrimeraAberturaDelAmbiente(ambiente.getId());

    this.thenEsVentanaSinRadio(abertura);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarTipoDeAberturaCuandoSeGuardanPuertaYVentana() {
    Ambiente ambiente = this.dadoQueExisteElAmbienteConPuertaYVentana(4.0, 5.0, "Living");
    this.flushYClear();

    List<Abertura> aberturas = this.whenBuscoLasAberturasDelAmbiente(ambiente.getId());

    this.thenContienePuertaYVentana(aberturas);
  }

  // -------------------------------------------------------------------------
  // Tests para HU-01
  // -------------------------------------------------------------------------

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverLaListaDeMueblesVaciaCuandoElAmbienteNoTieneMuebles() {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(4.0, 5.0, "Living");
    this.flushYClear();

    List<Mueble> muebles = this.whenBuscoLosMueblesDelAmbiente(ambiente.getId());

    this.thenLaListaDeMueblesEstaVacia(muebles);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRecuperarElAnchoDelPrimerAmbienteCuandoGuardoDos() {
    Ambiente living = this.dadoQueExisteElAmbiente(4.0, 5.0, "Living");
    this.dadoQueExisteElAmbiente(3.0, 3.5, "Dormitorio");
    this.flushYClear();

    Ambiente obtenido = this.whenBuscoElAmbiente(living.getId());

    this.thenElAnchoEs(obtenido, 4.0);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRecuperarElAnchoDelSegundoAmbienteCuandoGuardoDos() {
    this.dadoQueExisteElAmbiente(4.0, 5.0, "Living");
    Ambiente dormitorio = this.dadoQueExisteElAmbiente(3.0, 3.5, "Dormitorio");
    this.flushYClear();

    Ambiente obtenido = this.whenBuscoElAmbiente(dormitorio.getId());

    this.thenElAnchoEs(obtenido, 3.0);
  }

  private Ambiente dadoQueExisteElAmbientePara(
    Double ancho,
    Double largo,
    String tipo,
    Usuario usuario
  ) {
    TipoDeAmbiente tipoDeAmbiente = new TipoDeAmbiente(tipo);
    this.sessionFactory.getCurrentSession().persist(tipoDeAmbiente);
    Ambiente ambiente = new Ambiente(ancho, largo, tipoDeAmbiente);
    ambiente.setUsuario(usuario);
    this.sessionFactory.getCurrentSession().persist(ambiente);
    return ambiente;
  }

  private Mueble dadoQueExisteElMueble(String nombre) {
    Mueble mueble = new Mueble(nombre, 1.0, 2.0);
    this.sessionFactory.getCurrentSession().persist(mueble);
    return mueble;
  }

  private Ambiente dadoQueExisteElAmbiente(Double ancho, Double largo, String tipo) {
    TipoDeAmbiente tipoDeAmbiente = new TipoDeAmbiente(tipo);
    this.sessionFactory.getCurrentSession().persist(tipoDeAmbiente);
    Ambiente ambiente = new Ambiente(ancho, largo, tipoDeAmbiente);
    this.sessionFactory.getCurrentSession().persist(ambiente);
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  // -------------------------------------------------------------------------
  // Given (preparacion) - HU-04
  // -------------------------------------------------------------------------

  private Ambiente dadoQueExisteElAmbienteConPuerta(Double ancho, Double largo, String tipo) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.9));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConPuertaConRadio(
    Double ancho,
    Double largo,
    String tipo,
    Double radioDeGiro
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(
      new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, radioDeGiro)
    );
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConPuertaYVentana(
    Double ancho,
    Double largo,
    String tipo
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.DERECHO, 0.5, 1.5, null));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConDosVentanas(Double ancho, Double largo, String tipo) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.DERECHO, 0.5, 1.5, null));
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.IZQUIERDO, 0.5, 1.5, null));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConVentana(Double ancho, Double largo, String tipo) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.DERECHO, 0.5, 1.5, null));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConPuertaEnMuro(
    Double ancho,
    Double largo,
    String tipo,
    Muro muro
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, muro, 1.0, 0.9, 0.9));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConAberturaEnPosicion(
    Double ancho,
    Double largo,
    String tipo,
    Double posicion
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, posicion, 0.9, 0.9));
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private Ambiente dadoQueExisteElAmbienteConPuertaYObstaculo(
    Double ancho,
    Double largo,
    String tipo
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.9));
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.RADIADOR, Muro.IZQUIERDO, 0.5, 1.0, 0.2)
    );
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  // -------------------------------------------------------------------------
  // When (ejecucion) - HU-01
  // -------------------------------------------------------------------------

  private Ambiente whenBuscoElAmbiente(Long id) {
    return this.repositorioAmbiente.buscarPorId(id);
  }

  private List<Mueble> whenBuscoLosMueblesDelAmbiente(Long id) {
    return this.repositorioAmbiente.buscarPorId(id).getMuebles();
  }

  // -------------------------------------------------------------------------
  // When (ejecucion) - HU-04
  // -------------------------------------------------------------------------

  private List<Abertura> whenBuscoLasAberturasDelAmbiente(Long id) {
    return this.repositorioAmbiente.buscarPorId(id).getAberturas();
  }

  private Abertura whenBuscoLaPrimeraAberturaDelAmbiente(Long id) {
    return this.whenBuscoLasAberturasDelAmbiente(id).get(0);
  }

  private List<Obstaculo> whenBuscoLosObstaculosDelAmbiente(Long id) {
    return this.repositorioAmbiente.buscarPorId(id).getObstaculos();
  }

  // -------------------------------------------------------------------------
  // Then (validacion) - HU-01
  // -------------------------------------------------------------------------

  private void thenLaListaDeMueblesEstaVacia(List<Mueble> muebles) {
    assertThat(muebles, is(empty()));
  }

  private void thenElAnchoEs(Ambiente ambiente, Double anchoEsperado) {
    assertThat(ambiente.getAncho(), is(anchoEsperado));
  }

  // -------------------------------------------------------------------------
  // Then (validacion) - HU-04
  // -------------------------------------------------------------------------

  private void thenLaCantidadDeAberturasEs(List<Abertura> aberturas, Integer cantidadEsperada) {
    assertThat(aberturas, hasSize(cantidadEsperada));
  }

  private void thenElRadioDeGiroEs(Abertura abertura, Double radioEsperado) {
    assertThat(abertura.getRadioDeGiro(), is(radioEsperado));
  }

  private void thenEsVentanaSinRadio(Abertura abertura) {
    assertThat(abertura.getTipo(), is(TipoAbertura.VENTANA));
    assertThat(abertura.getRadioDeGiro(), is(nullValue()));
  }

  private void thenContienePuertaYVentana(List<Abertura> aberturas) {
    assertThat(aberturas, hasSize(2));
    assertThat(
      aberturas,
      containsInAnyOrder(
        hasProperty("tipo", is(TipoAbertura.PUERTA)),
        hasProperty("tipo", is(TipoAbertura.VENTANA))
      )
    );
  }

  private void thenContieneDosVentanas(List<Abertura> aberturas) {
    assertThat(aberturas, hasSize(2));
    assertThat(
      aberturas,
      containsInAnyOrder(
        hasProperty("tipo", is(TipoAbertura.VENTANA)),
        hasProperty("tipo", is(TipoAbertura.VENTANA))
      )
    );
  }

  private void thenElMuroEs(Abertura abertura, Muro muroEsperado) {
    assertThat(abertura.getMuro(), is(muroEsperado));
  }

  private void thenElAnchoDeLaAberturaEs(Abertura abertura, Double anchoEsperado) {
    assertThat(abertura.getAncho(), is(anchoEsperado));
  }

  private void thenLaPosicionDeLaAberturaEs(Abertura abertura, Double posicionEsperada) {
    assertThat(abertura.getPosicion(), is(posicionEsperada));
  }

  private void thenQuedaSoloLaVentana(List<Abertura> aberturas) {
    assertThat(aberturas, hasSize(1));
    assertThat(aberturas.get(0).getTipo(), is(TipoAbertura.VENTANA));
  }

  private void thenLaCantidadDeObstaculosEs(List<Obstaculo> obstaculos, Integer cantidadEsperada) {
    assertThat(obstaculos, hasSize(cantidadEsperada));
  }

  private Ambiente dadoQueExisteElAmbienteConAberturaDeAncho(
    Double ancho,
    Double largo,
    String tipo,
    Double anchoAbertura
  ) {
    Ambiente ambiente = this.dadoQueExisteElAmbiente(ancho, largo, tipo);
    ambiente.agregarAbertura(
      new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, anchoAbertura, 0.9)
    );
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private void flushYClear() {
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();
  }
}
