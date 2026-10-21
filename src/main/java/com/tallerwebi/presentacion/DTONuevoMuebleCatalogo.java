package com.tallerwebi.presentacion;

import java.util.ArrayList;
import java.util.List;

public class DTONuevoMuebleCatalogo {

  private String nombre;
  private String descripcion;
  private Double precio;
  private String estilo;
  private Long categoria;
  private Double ancho;
  private Double largo;
  private List<String> tiposDeAmbiente = new ArrayList<>();

  public DTONuevoMuebleCatalogo() {}

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Double getPrecio() {
    return precio;
  }

  public void setPrecio(Double precio) {
    this.precio = precio;
  }

  public String getEstilo() {
    return estilo;
  }

  public void setEstilo(String estilo) {
    this.estilo = estilo;
  }

  public Long getCategoria() {
    return categoria;
  }

  public void setCategoria(Long categoria) {
    this.categoria = categoria;
  }

  public Double getAncho() {
    return ancho;
  }

  public void setAncho(Double ancho) {
    this.ancho = ancho;
  }

  public Double getLargo() {
    return largo;
  }

  public void setLargo(Double largo) {
    this.largo = largo;
  }

  public List<String> getTiposDeAmbiente() {
    return tiposDeAmbiente;
  }

  public void setTiposDeAmbiente(List<String> tiposDeAmbiente) {
    this.tiposDeAmbiente = tiposDeAmbiente;
  }
}
