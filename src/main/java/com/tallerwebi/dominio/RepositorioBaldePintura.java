package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioBaldePintura {
  List<BaldePintura> obtenerTodos();
  BaldePintura buscarPorId(Long id);
  BaldePintura buscarPorHex(String hex);
  BaldePintura buscarPorColorNombre(String nombre);
  void guardar(BaldePintura baldePintura);
  void eliminarTodos();
}
