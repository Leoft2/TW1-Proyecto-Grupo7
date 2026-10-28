package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BaldePintura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String colorNombre;
  private String hex;
  private Double cantidadLitros;
  private Double precio;

  public BaldePintura() {}

  public BaldePintura(
    String nombre,
    String colorNombre,
    String hex,
    Double cantidadLitros,
    Double precio
  ) {
    this.nombre = nombre;
    this.colorNombre = colorNombre;
    this.hex = hex;
    this.cantidadLitros = cantidadLitros;
    this.precio = precio;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getColorNombre() {
    return colorNombre;
  }

  public void setColorNombre(String colorNombre) {
    this.colorNombre = colorNombre;
  }

  public String getHex() {
    return hex;
  }

  public void setHex(String hex) {
    this.hex = hex;
  }

  public Double getCantidadLitros() {
    return cantidadLitros;
  }

  public void setCantidadLitros(Double cantidadLitros) {
    this.cantidadLitros = cantidadLitros;
  }

  public Double getPrecio() {
    return precio;
  }

  public void setPrecio(Double precio) {
    this.precio = precio;
  }
}
