package com.tallerwebi.dominio;

public interface RepositorioPlano {
  void guardar(Plano plano, String usuarioEmail);
  Plano buscarUltimoDe(String usuarioEmail);
}
