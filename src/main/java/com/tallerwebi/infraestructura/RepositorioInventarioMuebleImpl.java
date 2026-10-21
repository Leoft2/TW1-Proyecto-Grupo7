package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RepositorioInventarioMueble;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioInventarioMuebleImpl implements RepositorioInventarioMueble {

  private final SessionFactory sessionFactory;
  private final BorradorDeMueble borradorDeMueble;

  @Autowired
  public RepositorioInventarioMuebleImpl(
    SessionFactory sessionFactory,
    BorradorDeMueble borradorDeMueble
  ) {
    this.sessionFactory = sessionFactory;
    this.borradorDeMueble = borradorDeMueble;
  }

  @Override
  public void guardar(Mueble mueble) {
    sessionFactory.getCurrentSession().persist(mueble);
  }

  @Override
  public Mueble buscarPorId(int id) {
    return sessionFactory.getCurrentSession().get(Mueble.class, id);
  }

  @Override
  public List<Mueble> listarTodos() {
    String query =
      "FROM Mueble WHERE (enCatalogo = false OR enCatalogo IS NULL) " +
      "AND ancho IS NOT NULL AND largo IS NOT NULL";
    return sessionFactory.getCurrentSession().createQuery(query, Mueble.class).getResultList();
  }

  @Override
  public void eliminar(int id) {
    borradorDeMueble.eliminar(id);
  }
}
