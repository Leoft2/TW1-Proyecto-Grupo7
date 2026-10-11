package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioInventarioMueble {
  void guardar(Mueble mueble);

  Mueble buscarPorId(int id);

  List<Mueble> listarTodos();

  void eliminar(int id);
}
