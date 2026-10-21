package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Plano;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
  Debido a la relacion del mueble con un plano, no se puede eliminar un mueble directamente.
  Se debe eliminar primero la relacion con el plano, y luego eliminar el mueble.
  Ese es el proposito de esta clase.
*/
@Component
public class BorradorDeMueble {

  private static final String PLANOS_QUE_EXCLUYEN_MUEBLE =
    "select p from Plano p join p.mueblesExcluidos m where m.id = :id";
  private static final String ELIMINAR_MUEBLES_UBICADOS =
    "delete from MuebleUbicado u where u.mueble.id = :id";
  private static final String ELIMINAR_MUEBLE_TIPO_AMBIENTE =
    "delete from MuebleTipoDeAmbiente mta where mta.mueble.id = :id";
  private static final String ELIMINAR_MUEBLE = "delete from Mueble where id = :id";

  private final SessionFactory sessionFactory;

  @Autowired
  public BorradorDeMueble(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void eliminar(int id) {
    quitarMuebleDeExcluidos(id);
    session().flush();

    ejecutarEliminacion(ELIMINAR_MUEBLES_UBICADOS, id);
    ejecutarEliminacion(ELIMINAR_MUEBLE_TIPO_AMBIENTE, id);
    ejecutarEliminacion(ELIMINAR_MUEBLE, id);
  }

  private void quitarMuebleDeExcluidos(int id) {
    List<Plano> planos = session()
      .createQuery(PLANOS_QUE_EXCLUYEN_MUEBLE, Plano.class)
      .setParameter("id", id)
      .getResultList();

    for (Plano plano : planos) {
      plano.getMueblesExcluidos().removeIf(mueble -> mueble.getId() == id);
    }
  }

  private void ejecutarEliminacion(String hql, int id) {
    session().createMutationQuery(hql).setParameter("id", id).executeUpdate();
  }

  private Session session() {
    return sessionFactory.getCurrentSession();
  }
}
