package com.tallerwebi.dominio;

record AnalisisAmbiente(
  double ancho,
  double largo,
  double altura,
  String tipoDeAmbiente,
  Muro muroPuerta
) {
  static final double ALTURA_POR_DEFECTO = 2.7;

  static AnalisisAmbiente de(Ambiente ambiente) {
    double ancho = valorODefecto(ambiente.getAncho(), 0.0);
    double largo = valorODefecto(ambiente.getLargo(), 0.0);
    double altura = valorODefecto(ambiente.getAltura(), ALTURA_POR_DEFECTO);

    String tipo = null;
    if (ambiente.getTipoDeAmbiente() != null) {
      tipo = ambiente.getTipoDeAmbiente().getNombre();
    }

    Muro muroPuerta = buscarMuroDePuerta(ambiente);

    return new AnalisisAmbiente(ancho, largo, altura, tipo, muroPuerta);
  }

  private static double valorODefecto(Double valor, double defecto) {
    if (valor == null) {
      return defecto;
    }

    return valor;
  }

  private static Muro buscarMuroDePuerta(Ambiente ambiente) {
    if (ambiente.getAberturas() == null) {
      return null;
    }

    for (Abertura abertura : ambiente.getAberturas()) {
      if (abertura != null && TipoAbertura.PUERTA.equals(abertura.getTipo())) {
        return abertura.getMuro();
      }
    }

    return null;
  }

  boolean esPasillo() {
    return "Pasillo".equals(tipoDeAmbiente);
  }

  double area() {
    return ancho * largo;
  }

  double areaTecho() {
    return ancho * largo;
  }

  double areaParedIzquierda() {
    return largo * altura;
  }

  double areaParedFondo() {
    return ancho * altura;
  }

  double areaParedDerecha() {
    return largo * altura;
  }

  double areaParedes() {
    return 2.0 * (ancho + largo) * altura;
  }

  double areaTotal() {
    return areaParedes() + areaTecho();
  }
}
