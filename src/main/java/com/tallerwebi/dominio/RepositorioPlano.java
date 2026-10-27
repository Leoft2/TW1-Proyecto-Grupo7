package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioPlano {
  void guardar(Plano plano, String usuarioEmail);
  Plano buscarUltimoDe(String usuarioEmail);
  List<Plano> buscarHistorialDe(String usuarioEmail);
  Plano activar(Long planoId, String usuarioEmail);
}
