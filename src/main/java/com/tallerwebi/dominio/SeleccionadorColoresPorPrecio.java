package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SeleccionadorColoresPorPrecio {

  private final RepositorioBaldePintura repositorioBaldePintura;

  public SeleccionadorColoresPorPrecio(RepositorioBaldePintura repositorioBaldePintura) {
    this.repositorioBaldePintura = repositorioBaldePintura;
  }

  public List<Color> coloresOrdenadosPorPrecio(List<Color> colores) {
    if (colores == null || colores.isEmpty()) {
      return new ArrayList<>();
    }
    return colores
      .stream()
      .sorted(Comparator.comparingDouble(this::precioDelBalde))
      .collect(Collectors.toList());
  }

  private double precioDelBalde(Color color) {
    if (this.repositorioBaldePintura == null || color == null) {
      return Double.MAX_VALUE;
    }
    BaldePintura balde = this.repositorioBaldePintura.buscarPorHex(color.getHex());
    if (balde == null) {
      balde = this.repositorioBaldePintura.buscarPorColorNombre(color.getNombre());
    }
    if (balde == null || balde.getPrecio() == null) {
      return Double.MAX_VALUE;
    }
    return balde.getPrecio();
  }
}
