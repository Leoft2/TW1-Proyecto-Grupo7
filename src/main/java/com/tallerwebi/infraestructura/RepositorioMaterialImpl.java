package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.RepositorioMaterial;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioMaterial")
public class RepositorioMaterialImpl implements RepositorioMaterial {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioMaterialImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Material> obtenerTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Material ORDER BY nombre", Material.class)
      .getResultList();
  }

  @Override
  public Material buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Material.class, id);
  }
}
