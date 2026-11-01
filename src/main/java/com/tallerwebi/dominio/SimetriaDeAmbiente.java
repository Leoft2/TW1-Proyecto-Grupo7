package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

public class SimetriaDeAmbiente {

  public Ambiente transformarAmbiente(
    Ambiente ambiente,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Ambiente transformado = new Ambiente(
      ambiente.getAncho(),
      ambiente.getLargo(),
      ambiente.getTipoDeAmbiente()
    );
    transformado.setAnchoMinimoDePasillo(ambiente.getAnchoMinimoDePasillo());
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      transformado.agregarObstaculo(
        transformarObstaculo(ambiente, obstaculo, reflejarHorizontalmente, reflejarVerticalmente)
      );
    }
    for (Abertura abertura : ambiente.getAberturas()) {
      transformado.agregarAbertura(
        transformarAbertura(ambiente, abertura, reflejarHorizontalmente, reflejarVerticalmente)
      );
    }
    return transformado;
  }

  private Obstaculo transformarObstaculo(
    Ambiente ambiente,
    Obstaculo obstaculo,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro muro = transformarMuro(
      obstaculo.getMuro(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    double posicion = transformarPosicionEnMuro(
      ambiente,
      obstaculo.getMuro(),
      obstaculo.getPosicion(),
      obstaculo.getAncho(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    return new Obstaculo(
      obstaculo.getTipo(),
      muro,
      posicion,
      obstaculo.getAncho(),
      obstaculo.getProfundidad()
    );
  }

  private Abertura transformarAbertura(
    Ambiente ambiente,
    Abertura abertura,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro muro = transformarMuro(abertura.getMuro(), reflejarHorizontalmente, reflejarVerticalmente);
    double posicion = transformarPosicionEnMuro(
      ambiente,
      abertura.getMuro(),
      abertura.getPosicion(),
      abertura.getAncho(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    return new Abertura(
      abertura.getTipo(),
      muro,
      posicion,
      abertura.getAncho(),
      abertura.getRadioDeGiro()
    );
  }

  private Muro transformarMuro(
    Muro muro,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro resultado = muro;
    if (reflejarHorizontalmente) {
      resultado =
        switch (resultado) {
          case DERECHO -> Muro.IZQUIERDO;
          case IZQUIERDO -> Muro.DERECHO;
          default -> resultado;
        };
    }
    if (reflejarVerticalmente) {
      resultado =
        switch (resultado) {
          case SUPERIOR -> Muro.INFERIOR;
          case INFERIOR -> Muro.SUPERIOR;
          default -> resultado;
        };
    }
    return resultado;
  }

  private double transformarPosicionEnMuro(
    Ambiente ambiente,
    Muro muro,
    Double posicion,
    Double ancho,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    double resultado = posicion;
    if (reflejarHorizontalmente && (muro == Muro.SUPERIOR || muro == Muro.INFERIOR)) {
      resultado = ambiente.getAncho() - resultado - ancho;
    }
    if (reflejarVerticalmente && (muro == Muro.DERECHO || muro == Muro.IZQUIERDO)) {
      resultado = ambiente.getLargo() - resultado - ancho;
    }
    return resultado;
  }

  public Plano transformarPlanoAlAmbienteOriginal(
    Plano planoTransformado,
    Ambiente ambienteOriginal,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    for (MuebleUbicado ubicado : planoTransformado.getMuebles()) {
      double posicionX = ubicado.getPosicionX();
      double posicionY = ubicado.getPosicionY();
      if (reflejarHorizontalmente) {
        posicionX = ambienteOriginal.getAncho() - posicionX - ubicado.getMueble().getAncho();
      }
      if (reflejarVerticalmente) {
        posicionY = ambienteOriginal.getLargo() - posicionY - ubicado.getMueble().getLargo();
      }
      muebles.add(new MuebleUbicado(ubicado.getMueble(), posicionX, posicionY));
    }
    Plano plano = new Plano(ambienteOriginal, muebles, new ArrayList<>(), new ArrayList<>());
    plano.setMensaje(planoTransformado.getMensaje());
    return plano;
  }
}
