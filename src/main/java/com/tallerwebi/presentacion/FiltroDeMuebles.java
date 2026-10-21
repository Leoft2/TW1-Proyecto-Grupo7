package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.OrdenDeMuebles;
import com.tallerwebi.presentacion.excepcion.FiltroDeMueblesInvalidoException;
import java.util.Arrays;

public final class FiltroDeMuebles {

  private final Estilo estilo;
  private final Double precioMaximo;
  private final Double precioMinimo;
  private final String tipoDeAmbiente;
  private final OrdenDeMuebles orden;
  private final boolean soloSostenibles;
  private final Long materialId;

  private FiltroDeMuebles(
    Estilo estilo,
    Double precioMaximo,
    Double precioMinimo,
    String tipoDeAmbiente,
    OrdenDeMuebles orden,
    boolean soloSostenibles,
    Long materialId
  ) {
    this.estilo = estilo;
    this.precioMaximo = precioMaximo;
    this.precioMinimo = precioMinimo;
    this.tipoDeAmbiente = tipoDeAmbiente;
    this.orden = orden;
    this.soloSostenibles = soloSostenibles;
    this.materialId = materialId;
  }

  public static FiltroDeMuebles delCatalogo(
    String estilo,
    String precioMaximo,
    String precioMinimo,
    String tipoDeAmbiente,
    String orden,
    String soloSostenibles,
    String materialId
  ) throws FiltroDeMueblesInvalidoException {
    if (
      esUnEstiloDesconocido(estilo) ||
      esUnPrecioInvalido(precioMaximo) ||
      esUnPrecioInvalido(precioMinimo) ||
      esUnOrdenDesconocido(orden) ||
      esUnMaterialInvalido(materialId)
    ) {
      throw new FiltroDeMueblesInvalidoException(
        "El filtro del catalogo tiene criterios invalidos"
      );
    }

    return new FiltroDeMuebles(
      aEstilo(estilo),
      aPrecio(precioMaximo),
      aPrecio(precioMinimo),
      aTipoDeAmbiente(tipoDeAmbiente),
      aOrden(orden),
      aSoloSostenibles(soloSostenibles),
      aMaterial(materialId)
    );
  }

  public static FiltroDeMuebles deUnaPropuesta(
    String estilo,
    String presupuesto,
    String soloSostenibles,
    String materialId
  ) throws FiltroDeMueblesInvalidoException {
    if (
      estaVacio(estilo) ||
      estaVacio(presupuesto) ||
      esUnEstiloDesconocido(estilo) ||
      esUnPrecioInvalido(presupuesto) ||
      esUnMaterialInvalido(materialId)
    ) {
      throw new FiltroDeMueblesInvalidoException(
        "La propuesta necesita un estilo y un presupuesto validos"
      );
    }

    return new FiltroDeMuebles(
      Estilo.valueOf(estilo),
      Double.parseDouble(presupuesto),
      null,
      null,
      null,
      aSoloSostenibles(soloSostenibles),
      aMaterial(materialId)
    );
  }

  public Estilo getEstilo() {
    return this.estilo;
  }

  public Double getPrecioMaximo() {
    return this.precioMaximo;
  }

  public Double getPrecioMinimo() {
    return this.precioMinimo;
  }

  public String getTipoDeAmbiente() {
    return this.tipoDeAmbiente;
  }

  public OrdenDeMuebles getOrden() {
    return this.orden;
  }

  public boolean getSoloSostenibles() {
    return this.soloSostenibles;
  }

  public Long getMaterialId() {
    return this.materialId;
  }

  private static boolean estaVacio(String valor) {
    return valor == null || valor.isBlank();
  }

  private static boolean esUnEstiloDesconocido(String estilo) {
    return (
      !estaVacio(estilo) &&
      Arrays.stream(Estilo.values()).noneMatch(candidato -> candidato.name().equals(estilo))
    );
  }

  private static boolean esUnPrecioInvalido(String precio) {
    return !estaVacio(precio) && (noEsUnNumero(precio) || Double.parseDouble(precio) < 0);
  }

  private static boolean esUnOrdenDesconocido(String orden) {
    if (estaVacio(orden)) {
      return false;
    }
    return Arrays
      .stream(OrdenDeMuebles.values())
      .noneMatch(candidato -> candidato.name().equals(orden));
  }

  private static boolean noEsUnNumero(String valor) {
    try {
      Double.parseDouble(valor);
      return false;
    } catch (NumberFormatException e) {
      return true;
    }
  }

  private static boolean esUnMaterialInvalido(String materialId) {
    if (estaVacio(materialId)) {
      return false;
    }
    try {
      Long.parseLong(materialId);
      return false;
    } catch (NumberFormatException e) {
      return true;
    }
  }

  private static Estilo aEstilo(String estilo) {
    return estaVacio(estilo) ? null : Estilo.valueOf(estilo);
  }

  private static Long aMaterial(String materialId) {
    return estaVacio(materialId) ? null : Long.valueOf(materialId);
  }

  private static Double aPrecio(String precio) {
    return estaVacio(precio) ? null : Double.parseDouble(precio);
  }

  private static String aTipoDeAmbiente(String tipoDeAmbiente) {
    return estaVacio(tipoDeAmbiente) ? null : tipoDeAmbiente;
  }

  private static OrdenDeMuebles aOrden(String orden) {
    return estaVacio(orden) ? null : OrdenDeMuebles.valueOf(orden);
  }

  private static boolean aSoloSostenibles(String soloSostenibles) {
    return Boolean.parseBoolean(soloSostenibles);
  }
}
