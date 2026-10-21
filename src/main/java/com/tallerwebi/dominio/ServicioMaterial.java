package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioMaterial {
  List<Material> obtenerMateriales();

  Material buscarPorId(Long id);
}
