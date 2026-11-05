package com.tallerwebi.dominio;

public class DetalleBaldeScrapeado {

  private String nombre;
  private Double litros;

  public DetalleBaldeScrapeado() {}

  public DetalleBaldeScrapeado(String nombre, Double litros) {
    this.nombre = nombre;
    this.litros = litros;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Double getLitros() {
    return litros;
  }

  public void setLitros(Double litros) {
    this.litros = litros;
  }
}
