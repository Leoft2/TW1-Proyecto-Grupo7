package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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
public class RepositorioPlanoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPlanoImpl repositorioPlano;

  @BeforeEach
  public void init() {
    repositorioPlano = new RepositorioPlanoImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRecuperarLaPosicionActualizadaDelUltimoPlanoDeUnUsuario() {
    //Given
    Usuario usuario = givenTengoUnUsuario("plano@unlam.edu.ar");
    givenTengoUnPlanoGuardadoPara(usuario);

    //When
    Plano guardado = repositorioPlano.buscarUltimoDe(usuario.getEmail());
    guardado.getMuebles().get(0).moverA(1.0, 1.5);
    repositorioPlano.guardar(guardado, usuario.getEmail());
    flushYClear();
    Plano recuperado = repositorioPlano.buscarUltimoDe(usuario.getEmail());

    //Then
    assertThat(recuperado, is(notNullValue()));
    assertThat(recuperado.getUsuario().getEmail(), is(equalTo(usuario.getEmail())));
    assertThat(recuperado.getMuebles().get(0).getPosicionX(), is(equalTo(1.0)));
    assertThat(recuperado.getMuebles().get(0).getPosicionY(), is(equalTo(1.5)));
  }

  /*
   * HU-12: Historial de versiones de distribucion
   * - Dado que genero una nueva distribucion, cuando se guarda, queda como version nueva sin borrar las anteriores.
   * - Dado un historial de versiones, cuando selecciono una anterior, el sistema la restaura como la activa.
   */
  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaNuevaDistribucionComoVersionNuevaSinBorrarLasAnteriores() {
    //Given
    Usuario usuario = givenTengoUnUsuario("historial@unlam.edu.ar");
    givenTengoUnPlanoGuardadoPara(usuario);

    //When
    givenTengoUnPlanoGuardadoPara(usuario);
    List<Plano> historial = repositorioPlano.buscarHistorialDe(usuario.getEmail());

    //Then
    assertThat(historial.size(), is(equalTo(2)));
    assertThat(historial.get(0).isActiva(), is(equalTo(true)));
    assertThat(historial.get(1).isActiva(), is(equalTo(false)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRestaurarUnaVersionAnteriorComoLaActiva() {
    //Given
    Usuario usuario = givenTengoUnUsuario("restaurar@unlam.edu.ar");
    givenTengoUnPlanoGuardadoPara(usuario);
    givenTengoUnPlanoGuardadoPara(usuario);
    List<Plano> historial = repositorioPlano.buscarHistorialDe(usuario.getEmail());
    Long versionAnterior = historial.get(1).getId();

    //When
    repositorioPlano.activar(versionAnterior, usuario.getEmail());
    flushYClear();
    Plano activa = repositorioPlano.buscarUltimoDe(usuario.getEmail());

    //Then
    assertThat(activa.getId(), is(equalTo(versionAnterior)));
    assertThat(repositorioPlano.buscarHistorialDe(usuario.getEmail()).size(), is(equalTo(2)));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaRestaurarUnaVersionDeOtroUsuario() {
    //Given
    Usuario usuario = givenTengoUnUsuario("dueno@unlam.edu.ar");
    givenTengoUnPlanoGuardadoPara(usuario);
    Long planoId = repositorioPlano.buscarUltimoDe(usuario.getEmail()).getId();

    //When
    Plano restaurado = repositorioPlano.activar(planoId, "otro@unlam.edu.ar");

    //Then
    assertThat(restaurado, is(nullValue()));
  }

  private Usuario givenTengoUnUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private void givenTengoUnPlanoGuardadoPara(Usuario usuario) {
    Mueble mueble = new Mueble("Silla", 1.0, 1.0);
    sessionFactory.getCurrentSession().persist(mueble);
    List<MuebleUbicado> muebles = new ArrayList<>();
    muebles.add(new MuebleUbicado(mueble, 0.0, 0.0));
    Plano plano = new Plano(new Ambiente(4.0, 3.0), muebles, new ArrayList<>(), new ArrayList<>());
    repositorioPlano.guardar(plano, usuario.getEmail());
    flushYClear();
  }

  private void flushYClear() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
