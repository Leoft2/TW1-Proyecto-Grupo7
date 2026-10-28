package com.tallerwebi.dominio;

import java.util.List;

@FunctionalInterface
public interface ServicioRankingDeDistribuciones {
  List<OpcionDeDistribucion> generarRanking(Ambiente ambiente, List<Mueble> muebles);
}
