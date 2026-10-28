package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.RepositorioPlano;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioPlano")
public class RepositorioPlanoImpl implements RepositorioPlano {

  private static final String TIPO_AMBIENTE_PERSONALIZADO = "Personalizado";
  private static final String PARAMETRO_USUARIO = "usuario";
  private static final String PARAMETRO_EMAIL = "email";
  private static final String PARAMETRO_ID = "id";

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPlanoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Plano plano, String usuarioEmail) {
    Usuario usuario = sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email", Usuario.class)
      .setParameter(PARAMETRO_EMAIL, usuarioEmail)
      .uniqueResult();
    if (usuario == null) {
      throw new UsuarioNoEncontrado();
    }

    TipoDeAmbiente tipoDeAmbiente = sessionFactory
      .getCurrentSession()
      .createQuery("from TipoDeAmbiente where nombre = :nombre", TipoDeAmbiente.class)
      .setParameter("nombre", TIPO_AMBIENTE_PERSONALIZADO)
      .uniqueResult();
    if (tipoDeAmbiente == null) {
      tipoDeAmbiente = new TipoDeAmbiente(TIPO_AMBIENTE_PERSONALIZADO);
      sessionFactory.getCurrentSession().persist(tipoDeAmbiente);
    }

    plano.setUsuario(usuario);
    plano.getAmbiente().setUsuario(usuario);
    plano.getAmbiente().setTipoDeAmbiente(tipoDeAmbiente);
    if (plano.getId() == null) {
      sessionFactory
        .getCurrentSession()
        .createMutationQuery("update Plano set activa = false where usuario = :usuario")
        .setParameter(PARAMETRO_USUARIO, usuario)
        .executeUpdate();
      plano.setActiva(true);
      plano.setFechaDeCreacion(LocalDateTime.now());
      sessionFactory.getCurrentSession().persist(plano);
    } else {
      sessionFactory.getCurrentSession().merge(plano);
    }
  }

  @Override
  public Plano buscarUltimoDe(String usuarioEmail) {
    Plano plano = sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Plano where usuario.email = :email order by activa desc, id desc",
        Plano.class
      )
      .setParameter(PARAMETRO_EMAIL, usuarioEmail)
      .setMaxResults(1)
      .uniqueResult();
    if (plano != null) {
      Hibernate.initialize(plano.getMuebles());
      Hibernate.initialize(plano.getMueblesExcluidos());
      Hibernate.initialize(plano.getMotivosDeExclusion());
      Hibernate.initialize(plano.getAmbiente().getObstaculos());
      Hibernate.initialize(plano.getAmbiente().getAberturas());
      for (MuebleUbicado muebleUbicado : plano.getMuebles()) {
        Hibernate.initialize(muebleUbicado.getMueble());
      }
    }
    return plano;
  }

  @Override
  public List<Plano> buscarHistorialDe(String usuarioEmail) {
    List<Plano> planos = sessionFactory
      .getCurrentSession()
      .createQuery("from Plano where usuario.email = :email order by id desc", Plano.class)
      .setParameter(PARAMETRO_EMAIL, usuarioEmail)
      .getResultList();
    for (Plano plano : planos) {
      inicializar(plano);
    }
    return planos;
  }

  @Override
  public Plano activar(Long planoId, String usuarioEmail) {
    Plano plano = sessionFactory
      .getCurrentSession()
      .createQuery("from Plano where id = :id and usuario.email = :email", Plano.class)
      .setParameter(PARAMETRO_ID, planoId)
      .setParameter(PARAMETRO_EMAIL, usuarioEmail)
      .uniqueResult();
    if (plano == null) {
      return null;
    }
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("update Plano set activa = false where usuario = :usuario")
      .setParameter(PARAMETRO_USUARIO, plano.getUsuario())
      .executeUpdate();
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("update Plano set activa = true where id = :id")
      .setParameter(PARAMETRO_ID, planoId)
      .executeUpdate();
    sessionFactory.getCurrentSession().refresh(plano);
    inicializar(plano);
    return plano;
  }

  private void inicializar(Plano plano) {
    Hibernate.initialize(plano.getMuebles());
    Hibernate.initialize(plano.getMueblesExcluidos());
    Hibernate.initialize(plano.getMotivosDeExclusion());
    Hibernate.initialize(plano.getAmbiente().getObstaculos());
    Hibernate.initialize(plano.getAmbiente().getAberturas());
    for (MuebleUbicado muebleUbicado : plano.getMuebles()) {
      Hibernate.initialize(muebleUbicado.getMueble());
    }
  }
}
