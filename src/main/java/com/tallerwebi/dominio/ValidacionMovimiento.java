package com.tallerwebi.dominio;

/** Resultado de validar un movimiento manual de un mueble contra las zonas de exclusion. */
public class ValidacionMovimiento {

  private final boolean interferencia;

  public ValidacionMovimiento(boolean hayInterferencia) {
    this.interferencia = hayInterferencia;
  }

  public boolean hayInterferencia() {
    return interferencia;
  }
}
