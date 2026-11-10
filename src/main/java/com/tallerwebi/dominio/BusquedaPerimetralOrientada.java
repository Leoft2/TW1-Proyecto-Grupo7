package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BusquedaPerimetralOrientada {

  private static final int MAXIMO_DE_INTENTOS = 20000;
  private static final double PASO_MINIMO = 0.25;
  private final ServicioPlano2D servicioPlano;
  private final ServicioReglasDeUbicacion reglas;
  private final ServicioOrientacionFuncional orientacion;
  private int intentos;

  public BusquedaPerimetralOrientada(
    ServicioPlano2D servicioPlano,
    ServicioReglasDeUbicacion reglas,
    ServicioOrientacionFuncional orientacion
  ) {
    this.servicioPlano = servicioPlano;
    this.reglas = reglas;
    this.orientacion = orientacion;
  }

  public Optional<Plano> buscar(
    Ambiente ambiente,
    List<Mueble> muebles,
    List<ZonaExclusion> zonas
  ) {
    if (!requiereOrientacion(muebles)) {
      return Optional.empty();
    }
    intentos = 0;
    return buscarMueble(ambiente, muebles, zonas, new ArrayList<>());
  }

  private Optional<Plano> buscarMueble(
    Ambiente ambiente,
    List<Mueble> muebles,
    List<ZonaExclusion> zonas,
    List<MuebleUbicado> ubicados
  ) {
    if (ubicados.size() == muebles.size()) {
      Plano plano = new Plano(
        ambiente,
        new ArrayList<>(ubicados),
        new ArrayList<>(),
        new ArrayList<>()
      );
      if (orientacion.cumpleOrientacion(plano) && posicionesValidas(plano, zonas)) {
        return Optional.of(plano);
      }
      if (new RelacionSofaTelevisor().haySofaYTelevisor(muebles)) {
        return buscarPropuesta(plano, zonas);
      }
      return Optional.empty();
    }
    Mueble mueble = muebles.get(ubicados.size());
    for (Muro muro : Muro.values()) {
      if (intentos >= MAXIMO_DE_INTENTOS) {
        return Optional.empty();
      }
      Optional<Plano> resultado = buscarEnMuro(ambiente, muebles, zonas, ubicados, mueble, muro);
      if (resultado.isPresent()) {
        return resultado;
      }
    }
    return Optional.empty();
  }

  private Optional<Plano> buscarPropuesta(Plano plano, List<ZonaExclusion> zonas) {
    for (Plano propuesta : new PropuestasDeOrientacion().generar(plano)) {
      if (posicionesValidas(propuesta, zonas) && orientacion.cumpleOrientacion(propuesta)) {
        return Optional.of(propuesta);
      }
    }
    return Optional.empty();
  }

  private Optional<Plano> buscarEnMuro(
    Ambiente ambiente,
    List<Mueble> muebles,
    List<ZonaExclusion> zonas,
    List<MuebleUbicado> ubicados,
    Mueble mueble,
    Muro muro
  ) {
    double limite =
      muro.largoEn(ambiente) -
      ((muro == Muro.SUPERIOR || muro == Muro.INFERIOR) ? mueble.getAncho() : mueble.getLargo());
    double paso = Math.max(PASO_MINIMO, ambiente.getAnchoMinimoDePasillo());
    for (double avance = 0; avance <= limite + ZonaNoDisponible.TOLERANCIA; avance += paso) {
      if (intentos >= MAXIMO_DE_INTENTOS) {
        return Optional.empty();
      }
      intentos++;
      MuebleUbicado candidato = candidatoEn(mueble, ambiente, muro, avance);
      if (!valida(candidato, ambiente, ubicados, zonas)) {
        continue;
      }
      ubicados.add(candidato);
      Optional<Plano> resultado = buscarMueble(ambiente, muebles, zonas, ubicados);
      ubicados.remove(ubicados.size() - 1);
      if (resultado.isPresent()) {
        return resultado;
      }
    }
    return Optional.empty();
  }

  private MuebleUbicado candidatoEn(Mueble mueble, Ambiente ambiente, Muro muro, double avance) {
    double posicionX =
      switch (muro) {
        case SUPERIOR, INFERIOR -> avance;
        case DERECHO -> ambiente.getAncho() - mueble.getAncho();
        case IZQUIERDO -> 0.0;
      };
    double posicionY =
      switch (muro) {
        case IZQUIERDO, DERECHO -> avance;
        case INFERIOR -> ambiente.getLargo() - mueble.getLargo();
        case SUPERIOR -> 0.0;
      };
    return new MuebleUbicado(mueble, posicionX, posicionY);
  }

  private boolean posicionesValidas(Plano plano, List<ZonaExclusion> zonas) {
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      List<MuebleUbicado> otros = new ArrayList<>(plano.getMuebles());
      otros.remove(ubicado);
      if (!valida(ubicado, plano.getAmbiente(), otros, zonas)) {
        return false;
      }
    }
    return true;
  }

  private boolean valida(
    MuebleUbicado ubicado,
    Ambiente ambiente,
    List<MuebleUbicado> otros,
    List<ZonaExclusion> zonas
  ) {
    if (!servicioPlano.estaDentroDelPerimetro(ubicado, ambiente)) {
      return false;
    }
    return zonas == null
      ? reglas.esUbicacionValida(ubicado, ambiente, otros)
      : reglas.esUbicacionValida(ubicado, ambiente, otros, zonas);
  }

  private boolean requiereOrientacion(List<Mueble> muebles) {
    for (Mueble mueble : muebles) {
      CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
      if (categoria == null || categoria.getNombre() == null) {
        continue;
      }
      String nombre = categoria.getNombre();
      if (
        nombre.startsWith("Cama") ||
        nombre.startsWith("Sofá") ||
        nombre.startsWith("Sofa") ||
        nombre.startsWith("Silla") ||
        nombre.startsWith("Escritorio")
      ) {
        return true;
      }
    }
    return false;
  }
}
