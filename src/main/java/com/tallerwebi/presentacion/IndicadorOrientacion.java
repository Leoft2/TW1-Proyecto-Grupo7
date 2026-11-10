package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Ambiente;
import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.MuebleUbicado;
import com.tallerwebi.dominio.ServicioOrientacionDeMuebles;
import com.tallerwebi.dominio.ServicioOrientacionDeMueblesImpl;
import org.springframework.stereotype.Component;

@Component
public class IndicadorOrientacion {

  private static final double TOLERANCIA = 1e-9;
  private final ServicioOrientacionDeMuebles servicio = new ServicioOrientacionDeMueblesImpl();

  public String frente(MuebleUbicado ubicado, Ambiente ambiente) {
    CategoriaDeMueble categoria = ubicado.getMueble().getCategoriaDeMueble();
    if (categoria == null || categoria.getNombre() == null) {
      return "";
    }
    String nombre = categoria.getNombre();
    if (
      !nombre.startsWith("Cama") &&
      !nombre.startsWith("Sofá") &&
      !nombre.startsWith("Sofa") &&
      !nombre.startsWith("Silla") &&
      !nombre.startsWith("Escritorio")
    ) {
      return "";
    }
    if (!esValida(ubicado, ambiente)) {
      return "";
    }
    return servicio.calcularFrente(ubicado, ambiente).map(Enum::name).orElse("");
  }

  private boolean esValida(MuebleUbicado ubicado, Ambiente ambiente) {
    return (
      ambiente != null &&
      ejeValido(ubicado.getPosicionX(), ubicado.getMueble().getAncho(), ambiente.getAncho()) &&
      ejeValido(ubicado.getPosicionY(), ubicado.getMueble().getLargo(), ambiente.getLargo())
    );
  }

  private boolean ejeValido(Double posicion, Double medida, Double ambiente) {
    if (posicion == null || medida == null || ambiente == null) {
      return false;
    }
    return (
      Double.isFinite(posicion) &&
      Double.isFinite(medida) &&
      Double.isFinite(ambiente) &&
      medida > 0 &&
      ambiente > 0 &&
      posicion >= -TOLERANCIA &&
      posicion + medida <= ambiente + TOLERANCIA
    );
  }
}
