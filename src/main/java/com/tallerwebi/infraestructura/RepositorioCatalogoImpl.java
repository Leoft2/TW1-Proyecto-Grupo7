package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.RepositorioCatalogo;
import com.tallerwebi.dominio.TipoDeAmbiente;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioCatalogo")
public class RepositorioCatalogoImpl implements RepositorioCatalogo {

  private final SessionFactory sessionFactory;
  private final BorradorDeMueble borradorDeMueble;

  @Autowired
  public RepositorioCatalogoImpl(SessionFactory sessionFactory, BorradorDeMueble borradorDeMueble) {
    this.sessionFactory = sessionFactory;
    this.borradorDeMueble = borradorDeMueble;
  }

  @Override
  public void guardarMueble(Mueble mueble) {
    sessionFactory.getCurrentSession().persist(mueble);
  }

  @Override
  public List<Mueble> ObtenerTodosLosMuebles() {
    String query = "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble WHERE m.enCatalogo = true";
    return sessionFactory.getCurrentSession().createQuery(query, Mueble.class).getResultList();
  }

  @Override
  public void eliminar(int id) {
    borradorDeMueble.eliminar(id);
  }

  @Override
  public Mueble ObtenerMueblePorId(int id) {
    String query =
      "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble " +
      "LEFT JOIN FETCH m.tiposDeAmbienteAsociados mta LEFT JOIN FETCH mta.tipoDeAmbiente " +
      "WHERE m.id = :id";
    Query<Mueble> hibernateQuery = sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameter("id", id);
    return hibernateQuery.uniqueResult();
  }

  @Override
  public TipoDeAmbiente buscarTipoDeAmbientePorNombre(String nombre) {
    String query = "FROM TipoDeAmbiente t WHERE t.nombre = :nombre";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, TipoDeAmbiente.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  @Override
  public void guardarTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente) {
    sessionFactory.getCurrentSession().persist(tipoDeAmbiente);
  }
}
