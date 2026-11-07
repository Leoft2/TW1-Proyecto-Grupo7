package com.tallerwebi.dominio;

import java.util.Optional;

public interface RepositorioLitros {
  void guardar(Litros litros);
  Optional<Litros> buscarPorValor(Double valor);
}
