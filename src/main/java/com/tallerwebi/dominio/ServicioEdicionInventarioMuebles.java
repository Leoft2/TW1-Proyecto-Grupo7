package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.List;

public interface ServicioEdicionInventarioMuebles {
  void actualizar(int id, Mueble datos) throws MuebleNoEncontrado;

  void eliminar(int id) throws MuebleNoEncontrado;

  Mueble buscarPorId(int id) throws MuebleNoEncontrado;

  List<Mueble> listar();

  void crear(Mueble mueble);
}
