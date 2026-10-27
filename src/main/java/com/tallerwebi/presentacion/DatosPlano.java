package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Ambiente;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario de confirmacion del ambiente. Es el objeto que Spring bindea del request, porque
 * Ambiente no se puede instanciar desde un formulario (no tiene constructor vacio) y Mueble es
 * una entidad de JPA que no conviene usar como portador de datos del formulario.
 */
public class DatosPlano {

  private Double ancho;
  private Double largo;
  private Double anchoMinimoDePasillo;
  private List<DatosMueble> muebles = new ArrayList<>();
  private List<DatosObstaculo> obstaculos = new ArrayList<>();
  private List<DatosAbertura> aberturas = new ArrayList<>();

  public DatosPlano() {}

  /** Arma el ambiente con sus obstáculos, salteando las filas de obstáculo sin medidas. */
  public Ambiente aAmbiente() {
    Ambiente ambiente = new Ambiente(ancho, largo);
    ambiente.setAnchoMinimoDePasillo(anchoMinimoDePasillo);
    for (DatosObstaculo datosObstaculo : obstaculos) {
      if (!datosObstaculo.estaVacio()) {
        ambiente.agregarObstaculo(datosObstaculo.aObstaculo());
      }
    }
    for (DatosAbertura datosAbertura : getAberturas()) {
      if (!datosAbertura.estaIncompleta()) {
        ambiente.agregarAbertura(
          new com.tallerwebi.dominio.Abertura(
            datosAbertura.getTipo(),
            datosAbertura.getMuro(),
            datosAbertura.getPosicion(),
            datosAbertura.getAncho(),
            datosAbertura.getRadioDeGiro()
          )
        );
      }
    }
    return ambiente;
  }

  /** True si alguna fila de abertura quedó a medias: hay que completarla o quitarla. */
  public boolean hayAberturasIncompletas() {
    for (DatosAbertura datosAbertura : getAberturas()) {
      if (datosAbertura.estaIncompleta()) {
        return true;
      }
    }
    return false;
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

  public Double getAnchoMinimoDePasillo() {
    return anchoMinimoDePasillo;
  }

  public void setAnchoMinimoDePasillo(Double anchoMinimoDePasillo) {
    this.anchoMinimoDePasillo = anchoMinimoDePasillo;
  }

  public List<DatosMueble> getMuebles() {
    if (muebles == null) {
      muebles = new ArrayList<>();
    }
    return muebles;
  }

  public void setMuebles(List<DatosMueble> muebles) {
    this.muebles = muebles;
  }

  public List<DatosObstaculo> getObstaculos() {
    if (obstaculos == null) {
      obstaculos = new ArrayList<>();
    }
    return obstaculos;
  }

  public void setObstaculos(List<DatosObstaculo> obstaculos) {
    this.obstaculos = obstaculos;
  }

  public List<DatosAbertura> getAberturas() {
    if (aberturas == null) {
      aberturas = new ArrayList<>();
    }
    return aberturas;
  }

  public void setAberturas(List<DatosAbertura> aberturas) {
    this.aberturas = aberturas;
  }
}
