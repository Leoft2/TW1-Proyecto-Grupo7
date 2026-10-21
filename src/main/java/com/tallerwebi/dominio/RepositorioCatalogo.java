package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioCatalogo {
  void guardarMueble(Mueble mueble);
  List<Mueble> ObtenerMueblesDelCatalogo(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    String nombreTipoDeAmbiente,
    Boolean soloSostenibles,
    Long materialId
  );
  List<Mueble> ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    String nombreTipoDeAmbiente,
    Boolean soloSostenibles,
    Long materialId
  );
  List<Mueble> ObtenerMueblesConPrecioMenorAl(Double precioMaximo);
  List<Mueble> ObtenerMueblesDeEstilo(Estilo estilo);
  List<Mueble> ObtenerMueblesDeEstilos(List<Estilo> estilos);
  Mueble ObtenerMuebleMasBaratoDeEstilo(Estilo estilo);
  Mueble ObtenerMueblePorId(int id);
  void eliminar(int id);
  TipoDeAmbiente buscarTipoDeAmbientePorNombre(String nombre);
  void guardarTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente);
}
