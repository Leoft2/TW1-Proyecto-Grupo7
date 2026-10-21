package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.dominio.excepcion.PresupuestoNuloException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCatalogoTest {

  private ServicioCatalogo servicioCatalogo;
  private RepositorioCatalogo repositorioCatalogoMock;

  @BeforeEach
  public void init() {
    this.repositorioCatalogoMock = mock(RepositorioCatalogo.class);
    this.servicioCatalogo = new ServicioCatalogoImpl(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnPresupuestoNegativo_cuandoSeFiltraPorPrecio_entoncesLanzaPresupuestoNegativoException() {
    Double precioMaximo = -100.0;

    PresupuestoNegativoException exception = assertThrows(
      PresupuestoNegativoException.class,
      () -> this.servicioCatalogo.ObtenerMueblesConUnPrecioMenorAl(precioMaximo)
    );

    assertNotNull(exception);
  }

  @Test
  public void dadoUnPresupuestoNulo_cuandoSeFiltraPorPrecio_entoncesLanzaPresupuestoNuloException() {
    Double precioMaximo = null;

    PresupuestoNuloException exception = assertThrows(
      PresupuestoNuloException.class,
      () -> this.servicioCatalogo.ObtenerMueblesConUnPrecioMenorAl(precioMaximo)
    );

    assertNotNull(exception);
  }

  @Test
  public void dadoUnPrecioMaximo_cuandoBuscoMueblesConUnPrecioMenorAl_entoncesSeLoPidoAlRepositorio()
    throws PresupuestoNegativoException, PresupuestoNuloException {
    Mueble barato = this.dadoQueTengoUnMueble("Lámpara", 25000.0, Estilo.Retro);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(barato);
    when(this.repositorioCatalogoMock.ObtenerMueblesConPrecioMenorAl(50000.0)).thenReturn(muebles);

    List<Mueble> resultado = this.servicioCatalogo.ObtenerMueblesConUnPrecioMenorAl(50000.0);

    assertThat(resultado, contains(barato));
    verify(this.repositorioCatalogoMock).ObtenerMueblesConPrecioMenorAl(50000.0);
  }

  @Test
  public void dadoUnPresupuestoInvalido_cuandoBuscoMueblesConUnPrecioMenorAl_entoncesNoLePidoNadaAlRepositorio() {
    assertThrows(
      PresupuestoNegativoException.class,
      () -> this.servicioCatalogo.ObtenerMueblesConUnPrecioMenorAl(-1.0)
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnEstilo_cuandoBuscoMueblesDeEstilo_entoncesSeLoPidoAlRepositorio() {
    Mueble retro = this.dadoQueTengoUnMueble("Sillón Retró", 85000.0, Estilo.Retro);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(retro);
    when(this.repositorioCatalogoMock.ObtenerMueblesDeEstilo(Estilo.Retro)).thenReturn(muebles);

    List<Mueble> resultado = this.servicioCatalogo.ObtenerMueblesDeEstilo(Estilo.Retro);

    assertThat(resultado, contains(retro));
    verify(this.repositorioCatalogoMock).ObtenerMueblesDeEstilo(Estilo.Retro);
  }

  @Test
  public void dadoVariosEstilos_cuandoBuscoMueblesDeEstilos_entoncesSeLosPidoAlRepositorio() {
    Mueble retro = this.dadoQueTengoUnMueble("Sillón Retró", 85000.0, Estilo.Retro);
    Mueble minimalista = this.dadoQueTengoUnMueble("Escritorio", 40000.0, Estilo.Minimalista);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(retro);
    muebles.add(minimalista);
    List<Estilo> estilos = Arrays.asList(Estilo.Retro, Estilo.Minimalista);
    when(this.repositorioCatalogoMock.ObtenerMueblesDeEstilos(estilos)).thenReturn(muebles);

    List<Mueble> resultado = this.servicioCatalogo.ObtenerMueblesDeEstilos(estilos);

    assertThat(resultado, containsInAnyOrder(retro, minimalista));
    verify(this.repositorioCatalogoMock).ObtenerMueblesDeEstilos(estilos);
  }

  @Test
  public void dadoUnEstilo_cuandoBuscoElMuebleMasBaratoDeEseEstilo_entoncesSeLoPidoAlRepositorio() {
    Mueble masBarato = this.dadoQueTengoUnMueble("Lámpara Retró", 25000.0, Estilo.Retro);
    when(this.repositorioCatalogoMock.ObtenerMuebleMasBaratoDeEstilo(Estilo.Retro))
      .thenReturn(masBarato);

    Mueble resultado = this.servicioCatalogo.ObtenerMuebleMasBaratoDeCategoria(Estilo.Retro);

    assertThat(resultado, is(masBarato));
    verify(this.repositorioCatalogoMock).ObtenerMuebleMasBaratoDeEstilo(Estilo.Retro);
  }

  @Test
  public void dadoQueNoHayMueblesDeUnEstilo_cuandoBuscoElMuebleMasBaratoDeEseEstilo_entoncesObtengoNull() {
    when(this.repositorioCatalogoMock.ObtenerMuebleMasBaratoDeEstilo(Estilo.Retro))
      .thenReturn(null);

    Mueble resultado = this.servicioCatalogo.ObtenerMuebleMasBaratoDeCategoria(Estilo.Retro);

    assertThat(resultado, is(nullValue()));
  }

  @Test
  public void dadoQueOrdenoPorPrecioAscendente_cuandoBuscoMueblesOrdenados_entoncesPidoLosMueblesDelCatalogo() {
    List<Mueble> muebles = new ArrayList<>();
    when(this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(null, null, null, null, null, null))
      .thenReturn(muebles);

    List<Mueble> resultado = this.servicioCatalogo.ObtenerMueblesOrdenadosPorPrecioAsc();

    assertThat(resultado, is(muebles));
    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(null, null, null, null, null, null);
  }

  @Test
  public void dadoQueOrdenoPorPrecioDescendente_cuandoBuscoMueblesOrdenados_entoncesPidoLosMueblesDelCatalogoOrdenados() {
    List<Mueble> muebles = new ArrayList<>();
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
          null,
          null,
          null,
          null,
          null,
          null
        )
    )
      .thenReturn(muebles);

    List<Mueble> resultado = this.servicioCatalogo.ObtenerMueblesOrdenadosPorPrecioDesc();

    assertThat(resultado, is(muebles));
    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(null, null, null, null, null, null);
  }

  @Test
  public void dadoQueNoSeFiltraPorNada_cuandoBuscoMueblesQueCumplan_entoncesPidoTodoElCatalogo()
    throws PresupuestoNegativoException {
    List<Mueble> muebles = new ArrayList<>();
    when(this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(null, null, null, null, null, null))
      .thenReturn(muebles);

    List<Mueble> resultado =
      this.servicioCatalogo.ObtenerMueblesQueCumplan(null, null, null, null, null);

    assertThat(resultado, is(muebles));
    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(null, null, null, null, null, null);
  }

  @Test
  public void dadoUnTipoDeAmbiente_cuandoBuscoMueblesQueCumplan_entoncesSeLoPidoAlRepositorioPorSuNombre()
    throws PresupuestoNegativoException {
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(
          null,
          50000.0,
          Estilo.Retro,
          "Living",
          false,
          3L
        )
    )
      .thenReturn(new ArrayList<>());

    this.servicioCatalogo.ObtenerMueblesQueCumplan(
        new TipoDeAmbiente("Living"),
        Estilo.Retro,
        50000.0,
        false,
        3L
      );

    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(null, 50000.0, Estilo.Retro, "Living", false, 3L);
  }

  @Test
  public void dadoVariosCriterios_cuandoBuscoMueblesFiltrados_entoncesSeLosPidoAlRepositorioConTodos()
    throws PresupuestoNegativoException {
    List<Mueble> muebles = new ArrayList<>();
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(
          50000.0,
          100000.0,
          Estilo.Retro,
          "Living",
          true,
          3L
        )
    )
      .thenReturn(muebles);

    List<Mueble> resultado =
      this.servicioCatalogo.ObtenerMueblesFiltrados(
          50000.0,
          100000.0,
          Estilo.Retro,
          new TipoDeAmbiente("Living"),
          null,
          true,
          3L
        );

    assertThat(resultado, is(muebles));
    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(50000.0, 100000.0, Estilo.Retro, "Living", true, 3L);
  }

  @Test
  public void dadoQueOrdenoPorPrecioDescendente_cuandoBuscoMueblesFiltrados_entoncesPidoLosMueblesOrdenados()
    throws PresupuestoNegativoException {
    List<Mueble> muebles = new ArrayList<>();
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
          null,
          null,
          null,
          null,
          null,
          null
        )
    )
      .thenReturn(muebles);

    List<Mueble> resultado =
      this.servicioCatalogo.ObtenerMueblesFiltrados(
          null,
          null,
          null,
          null,
          OrdenDeMuebles.PRECIO_DESC,
          null,
          null
        );

    assertThat(resultado, is(muebles));
    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(null, null, null, null, null, null);
  }

  @Test
  public void dadoUnPrecioMinimoNegativo_cuandoBuscoMueblesFiltrados_entoncesLanzaPresupuestoNegativoException() {
    PresupuestoNegativoException exception = assertThrows(
      PresupuestoNegativoException.class,
      () -> this.servicioCatalogo.ObtenerMueblesFiltrados(-1.0, null, null, null, null, null, null)
    );

    assertNotNull(exception);
  }

  @Test
  public void dadoUnPrecioMaximoNegativo_cuandoBuscoMueblesQueCumplan_entoncesLanzaPresupuestoNegativoException() {
    PresupuestoNegativoException exception = assertThrows(
      PresupuestoNegativoException.class,
      () -> this.servicioCatalogo.ObtenerMueblesQueCumplan(null, Estilo.Retro, -1.0, null, null)
    );

    assertNotNull(exception);
  }

  @Test
  public void dadoUnMuebleEnElCatalogo_cuandoLoBuscoPorId_entoncesDevuelvoEseMueble()
    throws MuebleNoEncontrado {
    Mueble retro = this.dadoQueTengoUnMueble("Sillón Retró", 85000.0, Estilo.Retro);
    retro.setId(7);
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(retro);

    Mueble mueble = this.servicioCatalogo.ObtenerMueblePorId(7);

    assertThat(mueble, is(notNullValue()));
    assertThat(mueble.getNombre(), is("Sillón Retró"));
  }

  @Test
  public void dadoQueNoHayNingunMuebleConEseId_cuandoLoBuscoPorId_entoncesLanzaUnaExceptionMuebleNoEncontrado() {
    MuebleNoEncontrado exception = assertThrows(
      MuebleNoEncontrado.class,
      () -> this.servicioCatalogo.ObtenerMueblePorId(999)
    );

    assertNotNull(exception);
  }

  @Test
  public void dadoUnMuebleValido_cuandoLoRegistroEnElCatalogo_entoncesSeGuardaMarcadoComoDeCatalogo() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();

    this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);

    assertTrue(mueble.getEnCatalogo());
    assertThat(mueble.getAncho(), is(0.80));
    assertThat(mueble.getLargo(), is(0.85));
    verify(this.repositorioCatalogoMock).guardarMueble(mueble);
  }

  @Test
  public void dadoUnMuebleSinNombre_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setNombre("   ");

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleConPrecioCero_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setPrecio(0.0);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleSinEstilo_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setEstilo(null);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleSinCategoria_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setCategoriaDeMueble(null);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleSinAncho_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setAncho(null);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleSinLargo_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setLargo(0.0);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleSinTiposDeAmbiente_cuandoLoRegistroEnElCatalogo_entoncesLanzaValidacionException() {
    Mueble mueble = this.dadoQueTengoUnMuebleParaRegistrar();
    mueble.setTiposDeAmbiente(new ArrayList<>());

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.RegistrarMuebleDelCatalogo(mueble);
      }
    );

    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnNombreDeTipoDeAmbiente_cuandoLoBusco_entoncesLoPidoAlRepositorio() {
    TipoDeAmbiente living = new TipoDeAmbiente("Living");
    when(this.repositorioCatalogoMock.buscarTipoDeAmbientePorNombre("Living")).thenReturn(living);

    TipoDeAmbiente tipoObtenido = this.servicioCatalogo.buscarTipoDeAmbientePorNombre("Living");

    assertThat(tipoObtenido, is(living));
  }

  @Test
  public void dadoUnNombreDeTipoDeAmbienteInexistente_cuandoLoBusco_entoncesSeCreaEnElRepositorio() {
    TipoDeAmbiente tipoObtenido = this.servicioCatalogo.buscarTipoDeAmbientePorNombre("Garaje");

    assertThat(tipoObtenido, is(notNullValue()));
    assertThat(tipoObtenido.getNombre(), is("Garaje"));
    verify(this.repositorioCatalogoMock).guardarTipoDeAmbiente(tipoObtenido);
  }

  @Test
  public void dadoUnNombreDeTipoDeAmbienteVacio_cuandoLoObtengo_entoncesNoPidoNadaAlRepositorio() {
    TipoDeAmbiente tipoObtenido = this.servicioCatalogo.buscarTipoDeAmbientePorNombre("  ");

    assertThat(tipoObtenido, is(nullValue()));
    verifyNoInteractions(this.repositorioCatalogoMock);
  }

  @Test
  public void dadoUnMuebleExistente_cuandoLoModificoEnElCatalogo_entoncesSeGuardanLosCambios()
    throws MuebleNoEncontrado {
    Mueble existente = new Mueble();
    existente.setId(7);
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(existente);
    Mueble datos = this.dadoQueTengoUnMuebleParaRegistrar();

    this.servicioCatalogo.modificarMuebleDelCatalogo(7, datos);

    assertThat(existente.getNombre(), is("Sillón Retró"));
    assertThat(existente.getPrecio(), is(85000.0));
    assertThat(existente.getAncho(), is(0.80));
    assertThat(existente.getLargo(), is(0.85));
    assertThat(existente.getTiposDeAmbiente().size(), is(1));
    verify(this.repositorioCatalogoMock).guardarMueble(existente);
  }

  @Test
  public void dadoUnMuebleExistente_cuandoLoModificoEnElCatalogo_conOtroMaterial_entoncesSeGuardaElMaterialNuevo()
    throws MuebleNoEncontrado {
    Mueble existente = new Mueble();
    existente.setId(7);
    existente.setMaterial(new Material("Madera", true));
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(existente);
    Mueble datos = this.dadoQueTengoUnMuebleParaRegistrar();
    datos.setMaterial(new Material("Bambú", true));

    this.servicioCatalogo.modificarMuebleDelCatalogo(7, datos);

    assertThat(existente.getMaterial(), is(new Material("Bambú", true)));
  }

  @Test
  public void dadoUnMuebleExistente_cuandoLoModificoEnElCatalogo_sinMaterial_entoncesSeQuitaElMaterial()
    throws MuebleNoEncontrado {
    Mueble existente = new Mueble();
    existente.setId(7);
    existente.setMaterial(new Material("Madera", true));
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(existente);
    Mueble datos = this.dadoQueTengoUnMuebleParaRegistrar();
    datos.setMaterial(null);

    this.servicioCatalogo.modificarMuebleDelCatalogo(7, datos);

    assertThat(existente.getMaterial(), is(nullValue()));
  }

  @Test
  public void dadoUnMuebleInexistente_cuandoLoModificoEnElCatalogo_entoncesLanzaMuebleNoEncontrado() {
    Mueble datos = this.dadoQueTengoUnMuebleParaRegistrar();

    assertThrows(
      MuebleNoEncontrado.class,
      () -> {
        this.servicioCatalogo.modificarMuebleDelCatalogo(99, datos);
      }
    );

    verify(this.repositorioCatalogoMock, never()).guardarMueble(datos);
  }

  @Test
  public void dadoUnMuebleConDatosInvalidos_cuandoLoModificoEnElCatalogo_entoncesNoSeGuarda() {
    Mueble existente = new Mueble();
    existente.setId(7);
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(existente);
    Mueble datos = this.dadoQueTengoUnMuebleParaRegistrar();
    datos.setAncho(0.0);

    assertThrows(
      ValidacionException.class,
      () -> {
        this.servicioCatalogo.modificarMuebleDelCatalogo(7, datos);
      }
    );

    verify(this.repositorioCatalogoMock, never()).guardarMueble(datos);
  }

  @Test
  public void dadoUnMuebleExistente_cuandoLoEliminoDelCatalogo_entoncesSeElimina()
    throws MuebleNoEncontrado {
    Mueble existente = new Mueble();
    existente.setId(7);
    when(this.repositorioCatalogoMock.ObtenerMueblePorId(7)).thenReturn(existente);

    this.servicioCatalogo.eliminarMuebleDelCatalogo(7);

    verify(this.repositorioCatalogoMock).eliminar(7);
  }

  @Test
  public void dadoUnMuebleInexistente_cuandoLoEliminoDelCatalogo_entoncesLanzaMuebleNoEncontrado() {
    assertThrows(
      MuebleNoEncontrado.class,
      () -> {
        this.servicioCatalogo.eliminarMuebleDelCatalogo(99);
      }
    );

    verify(this.repositorioCatalogoMock, never()).eliminar(99);
  }

  @Test
  public void dadoQueFiltroPorUnMaterial_cuandoBuscoMueblesQueCumplan_entoncesSeLoPidoAlRepositorio()
    throws PresupuestoNegativoException {
    when(this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(null, null, null, null, null, 3L))
      .thenReturn(new ArrayList<>());

    this.servicioCatalogo.ObtenerMueblesQueCumplan(null, null, null, null, 3L);

    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(null, null, null, null, null, 3L);
  }

  @Test
  public void dadoQueSoloFiltroPorSostenibles_cuandoBuscoMueblesFiltrados_entoncesSeLoPidoAlRepositorio()
    throws PresupuestoNegativoException {
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(
          50000.0,
          90000.0,
          Estilo.Retro,
          null,
          true,
          null
        )
    )
      .thenReturn(new ArrayList<>());

    this.servicioCatalogo.ObtenerMueblesFiltrados(
        50000.0,
        90000.0,
        Estilo.Retro,
        null,
        null,
        true,
        null
      );

    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(50000.0, 90000.0, Estilo.Retro, null, true, null);
  }

  @Test
  public void dadoQueNoFiltroPorSostenibles_cuandoBuscoMueblesFiltrados_entoncesNoPidoElFiltroDeSostenibilidad()
    throws PresupuestoNegativoException {
    when(
      this.repositorioCatalogoMock.ObtenerMueblesDelCatalogo(
          null,
          null,
          Estilo.Retro,
          null,
          false,
          null
        )
    )
      .thenReturn(new ArrayList<>());

    this.servicioCatalogo.ObtenerMueblesFiltrados(
        null,
        null,
        Estilo.Retro,
        null,
        null,
        false,
        null
      );

    verify(this.repositorioCatalogoMock)
      .ObtenerMueblesDelCatalogo(null, null, Estilo.Retro, null, false, null);
    verify(this.repositorioCatalogoMock, never())
      .ObtenerMueblesDelCatalogo(any(), any(), any(), any(), eq(true), any());
  }

  private Mueble dadoQueTengoUnMuebleParaRegistrar() {
    Mueble mueble = new Mueble();
    mueble.setNombre("Sillón Retró");
    mueble.setDescripcion("Sillón de voluteadas tapizado en terciopelo.");
    mueble.setPrecio(85000.0);
    mueble.setEstilo(Estilo.Retro);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble("Sillón", 0.80, 0.85));
    mueble.setAncho(0.80);
    mueble.setLargo(0.85);
    List<TipoDeAmbiente> tiposDeAmbiente = new ArrayList<>();
    tiposDeAmbiente.add(new TipoDeAmbiente("Living"));
    mueble.setTiposDeAmbiente(tiposDeAmbiente);
    return mueble;
  }

  private Mueble dadoQueTengoUnMueble(String nombre, double precio, Estilo estilo) {
    Mueble mueble = new Mueble();
    mueble.setNombre(nombre);
    mueble.setPrecio(precio);
    mueble.setEstilo(estilo);
    return mueble;
  }

  @Test
  public void dadoUnMuebleRegistrado_cuandoLoGuardoEnElCatalogo_entoncesSeGuarda() {
    Mueble mueble = this.dadoQueTengoUnMueble("Sillón Retró", 85000.0, Estilo.Retro);

    this.servicioCatalogo.RegistrarMueble(mueble);

    verify(this.repositorioCatalogoMock).guardarMueble(mueble);
    assertEquals("Sillón Retró", mueble.getNombre());
  }
}
