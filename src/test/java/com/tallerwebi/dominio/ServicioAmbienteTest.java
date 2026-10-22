package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioAmbienteTest {

  private RepositorioAmbiente repositorioAmbiente;
  private ServicioAmbiente servicioAmbiente;

  @BeforeEach
  public void init() {
    this.repositorioAmbiente = mock(RepositorioAmbiente.class);
    this.servicioAmbiente = new ServicioAmbienteImpl(this.repositorioAmbiente);
  }

  @Test
  public void dadoQueHayAmbientes_cuandoLosPido_entoncesLosTraigoDelRepositorio() {
    List<Ambiente> ambientes = new ArrayList<>();
    ambientes.add(new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación")));
    when(this.repositorioAmbiente.obtenerTodos()).thenReturn(ambientes);

    List<Ambiente> obtenidos = this.servicioAmbiente.obtenerAmbientes();

    assertThat(obtenidos, contains(ambientes.get(0)));
  }

  @Test
  public void dadoUnEmail_cuandoPidoLosAmbientesDeEseUsuario_entoncesLosTraigoDelRepositorio() {
    List<Ambiente> ambientes = new ArrayList<>();
    ambientes.add(new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación")));
    when(this.repositorioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar")).thenReturn(ambientes);

    List<Ambiente> obtenidos = this.servicioAmbiente.obtenerAmbientesDe("test@unlam.edu.ar");

    assertThat(obtenidos, contains(ambientes.get(0)));
  }

  @Test
  public void dadoUnId_cuandoBuscoElAmbiente_entoncesLoTraigoDelRepositorio() {
    Ambiente ambiente = new Ambiente(4.0, 3.0, new TipoDeAmbiente("Habitación"));
    when(this.repositorioAmbiente.buscarPorId(1L)).thenReturn(ambiente);

    Ambiente obtenido = this.servicioAmbiente.buscarAmbiente(1L);

    assertThat(obtenido, is(ambiente));
  }

  // -------------------------------------------------------------------------
  // Tests HU-04:Aberturas
  // -------------------------------------------------------------------------

  @Test
  public void deberiaAgregarAberturaCuandoElAmbienteExiste() {
    Ambiente ambiente = givenAmbientePersistido(1L);

    whenAgregoPuertaAlAmbiente(ambiente.getId());

    thenElAmbienteTieneUnaAberturaDelTipo(ambiente, TipoAbertura.PUERTA);
  }

  @Test
  public void deberiaLanzarExcepcionCuandoElAmbienteNoExisteAlAgregarAbertura() {
    givenAmbienteInexistente(99L);

    assertThrows(
      RuntimeException.class,
      () -> whenAgregoPuertaAlAmbiente(99L),
      "Cuando el ambiente no existe, debe lanzar una excepción al agregar una abertura"
    );
  }

  @Test
  public void deberiaBuscarElAmbienteEnElRepositorioCuandoSeAgregaUnaAbertura() {
    Ambiente ambiente = givenAmbientePersistido(1L);

    whenAgregoPuertaAlAmbiente(ambiente.getId());

    thenSeInvocoBuscarPorId(ambiente.getId());
  }

  // Given
  private Ambiente givenAmbientePersistido(Long id) {
    Ambiente ambiente = new Ambiente(4.0, 5.0, new TipoDeAmbiente("Living"));
    when(repositorioAmbiente.buscarPorId(org.mockito.ArgumentMatchers.any())).thenReturn(ambiente);
    return ambiente;
  }

  private void givenAmbienteInexistente(Long id) {
    when(repositorioAmbiente.buscarPorId(id)).thenReturn(null);
  }

  // When
  private void whenAgregoPuertaAlAmbiente(Long ambienteId) {
    Abertura abertura = new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 1.0, 0.9, 0.9);
    servicioAmbiente.agregarAbertura(ambienteId, abertura);
  }

  // Then
  private void thenElAmbienteTieneUnaAberturaDelTipo(Ambiente ambiente, TipoAbertura tipoEsperado) {
    assertThat(ambiente.getAberturas(), hasSize(1));
    assertThat(ambiente.getAberturas().get(0).getTipo(), is(tipoEsperado));
  }

  private void thenSeInvocoBuscarPorId(Long id) {
    verify(repositorioAmbiente, times(1)).buscarPorId(id);
  }
}
