package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Material {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private boolean sostenible;

  public Material() {}

  public Material(String nombre, boolean sostenible) {
    this.nombre = nombre;
    this.sostenible = sostenible;
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

  public boolean isSostenible() {
    return this.sostenible;
  }

  public void setSostenible(boolean sostenible) {
    this.sostenible = sostenible;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Material)) {
      return false;
    }
    Material otro = (Material) objeto;
    if (this.nombre == null) {
      return otro.nombre == null;
    }
    return this.nombre.equals(otro.nombre);
  }

  @Override
  public int hashCode() {
    if (this.nombre == null) {
      return 0;
    }
    return this.nombre.hashCode();
  }
}
