package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import java.util.ArrayList;
import java.util.List;

public class AdvertenciasDeApertura {

  private static final String MENSAJE_ADVERTENCIA =
    "La puerta del muro %s en la posición %s m no tiene pared libre para abrir; " +
    "la hoja podría chocar con muebles.";

  private final ServicioReglasDeApertura servicioReglasDeApertura;

  public AdvertenciasDeApertura(ServicioReglasDeApertura servicioReglasDeApertura) {
    this.servicioReglasDeApertura = servicioReglasDeApertura;
  }

  public List<String> calcular(Ambiente ambiente) {
    List<String> advertencias = new ArrayList<>();
    if (ambiente == null || ambiente.getAncho() == null || ambiente.getLargo() == null) {
      return advertencias;
    }
    for (Abertura abertura : ambiente.getAberturas()) {
      if (abertura.getTipo() == TipoAbertura.PUERTA) {
        ReglaDeApertura regla = servicioReglasDeApertura.determinarApertura(abertura, ambiente);
        if (!regla.getHayParedLibre()) {
          advertencias.add(
            String.format(
              MENSAJE_ADVERTENCIA,
              abertura.getMuro().getNombre(),
              abertura.getPosicion()
            )
          );
        }
      }
    }
    return advertencias;
  }
}
