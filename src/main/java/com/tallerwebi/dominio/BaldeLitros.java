package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "balde_litros")
public class BaldeLitros {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "balde_id", nullable = false)
  private BaldePintura balde;

  @ManyToOne(optional = false)
  @JoinColumn(name = "litros_id", nullable = false)
  private Litros litros;

  public BaldeLitros() {}

  public BaldeLitros(BaldePintura balde, Litros litros) {
    this.balde = balde;
    this.litros = litros;
  }

  public Long getId() {
    return id;
  }

  public BaldePintura getBalde() {
    return balde;
  }

  public Litros getLitros() {
    return litros;
  }
}
