package com.tallerwebi.dominio;

import java.util.List;

public final class Recomendacion {

  private Estilo estilo;
  private List<Color> colores;
  private List<Color> coloresSecundarios;
  private Iluminacion iluminacion;

  public Recomendacion(Estilo estilo, List<Color> colores, Iluminacion iluminacion) {
    this.estilo = estilo;
    if (colores != null) {
      this.colores = List.copyOf(colores);
    }
    this.iluminacion = iluminacion;
  }

  public Recomendacion(
    Estilo estilo,
    List<Color> colores,
    List<Color> coloresSecundarios,
    Iluminacion iluminacion
  ) {
    this.estilo = estilo;
    if (colores != null) {
      this.colores = List.copyOf(colores);
    }
    if (coloresSecundarios != null) {
      this.coloresSecundarios = List.copyOf(coloresSecundarios);
    }
    this.iluminacion = iluminacion;
  }

  public Recomendacion() {}

  public Estilo getEstilo() {
    return this.estilo;
  }

  public List<Color> getColores() {
    return this.colores;
  }

  public List<Color> getColoresSecundarios() {
    return this.coloresSecundarios;
  }

  public Iluminacion getIluminacion() {
    return this.iluminacion;
  }

  public void setEstilo(Estilo estilo) {
    this.estilo = estilo;
  }

  public void setColores(List<Color> colores) {
    if (colores != null) {
      this.colores = List.copyOf(colores);
    }
  }

  public void setColoresSecundarios(List<Color> coloresSecundarios) {
    if (coloresSecundarios != null) {
      this.coloresSecundarios = List.copyOf(coloresSecundarios);
    }
  }

  public void setIluminacion(Iluminacion iluminacion) {
    this.iluminacion = iluminacion;
  }
}
