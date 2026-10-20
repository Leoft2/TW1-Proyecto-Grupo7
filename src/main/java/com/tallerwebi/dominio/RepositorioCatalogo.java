package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCatalogo {
  void guardarMueble(Mueble mueble);
  List<Mueble> ObtenerTodosLosMuebles();
  Mueble ObtenerMueblePorId(int id);
  void eliminar(int id);
  TipoDeAmbiente buscarTipoDeAmbientePorNombre(String nombre);
  void guardarTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente);
}
