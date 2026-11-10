package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.datos.HsqldbConDatosTestConfig;
import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.BaldeLitros;
import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.FormaPintura;
import com.tallerwebi.dominio.Litros;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RecomendacionPintura;
import com.tallerwebi.dominio.RepositorioAmbiente;
import com.tallerwebi.dominio.ServicioCatalogo;
import com.tallerwebi.dominio.ServicioCatalogoImpl;
import com.tallerwebi.dominio.ServicioRecomendacionPintura;
import com.tallerwebi.dominio.ServicioRecomendacionPinturaImpl;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HsqldbConDatosTestConfig.class })
public class DataSqlTest {

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoElCatalogo_entoncesEstanLosMueblesSembrados() {
    List<Mueble> muebles = this.obtenerMuebles();

    assertThat(muebles, hasSize(18));
    assertThat(
      muebles.stream().map(Mueble::getEstilo).distinct().toList(),
      containsInAnyOrder(Estilo.values())
    );
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoUnMueblePorId_entoncesElEstiloSeGuardaComoTexto() {
    Mueble primerMueble =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Mueble WHERE id = :id", Mueble.class)
        .setParameter("id", 1)
        .getSingleResult();

    assertThat(primerMueble.getNombre(), is("Sillón Retró"));
    assertThat(primerMueble.getPrecio(), is(85000.0));
    assertThat(primerMueble.getEstilo(), is(Estilo.Retro));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoElCatalogo_entoncesTodosLosMueblesTienenDescripcion() {
    List<String> descripcionesVacias =
      this.obtenerMuebles()
        .stream()
        .filter(mueble -> mueble.getDescripcion() == null || mueble.getDescripcion().isBlank())
        .map(Mueble::getNombre)
        .toList();

    assertThat(descripcionesVacias, hasSize(0));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLasCategorias_entoncesEstanSembradas() {
    assertThat(this.obtenerCategorias(), is(not(empty())));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLasCategorias_entoncesTodasTienenMedidasPositivas() {
    List<String> categoriasConMedidasInvalidas =
      this.obtenerCategorias()
        .stream()
        .filter(categoria ->
          !esPositivo(categoria.getAnchoPromedio()) ||
          !esPositivo(categoria.getProfundidadPromedio())
        )
        .map(CategoriaDeMueble::getNombre)
        .toList();

    assertThat(categoriasConMedidasInvalidas, hasSize(0));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoElCatalogo_entoncesTodosLosMueblesTienenTipoDeAmbiente() {
    List<String> mueblesSinTipo =
      this.obtenerMuebles()
        .stream()
        .filter(mueble -> mueble.getTiposDeAmbiente().isEmpty())
        .map(Mueble::getNombre)
        .toList();

    assertThat(mueblesSinTipo, hasSize(0));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoCadaTipoDeAmbiente_entoncesAlMenosTieneUnMueble() {
    for (String nombre : TipoDeAmbiente.nombresConocidos()) {
      boolean tieneMuebles =
        this.obtenerMuebles()
          .stream()
          .anyMatch(mueble ->
            mueble.getTiposDeAmbiente().stream().anyMatch(tipo -> nombre.equals(tipo.getNombre()))
          );

      assertThat("El tipo " + nombre + " necesita al menos un mueble", tieneMuebles, is(true));
    }
  }

  @Test
  @Transactional
  public void dadoQueLosMueblesSembrados_cuandoFiltroElCatalogoPorHabitacion_entoncesAparecenLasCamas()
    throws PresupuestoNegativoException {
    ServicioCatalogo servicioCatalogo = new ServicioCatalogoImpl(
      new RepositorioCatalogoImpl(this.sessionFactory, new BorradorDeMueble(this.sessionFactory))
    );

    List<String> nombresDeHabitacion = servicioCatalogo
      .ObtenerMueblesFiltrados(null, null, null, new TipoDeAmbiente("Habitación"), null, null, null)
      .stream()
      .map(Mueble::getNombre)
      .toList();

    assertThat(nombresDeHabitacion, hasItem("Cama Boho"));
    assertThat(nombresDeHabitacion, not(hasItem("Mesa de Comedor Japandi")));
  }

  @Test
  @Transactional
  public void dadoQueLosMueblesSembrados_cuandoFiltroPorUnRangoDePrecio_entoncesSoloTraeLosDelRango()
    throws PresupuestoNegativoException {
    ServicioCatalogo servicioCatalogo = new ServicioCatalogoImpl(
      new RepositorioCatalogoImpl(this.sessionFactory, new BorradorDeMueble(this.sessionFactory))
    );

    List<Mueble> rango = servicioCatalogo.ObtenerMueblesFiltrados(
      20000.0,
      50000.0,
      null,
      null,
      null,
      null,
      null
    );

    assertThat(rango, is(not(empty())));
    for (Mueble mueble : rango) {
      assertThat(mueble.getPrecio() >= 20000.0 && mueble.getPrecio() <= 50000.0, is(true));
    }
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosAmbientes_entoncesElUsuarioDeTestTieneAmbientesConTipo() {
    List<Ambiente> ambientes = this.obtenerAmbientes();

    assertThat(ambientes, hasSize(8));
    List<String> tipos = new ArrayList<>();
    for (Ambiente ambiente : ambientes) {
      assertThat(ambiente.getUsuario(), is(notNullValue()));
      assertThat(ambiente.getUsuario().getEmail(), is("test@unlam.edu.ar"));
      assertThat(ambiente.getTipoDeAmbiente(), is(notNullValue()));
      tipos.add(ambiente.getTipoDeAmbiente().getNombre());
    }

    assertThat(tipos, hasItems("Living", "Habitación", "Pasillo", "Cocina", "Comedor"));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosAmbientesDelUsuarioDeTest_entoncesCadaAmbienteTieneSusMuebles() {
    RepositorioAmbiente repositorioAmbiente = new RepositorioAmbienteImpl(this.sessionFactory);

    List<Ambiente> ambientes = repositorioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar");

    assertThat(ambientes, hasSize(8));
    assertThat(
      nombresDeMueblesDe(ambientes.get(0)),
      containsInAnyOrder("Cama Boho", "Escritorio Minimalista", "Lámpara de Mesa Japandi")
    );
    assertThat(
      nombresDeMueblesDe(ambientes.get(1)),
      containsInAnyOrder(
        "Mesada de Cocina Minimalista",
        "Alacena Industrial",
        "Zapatero Escandinavo"
      )
    );
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosAmbientesDelUsuarioTest_entoncesEstanRepresentadasTodasLasFormasDePintura() {
    RepositorioAmbiente repositorioAmbiente = new RepositorioAmbienteImpl(this.sessionFactory);
    List<Ambiente> ambientes = repositorioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar");
    ServicioRecomendacionPintura servicioRecomendacion = new ServicioRecomendacionPinturaImpl();

    Set<FormaPintura> formas = EnumSet.noneOf(FormaPintura.class);
    for (Ambiente ambiente : ambientes) {
      Hibernate.initialize(ambiente.getAberturas());
      RecomendacionPintura recomendacion = servicioRecomendacion.recomendar(ambiente, null);
      assertThat(recomendacion.getFormaPintura(), is(notNullValue()));
      formas.add(recomendacion.getFormaPintura());
    }

    assertThat(formas, is(EnumSet.allOf(FormaPintura.class)));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoElCatalogo_entoncesTodosLosMueblesEstanMarcadosComoDelCatalogo() {
    List<String> mueblesSinMarcar =
      this.obtenerMuebles()
        .stream()
        .filter(mueble -> !mueble.getEnCatalogo())
        .map(Mueble::getNombre)
        .toList();

    assertThat(mueblesSinMarcar, hasSize(0));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosMateriales_entoncesEstanSembrados() {
    assertThat(
      this.obtenerNombresDeMateriales(),
      containsInAnyOrder(
        "Madera certificada",
        "Madera reciclada",
        "Bambú",
        "Madera común",
        "Metal",
        "Melamina",
        "Plástico",
        "Otro"
      )
    );
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoElCatalogo_entoncesTodosLosMueblesTienenMaterial() {
    List<String> mueblesSinMaterial =
      this.obtenerMuebles()
        .stream()
        .filter(mueble -> mueble.getMaterial() == null)
        .map(Mueble::getNombre)
        .toList();

    assertThat(mueblesSinMaterial, hasSize(0));
  }

  @Test
  @Transactional
  public void dadoQueLosMueblesSembrados_cuandoFiltroPorSoloSostenibles_entoncesSoloTraeLosSostenibles()
    throws PresupuestoNegativoException {
    ServicioCatalogo servicioCatalogo = new ServicioCatalogoImpl(
      new RepositorioCatalogoImpl(this.sessionFactory, new BorradorDeMueble(this.sessionFactory))
    );

    List<Mueble> sostenibles = servicioCatalogo.ObtenerMueblesFiltrados(
      null,
      null,
      null,
      null,
      null,
      true,
      null
    );

    assertThat(sostenibles, is(not(empty())));
    for (Mueble mueble : sostenibles) {
      assertThat(mueble.getMaterial().isSostenible(), is(true));
    }
  }

  private List<String> obtenerNombresDeMateriales() {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Material", Material.class)
      .getResultList()
      .stream()
      .map(Material::getNombre)
      .toList();
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosUsuarios_entoncesHayUnUsuarioComunYUnAdministrador() {
    Usuario usuarioDeTest = this.obtenerUsuario("test@unlam.edu.ar");
    Usuario administrador = this.obtenerUsuario("admin@unlam.edu.ar");

    assertThat(usuarioDeTest.getRol(), is("USER"));
    assertThat(administrador.getRol(), is("ADMIN"));
  }

  @Test
  @Transactional
  public void dadoQueArrancaLaAplicacion_cuandoConsultoLosBaldes_entoncesTienenSusOpcionesDeLitrosSembradas() {
    List<BaldePintura> baldes =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM BaldePintura", BaldePintura.class)
        .getResultList();

    assertThat(baldes, hasSize(15));
    for (BaldePintura balde : baldes) {
      assertThat(balde.getCantidadLitros(), is(4.0));
      assertThat(balde.getUrl(), is(nullValue()));
      assertThat(balde.getOpcionesLitros(), is("1,4,10,20"));
    }

    Long cantidadLitros =
      this.sessionFactory.getCurrentSession()
        .createQuery("SELECT COUNT(l) FROM Litros l", Long.class)
        .getSingleResult();
    assertThat(cantidadLitros, is(4L));

    Long cantidadBaldeLitros =
      this.sessionFactory.getCurrentSession()
        .createQuery("SELECT COUNT(bl) FROM BaldeLitros bl", Long.class)
        .getSingleResult();
    assertThat(cantidadBaldeLitros, is(60L));
  }

  @Test
  @Transactional
  public void dadoQueGuardoUnBaldeConOpcionesLitros_cuandoLoRecargoDesdeLaBaseDeDatos_entoncesPersistenLasOpciones() {
    BaldePintura balde = new BaldePintura(
      "Balde Test",
      "Rojo",
      "#ff0000",
      "/test",
      4.0,
      5000.0,
      null
    );
    balde.agregarOpcionLitros(
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Litros WHERE valor = 4.0", Litros.class)
        .getSingleResult()
    );
    balde.agregarOpcionLitros(
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Litros WHERE valor = 10.0", Litros.class)
        .getSingleResult()
    );
    this.sessionFactory.getCurrentSession().persist(balde);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    BaldePintura recargado =
      this.sessionFactory.getCurrentSession().get(BaldePintura.class, balde.getId());

    assertThat(recargado.getOpcionesLitros(), is("4,10"));
  }

  private Usuario obtenerUsuario(String email) {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Usuario WHERE email = :email", Usuario.class)
      .setParameter("email", email)
      .getSingleResult();
  }

  private List<String> nombresDeMueblesDe(Ambiente ambiente) {
    return ambiente.getMuebles().stream().map(Mueble::getNombre).toList();
  }

  private List<Ambiente> obtenerAmbientes() {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Ambiente", Ambiente.class)
      .getResultList();
  }

  private List<CategoriaDeMueble> obtenerCategorias() {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM CategoriaDeMueble", CategoriaDeMueble.class)
      .getResultList();
  }

  private static boolean esPositivo(Double medida) {
    return medida != null && medida > 0;
  }

  private List<Mueble> obtenerMuebles() {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Mueble", Mueble.class)
      .getResultList();
  }
}
