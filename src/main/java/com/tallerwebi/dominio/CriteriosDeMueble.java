package com.tallerwebi.dominio;

/**
 * Criterios de filtrado del catálogo. Encapsula las condiciones que debe cumplir un mueble
 * para aparecer en un listado, de forma que el servicio no acumule la complejidad de cada
 * combinación de filtros.
 */
public class CriteriosDeMueble {

  private final Double precioMinimo;
  private final Double precioMaximo;
  private final Estilo estilo;
  private final TipoDeAmbiente tipoDeAmbiente;
  private final Long materialId;

  public CriteriosDeMueble(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    TipoDeAmbiente tipoDeAmbiente,
    Long materialId
  ) {
    this.precioMinimo = precioMinimo;
    this.precioMaximo = precioMaximo;
    this.estilo = estilo;
    this.tipoDeAmbiente = tipoDeAmbiente;
    this.materialId = materialId;
  }

  public boolean cumple(Mueble mueble) {
    return (
      cumpleElEstilo(mueble) &&
      cumpleElPrecioMinimo(mueble) &&
      cumpleElPrecioMaximo(mueble) &&
      cumpleElMaterial(mueble) &&
      cumpleElTipoDeAmbiente(mueble)
    );
  }

  private boolean cumpleElEstilo(Mueble mueble) {
    return estilo == null || mueble.getEstilo() == estilo;
  }

  private boolean cumpleElPrecioMinimo(Mueble mueble) {
    return precioMinimo == null || mueble.getPrecio() >= precioMinimo;
  }

  private boolean cumpleElPrecioMaximo(Mueble mueble) {
    return precioMaximo == null || mueble.getPrecio() <= precioMaximo;
  }

  private boolean cumpleElMaterial(Mueble mueble) {
    return (
      materialId == null ||
      (mueble.getMaterial() != null && materialId.equals(mueble.getMaterial().getId()))
    );
  }

  private boolean cumpleElTipoDeAmbiente(Mueble mueble) {
    if (tipoDeAmbiente == null || tipoDeAmbiente.getNombre() == null) {
      return true;
    }
    for (TipoDeAmbiente tipoDelMueble : mueble.getTiposDeAmbiente()) {
      if (tipoDeAmbiente.getNombre().equals(tipoDelMueble.getNombre())) {
        return true;
      }
    }
    return false;
  }
}
