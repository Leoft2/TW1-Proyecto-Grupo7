package com.tallerwebi.dominio;

import java.util.List;

public class CalculadorLitros {

  public void elegirLitroMasBarato(RecomendacionPintura.ItemPintura item, BaldePintura balde) {
    List<Double> opciones = balde.getValoresOpcionesLitros();
    if (opciones == null || opciones.isEmpty()) {
      item.setLitrosSeleccionados(balde.getCantidadLitros());
      return;
    }

    Double metros = item.getMetrosCuadrados();
    Double precio = balde.getPrecio();
    if (metros == null || metros <= 0 || precio == null) {
      item.setLitrosSeleccionados(balde.getCantidadLitros());
      return;
    }

    item.setLitrosSeleccionados(seleccionarMejorLitro(metros, precio, opciones));
  }

  private Double seleccionarMejorLitro(Double metros, Double precio, List<Double> opciones) {
    Double mejorLitros = null;
    Double mejorCosto = null;

    for (Double litros : opciones) {
      if (litros == null || litros <= 0) {
        continue;
      }
      int cantidad = (int) Math.ceil(metros / litros);
      Double costo = cantidad * precio;
      if (
        mejorCosto == null ||
        costo < mejorCosto ||
        (costo.equals(mejorCosto) && litros > mejorLitros)
      ) {
        mejorCosto = costo;
        mejorLitros = litros;
      }
    }

    if (mejorLitros != null) {
      return mejorLitros;
    }
    return null;
  }
}
