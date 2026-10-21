package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RepositorioCatalogo;
import com.tallerwebi.dominio.TipoDeAmbiente;
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
public class RepositorioCatalogoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioCatalogo repositorioCatalogo;

  @BeforeEach
  public void init() {
    repositorioCatalogo =
      new RepositorioCatalogoImpl(sessionFactory, new BorradorDeMueble(sessionFactory));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnNuevoMuebleCuandoLoGuardoEntoncesSeGuarda() {
    String nombreMueble = "Mueble Nuevo";
    Mueble mueble = this.dadoQueTengoUnMueble(nombreMueble, 100.0, Estilo.Retro);
    this.cuandoGuardoUnMueble(mueble);
    this.entoncesSeGuardoElMueble(nombreMueble, 100.0, Estilo.Retro, mueble);
  }

  @Test
  @Transactional
  @Rollback
  public void DadoQueNoHayNingunMuebleCuandoBuscoUnoEntoncesObtengoNull() {
    Mueble mueble = this.dadoQueTengoUnMueble("Mueble inexistente", 200.0, Estilo.Retro);
    this.entoncesElMuebleObtenidoEsNull(mueble);
  }

  @Test
  @Transactional
  @Rollback
  public void DadoQueHayMueblesRegistradosCuandoPidoTodosEntoncesObtengoTodos() {
    Mueble mueble1 = this.dadoQueTengoUnMueble("Mueble 1", 150.0, Estilo.Retro);
    Mueble mueble2 = this.dadoQueTengoUnMueble("Mueble 2", 250.0, Estilo.Retro);
    this.cuandoGuardoUnMueble(mueble1);
    this.cuandoGuardoUnMueble(mueble2);

    List<Mueble> mueblesObtenidos = this.repositorioCatalogo.ObtenerTodosLosMuebles();

    assertThat(mueblesObtenidos.size(), is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnMuebleDeInventarioCuandoPidoTodosLosDelCatalogoEntoncesNoAparece() {
    Mueble muebleDeCatalogo = this.dadoQueTengoUnMueble("Mueble del catálogo", 150.0, Estilo.Retro);
    Mueble muebleDeInventario = new Mueble();
    muebleDeInventario.setNombre("Mueble del inventario");
    muebleDeInventario.setPrecio(250.0);
    muebleDeInventario.setEstilo(Estilo.Japandi);
    this.cuandoGuardoUnMueble(muebleDeCatalogo);
    this.cuandoGuardoUnMueble(muebleDeInventario);

    List<Mueble> mueblesObtenidos = this.repositorioCatalogo.ObtenerTodosLosMuebles();

    assertThat(mueblesObtenidos.size(), is(equalTo(1)));
    assertThat(mueblesObtenidos.get(0).getNombre(), is(equalTo("Mueble del catálogo")));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnMuebleGuardadoCuandoLoBuscoPorIdEntoncesObtengoEseMueble() {
    Mueble mueble = this.dadoQueTengoUnMueble("Sillón Retró", 85000.0, Estilo.Retro);
    mueble.setDescripcion("Sillón de voluteadas tapizado en terciopelo.");
    this.cuandoGuardoUnMueble(mueble);

    Mueble muebleObtenido = this.repositorioCatalogo.ObtenerMueblePorId(mueble.getId());

    assertThat(muebleObtenido, is(notNullValue()));
    assertThat(muebleObtenido.getNombre(), is(equalTo("Sillón Retró")));
    assertThat(
      muebleObtenido.getDescripcion(),
      is(equalTo("Sillón de voluteadas tapizado en terciopelo."))
    );
  }

  @Test
  @Transactional
  @Rollback
  public void DadoQueNoHayNingunMuebleConEseIdCuandoLoBuscoEntoncesObtengoUnMuebleNull() {
    Mueble mueble = this.dadoQueTengoUnMueble("Mueble 1", 150.0, Estilo.Retro);
    this.cuandoGuardoUnMueble(mueble);

    Mueble muebleObtenido = this.repositorioCatalogo.ObtenerMueblePorId(mueble.getId() + 1000);

    assertThat(muebleObtenido, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnMuebleConTiposDeAmbienteCuandoLoBuscoPorIdEntoncesLosTiposYaEstanInicializados() {
    Integer idDelMueble = this.dadoQueGuardoUnMuebleConTipoDeAmbiente("Sillón Retró", "Living");
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    Mueble muebleObtenido = this.repositorioCatalogo.ObtenerMueblePorId(idDelMueble);
    this.sessionFactory.getCurrentSession().clear();

    assertThat(muebleObtenido.getTiposDeAmbiente().size(), is(1));
    assertThat(muebleObtenido.getTiposDeAmbiente().get(0).getNombre(), is(equalTo("Living")));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnMuebleDelCatalogoCuandoLoEliminoEntoncesYaNoExiste() {
    Integer idDelMueble = this.dadoQueGuardoUnMuebleConTipoDeAmbiente("Sillón Retró", "Living");
    this.sessionFactory.getCurrentSession().flush();

    this.repositorioCatalogo.eliminar(idDelMueble);
    this.sessionFactory.getCurrentSession().clear();

    assertThat(this.repositorioCatalogo.ObtenerMueblePorId(idDelMueble), is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoVariosMueblesDelCatalogoCuandoEliminoUnoEntoncesLosDemasSiguen() {
    Integer idAEliminar = this.dadoQueGuardoUnMuebleConTipoDeAmbiente("Sillón Retró", "Living");
    Mueble mesa = this.dadoQueTengoUnMueble("Mesa Ratona", 45000.0, Estilo.Boho);
    this.cuandoGuardoUnMueble(mesa);
    this.sessionFactory.getCurrentSession().flush();

    this.repositorioCatalogo.eliminar(idAEliminar);
    this.sessionFactory.getCurrentSession().clear();

    Mueble mesaObtenida = this.repositorioCatalogo.ObtenerMueblePorId(mesa.getId());
    assertThat(mesaObtenida, is(notNullValue()));
    assertThat(mesaObtenida.getNombre(), is(equalTo("Mesa Ratona")));
  }

  private Integer dadoQueGuardoUnMuebleConTipoDeAmbiente(String nombre, String nombreDelTipo) {
    TipoDeAmbiente tipoDeAmbiente = this.dadoQueTengoUnTipoDeAmbienteGuardado(nombreDelTipo);
    Mueble mueble = this.dadoQueTengoUnMueble(nombre, 85000.0, Estilo.Retro);
    mueble.setAncho(0.80);
    mueble.setLargo(0.85);
    mueble.agregarTipoDeAmbiente(tipoDeAmbiente);
    this.cuandoGuardoUnMueble(mueble);
    return mueble.getId();
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnTipoDeAmbienteGuardadoCuandoLoBuscoPorNombreEntoncesObtengoEseTipo() {
    this.dadoQueTengoUnTipoDeAmbienteGuardado("Living");

    TipoDeAmbiente tipoObtenido = this.repositorioCatalogo.buscarTipoDeAmbientePorNombre("Living");

    assertThat(tipoObtenido, is(notNullValue()));
    assertThat(tipoObtenido.getNombre(), is(equalTo("Living")));
  }

  @Test
  @Transactional
  @Rollback
  public void DadoUnTipoDeAmbienteQueNoExisteCuandoLoBuscoPorNombreEntoncesObtengoNull() {
    TipoDeAmbiente tipoObtenido = this.repositorioCatalogo.buscarTipoDeAmbientePorNombre("Garaje");

    assertThat(tipoObtenido, is(nullValue()));
  }

  private TipoDeAmbiente dadoQueTengoUnTipoDeAmbienteGuardado(String nombre) {
    TipoDeAmbiente tipoDeAmbiente = new TipoDeAmbiente(nombre);
    this.sessionFactory.getCurrentSession().persist(tipoDeAmbiente);
    return tipoDeAmbiente;
  }

  private Mueble dadoQueTengoUnMueble(String nombre, double precio, Estilo estilo) {
    Mueble mueble = new Mueble();
    mueble.setNombre(nombre);
    mueble.setPrecio(precio);
    mueble.setEstilo(estilo);
    mueble.setEnCatalogo(true);
    return mueble;
  }

  private void cuandoGuardoUnMueble(Mueble mueble) {
    repositorioCatalogo.guardarMueble(mueble);
  }

  private void entoncesSeGuardoElMueble(
    String nombre,
    double precio,
    Estilo estilo,
    Mueble muebleEsperado
  ) {
    String query = "FROM Mueble WHERE nombre = :nombre AND precio = :precio AND estilo = :estilo";
    Mueble muebleObtenido =
      this.sessionFactory.getCurrentSession()
        .createQuery(query, Mueble.class)
        .setParameter("nombre", nombre)
        .setParameter("precio", precio)
        .setParameter("estilo", estilo)
        .getSingleResult();

    this.entoncesElMuebleObtenidoEsCorrecto(muebleEsperado, muebleObtenido);
  }

  private void entoncesElMuebleObtenidoEsCorrecto(Mueble muebleEsperado, Mueble muebleObtenido) {
    assertThat(muebleObtenido.getNombre(), is(equalTo(muebleEsperado.getNombre())));
    assertThat(muebleObtenido.getPrecio(), is(equalTo(muebleEsperado.getPrecio())));
    assertThat(muebleObtenido.getEstilo(), is(equalTo(muebleEsperado.getEstilo())));
  }

  private void entoncesElMuebleObtenidoEsNull(Mueble muebleEsperado) {
    String query = "FROM Mueble WHERE nombre = :nombre AND precio = :precio AND estilo = :estilo";
    Mueble muebleObtenido =
      this.sessionFactory.getCurrentSession()
        .createQuery(query, Mueble.class)
        .setParameter("nombre", muebleEsperado.getNombre())
        .setParameter("precio", muebleEsperado.getPrecio())
        .setParameter("estilo", muebleEsperado.getEstilo())
        .uniqueResult();
    assertNull(muebleObtenido);
  }
}
