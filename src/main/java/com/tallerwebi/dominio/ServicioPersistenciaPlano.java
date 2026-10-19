package com.tallerwebi.dominio;

public interface ServicioPersistenciaPlano {
  void guardar(Plano plano, String usuarioEmail);
  Plano buscarUltimoDe(String usuarioEmail);
}
