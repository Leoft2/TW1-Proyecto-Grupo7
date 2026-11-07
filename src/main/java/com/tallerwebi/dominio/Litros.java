package com.tallerwebi.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Litros {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private Double valor;

  public Litros() {}

  public Litros(Double valor) {
    this.valor = valor;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getValor() {
    return valor;
  }

  public void setValor(Double valor) {
    this.valor = valor;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof Litros)) {
      return false;
    }
    Litros otro = (Litros) objeto;
    if (this.valor == null) {
      return otro.valor == null;
    }
    return this.valor.equals(otro.valor);
  }

  @Override
  public int hashCode() {
    if (this.valor == null) {
      return 0;
    }
    return this.valor.hashCode();
  }
}
