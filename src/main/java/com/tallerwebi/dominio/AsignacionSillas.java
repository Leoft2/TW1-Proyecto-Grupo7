package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AsignacionSillas {

  public static final int MAXIMO_POR_MESA = 4;
  private final RelacionSillaMesa relacion;

  public AsignacionSillas(RelacionSillaMesa relacion) {
    this.relacion = relacion;
  }

  public Map<MuebleUbicado, MuebleUbicado> asignar(List<MuebleUbicado> muebles) {
    Map<MuebleUbicado, MuebleUbicado> asignaciones = new LinkedHashMap<>();
    for (MuebleUbicado silla : muebles) {
      if (relacion.esSilla(silla.getMueble())) {
        asignarSilla(silla, muebles, asignaciones, new HashSet<>());
      }
    }
    return asignaciones;
  }

  public boolean tieneCapacidad(List<MuebleUbicado> muebles) {
    int mesas = 0;
    for (MuebleUbicado ubicado : muebles) {
      if (relacion.esMesa(ubicado.getMueble())) {
        mesas++;
      }
    }
    return asignar(muebles).size() < mesas * MAXIMO_POR_MESA;
  }

  public int contarEnMesa(MuebleUbicado mesa, Map<MuebleUbicado, MuebleUbicado> asignaciones) {
    return ocupantes(mesa, asignaciones).size();
  }

  private boolean asignarSilla(
    MuebleUbicado silla,
    List<MuebleUbicado> muebles,
    Map<MuebleUbicado, MuebleUbicado> asignaciones,
    Set<MuebleUbicado> visitadas
  ) {
    for (MuebleUbicado mesa : muebles) {
      if (
        !relacion.esMesa(mesa.getMueble()) ||
        visitadas.contains(mesa) ||
        relacion.frenteSiLibre(silla, mesa, muebles).isEmpty()
      ) {
        continue;
      }
      visitadas.add(mesa);
      List<MuebleUbicado> ocupantes = ocupantes(mesa, asignaciones);
      if (ocupantes.size() < MAXIMO_POR_MESA) {
        asignaciones.put(silla, mesa);
        return true;
      }
      for (MuebleUbicado ocupante : ocupantes) {
        if (asignarSilla(ocupante, muebles, asignaciones, visitadas)) {
          asignaciones.put(silla, mesa);
          return true;
        }
      }
    }
    return false;
  }

  private List<MuebleUbicado> ocupantes(
    MuebleUbicado mesa,
    Map<MuebleUbicado, MuebleUbicado> asignaciones
  ) {
    List<MuebleUbicado> resultado = new ArrayList<>();
    for (Map.Entry<MuebleUbicado, MuebleUbicado> asignacion : asignaciones.entrySet()) {
      if (mesa.equals(asignacion.getValue())) {
        resultado.add(asignacion.getKey());
      }
    }
    return resultado;
  }
}
