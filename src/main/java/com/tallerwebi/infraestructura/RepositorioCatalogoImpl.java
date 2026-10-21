package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Estilo;
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

  private static final String MUEBLES_DEL_CATALOGO =
    "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble LEFT JOIN m.material mat " +
    "WHERE m.enCatalogo = true " +
    "AND (:precioMinimo IS NULL OR m.precio >= :precioMinimo) " +
    "AND (:precioMaximo IS NULL OR m.precio <= :precioMaximo) " +
    "AND (:estilo IS NULL OR m.estilo = :estilo) " +
    "AND (:materialId IS NULL OR mat.id = :materialId) " +
    "AND (:soloSostenibles IS NULL OR :soloSostenibles = false " +
    "OR mat IS NULL OR mat.sostenible = true) " +
    "AND (:nombreTipoDeAmbiente IS NULL OR EXISTS (SELECT mta FROM MuebleTipoDeAmbiente mta " +
    "WHERE mta.mueble = m AND mta.tipoDeAmbiente.nombre = :nombreTipoDeAmbiente)) ";


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
  public List<Mueble> ObtenerMueblesDelCatalogo(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    String nombreTipoDeAmbiente,
    Boolean soloSostenibles,
    Long materialId
  ) {
    String query = MUEBLES_DEL_CATALOGO + "ORDER BY m.precio ASC";
    return this.buscarMueblesDelCatalogo(
        query,
        precioMinimo,
        precioMaximo,
        estilo,
        nombreTipoDeAmbiente,
        soloSostenibles,
        materialId
      );
  }

  @Override
  public List<Mueble> ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    String nombreTipoDeAmbiente,
    Boolean soloSostenibles,
    Long materialId
  ) {
    String query = MUEBLES_DEL_CATALOGO + "ORDER BY m.precio DESC";
    return this.buscarMueblesDelCatalogo(
        query,
        precioMinimo,
        precioMaximo,
        estilo,
        nombreTipoDeAmbiente,
        soloSostenibles,
        materialId
      );
  }

  @Override
  public List<Mueble> ObtenerMueblesConPrecioMenorAl(Double precioMaximo) {
    String query = "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble WHERE m.enCatalogo = true AND m.precio <= :precioMaximo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameter("precioMaximo", precioMaximo)
      .getResultList();
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilo(Estilo estilo) {
    String query = "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble WHERE m.enCatalogo = true AND m.estilo = :estilo";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameter("estilo", estilo)
      .getResultList();
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilos(List<Estilo> estilos) {
    String query = "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble WHERE m.enCatalogo = true AND m.estilo IN (:estilos)";
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameterList("estilos", estilos)
      .getResultList();
  }

  @Override
  public Mueble ObtenerMuebleMasBaratoDeEstilo(Estilo estilo) {
    String query = "FROM Mueble m LEFT JOIN FETCH m.categoriaDeMueble WHERE m.enCatalogo = true AND m.estilo = :estilo ORDER BY m.precio ASC";
    Query<Mueble> hibernateQuery = sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameter("estilo", estilo)
      .setMaxResults(1);
    return hibernateQuery.uniqueResult();
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

  private List<Mueble> buscarMueblesDelCatalogo(
    String query,
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    String nombreTipoDeAmbiente,
    Boolean soloSostenibles,
    Long materialId
  ) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(query, Mueble.class)
      .setParameter("precioMinimo", precioMinimo)
      .setParameter("precioMaximo", precioMaximo)
      .setParameter("estilo", estilo)
      .setParameter("materialId", materialId)
      .setParameter("soloSostenibles", soloSostenibles)
      .setParameter("nombreTipoDeAmbiente", nombreTipoDeAmbiente)
      .getResultList();
  }
}
