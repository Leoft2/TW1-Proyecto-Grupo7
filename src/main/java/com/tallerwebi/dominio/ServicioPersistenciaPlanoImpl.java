package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioPersistenciaPlanoImpl implements ServicioPersistenciaPlano {

  private final RepositorioPlano repositorioPlano;

  @Autowired
  public ServicioPersistenciaPlanoImpl(RepositorioPlano repositorioPlano) {
    this.repositorioPlano = repositorioPlano;
  }

  @Override
  public void guardar(Plano plano, String usuarioEmail) {
    repositorioPlano.guardar(plano, usuarioEmail);
  }

  @Override
  public Plano buscarUltimoDe(String usuarioEmail) {
    return repositorioPlano.buscarUltimoDe(usuarioEmail);
  }
}
