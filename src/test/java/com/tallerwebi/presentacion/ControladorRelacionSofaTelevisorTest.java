package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.OpcionDeDistribucion;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionInventarioMuebles;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioPersistenciaPlano;
import com.tallerwebi.dominio.ServicioPlano2DImpl;
import com.tallerwebi.dominio.ServicioRankingDeDistribuciones;
import com.tallerwebi.dominio.ServicioRankingDeDistribucionesImpl;
import com.tallerwebi.dominio.ServicioReglasDeUbicacionImpl;
import com.tallerwebi.dominio.ServicioZonaExclusion;
import com.tallerwebi.dominio.ServicioZonaExclusionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRelacionSofaTelevisorTest {

  @Test
  public void deberiaInformarSiNoSeEncuentraUnaDistribucionParaSofaYRack() {
    //Given
    Ambiente ambiente = new Ambiente(2.0, 2.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      new ServicioPlano2DImpl(zonas),
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
    ServicioEdicionInventarioMuebles inventario = mock(ServicioEdicionInventarioMuebles.class);
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 1.0, 1.0));
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5));
    when(inventario.listar()).thenReturn(muebles);
    ControladorRankingDeDistribuciones controlador = new ControladorRankingDeDistribuciones(
      ranking,
      inventario,
      mock(ServicioPersistenciaPlano.class)
    );
    MockHttpSession sesion = new MockHttpSession();
    sesion.setAttribute("ambiente", ambiente);

    //When
    ModelAndView vista = controlador.generarRanking(null, null, sesion);

    //Then
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);
    assertThat(opciones.isEmpty(), is(equalTo(true)));
    assertThat(vista.getViewName(), is(equalTo("ranking-distribuciones")));
    assertThat((String) vista.getModel().get("mensaje"), containsString("sofá y rack de TV"));
    assertThat((String) vista.getModel().get("mensaje"), containsString("entre 1,5 y 4 m"));
  }

  @Test
  public void deberiaInformarElConflictoAlGenerarPlanoDirectamente() {
    //Given
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ControladorPlano2D controlador = new ControladorPlano2D(
      new ServicioPlano2DImpl(zonas),
      mock(ServicioCategoriaDeMueble.class)
    );
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 1.0, 1.0));
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5));

    //When
    ModelAndView vista = controlador.generarPlano(new Ambiente(2.0, 2.0), muebles);

    //Then
    assertThat((String) vista.getModel().get("error"), containsString("sofá y rack de TV"));
  }

  @Test
  public void deberiaBuscarAlternativaSiElPrimerPlanoExcluyeElRack() {
    //Given
    Ambiente ambiente = new Ambiente(10.0, 10.0);
    ServicioZonaExclusion zonas = new ServicioZonaExclusionImpl();
    ServicioPlano2DImpl servicioPlano = spy(new ServicioPlano2DImpl(zonas));
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(givenMueble("Sofá", 2.0, 1.0));
    muebles.add(givenMueble("Rack de TV", 1.0, 0.5));
    List<MuebleUbicado> ubicados = new ArrayList<>();
    ubicados.add(new MuebleUbicado(muebles.get(0), 1.0, 0.0));
    List<Mueble> excluidos = new ArrayList<>();
    excluidos.add(muebles.get(1));
    Plano planoInicial = new Plano(ambiente, ubicados, excluidos, new ArrayList<>());
    doReturn(planoInicial).when(servicioPlano).generarPlano(any(Ambiente.class), anyList());
    ControladorPlano2D controlador = new ControladorPlano2D(
      servicioPlano,
      mock(ServicioCategoriaDeMueble.class)
    );

    //When
    ModelAndView vista = controlador.generarPlano(ambiente, muebles);

    //Then
    Plano plano = (Plano) vista.getModel().get("plano");
    assertThat(plano.getMuebles().size(), is(equalTo(2)));
    assertThat(plano.getMueblesExcluidos().isEmpty(), is(equalTo(true)));
  }

  private Mueble givenMueble(String categoria, double ancho, double largo) {
    Mueble mueble = new Mueble(categoria, ancho, largo);
    mueble.setCategoriaDeMueble(new CategoriaDeMueble(categoria, ancho, largo));
    return mueble;
  }
}
