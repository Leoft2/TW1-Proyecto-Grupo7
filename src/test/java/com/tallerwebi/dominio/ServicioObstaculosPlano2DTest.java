package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Criterios de aceptación:
 * Dado que registré un obstáculo fijo en un muro, cuando se calcula la distribución,
 * entonces esa zona queda marcada como no disponible.
 * -
 * Dado que un mueble colisiona con un obstáculo fijo, cuando se muestra el plano,
 * entonces el sistema resalta visualmente el conflicto.
 */
public class ServicioObstaculosPlano2DTest {

  private static final double TOLERANCIA = 1e-9;

  private final ServicioPlano2D servicioPlano2D = new ServicioPlano2DImpl(
    new ServicioZonaExclusionImpl()
  );
  private final Ambiente ambiente = new Ambiente(4.0, 3.0);
  private final Mueble rack = new Mueble("Rack", 1.0, 0.5);

  @Test
  public void deberiaRechazarElPlanoCuandoUnObstaculoSeSaleDelMuro() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.DERECHO, 2.5, 0.8));

    assertThrows(ValidacionException.class, () -> whenGeneroElPlano(ambiente, Arrays.asList(rack)));
  }

  @Test
  public void deberiaUbicarElMuebleDespuesDelObstaculoCuandoElObstaculoEstaEnElMuroSuperior() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 0.8));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 0.8, 0.0);
  }

  @Test
  public void deberiaPasarAlMuroDerechoCuandoUnObstaculoOcupaTodoElMuroSuperior() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 4.0));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 3.0, 0.5);
  }

  @Test
  public void deberiaUbicarElMuebleDebajoDelObstaculoCuandoElObstaculoEstaEnElMuroDerecho() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.DERECHO, 0.5, 0.8));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 3.0, 1.3);
  }

  @Test
  public void deberiaUbicarElMuebleALaIzquierdaDelObstaculoCuandoElObstaculoEstaEnElMuroInferior() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.DERECHO, 0.0, 3.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.INFERIOR, 2.5, 0.8));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 1.5, 2.5);
  }

  @Test
  public void deberiaUbicarElMuebleArribaDelObstaculoCuandoElObstaculoEstaEnElMuroIzquierdo() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.DERECHO, 0.0, 3.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.INFERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.IZQUIERDO, 1.8, 0.8));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 0.0, 1.3);
  }

  @Test
  public void deberiaUbicarElMuebleEnConflictoCuandoLosObstaculosNoDejanLugarLibre() {
    givenQueTodosLosMurosEstanCubiertos();

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaEn(plano, 0.0, 0.0);
    thenElMuebleQuedaEnConflicto(plano);
  }

  @Test
  public void deberiaUbicarElMuebleSinConflictoCuandoNoPisaNingunObstaculo() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 0.8));

    Plano plano = whenGeneroElPlano(ambiente, Arrays.asList(rack));

    thenElMuebleQuedaSinConflicto(plano);
  }

  @Test
  public void deberiaUbicarElMuebleJustoEntreElObstaculoYLaEsquinaAunqueHayaErrorDeRedondeo() {
    Ambiente ambienteDeTresTreinta = givenUnAmbiente(3.3, 3.0);
    ambienteDeTresTreinta.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 2.2));
    Mueble mesa = givenUnMueble("Mesa", 1.1, 0.5);

    Plano plano = whenGeneroElPlano(ambienteDeTresTreinta, Arrays.asList(mesa));

    thenElMuebleQuedaEn(plano, 2.2, 0.0);
  }

  private Ambiente givenUnAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private Mueble givenUnMueble(String nombre, Double ancho, Double largo) {
    return new Mueble(nombre, ancho, largo);
  }

  private Obstaculo givenUnObstaculoEn(Muro muro, Double posicion, Double ancho) {
    return new Obstaculo(TipoDeObstaculo.RADIADOR, muro, posicion, ancho, 0.15);
  }

  private void givenQueTodosLosMurosEstanCubiertos() {
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.SUPERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.DERECHO, 0.0, 3.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.INFERIOR, 0.0, 4.0));
    ambiente.agregarObstaculo(givenUnObstaculoEn(Muro.IZQUIERDO, 0.0, 3.0));
  }

  private Plano whenGeneroElPlano(Ambiente ambiente, List<Mueble> muebles) {
    return servicioPlano2D.generarPlano(ambiente, muebles);
  }

  private void thenElMuebleQuedaEn(Plano plano, double posicionX, double posicionY) {
    MuebleUbicado ubicado = plano.getMuebles().get(0);
    assertEquals(posicionX, ubicado.getPosicionX(), TOLERANCIA);
    assertEquals(posicionY, ubicado.getPosicionY(), TOLERANCIA);
  }

  private void thenElMuebleQuedaEnConflicto(Plano plano) {
    assertTrue(plano.getMuebles().get(0).isEnConflicto());
  }

  private void thenElMuebleQuedaSinConflicto(Plano plano) {
    assertFalse(plano.getMuebles().get(0).isEnConflicto());
  }
}
