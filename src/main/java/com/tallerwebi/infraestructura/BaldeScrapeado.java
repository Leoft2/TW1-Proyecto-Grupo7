package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.BaldePintura;

public class BaldeScrapeado {

  private String href;
  private String url;
  private String nombre;
  private Double precio;
  private Double litros;
  private String colorNombre;
  private String hex;

  public BaldeScrapeado() {}

  public BaldeScrapeado(String href, String nombre, Double precio, Double litros) {
    this.href = href;
    this.nombre = nombre;
    this.precio = precio;
    this.litros = litros;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Double getPrecio() {
    return precio;
  }

  public void setPrecio(Double precio) {
    this.precio = precio;
  }

  public Double getLitros() {
    return litros;
  }

  public void setLitros(Double litros) {
    this.litros = litros;
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

  public BaldePintura aBaldePintura() {
    return new BaldePintura(nombre, colorNombre, hex, url, litros, precio);
  }
}
