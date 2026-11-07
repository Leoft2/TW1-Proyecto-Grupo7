package com.tallerwebi.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
public class BaldePintura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String colorNombre;
  private String hex;
  private String url;
  private Double cantidadLitros;
  private Double precio;

  @OneToMany(
    mappedBy = "balde",
    cascade = CascadeType.ALL,
    orphanRemoval = true,
    fetch = FetchType.EAGER
  )
  @OrderBy("id ASC")
  private List<BaldeLitros> litrosOpciones = new ArrayList<>();

  public BaldePintura() {}

  public BaldePintura(
    String nombre,
    String colorNombre,
    String hex,
    String url,
    Double cantidadLitros,
    Double precio,
    String opcionesLitros
  ) {
    this.nombre = nombre;
    this.colorNombre = colorNombre;
    this.hex = hex;
    this.url = url;
    this.cantidadLitros = cantidadLitros;
    this.precio = precio;
    this.litrosOpciones = new ArrayList<>();
    if (opcionesLitros != null && !opcionesLitros.isBlank()) {
      for (String parte : opcionesLitros.split(",")) {
        String valorTexto = parte.trim();
        if (!valorTexto.isEmpty() && valorTexto.matches("\\d+(\\.\\d+)?")) {
          this.litrosOpciones.add(
              new BaldeLitros(this, new Litros(Double.parseDouble(valorTexto)))
            );
        }
      }
    }
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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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

  public List<BaldeLitros> getLitrosOpciones() {
    return litrosOpciones;
  }

  public void setLitrosOpciones(List<BaldeLitros> litrosOpciones) {
    this.litrosOpciones = litrosOpciones;
  }

  public void limpiarOpcionesLitros() {
    this.litrosOpciones.clear();
  }

  public void agregarOpcionLitros(Litros litros) {
    this.litrosOpciones.add(new BaldeLitros(this, litros));
  }

  public List<Double> getValoresOpcionesLitros() {
    List<Double> valores = new ArrayList<>();
    for (BaldeLitros opcion : litrosOpciones) {
      if (opcion == null || opcion.getLitros() == null) {
        continue;
      }
      Double valor = opcion.getLitros().getValor();
      if (valor != null && !valores.contains(valor)) {
        valores.add(valor);
      }
    }
    valores.sort(Comparator.naturalOrder());
    return valores;
  }

  public List<String> getOpcionesLitrosFormateadas() {
    List<String> opciones = new ArrayList<>();
    for (Double valor : getValoresOpcionesLitros()) {
      opciones.add(formatearValor(valor));
    }
    return opciones;
  }

  public String getOpcionesLitros() {
    return String.join(",", getOpcionesLitrosFormateadas());
  }

  private String formatearValor(Double valor) {
    if (valor == Math.floor(valor)) {
      return String.valueOf(valor.intValue());
    }
    return String.valueOf(valor);
  }
}
