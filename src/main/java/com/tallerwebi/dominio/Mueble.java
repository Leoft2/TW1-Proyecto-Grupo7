package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
  Duda: Como le doy la vuelta al TooManyFields?...
*/
@Entity
@SuppressWarnings("PMD.TooManyFields")
public class Mueble {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String nombre;
  private String descripcion;
  private double precio;
  private Double ancho;
  private Double largo;
  private Boolean enCatalogo = false;

  @Enumerated(EnumType.STRING)
  private Estilo estilo;

  @ManyToOne
  @JoinColumn(name = "material_id")
  private Material material;

  @OneToMany(mappedBy = "mueble", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<MuebleTipoDeAmbiente> tiposDeAmbienteAsociados = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "categoria_de_mueble_id")
  private CategoriaDeMueble categoriaDeMueble;

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  /**
   * Convención de coordenadas: origen (0,0) en la esquina superior izquierda.
   * Eje X: horizontal, de 0 a ancho.
   * Eje Y: vertical, de 0 a largo.
   */
  public Mueble() {}

  public Mueble(String nombre, Double ancho, Double largo) {
    this.nombre = nombre;
    this.ancho = ancho;
    this.largo = largo;
  }

  public void setPrecio(double _precio) {
    this.precio = _precio;
  }

  public double getPrecio() {
    return this.precio;
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

  public void setEstilo(Estilo estilo) {
    this.estilo = estilo;
  }

  public Estilo getEstilo() {
    return this.estilo;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public Material getMaterial() {
    return this.material;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getNombre() {
    return this.nombre;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public Boolean getEnCatalogo() {
    return enCatalogo;
  }

  public void setEnCatalogo(Boolean enCatalogo) {
    this.enCatalogo = enCatalogo;
  }

  @Override
  public boolean equals(Object otro) {
    if (this == otro) {
      return true;
    }
    if (!(otro instanceof Mueble)) {
      return false;
    }
    Mueble otroMueble = (Mueble) otro;
    return (
      Objects.equals(ancho, otroMueble.ancho) &&
      Objects.equals(largo, otroMueble.largo) &&
      Double.compare(precio, otroMueble.precio) == 0 &&
      Objects.equals(nombre, otroMueble.nombre) &&
      Objects.equals(descripcion, otroMueble.descripcion) &&
      estilo == otroMueble.estilo
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(nombre, descripcion, precio, ancho, largo, estilo);
  }

  public List<TipoDeAmbiente> getTiposDeAmbiente() {
    return this.tiposDeAmbienteAsociados.stream()
      .map(MuebleTipoDeAmbiente::getTipoDeAmbiente)
      .toList();
  }

  public void setTiposDeAmbiente(List<TipoDeAmbiente> tiposDeAmbiente) {
    this.tiposDeAmbienteAsociados.clear();
    if (tiposDeAmbiente != null) {
      for (TipoDeAmbiente tipoDeAmbiente : tiposDeAmbiente) {
        this.agregarTipoDeAmbiente(tipoDeAmbiente);
      }
    }
  }

  public void agregarTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente) {
    this.tiposDeAmbienteAsociados.add(new MuebleTipoDeAmbiente(this, tipoDeAmbiente));
  }

  public void quitarTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente) {
    this.tiposDeAmbienteAsociados.removeIf(asociado -> asociado.esDelTipo(tipoDeAmbiente));
  }

  public CategoriaDeMueble getCategoriaDeMueble() {
    return categoriaDeMueble;
  }

  public void setCategoriaDeMueble(CategoriaDeMueble categoriaDeMueble) {
    this.categoriaDeMueble = categoriaDeMueble;
  }
}
