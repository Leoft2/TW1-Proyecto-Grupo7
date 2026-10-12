package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Criterios de aceptación:
 * Dado que cargué ancho y largo del ambiente, cuando confirmo los datos,
 * entonces el sistema muestra un plano 2D con los muebles principales ubicados sobre los muros perimetrales.
 * -
 * Dado que el ambiente no tiene muebles cargados, cuando se genera la distribución,
 * entonces el sistema muestra un mensaje indicando que no hay mobiliario para distribuir.
 * -
 * Dado un mueble cuyas dimensiones exceden el espacio disponible en el ambiente, cuando se genera la distribución,
 * entonces el sistema muestra un mensaje indicando que ese mueble no entra y lo excluye de la propuesta.
 * -
 * Dado más de un mueble del mismo tipo (ej. dos sillones), cuando se calcula la ubicación,
 * entonces el sistema define un criterio de orden de colocación (ej. por tamaño o por orden de carga) y lo aplica de forma consistente.
 */

public class ServicioPlano2DTest {

  private final ServicioPlano2D servicioPlano2D = new ServicioPlano2DImpl();

  // -------------------------------------------------------------------------
  // Validacion de las dimensiones del ambiente
  // -------------------------------------------------------------------------

  @Test
  public void deberiaLanzarValidacionExceptionCuandoElAnchoDelAmbienteEsNegativo() {
    Ambiente ambienteConAnchoNegativo = givenAmbiente(-5.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    assertThrows(
      ValidacionException.class,
      () -> servicioPlano2D.generarPlano(ambienteConAnchoNegativo, muebles)
    );
  }

  @Test
  public void deberiaLanzarValidacionExceptionCuandoElAnchoDelAmbienteEsCero() {
    Ambiente ambienteSinAncho = givenAmbiente(0.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    assertThrows(
      ValidacionException.class,
      () -> servicioPlano2D.generarPlano(ambienteSinAncho, muebles)
    );
  }

  @Test
  public void deberiaLanzarValidacionExceptionCuandoElAnchoDelAmbienteEsNulo() {
    Ambiente ambienteSinAncho = givenAmbiente(null, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    assertThrows(
      ValidacionException.class,
      () -> servicioPlano2D.generarPlano(ambienteSinAncho, muebles)
    );
  }

  // -------------------------------------------------------------------------
  // Generacion del plano
  // -------------------------------------------------------------------------

  @Test
  public void deberiaConservarElAmbienteDelPlanoCuandoLasDimensionesYMueblesSonValidos() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);

    thenElAmbienteDelPlanoEs(planoGenerado, ambiente);
  }

  @Test
  public void deberiaUbicarTodosLosMueblesValidosEnElPlanoCuandoLasDimensionesYMueblesSonValidos() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);

    thenLaCantidadDeMueblesDelPlanoEs(planoGenerado, muebles.size());
  }

  @Test
  public void deberiaRetornarMismoResultadoCuandoSeEjecutaDosVecesConMismoInput() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    Plano primerPlano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);
    Plano segundoPlano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);

    thenLasPosicionesDeAmbosPlanosSonIguales(primerPlano, segundoPlano);
  }

  @Test
  public void deberiaUbicarLosMueblesDentroDelPerimetroCuandoDimensionesYMueblesSonValidos() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> muebles = givenMueblesPorDefecto();

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);
    List<Boolean> resultadosDePerimetro = whenVerificoSiCadaMuebleEstaEnElPerimetro(planoGenerado);

    thenTodosLosMueblesEstanDentroDelPerimetro(resultadosDePerimetro);
  }

  // -------------------------------------------------------------------------
  // Perimetro
  // -------------------------------------------------------------------------

  @Test
  public void deberiaConsiderarQueUnMuebleEstaDentroDelPerimetroCuandoElMuebleCabeEnElPerimetro() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    MuebleUbicado muebleUbicado = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.0, 2.0);

    Boolean muebleEstaDentroDelPerimetro = whenVerificoSiElMuebleEstaEnElPerimetro(
      muebleUbicado,
      ambiente
    );

    thenElResultadoDeEstarDentroDelPerimetroEs(true, muebleEstaDentroDelPerimetro);
  }

  @Test
  public void deberiaConsiderarQueNoEstaDentroDelPerimetroCuandoLaCoordenadaXEsNegativa() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    MuebleUbicado muebleUbicado = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), -0.5, 0.0);

    Boolean muebleEstaDentroDelPerimetro = whenVerificoSiElMuebleEstaEnElPerimetro(
      muebleUbicado,
      ambiente
    );

    thenElResultadoDeEstarDentroDelPerimetroEs(false, muebleEstaDentroDelPerimetro);
  }

  @Test
  public void deberiaConsiderarQueUnMuebleNoEstaDentroDelPerimetroCuandoElMuebleSeSaleDelPerimetro() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    MuebleUbicado muebleUbicado = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 3.5, 0.0);

    Boolean muebleEstaDentroDelPerimetro = whenVerificoSiElMuebleEstaEnElPerimetro(
      muebleUbicado,
      ambiente
    );

    thenElResultadoDeEstarDentroDelPerimetroEs(false, muebleEstaDentroDelPerimetro);
  }

  @Test
  public void deberiaConsiderarQueNoEstaDentroDelPerimetroCuandoElMuebleSeSaleDelPerimetroEnElEjeY() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    // 2.5 + 1.0 = 3.5 > 3.0 (largo)
    MuebleUbicado muebleUbicado = givenMuebleUbicado(givenMueble("Mesa", 1.0, 1.0), 0.0, 2.5);

    Boolean muebleEstaDentroDelPerimetro = whenVerificoSiElMuebleEstaEnElPerimetro(
      muebleUbicado,
      ambiente
    );

    thenElResultadoDeEstarDentroDelPerimetroEs(false, muebleEstaDentroDelPerimetro);
  }

  // -------------------------------------------------------------------------
  // Recorrido de los muros
  // -------------------------------------------------------------------------

  @Test
  public void deberiaUbicarElMuebleEnElMuroDerechoCuandoElMuroSuperiorEstaOcupado() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    // el muro superior del ambiente mide 4.0, asi que los primeros 4 sillones de 1.0 lo llenan
    List<Mueble> sillones = givenSillones(5);

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, sillones);

    thenElUltimoMuebleEstaEnLaPosicion(new ArrayList<>(Arrays.asList(3.0, 0.5)), planoGenerado);
  }

  @Test
  public void deberiaUbicarElMuebleEnElMuroInferiorCuandoElMuroDerechoEstaOcupado() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    // el muro superior consume 4 sillones y el derecho 4 mas, porque cada sillon
    // avanza 0.5 de largo entre Y 0.5 y Y 2.5 con la esquina superior libre, asi que
    // el noveno sillon es el primero que toca el muro inferior
    List<Mueble> sillones = givenSillones(9);

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, sillones);

    thenElUltimoMuebleEstaEnLaPosicion(new ArrayList<>(Arrays.asList(2.0, 2.5)), planoGenerado);
  }

  @Test
  public void deberiaUbicarElMuebleEnElMuroIzquierdoCuandoLosMurosPreviosEstanOcupados() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    // el muro superior consume 4 sillones, el derecho 4 mas y el inferior 3, asi que
    // el duodecimo es el primero que toca el muro izquierdo, que se recorre de abajo
    // hacia arriba arrancando con la esquina inferior libre
    List<Mueble> sillones = givenSillones(12);

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, sillones);

    thenElUltimoMuebleEstaEnLaPosicion(new ArrayList<>(Arrays.asList(0.0, 2.0)), planoGenerado);
  }

  // -------------------------------------------------------------------------
  // Criterio de orden: mismo tipo de mueble
  // -------------------------------------------------------------------------

  @Test
  public void deberiaAsignarPosicionesDistintasCuandoHayMueblesDelMismoTipo() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    Mueble sillonChico = givenMueble("Sillon", 0.5, 0.5);
    Mueble sillonGrande = givenMueble("Sillon", 1.0, 1.0);
    List<Mueble> sillones = new ArrayList<>(Arrays.asList(sillonChico, sillonGrande));

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, sillones);

    thenLosMueblesTienenPosicionesDistintas(planoGenerado);
  }

  @Test
  public void deberiaUbicarElMuebleMasGrandePrimeroCuandoHayMueblesDelMismoTipo() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    Mueble sillonChico = givenMueble("Sillon", 0.5, 0.5);
    Mueble sillonGrande = givenMueble("Sillon", 1.0, 1.0);
    // el chico se carga primero, a proposito, para que el orden de entrada no alcance
    List<Mueble> sillones = new ArrayList<>(Arrays.asList(sillonChico, sillonGrande));

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, sillones);

    thenElPrimerMuebleDelPlanoEs(sillonGrande, planoGenerado);
  }

  // -------------------------------------------------------------------------
  // Ambiente sin mobiliario
  // -------------------------------------------------------------------------

  @Test
  public void deberiaRetornarMensajeSinMobiliarioCuandoNoHayMueblesCargadosEnElPlano() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> mueblesVacios = givenMueblesVacios();

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(
      ambiente,
      mueblesVacios
    );

    thenDevuelveMensajeSinMobiliario(planoGenerado);
  }

  @Test
  public void deberiaRetornarListaVaciaCuandoNoHayMueblesCargados() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    List<Mueble> mueblesVacios = givenMueblesVacios();

    Plano planoGenerado = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(
      ambiente,
      mueblesVacios
    );

    thenDevuelveListaVaciaDeMuebles(planoGenerado);
  }

  // -------------------------------------------------------------------------
  // Mueble que excede el espacio disponible
  // -------------------------------------------------------------------------

  @Test
  public void deberiaSeguirDesdeDondeQuedoElRecorridoCuandoUnMuebleSeExcluye() {
    Ambiente ambiente = givenAmbiente(4.0, 3.0);
    Mueble placard = givenMueble("Placard", 1.5, 3.0);
    Mueble cama = givenMueble("Cama", 3.0, 1.5);
    Mueble mesa = givenMueble("Mesa", 0.5, 0.5);
    List<Mueble> muebles = new ArrayList<>(Arrays.asList(placard, cama, mesa));

    Plano plano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);

    thenElUltimoMuebleEstaEnLaPosicion(new ArrayList<>(Arrays.asList(1.5, 0.0)), plano);
  }

  @Test
  public void deberiaExcluirMueblesDelResultadoCuandoElMuebleExcedeElEspacioDisponible() {
    Ambiente ambiente = givenAmbiente(2.0, 2.0);
    List<Mueble> muebles = new ArrayList<>(Arrays.asList(givenMueble("Sofa", 3.0, 2.0)));

    Plano plano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambiente, muebles);

    thenExcluyeMuebleDelResultado(plano);
  }

  @Test
  public void deberiaRegistrarMuebleComoExcluidoCuandoElMuebleExcedeElEspacioDisponible() {
    Ambiente ambientePequeno = givenAmbiente(2.0, 2.0);
    Mueble muebleGrande = givenMueble("Sofa", 3.0, 2.0);

    Plano plano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(
      ambientePequeno,
      new ArrayList<>(Arrays.asList(muebleGrande))
    );

    thenElMuebleEstaEnLaListaDeExcluidos(muebleGrande, plano);
  }

  @Test
  public void deberiaIncluirMotivoDeExclusionCuandoElMuebleExcedeElEspacioDisponible() {
    Ambiente ambientePequeno = givenAmbiente(2.0, 2.0);
    List<Mueble> muebles = new ArrayList<>(Arrays.asList(givenMueble("Sofa", 3.0, 2.0)));

    Plano plano = whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(ambientePequeno, muebles);

    thenElMotivoDeExclusionDiceQueElMuebleNoEntra(plano);
  }

  // -------------------------------------------------------------------------
  // Given (preparacion)
  // -------------------------------------------------------------------------

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private Mueble givenMueble(String nombre, Double ancho, Double largo) {
    return new Mueble(nombre, ancho, largo);
  }

  private MuebleUbicado givenMuebleUbicado(Mueble mueble, Double posicionX, Double posicionY) {
    return new MuebleUbicado(mueble, posicionX, posicionY);
  }

  private List<Mueble> givenMueblesPorDefecto() {
    return new ArrayList<>(
      Arrays.asList(givenMueble("Silla", 0.5, 0.5), givenMueble("Mesa", 1.0, 1.0))
    );
  }

  private List<Mueble> givenMueblesVacios() {
    return new ArrayList<>();
  }

  private List<Mueble> givenSillones(int cantidad) {
    List<Mueble> sillones = new ArrayList<>();
    for (int i = 0; i < cantidad; i++) {
      sillones.add(givenMueble("Sillon", 1.0, 0.5));
    }
    return sillones;
  }

  // -------------------------------------------------------------------------
  // When (ejecucion)
  // -------------------------------------------------------------------------

  private Plano whenCargoDimensionesYMueblesDelAmbienteGeneraUnPlano(
    Ambiente ambiente,
    List<Mueble> muebles
  ) {
    return servicioPlano2D.generarPlano(ambiente, muebles);
  }

  private Boolean whenVerificoSiElMuebleEstaEnElPerimetro(
    MuebleUbicado muebleUbicado,
    Ambiente ambiente
  ) {
    return servicioPlano2D.estaDentroDelPerimetro(muebleUbicado, ambiente);
  }

  private List<Boolean> whenVerificoSiCadaMuebleEstaEnElPerimetro(Plano plano) {
    List<Boolean> resultados = new ArrayList<>();
    for (MuebleUbicado muebleUbicado : plano.getMuebles()) {
      resultados.add(servicioPlano2D.estaDentroDelPerimetro(muebleUbicado, plano.getAmbiente()));
    }
    return resultados;
  }

  // -------------------------------------------------------------------------
  // Then (validacion)
  // -------------------------------------------------------------------------

  private void thenElAmbienteDelPlanoEs(Plano plano, Ambiente ambienteEsperado) {
    assertThat(plano.getAmbiente(), is(equalTo(ambienteEsperado)));
  }

  private void thenLaCantidadDeMueblesDelPlanoEs(Plano plano, int cantidadEsperada) {
    assertThat(plano.getMuebles().size(), is(equalTo(cantidadEsperada)));
  }

  private void thenElResultadoDeEstarDentroDelPerimetroEs(
    Boolean resultadoEsperado,
    Boolean elMuebleEstaDentroDelPerimetro
  ) {
    assertThat(elMuebleEstaDentroDelPerimetro, is(equalTo(resultadoEsperado)));
  }

  private void thenLasPosicionesDeAmbosPlanosSonIguales(Plano primerPlano, Plano segundoPlano) {
    assertThat(posicionesDe(segundoPlano), is(equalTo(posicionesDe(primerPlano))));
  }

  private List<List<Double>> posicionesDe(Plano plano) {
    return plano
      .getMuebles()
      .stream()
      .<List<Double>>map(muebleUbicado ->
        new ArrayList<>(Arrays.asList(muebleUbicado.getPosicionX(), muebleUbicado.getPosicionY()))
      )
      .toList();
  }

  private void thenTodosLosMueblesEstanDentroDelPerimetro(List<Boolean> resultadosDePerimetro) {
    assertThat(resultadosDePerimetro, is(equalTo(new ArrayList<>(Arrays.asList(true, true)))));
  }

  private void thenElUltimoMuebleEstaEnLaPosicion(List<Double> posicionEsperada, Plano plano) {
    List<MuebleUbicado> muebles = plano.getMuebles();
    MuebleUbicado ultimo = muebles.get(muebles.size() - 1);
    assertThat(
      new ArrayList<>(Arrays.asList(ultimo.getPosicionX(), ultimo.getPosicionY())),
      is(equalTo(posicionEsperada))
    );
  }

  private void thenElPrimerMuebleDelPlanoEs(Mueble muebleEsperado, Plano plano) {
    assertThat(plano.getMuebles().get(0).getMueble(), is(equalTo(muebleEsperado)));
  }

  private void thenLosMueblesTienenPosicionesDistintas(Plano plano) {
    MuebleUbicado primero = plano.getMuebles().get(0);
    MuebleUbicado segundo = plano.getMuebles().get(1);
    assertThat(
      new ArrayList<>(Arrays.asList(segundo.getPosicionX(), segundo.getPosicionY())),
      not(equalTo(new ArrayList<>(Arrays.asList(primero.getPosicionX(), primero.getPosicionY()))))
    );
  }

  private void thenDevuelveMensajeSinMobiliario(Plano planoGenerado) {
    assertThat(planoGenerado.getMensaje(), is(equalTo("No hay mobiliario para distribuir")));
  }

  private void thenDevuelveListaVaciaDeMuebles(Plano planoGenerado) {
    assertThat(planoGenerado.getMuebles().isEmpty(), is(true));
  }

  private void thenElMotivoDeExclusionDiceQueElMuebleNoEntra(Plano plano) {
    assertThat(
      plano.getMotivosDeExclusion(),
      is(equalTo(new ArrayList<>(Arrays.asList("El mueble Sofa no entra en el ambiente"))))
    );
  }

  private void thenElMuebleEstaEnLaListaDeExcluidos(Mueble mueble, Plano plano) {
    assertThat(plano.getMueblesExcluidos(), is(equalTo(new ArrayList<>(Arrays.asList(mueble)))));
  }

  private void thenExcluyeMuebleDelResultado(Plano plano) {
    assertThat(plano.getMuebles().isEmpty(), is(true));
  }
}
