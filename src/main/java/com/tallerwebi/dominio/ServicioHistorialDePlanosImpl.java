package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioHistorialDePlanosImpl implements ServicioHistorialDePlanos {

  private final RepositorioPlano repositorioPlano;

  @Autowired
  public ServicioHistorialDePlanosImpl(RepositorioPlano repositorioPlano) {
    this.repositorioPlano = repositorioPlano;
  }

  /** Todas las versiones del usuario, de la mas nueva a la mas vieja. */
  @Override
  public List<Plano> listarVersiones(String usuarioEmail) {
    return repositorioPlano.buscarHistorialDe(usuarioEmail);
  }

  /** Marca la version elegida como la activa. Devuelve null si no es una version del usuario. */
  @Override
  public Plano restaurarVersion(Long planoId, String usuarioEmail) {
    return repositorioPlano.activar(planoId, usuarioEmail);
  }
}
