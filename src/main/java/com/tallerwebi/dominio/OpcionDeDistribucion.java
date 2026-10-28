package com.tallerwebi.dominio;

import java.util.List;

public class OpcionDeDistribucion {

  private final Plano plano;
  private final int puntajeTotal;
  private final List<String> desglose;
  private final List<String> avisos;

  public OpcionDeDistribucion(
    Plano plano,
    int puntajeTotal,
    List<String> desglose,
    List<String> avisos
  ) {
    this.plano = plano;
    this.puntajeTotal = puntajeTotal;
    this.desglose = desglose;
    this.avisos = avisos;
  }

  public Plano getPlano() {
    return plano;
  }

  public int getPuntajeTotal() {
    return puntajeTotal;
  }

  public List<String> getDesglose() {
    return desglose;
  }

  public List<String> getAvisos() {
    return avisos;
  }
}
