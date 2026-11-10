package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import java.util.List;
import java.util.Locale;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioBaldePintura")
public class RepositorioBaldePinturaImpl implements RepositorioBaldePintura {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioBaldePinturaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<BaldePintura> obtenerTodos() {
    String query = "FROM BaldePintura";

    return this.sessionFactory.getCurrentSession()
      .createQuery(query, BaldePintura.class)
      .getResultList();
  }

  @Override
  public BaldePintura buscarPorId(Long id) {
    return this.sessionFactory.getCurrentSession().get(BaldePintura.class, id);
  }

  @Override
  public BaldePintura buscarPorHex(String hex) {
    if (hex == null || hex.isBlank()) {
      return null;
    }

    String hexNormalizado = hex.replace("#", "").toLowerCase(Locale.ROOT);
    String query = "FROM BaldePintura WHERE hex = :hex";

    return this.sessionFactory.getCurrentSession()
      .createQuery(query, BaldePintura.class)
      .setParameter("hex", hexNormalizado)
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public BaldePintura buscarPorColorNombre(String nombre) {
    if (nombre == null || nombre.isBlank()) {
      return null;
    }

    String query = "FROM BaldePintura WHERE colorNombre = :nombre";

    return this.sessionFactory.getCurrentSession()
      .createQuery(query, BaldePintura.class)
      .setParameter("nombre", nombre)
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public void guardar(BaldePintura baldePintura) {
    this.sessionFactory.getCurrentSession().persist(baldePintura);
  }

  @Override
  public void eliminarTodos() {
    String queryEliminarLitros = "DELETE FROM BaldeLitros";
    String queryEliminarBaldes = "DELETE FROM BaldePintura";

    this.sessionFactory.getCurrentSession()
      .createMutationQuery(queryEliminarLitros)
      .executeUpdate();

    this.sessionFactory.getCurrentSession()
      .createMutationQuery(queryEliminarBaldes)
      .executeUpdate();
  }
}
