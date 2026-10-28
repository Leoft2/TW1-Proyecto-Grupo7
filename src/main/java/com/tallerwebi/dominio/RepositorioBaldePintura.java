package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioBaldePintura {
  List<BaldePintura> obtenerTodos();
  BaldePintura buscarPorId(Long id);
  void guardar(BaldePintura baldePintura);
}
