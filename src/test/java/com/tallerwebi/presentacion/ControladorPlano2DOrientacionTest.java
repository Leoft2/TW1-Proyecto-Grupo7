package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioOrientacionFuncional;
import com.tallerwebi.dominio.ServicioOrientacionFuncionalImpl;
import com.tallerwebi.dominio.ServicioPlano2DImpl;
import com.tallerwebi.dominio.ServicioZonaExclusionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorPlano2DOrientacionTest {

  private ControladorPlano2D controlador;
  private ServicioOrientacionFuncional orientacion;

  @BeforeEach
  public void init() {
    controlador =
      new ControladorPlano2D(
        new ServicioPlano2DImpl(new ServicioZonaExclusionImpl()),
        mock(ServicioCategoriaDeMueble.class)
      );
    orientacion = new ServicioOrientacionFuncionalImpl();
  }

  @Test
  public void deberiaBuscarUnaAlternativaParaUnaCamaEnUnaEsquina() {
    //Given
    Ambiente ambiente = new Ambiente(5.0, 5.0);
    List<Mueble> muebles = givenUnaCama();

    //When
    ModelAndView resultado = whenSeGeneraElPlano(ambiente, muebles);

    //Then
    Plano plano = (Plano) resultado.getModel().get("plano");
    assertThat(orientacion.cumpleOrientacion(plano), is(equalTo(true)));
  }

  @Test
  public void deberiaInformarCuandoNoHayEspacioParaLosLateralesDeLaCama() {
    //Given
    Ambiente ambiente = new Ambiente(1.4, 2.0);
    List<Mueble> muebles = givenUnaCama();

    //When
    ModelAndView resultado = whenSeGeneraElPlano(ambiente, muebles);

    //Then
    assertThat(
      resultado.getModel().get("error"),
      is(equalTo("No se pudo orientar los muebles ni garantizar el acceso lateral de la cama."))
    );
  }

  private List<Mueble> givenUnaCama() {
    Mueble cama = new Mueble("Cama", 1.4, 1.9);
    cama.setCategoriaDeMueble(new CategoriaDeMueble("Cama doble", 1.4, 1.9));
    List<Mueble> muebles = new ArrayList<>();
    muebles.add(cama);
    return muebles;
  }

  private ModelAndView whenSeGeneraElPlano(Ambiente ambiente, List<Mueble> muebles) {
    return controlador.generarPlano(ambiente, muebles);
  }
}
