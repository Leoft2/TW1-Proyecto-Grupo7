package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.OpcionDeDistribucion;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import com.tallerwebi.dominio.ServicioRankingDeDistribuciones;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/*
 * HU-33B: Puntaje de ubicacion y ranking de las 3 mejores opciones
 * - Dado el ranking, cuando se confirma una opcion, queda activa y guardada en el historial.
 */
public class ControladorRankingDeDistribucionesTest {

  private ServicioRankingDeDistribuciones servicioRankingDeDistribuciones;
  private ServicioEdicionInventarioMuebles servicioEdicionInventarioMuebles;
  private ServicioPersistenciaPlano servicioPersistenciaPlano;
  private ControladorRankingDeDistribuciones controlador;
  private MockHttpSession sesion;

  @BeforeEach
  public void init() {
    servicioRankingDeDistribuciones = mock(ServicioRankingDeDistribuciones.class);
    servicioEdicionInventarioMuebles = mock(ServicioEdicionInventarioMuebles.class);
    servicioPersistenciaPlano = mock(ServicioPersistenciaPlano.class);
    controlador =
      new ControladorRankingDeDistribuciones(
        servicioRankingDeDistribuciones,
        servicioEdicionInventarioMuebles,
        servicioPersistenciaPlano
      );
    sesion = new MockHttpSession();
  }

  @Test
  public void deberiaMostrarLasOpcionesDelRanking() {
    //Given
    Ambiente ambiente = givenAmbienteEnSesion();
    List<Mueble> muebles = givenListaDeMuebles();
    List<OpcionDeDistribucion> opciones = givenOpciones();
    when(servicioEdicionInventarioMuebles.listar()).thenReturn(muebles);
    when(servicioRankingDeDistribuciones.generarRanking(ambiente, muebles)).thenReturn(opciones);

    //When
    ModelAndView vista = controlador.generarRanking(null, null, sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("ranking-distribuciones")));
    assertThat(vista.getModel().get("opciones"), is(equalTo(opciones)));
  }

  @Test
  public void deberiaGuardarLaOpcionConfirmadaComoNuevaVersion() {
    //Given
    sesion.setAttribute("USUARIO_EMAIL", "usuario@unlam.edu.ar");
    Plano planoElegido = givenPlano();
    List<OpcionDeDistribucion> opciones = givenOpciones(planoElegido);
    sesion.setAttribute("opcionesRanking", opciones);

    //When
    ModelAndView vista = controlador.confirmarOpcion(0, sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("redirect:/plano/actual")));
    assertThat(sesion.getAttribute("plano"), is(equalTo(planoElegido)));
    verify(servicioPersistenciaPlano).guardar(planoElegido, "usuario@unlam.edu.ar");
  }

  @Test
  public void deberiaPedirLoginAntesDeConfirmarUnaOpcion() {
    //Given
    sesion.setAttribute("opcionesRanking", givenOpciones());

    //When
    ModelAndView vista = controlador.confirmarOpcion(0, sesion);

    //Then
    assertThat(vista.getViewName(), is(equalTo("redirect:/login")));
  }

  private Ambiente givenAmbienteEnSesion() {
    Ambiente ambiente = new Ambiente(4.0, 3.0);
    sesion.setAttribute("ambiente", ambiente);
    return ambiente;
  }

  private List<Mueble> givenListaDeMuebles() {
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(new Mueble("Silla", 0.5, 0.5));
    return muebles;
  }

  private List<OpcionDeDistribucion> givenOpciones() {
    return givenOpciones(givenPlano());
  }

  private List<OpcionDeDistribucion> givenOpciones(Plano plano) {
    List<OpcionDeDistribucion> opciones = new ArrayList<>();
    opciones.add(new OpcionDeDistribucion(plano, 0, new ArrayList<>(), new ArrayList<>()));
    return opciones;
  }

  private Plano givenPlano() {
    return new Plano(
      new Ambiente(4.0, 3.0),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>()
    );
  }
}
