package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Litros;
import com.tallerwebi.dominio.RepositorioLitros;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioLitros")
public class RepositorioLitrosImpl implements RepositorioLitros {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioLitrosImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Litros litros) {
    this.sessionFactory.getCurrentSession().persist(litros);
  }

  @Override
  public Optional<Litros> buscarPorValor(Double valor) {
    List<Litros> resultado =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Litros WHERE valor = :valor", Litros.class)
        .setParameter("valor", valor)
        .getResultList();
    return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
  }
}
