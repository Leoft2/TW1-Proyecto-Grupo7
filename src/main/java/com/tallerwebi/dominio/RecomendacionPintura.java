package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

public class RecomendacionPintura {

  private FormaPintura formaPintura;
  private String descripcion;
  private String imagenSugerida;
  private List<ItemPintura> items = new ArrayList<ItemPintura>();
  private Double precioTotal;
  private ColorSuperficie colorSuperficie;

  public RecomendacionPintura() {}

  public FormaPintura getFormaPintura() {
    return formaPintura;
  }

  public void setFormaPintura(FormaPintura formaPintura) {
    this.formaPintura = formaPintura;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getImagenSugerida() {
    return imagenSugerida;
  }

  public void setImagenSugerida(String imagenSugerida) {
    this.imagenSugerida = imagenSugerida;
  }

  public List<ItemPintura> getItems() {
    return items;
  }

  public void setItems(List<ItemPintura> items) {
    this.items = items;
  }

  public void agregarItem(ItemPintura itemPintura) {
    this.items.add(itemPintura);
    calcularTotal();
  }

  public Double getPrecioTotal() {
    return precioTotal;
  }

  public ColorSuperficie getColorSuperficie() {
    return this.colorSuperficie;
  }

  public void setColorSuperficie(ColorSuperficie colorSuperficie) {
    this.colorSuperficie = colorSuperficie;
  }

  public void setPrecioTotal(Double precioTotal) {
    this.precioTotal = precioTotal;
  }

  public void calcularTotal() {
    Double total = 0.0;
    if (this.items == null) {
      this.precioTotal = 0.0;
      return;
    }
    for (ItemPintura item : this.items) {
      if (item != null && item.getPrecioTotal() != null) {
        total = total + item.getPrecioTotal();
      }
    }
    this.precioTotal = total;
  }

  public static class ItemPintura {

    private String zona;
    private String colorNombre;
    private String hex;
    private BaldePintura baldePintura;
    private Integer cantidadBaldes;
    private Double metrosCuadrados;
    private Double precioTotal;

    public ItemPintura() {}

    public String getZona() {
      return zona;
    }

    public void setZona(String zona) {
      this.zona = zona;
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

    public BaldePintura getBaldePintura() {
      return baldePintura;
    }

    public void setBaldePintura(BaldePintura baldePintura) {
      this.baldePintura = baldePintura;
    }

    public Integer getCantidadBaldes() {
      return cantidadBaldes;
    }

    public void setCantidadBaldes(Integer cantidadBaldes) {
      this.cantidadBaldes = cantidadBaldes;
    }

    public Double getMetrosCuadrados() {
      return metrosCuadrados;
    }

    public void setMetrosCuadrados(Double metrosCuadrados) {
      this.metrosCuadrados = metrosCuadrados;
    }

    public Double getPrecioTotal() {
      return precioTotal;
    }

    public void setPrecioTotal(Double precioTotal) {
      this.precioTotal = precioTotal;
    }

    private ColorSuperficie colorSuperficie;

    public ColorSuperficie getColorSuperficie() {
      return this.colorSuperficie;
    }

    public void setColorSuperficie(ColorSuperficie colorSuperficie) {
      this.colorSuperficie = colorSuperficie;
    }
  }
}
