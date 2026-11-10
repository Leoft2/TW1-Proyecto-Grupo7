package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Puntua el despeje frontal de un mueble y arma su linea de desglose. Hoy la regla aplica solo a
 * placards. No decide si la ubicacion es valida: eso lo hace ServicioReglasDeUbicacion.
 */
public class PuntajePorDespejeFrontal {

  private static final String CATEGORIA_PLACARD = "Placard";
  // El minimo coincide con el piso eliminatorio de HU-35.1: si cambia uno, el otro debe quedar
  // alineado.
  private static final double DESPEJE_FRONTAL_COMODO = 0.60;
  private static final double DESPEJE_FRONTAL_MINIMO = 0.40;
  private static final int PUNTOS_POR_DESPEJE_COMODO = 2;
  private static final int PUNTOS_POR_DESPEJE_MINIMO = 1;
  private static final int PUNTOS_MAXIMOS_POR_PLACARD = 2;

  private final ServicioReglasDeUbicacion servicioReglasDeUbicacion;

  public PuntajePorDespejeFrontal(ServicioReglasDeUbicacion servicioReglasDeUbicacion) {
    this.servicioReglasDeUbicacion = servicioReglasDeUbicacion;
  }

  public int puntajeMaximo(Mueble mueble) {
    return esPlacard(mueble) ? PUNTOS_MAXIMOS_POR_PLACARD : 0;
  }

  /**
   * Suma el puntaje del placard si corresponde y agrega la linea de desglose. Devuelve 0 sin tocar
   * el desglose cuando el mueble no es un placard.
   */
  public int agregarPuntaje(
    MuebleUbicado ubicado,
    Plano plano,
    Ambiente ambiente,
    List<ZonaExclusion> zonas,
    List<String> desglose
  ) {
    if (!esPlacard(ubicado.getMueble())) {
      return 0;
    }
    List<MuebleUbicado> otros = new ArrayList<>(plano.getMuebles());
    otros.remove(ubicado);
    Double despeje = servicioReglasDeUbicacion.despejeFrontalDe(ubicado, ambiente, otros, zonas);
    int puntos = puntosPorDespejeFrontal(despeje);
    desglose.add(ubicado.getMueble().getNombre() + ": despeje frontal (+" + puntos + ")");
    return puntos;
  }

  private int puntosPorDespejeFrontal(Double despeje) {
    if (despeje == null) {
      return 0;
    }
    if (despeje >= DESPEJE_FRONTAL_COMODO - ZonaNoDisponible.TOLERANCIA) {
      return PUNTOS_POR_DESPEJE_COMODO;
    }
    if (despeje >= DESPEJE_FRONTAL_MINIMO - ZonaNoDisponible.TOLERANCIA) {
      return PUNTOS_POR_DESPEJE_MINIMO;
    }
    return 0;
  }

  private boolean esPlacard(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria != null && categoria.getNombre().startsWith(CATEGORIA_PLACARD);
  }
}
