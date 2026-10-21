package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioMaterial {
  List<Material> obtenerTodos();

  Material buscarPorId(Long id);
}
