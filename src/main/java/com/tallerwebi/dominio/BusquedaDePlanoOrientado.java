package com.tallerwebi.dominio;

import java.util.List;
import java.util.Optional;

public class BusquedaDePlanoOrientado {

  public Optional<Plano> buscar(
    Ambiente ambiente,
    List<Mueble> muebles,
    ServicioPlano2D servicioPlano,
    ServicioZonaExclusion zonas
  ) {
    ServicioRankingDeDistribuciones ranking = new ServicioRankingDeDistribucionesImpl(
      servicioPlano,
      new ServicioReglasDeUbicacionImpl(zonas),
      zonas
    );
    List<OpcionDeDistribucion> opciones = ranking.generarRanking(ambiente, muebles);
    if (opciones.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(opciones.get(0).getPlano());
  }
}
