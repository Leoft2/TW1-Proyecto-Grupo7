package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;
import com.tallerwebi.dominio.RepositorioBaldePintura;
import java.util.List;
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
    List<BaldePintura> baldes =
      this.sessionFactory.getCurrentSession()
        .createQuery(query, BaldePintura.class)
        .getResultList();
    return baldes;
  }

  @Override
  public BaldePintura buscarPorId(Long id) {
    return this.sessionFactory.getCurrentSession().get(BaldePintura.class, id);
  }

  @Override
  public void guardar(BaldePintura baldePintura) {
    this.sessionFactory.getCurrentSession().persist(baldePintura);
  }
}
