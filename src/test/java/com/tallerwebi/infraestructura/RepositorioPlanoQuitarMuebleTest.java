package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.ServicioEdicionDePlano;
import com.tallerwebi.dominio.ServicioEdicionDePlanoImpl;
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
public class RepositorioPlanoQuitarMuebleTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPlanoImpl repositorioPlano;
  private ServicioEdicionDePlano servicioEdicionDePlano;

  @BeforeEach
  public void init() {
    repositorioPlano = new RepositorioPlanoImpl(sessionFactory);
    servicioEdicionDePlano = new ServicioEdicionDePlanoImpl();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaPersistirLaEliminacionDelMuebleEnElPlanoYElAmbiente() {
    //Given
    Usuario usuario = givenTengoUnUsuario();
    Plano plano = givenTengoUnPlanoConDosMuebles(usuario);

    //When
    boolean quitado = servicioEdicionDePlano.quitarMueble(plano, 0);
    repositorioPlano.guardar(plano, usuario.getEmail());
    flushYClear();
    Plano recuperado = repositorioPlano.buscarUltimoDe(usuario.getEmail());

    //Then
    assertThat(quitado, is(equalTo(true)));
    assertThat(recuperado, is(notNullValue()));
    assertThat(recuperado.getMuebles().size(), is(equalTo(1)));
    assertThat(recuperado.getMuebles().get(0).getMueble().getNombre(), is(equalTo("Mesa")));
    assertThat(recuperado.getAmbiente().getMuebles().size(), is(equalTo(1)));
    assertThat(recuperado.getAmbiente().getMuebles().get(0).getNombre(), is(equalTo("Mesa")));
  }

  private Usuario givenTengoUnUsuario() {
    Usuario usuario = new Usuario();
    usuario.setEmail("quitar-mueble@unlam.edu.ar");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Plano givenTengoUnPlanoConDosMuebles(Usuario usuario) {
    Mueble silla = new Mueble("Silla", 1.0, 1.0);
    Mueble mesa = new Mueble("Mesa", 1.0, 1.0);
    sessionFactory.getCurrentSession().persist(silla);
    sessionFactory.getCurrentSession().persist(mesa);

    Ambiente ambiente = new Ambiente(4.0, 3.0);
    ambiente.agregarMueble(silla);
    ambiente.agregarMueble(mesa);
    List<MuebleUbicado> mueblesUbicados = new ArrayList<>();
    mueblesUbicados.add(new MuebleUbicado(silla, 0.0, 0.0));
    mueblesUbicados.add(new MuebleUbicado(mesa, 1.0, 0.0));
    Plano plano = new Plano(ambiente, mueblesUbicados, new ArrayList<>(), new ArrayList<>());
    plano.setUsuario(usuario);
    repositorioPlano.guardar(plano, usuario.getEmail());
    flushYClear();
    return repositorioPlano.buscarUltimoDe(usuario.getEmail());
  }

  private void flushYClear() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
