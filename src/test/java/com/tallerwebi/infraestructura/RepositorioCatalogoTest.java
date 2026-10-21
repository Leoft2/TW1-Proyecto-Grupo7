package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RepositorioCatalogo;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.Arrays;
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

    List<Mueble> mueblesObtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, null, null);

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

    List<Mueble> mueblesObtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, null, null);

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

  @Test
  @Transactional
  @Rollback
  public void dadoUnTipoDeAmbienteNuevo_cuandoLoGuardoEnElRepositorio_entoncesSePuedeBuscarPorNombre() {
    this.repositorioCatalogo.guardarTipoDeAmbiente(new TipoDeAmbiente("Balcon"));

    TipoDeAmbiente tipoObtenido = this.repositorioCatalogo.buscarTipoDeAmbientePorNombre("Balcon");

    assertThat(tipoObtenido, is(notNullValue()));
    assertThat(tipoObtenido.getNombre(), is(equalTo("Balcon")));
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

  @Test
  @Transactional
  @Rollback
  public void dadoQueElFiltroDeSostenibilidadEstaApagado_cuandoBuscoMueblesDelCatalogo_entoncesDevuelveSosteniblesYNoSostenibles() {
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, false, null);

    assertThat(obtenidos.size(), is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueElFiltroDeSostenibilidadEsNulo_cuandoBuscoMueblesDelCatalogo_entoncesNoFiltraPorMaterial() {
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, null, null);

    assertThat(obtenidos.size(), is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueElFiltroDeSostenibilidadEstaEncendido_cuandoBuscoMueblesDelCatalogo_entoncesSoloDevuelveLosSostenibles() {
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");
    this.cuandoGuardoUnMuebleDeMaterial("Biblioteca reciclada", 50000.0, "Madera reciclada");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, true, null);

    assertThat(obtenidos.size(), is(equalTo(2)));
    for (Mueble mueble : obtenidos) {
      assertThat(mueble.getMaterial().isSostenible(), is(true));
    }
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnMuebleSinMaterial_cuandoElFiltroDeSostenibilidadEstaEncendido_entoncesElMuebleNoSeExcluye() {
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble sin material", 45000.0, null);
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, true, null);

    assertThat(obtenidos.size(), is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoElFiltroDeSostenibilidadConEstiloYPrecio_cuandoBuscoMueblesDelCatalogo_entoncesDevuelveSoloLosQueCumplenTodo() {
    Mueble cumple = this.cuandoGuardoUnMuebleDeMaterial("Sillón de bambú", 80000.0, "Bambú");
    cumple.setEstilo(Estilo.Japandi);
    this.sessionFactory.getCurrentSession().flush();
    this.cuandoGuardoUnMuebleDeMaterial("Sillón de metal", 80000.0, "Metal")
      .setEstilo(Estilo.Japandi);
    this.cuandoGuardoUnMuebleDeMaterial("Sillón carísimo de bambú", 200000.0, "Bambú")
      .setEstilo(Estilo.Japandi);
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de bambú", 80000.0, "Bambú").setEstilo(Estilo.Retro);

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          null,
          100000.0,
          Estilo.Japandi,
          null,
          true,
          null
        );

    assertThat(obtenidos.size(), is(equalTo(1)));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Sillón de bambú")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnMuebleQueNoEstaEnElCatalogo_cuandoBuscoMueblesDelCatalogo_entoncesNoAparece() {
    Mueble enCatalogo = this.dadoQueTengoUnMueble("Mueble del catálogo", 150.0, Estilo.Retro);
    enCatalogo.setMaterial(this.dadoQueTengoUnMaterialGuardado("Bambú"));
    Mueble fueraDelCatalogo = new Mueble();
    fueraDelCatalogo.setNombre("Mueble fuera del catálogo");
    fueraDelCatalogo.setPrecio(150.0);
    fueraDelCatalogo.setEstilo(Estilo.Retro);
    fueraDelCatalogo.setMaterial(this.dadoQueTengoUnMaterialGuardado("Bambú"));
    this.cuandoGuardoUnMueble(enCatalogo);
    this.cuandoGuardoUnMueble(fueraDelCatalogo);

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, true, null);

    assertThat(obtenidos.size(), is(equalTo(1)));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Mueble del catálogo")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnIdDeMaterial_cuandoBuscoMueblesDelCatalogo_entoncesSoloDevuelveLosDeEseMaterial() {
    Material bambu = this.dadoQueTengoUnMaterialGuardado("Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          null,
          null,
          null,
          null,
          false,
          bambu.getId()
        );

    assertThat(obtenidos.size(), is(equalTo(1)));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Silla de bambú")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnIdDeMaterialYSoloSostenibles_cuandoBuscoMueblesDelCatalogo_entoncesDevuelveSoloLosQueCumplenAmbos() {
    Material bambu = this.dadoQueTengoUnMaterialGuardado("Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Silla de bambú", 30000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa de metal", 40000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          null,
          null,
          null,
          null,
          true,
          bambu.getId()
        );

    assertThat(obtenidos.size(), is(equalTo(1)));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Silla de bambú")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnPrecioMinimo_cuandoBuscoMueblesDelCatalogo_entoncesExcluyeLosQueNoLoAlcanzan() {
    this.cuandoGuardoUnMuebleDeMaterial("Lámpara", 25000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Sillón Retró", 85000.0, "Metal");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(50000.0, null, null, null, null, null);

    assertThat(obtenidos, hasSize(1));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Sillón Retró")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnNombreDeTipoDeAmbiente_cuandoBuscoMueblesDelCatalogo_entoncesSoloDevuelveLosDeEseTipo() {
    this.cuandoGuardoUnMuebleConTipoDeAmbiente("Cama Boho", 120000.0, "Habitación");
    this.cuandoGuardoUnMuebleConTipoDeAmbiente("Mesa de Comedor", 60000.0, "Comedor");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          null,
          null,
          null,
          "Habitación",
          null,
          null
        );

    assertThat(obtenidos, hasSize(1));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Cama Boho")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnMuebleQueSirveParaVariosTipos_cuandoBuscoPorUnoDeEllos_entoncesElMuebleSeIncluye() {
    this.cuandoGuardoUnMuebleConTipoDeAmbiente("Escritorio", 40000.0, "Pasillo");
    this.cuandoGuardoUnMuebleConTipoDeAmbiente("Cama Boho", 120000.0, "Habitación");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          null,
          null,
          null,
          "Habitación",
          null,
          null
        );

    assertThat(obtenidos, hasSize(1));
    assertThat(obtenidos.get(0).getNombre(), is(equalTo("Cama Boho")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoMueblesConDistintosPrecios_cuandoBuscoMueblesDelCatalogo_entoncesVienenOrdenadosDeMenorAMayor() {
    this.cuandoGuardoUnMuebleDeMaterial("Mueble caro", 40000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble barato", 20000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble medio", 30000.0, "Bambú");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, null, null);

    assertThat(this.nombresDe(obtenidos), contains("Mueble barato", "Mueble medio", "Mueble caro"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoMueblesConDistintosPrecios_cuandoBuscoMueblesDelCatalogoOrdenadosPorPrecioDescendente_entoncesVienenOrdenadosDeMayorAMenor() {
    this.cuandoGuardoUnMuebleDeMaterial("Mueble caro", 40000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble barato", 20000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble medio", 30000.0, "Bambú");

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
          null,
          null,
          null,
          null,
          null,
          null
        );

    assertThat(this.nombresDe(obtenidos), contains("Mueble caro", "Mueble medio", "Mueble barato"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnPrecioMaximo_cuandoBuscoMueblesConPrecioMenorAl_entoncesSoloDevuelveLosQueLoCumplen() {
    this.cuandoGuardoUnMuebleDeMaterial("Mueble barato", 20000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mueble caro", 40000.0, "Bambú");

    List<Mueble> obtenidos = this.repositorioCatalogo.ObtenerMueblesConPrecioMenorAl(30000.0);

    assertThat(this.nombresDe(obtenidos), contains("Mueble barato"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueElPrecioEsIgualAlMaximo_cuandoBuscoMueblesConPrecioMenorAl_entoncesElMuebleSeIncluye() {
    this.cuandoGuardoUnMuebleDeMaterial("Mueble del limite", 30000.0, "Bambú");

    List<Mueble> obtenidos = this.repositorioCatalogo.ObtenerMueblesConPrecioMenorAl(30000.0);

    assertThat(this.nombresDe(obtenidos), contains("Mueble del limite"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnEstilo_cuandoBuscoMueblesDeEstilo_entoncesSoloDevuelveLosDeEseEstilo() {
    this.cuandoGuardoUnMuebleDeMaterial("Sillón Retró", 85000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa Japandi", 150000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Cama Boho", 120000.0, "Bambú").setEstilo(Estilo.Boho);

    List<Mueble> obtenidos = this.repositorioCatalogo.ObtenerMueblesDeEstilo(Estilo.Retro);

    assertThat(this.nombresDe(obtenidos), containsInAnyOrder("Sillón Retró", "Mesa Japandi"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoVariosEstilos_cuandoBuscoMueblesDeEstilos_entoncesSoloDevuelveLosDeEsosEstilos() {
    this.cuandoGuardoUnMuebleDeMaterial("Sillón Retró", 85000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Mesa Japandi", 150000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Cama Boho", 120000.0, "Bambú").setEstilo(Estilo.Boho);

    List<Mueble> obtenidos =
      this.repositorioCatalogo.ObtenerMueblesDeEstilos(Arrays.asList(Estilo.Retro, Estilo.Japandi));

    assertThat(this.nombresDe(obtenidos), containsInAnyOrder("Sillón Retró", "Mesa Japandi"));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoHayEstilos_cuandoBuscoMueblesDeEstilos_entoncesDevuelveUnaListaVacia() {
    this.cuandoGuardoUnMuebleDeMaterial("Sillón Retró", 85000.0, "Bambú");

    List<Mueble> obtenidos = this.repositorioCatalogo.ObtenerMueblesDeEstilos(Arrays.asList());

    assertThat(obtenidos, is(empty()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoVariosMueblesDeUnEstilo_cuandoBuscoElMasBaratoDeEseEstilo_entoncesDevuelveElDeMenorPrecio() {
    this.cuandoGuardoUnMuebleDeMaterial("Sillón Retró", 85000.0, "Bambú");
    this.cuandoGuardoUnMuebleDeMaterial("Lámpara Retró", 25000.0, "Bambú");

    Mueble masBarato = this.repositorioCatalogo.ObtenerMuebleMasBaratoDeEstilo(Estilo.Retro);

    assertThat(masBarato, is(notNullValue()));
    assertThat(masBarato.getNombre(), is(equalTo("Lámpara Retró")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoQueNoHayMueblesDeUnEstilo_cuandoBuscoElMasBaratoDeEseEstilo_entoncesObtengoNull() {
    this.cuandoGuardoUnMuebleDeMaterial("Mesa Japandi", 150000.0, "Bambú")
      .setEstilo(Estilo.Japandi);

    Mueble masBarato = this.repositorioCatalogo.ObtenerMuebleMasBaratoDeEstilo(Estilo.Retro);

    assertThat(masBarato, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnMuebleDelCatalogo_cuandoCambioSuMaterial_entoncesQuedaGuardadoElMaterialNuevo() {
    Mueble mueble =
      this.cuandoGuardoUnMuebleDeMaterial("Mueble con material", 100.0, "Madera certificada");
    this.sessionFactory.getCurrentSession().flush();
    int idDelMueble = mueble.getId();

    Mueble guardado = this.repositorioCatalogo.ObtenerMueblePorId(idDelMueble);
    guardado.setMaterial(this.dadoQueTengoUnMaterialGuardado("Bambú"));
    this.repositorioCatalogo.guardarMueble(guardado);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    Mueble releido = this.repositorioCatalogo.ObtenerMueblePorId(idDelMueble);

    assertThat(releido.getMaterial(), is(notNullValue()));
    assertThat(releido.getMaterial().getNombre(), is(equalTo("Bambú")));
  }

  @Test
  @Transactional
  @Rollback
  public void dadoUnMuebleEnElInventarioDeUnAmbiente_cuandoLoElimino_delCatalogo_entoncesSeEliminaYSaleDelAmbiente() {
    Mueble mueble = this.cuandoGuardoUnMuebleDeMaterial("Mueble del inventario", 100.0, "Bambú");
    this.sessionFactory.getCurrentSession().flush();
    Ambiente ambiente = this.dadoQueTengoUnAmbienteConElMueble(mueble);

    this.repositorioCatalogo.eliminar(mueble.getId());
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    assertThat(this.repositorioCatalogo.ObtenerMueblePorId(mueble.getId()), is(nullValue()));
    assertThat(this.cuantosMueblesTieneElAmbiente(ambiente.getId()), is(0L));
  }

  private List<String> nombresDe(List<Mueble> muebles) {
    return muebles.stream().map(Mueble::getNombre).toList();
  }

  private Material dadoQueTengoUnMaterialGuardado(String nombre) {
    String query = "FROM Material WHERE nombre = :nombre";
    Material existente =
      this.sessionFactory.getCurrentSession()
        .createQuery(query, Material.class)
        .setParameter("nombre", nombre)
        .uniqueResult();
    if (existente != null) {
      return existente;
    }
    Material material = new Material(nombre, this.esSostenible(nombre));
    this.sessionFactory.getCurrentSession().persist(material);
    return material;
  }

  private boolean esSostenible(String nombreDelMaterial) {
    return (
      "Madera certificada".equals(nombreDelMaterial) ||
      "Madera reciclada".equals(nombreDelMaterial) ||
      "Bambú".equals(nombreDelMaterial)
    );
  }

  private Ambiente dadoQueTengoUnAmbienteConElMueble(Mueble mueble) {
    TipoDeAmbiente tipoDeAmbiente = this.dadoQueTengoUnTipoDeAmbienteGuardado("Living");
    Ambiente ambiente = new Ambiente(4.0, 3.0, tipoDeAmbiente);
    ambiente.agregarMueble(mueble);
    this.sessionFactory.getCurrentSession().persist(ambiente);
    this.sessionFactory.getCurrentSession().flush();
    return ambiente;
  }

  private long cuantosMueblesTieneElAmbiente(Long idDelAmbiente) {
    return this.sessionFactory.getCurrentSession()
      .createQuery("SELECT COUNT(m) FROM Ambiente a JOIN a.muebles m WHERE a.id = :id", Long.class)
      .setParameter("id", idDelAmbiente)
      .getSingleResult();
  }

  private Mueble cuandoGuardoUnMuebleDeMaterial(String nombre, double precio, String material) {
    Mueble mueble = new Mueble();
    mueble.setNombre(nombre);
    mueble.setPrecio(precio);
    mueble.setEstilo(Estilo.Retro);
    if (material != null) {
      mueble.setMaterial(this.dadoQueTengoUnMaterialGuardado(material));
    }
    mueble.setEnCatalogo(true);
    this.cuandoGuardoUnMueble(mueble);
    return mueble;
  }

  private Mueble cuandoGuardoUnMuebleConTipoDeAmbiente(
    String nombre,
    double precio,
    String nombreDelTipo
  ) {
    Mueble mueble = this.dadoQueTengoUnMueble(nombre, precio, Estilo.Retro);
    mueble.agregarTipoDeAmbiente(this.dadoQueTengoUnTipoDeAmbienteGuardado(nombreDelTipo));
    this.cuandoGuardoUnMueble(mueble);
    return mueble;
  }
}
