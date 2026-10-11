package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioCategoriaDeMueble {
  List<CategoriaDeMueble> obtenerCategorias();

  CategoriaDeMueble buscarPorId(Long id);

  Mueble crearMueble(String nombre, Long idCategoria, Double ancho, Double profundidad);
}
