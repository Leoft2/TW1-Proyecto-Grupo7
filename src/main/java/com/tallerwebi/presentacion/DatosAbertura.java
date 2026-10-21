package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Muro;
import com.tallerwebi.dominio.TipoAbertura;

public class DatosAbertura {

  private TipoAbertura tipo;
  private Muro muro;
  private Double posicion;
  private Double ancho;
  private Double radioDeGiro;

  public TipoAbertura getTipo() {
    return tipo;
  }

  public void setTipo(TipoAbertura tipo) {
    this.tipo = tipo;
  }

  public Muro getMuro() {
    return muro;
  }

  public void setMuro(Muro muro) {
    this.muro = muro;
  }

  public Double getPosicion() {
    return posicion;
  }

  public void setPosicion(Double posicion) {
    this.posicion = posicion;
  }

  public Double getAncho() {
    return ancho;
  }

  public void setAncho(Double ancho) {
    this.ancho = ancho;
  }

  public Double getRadioDeGiro() {
    return radioDeGiro;
  }

  public void setRadioDeGiro(Double radioDeGiro) {
    this.radioDeGiro = radioDeGiro;
  }
}
