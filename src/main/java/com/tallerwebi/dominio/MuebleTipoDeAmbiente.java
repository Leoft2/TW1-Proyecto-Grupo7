package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tabla intermedia explicita entre Mueble y TipoDeAmbiente. Reemplaza la relacion
 * ManyToMany implicita para poder agregar atributos propios de la asociacion en el futuro
 * (por ejemplo, marcar un tipo de ambiente como principal).
 */
@Entity
@Table(name = "mueble_tipos_de_ambiente")
public class MuebleTipoDeAmbiente {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "mueble_id", nullable = false)
  private Mueble mueble;

  @ManyToOne(optional = false)
  @JoinColumn(name = "tipo_de_ambiente_id", nullable = false)
  private TipoDeAmbiente tipoDeAmbiente;

  public MuebleTipoDeAmbiente() {}

  public MuebleTipoDeAmbiente(Mueble mueble, TipoDeAmbiente tipoDeAmbiente) {
    this.mueble = mueble;
    this.tipoDeAmbiente = tipoDeAmbiente;
  }

  public Long getId() {
    return id;
  }

  public Mueble getMueble() {
    return mueble;
  }

  public TipoDeAmbiente getTipoDeAmbiente() {
    return tipoDeAmbiente;
  }

  public boolean esDelTipo(TipoDeAmbiente tipoDeAmbiente) {
    return this.tipoDeAmbiente != null && this.tipoDeAmbiente.equals(tipoDeAmbiente);
  }
}
