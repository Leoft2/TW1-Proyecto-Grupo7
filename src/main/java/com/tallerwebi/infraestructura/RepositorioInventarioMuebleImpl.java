package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.Plano;
import com.tallerwebi.dominio.RepositorioInventarioMueble;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioInventarioMuebleImpl implements RepositorioInventarioMueble {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioInventarioMuebleImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
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
    // El inventario solo lista muebles con medidas: los del catalogo (data.sql) se
    // guardan sin ancho/largo y no pertenecen al inventario del usuario.
    String query = "FROM Mueble WHERE ancho IS NOT NULL AND largo IS NOT NULL";
    return sessionFactory.getCurrentSession().createQuery(query, Mueble.class).getResultList();
  }

  @Override
  public void eliminar(int id) {
    // Un mueble puede estar excluido en planos guardados. La relacion many-to-many la
    // posee Plano, asi que se desvincula y se fuerza el flush para
    // que las filas de la tabla puente se borren antes de eliminar el mueble.
    List<Plano> planosQueLoExcluyen = sessionFactory
      .getCurrentSession()
      .createQuery("select p from Plano p join p.mueblesExcluidos m where m.id = :id", Plano.class)
      .setParameter("id", id)
      .getResultList();
    for (Plano plano : planosQueLoExcluyen) {
      plano.getMueblesExcluidos().removeIf(mueble -> mueble.getId() == id);
    }
    sessionFactory.getCurrentSession().flush();
    // Las ubicaciones en planos guardados tienen FK al mueble y no cascadean: se borran
    // antes que el mueble. La tabla puente de tipos de ambiente la limpia Hibernate al
    // borrar la entidad.
    sessionFactory
      .getCurrentSession()
      .createQuery("delete from MuebleUbicado u where u.mueble.id = :id")
      .setParameter("id", id)
      .executeUpdate();
    sessionFactory
      .getCurrentSession()
      .createQuery("delete from Mueble where id = :id")
      .setParameter("id", id)
      .executeUpdate();
  }
}
