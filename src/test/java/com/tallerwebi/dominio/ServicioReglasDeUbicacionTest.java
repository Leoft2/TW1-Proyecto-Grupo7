package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * HU-33A: Reglas de ubicacion segun la categoria del mueble
 * - Dado un mueble con categoria, cuando el algoritmo lo ubica, se aplican las reglas de su categoria.
 * - Dado un mueble sin categoria, se ubica sin reglas especificas y se avisa que asignar una categoria mejora el resultado.
 * - Dada una ubicacion candidata, se descarta si el mueble tapa una puerta, su radio de giro o un obstaculo fijo.
 * - Dada una ubicacion candidata, se descarta si deja un pasillo menor al minimo.
 */
public class ServicioReglasDeUbicacionTest {

  private ServicioReglasDeUbicacion servicioReglasDeUbicacion;

  @BeforeEach
  public void init() {
    servicioReglasDeUbicacion = new ServicioReglasDeUbicacionImpl(new ServicioZonaExclusionImpl());
  }

  @Test
  public void deberiaAceptarUnaUbicacionQueNoTapaNadaNiDejaPasilloEstrecho() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 0.0, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueTapaUnaPuerta() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 0.0, 1.0, 1.0));
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 0.0, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueTapaElRadioDeGiroDeUnaPuerta() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.PUERTA, Muro.SUPERIOR, 0.0, 1.0, 1.5));
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 0.0, 1.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, new ArrayList<MuebleUbicado>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueTapaUnObstaculoFijo() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.RADIADOR, Muro.SUPERIOR, 0.0, 1.0, 0.5)
    );
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 0.0, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueDejaUnPasilloMenorAlMinimoConUnMuro() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 0.3, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, new ArrayList<MuebleUbicado>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnaUbicacionQueDejaUnPasilloMenorAlMinimoConOtroMueble() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    MuebleUbicado ubicado = givenMuebleUbicado("Mesa", 1.0, 1.0, 0.0, 0.0);
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 1.3, 0.0);
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(ubicado);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, ubicados);

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaAceptarUnaUbicacionQueDejaUnPasilloIgualOMayorAlMinimo() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    MuebleUbicado ubicado = givenMuebleUbicado("Mesa", 1.0, 1.0, 0.0, 0.0);
    MuebleUbicado candidato = givenMuebleUbicado("Silla", 1.0, 1.0, 1.6, 0.0);
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(ubicado);

    //When
    boolean valida = whenEvaluoLaUbicacion(candidato, ambiente, ubicados);

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaCumplirLaReglaDeCategoriaCuandoLaCamaEstaOpuestaALaVentana() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 1.0, 1.0, null));
    MuebleUbicado cama = givenCamaUbicada(0.0, 2.0);

    //When
    boolean cumple = servicioReglasDeUbicacion.cumpleReglasDeCategoria(cama, ambiente);

    //Then
    thenLaUbicacionEs(cumple, true);
  }

  @Test
  public void deberiaNoCumplirLaReglaDeCategoriaCuandoLaCamaNoEstaOpuestaALaVentana() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 1.0, 1.0, null));
    MuebleUbicado cama = givenCamaUbicada(0.0, 0.0);

    //When
    boolean cumple = servicioReglasDeUbicacion.cumpleReglasDeCategoria(cama, ambiente);

    //Then
    thenLaUbicacionEs(cumple, false);
  }

  @Test
  public void deberiaUbicarSinReglasUnMuebleSinCategoria() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 4.0);
    ambiente.agregarAbertura(new Abertura(TipoAbertura.VENTANA, Muro.SUPERIOR, 1.0, 1.0, null));
    MuebleUbicado sinCategoria = givenMuebleUbicado("Silla", 1.0, 1.0, 0.0, 0.0);

    //When
    boolean cumple = servicioReglasDeUbicacion.cumpleReglasDeCategoria(sinCategoria, ambiente);

    //Then
    thenLaUbicacionEs(cumple, true);
  }

  @Test
  public void deberiaAvisarQueAsignarUnaCategoriaMejoraElResultadoSiElMuebleNoTieneCategoria() {
    //Given
    Mueble mueble = new Mueble("Silla", 1.0, 1.0);

    //When
    String aviso = servicioReglasDeUbicacion.avisoDeCategoria(mueble);

    //Then
    assertThat(
      aviso,
      is(
        equalTo(
          "El mueble Silla no tiene categoria. Asignarle una categoria mejora el resultado de la distribucion"
        )
      )
    );
  }

  @Test
  public void noDeberiaAvisarNadaSiElMuebleTieneCategoria() {
    //Given
    Mueble mueble = new Mueble("Cama", 1.0, 2.0);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble("Cama doble", 1.4, 1.9));

    //When
    String aviso = servicioReglasDeUbicacion.avisoDeCategoria(mueble);

    //Then
    assertThat(aviso, is(nullValue()));
  }

  // -------------------------------------------------------------------------
  // HU35.1: Piso eliminatorio de despeje frontal del placard
  // -------------------------------------------------------------------------

  @Test
  public void deberiaAceptarUnPlacardCuandoElDespejeFrontalEsExactamente40Centimetros() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.0);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoElDespejeFrontalEsMenorA40Centimetros() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 0.99);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoLaCaraFrontalEstaContraUnaPared() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 0.6);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoLaCaraFrontalChocaConOtroMueble() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 2.0);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);
    MuebleUbicado bloqueo = givenMuebleUbicado("Mesa", 1.0, 0.5, 1.5, 0.8);
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(bloqueo);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, ubicados);

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaAceptarUnPlacardCuandoLaCaraFrontalDejaUnPasilloDe50CentimetrosHastaOtroMueble() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 2.5);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);
    MuebleUbicado otro = givenMuebleUbicado("Mesa", 1.0, 1.0, 1.5, 1.1);
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(otro);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, ubicados);

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoLaCaraFrontalChocaConUnObstaculoFijo() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 2.0);
    ambiente.agregarObstaculo(
      new Obstaculo(TipoDeObstaculo.RADIADOR, Muro.INFERIOR, 1.0, 1.0, 1.2)
    );
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoLaCaraFrontalChocaConLaZonaDeBarridoDeUnaPuerta() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.2);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);
    List<ZonaExclusion> zonas = new ArrayList<>();
    zonas.add(new ZonaExclusion(1.0, 0.8, 1.0, 0.4, TipoAbertura.PUERTA));

    //When
    boolean valida = whenEvaluoLaUbicacionConZonas(placard, ambiente, new ArrayList<>(), zonas);

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaAceptarUnPlacardCuandoLaZonaDeBarridoDeUnaVentanaQuedaAdelante() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.2);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 1.8, 0.6);
    List<ZonaExclusion> zonas = new ArrayList<>();
    zonas.add(new ZonaExclusion(1.0, 0.9, 1.0, 0.3, TipoAbertura.VENTANA));

    //When
    boolean valida = whenEvaluoLaUbicacionConZonas(placard, ambiente, new ArrayList<>(), zonas);

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaAceptarUnMuebleDeOtraCategoriaCuandoElDespejeFrontalEsMenorA40Centimetros() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 0.6);
    Mueble mesa = new Mueble("Mesa", 1.8, 0.6);
    mesa.setCategoriaDeMueble(new CategoriaDeMueble("Mesa", 1.8, 0.6));
    MuebleUbicado ubicado = new MuebleUbicado(mesa, 1.0, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(ubicado, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaDescartarUnPlacardSinMuroDeApoyoCuandoDejaUnPasilloMenorAlMinimo() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.3);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.3, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaAceptarUnPlacardSinMuroDeApoyoCuandoDejaPasillosMayoresAlMinimo() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 2.1);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.7, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, true);
  }

  @Test
  public void deberiaDescartarUnPlacardCuandoUnaCaraNoFrontalDejaUnPasilloMenorA60Centimetros() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.0);
    MuebleUbicado placard = givenPlacardUbicado(0.3, 0.0, 1.8, 0.6);

    //When
    boolean valida = whenEvaluoLaUbicacion(placard, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaElegirElMuroDeApoyoDeMayorContactoCuandoElPlacardTocaDosMuros() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 2.0);
    MuebleUbicado placard = givenPlacardUbicado(0.0, 0.0, 1.8, 0.6);
    List<ZonaExclusion> zonas = new ArrayList<>();
    zonas.add(new ZonaExclusion(0.5, 0.8, 1.0, 0.4, TipoAbertura.PUERTA));

    //When
    boolean valida = whenEvaluoLaUbicacionConZonas(placard, ambiente, new ArrayList<>(), zonas);

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaElegirElPrimerMuroDelEnumCuandoElPlacardTocaDosMurosConElMismoContacto() {
    //Given
    Ambiente ambiente = givenAmbiente(3.0, 3.0);
    MuebleUbicado placard = givenPlacardUbicado(1.0, 0.0, 2.0, 2.0);
    List<ZonaExclusion> zonas = new ArrayList<>();
    zonas.add(new ZonaExclusion(1.3, 2.2, 1.0, 0.4, TipoAbertura.PUERTA));

    //When
    boolean valida = whenEvaluoLaUbicacionConZonas(placard, ambiente, new ArrayList<>(), zonas);

    //Then
    thenLaUbicacionEs(valida, false);
  }

  @Test
  public void deberiaAplicarLaReglaDeDespejeFrontalCuandoLaCategoriaEmpiezaConPlacard() {
    //Given
    Ambiente ambiente = givenAmbiente(4.0, 1.0);
    Mueble placard = new Mueble("Placard 2 puertas", 1.8, 0.6);
    placard.setCategoriaDeMueble(new CategoriaDeMueble("Placard 2 puertas", 1.8, 0.6));
    MuebleUbicado ubicado = new MuebleUbicado(placard, 1.0, 0.0);

    //When
    boolean valida = whenEvaluoLaUbicacion(ubicado, ambiente, new ArrayList<>());

    //Then
    thenLaUbicacionEs(valida, true);
  }

  // -------------------------------------------------------------------------
  // GIVEN
  // -------------------------------------------------------------------------

  private Ambiente givenAmbiente(Double ancho, Double largo) {
    return new Ambiente(ancho, largo);
  }

  private MuebleUbicado givenMuebleUbicado(
    String nombre,
    Double ancho,
    Double largo,
    Double posicionX,
    Double posicionY
  ) {
    return new MuebleUbicado(new Mueble(nombre, ancho, largo), posicionX, posicionY);
  }

  private MuebleUbicado givenPlacardUbicado(
    Double posicionX,
    Double posicionY,
    Double ancho,
    Double largo
  ) {
    Mueble placard = new Mueble("Placard", ancho, largo);
    placard.setCategoriaDeMueble(new CategoriaDeMueble("Placard", ancho, largo));
    return new MuebleUbicado(placard, posicionX, posicionY);
  }

  private MuebleUbicado givenCamaUbicada(Double posicionX, Double posicionY) {
    Mueble cama = new Mueble("Cama", 1.0, 2.0);
    cama.setCategoriaDeMueble(new CategoriaDeMueble("Cama individual", 1.0, 2.0));
    return new MuebleUbicado(cama, posicionX, posicionY);
  }

  // -------------------------------------------------------------------------
  // WHEN
  // -------------------------------------------------------------------------

  private boolean whenEvaluoLaUbicacion(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> ubicados
  ) {
    return servicioReglasDeUbicacion.esUbicacionValida(candidato, ambiente, ubicados);
  }

  private boolean whenEvaluoLaUbicacionConZonas(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> ubicados,
    List<ZonaExclusion> zonas
  ) {
    return servicioReglasDeUbicacion.esUbicacionValida(candidato, ambiente, ubicados, zonas);
  }

  // -------------------------------------------------------------------------
  // THEN
  // -------------------------------------------------------------------------

  private void thenLaUbicacionEs(boolean resultado, boolean esperado) {
    assertThat(resultado, is(equalTo(esperado)));
  }
}
