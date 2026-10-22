package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioZonaExclusionImpl implements ServicioZonaExclusion {

  private static final double PROFUNDIDAD_DE_ZONA_DE_VENTANA = 0.3;

  @Override
  public List<ZonaExclusion> calcularZonas(Ambiente ambiente) {
    if (ambiente == null) {
      throw new IllegalArgumentException("El ambiente no puede ser nulo");
    }
    List<ZonaExclusion> zonas = new ArrayList<>();
    for (Abertura abertura : ambiente.getAberturas()) {
      zonas.add(zonaDe(abertura, ambiente));
    }
    return zonas;
  }

  private ZonaExclusion zonaDe(Abertura abertura, Ambiente ambiente) {
    Double profundidad = profundidadDe(abertura, ambiente);
    return switch (abertura.getMuro()) {
      case SUPERIOR -> new ZonaExclusion(
        abertura.getPosicion(),
        0.0,
        abertura.getAncho(),
        profundidad,
        abertura.getTipo()
      );
      case DERECHO -> new ZonaExclusion(
        ambiente.getAncho() - profundidad,
        abertura.getPosicion(),
        profundidad,
        abertura.getAncho(),
        abertura.getTipo()
      );
      case INFERIOR -> new ZonaExclusion(
        abertura.getPosicion(),
        ambiente.getLargo() - profundidad,
        abertura.getAncho(),
        profundidad,
        abertura.getTipo()
      );
      case IZQUIERDO -> new ZonaExclusion(
        0.0,
        abertura.getPosicion(),
        profundidad,
        abertura.getAncho(),
        abertura.getTipo()
      );
    };
  }

  private Double profundidadDe(Abertura abertura, Ambiente ambiente) {
    Double profundidad = profundidadDeLaAbertura(abertura);
    Double disponible = abertura.getMuro().profundidadDisponibleEn(ambiente);
    return Math.min(profundidad, disponible);
  }

  /**
   * La ventana tiene una profundidad fija. La puerta usa su radio de giro y, cuando no se cargo,
   * el ancho de la hoja: la puerta gira sobre si misma y ocupa su propio ancho.
   */
  private Double profundidadDeLaAbertura(Abertura abertura) {
    if (abertura.getTipo() == TipoAbertura.VENTANA) {
      return PROFUNDIDAD_DE_ZONA_DE_VENTANA;
    }
    if (abertura.getRadioDeGiro() != null) {
      return abertura.getRadioDeGiro();
    }
    return abertura.getAncho();
  }
}
