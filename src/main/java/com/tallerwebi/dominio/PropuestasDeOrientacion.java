package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropuestasDeOrientacion {

  private static final String CATEGORIA_RACK = "Rack de TV";
  private final ServicioOrientacionDeMuebles orientacion = new ServicioOrientacionDeMueblesImpl();

  public List<Plano> generar(Plano plano) {
    List<Plano> propuestas = new ArrayList<>();
    List<MuebleUbicado> muebles = plano.getMuebles();
    for (MuebleUbicado ubicado : muebles) {
      String referencia = categoriaDeReferencia(ubicado);
      if (referencia.isEmpty()) {
        continue;
      }
      Optional<Muro> frente = orientacion.calcularFrente(ubicado, plano.getAmbiente());
      if (frente.isEmpty()) {
        continue;
      }
      for (int indice = 0; indice < muebles.size(); indice++) {
        if (categoriaDe(muebles.get(indice)).startsWith(referencia)) {
          propuestas.add(crearPropuesta(plano, ubicado, frente.get(), indice));
          if (CATEGORIA_RACK.equals(referencia)) {
            for (
              double distancia = RelacionSofaTelevisor.DISTANCIA_MINIMA;
              distancia <= RelacionSofaTelevisor.DISTANCIA_MAXIMA;
              distancia += 0.5
            ) {
              propuestas.add(
                crearPropuestaConDistancia(plano, ubicado, frente.get(), indice, distancia)
              );
            }
          }
        }
      }
    }
    return propuestas;
  }

  private Plano crearPropuesta(Plano plano, MuebleUbicado origen, Muro frente, int indice) {
    Ambiente ambiente = plano.getAmbiente();
    Mueble referencia = plano.getMuebles().get(indice).getMueble();
    double posicionX = centrar(
      origen.getPosicionX(),
      origen.getMueble().getAncho(),
      referencia.getAncho(),
      ambiente.getAncho(),
      ambiente.getAnchoMinimoDePasillo()
    );
    double posicionY = centrar(
      origen.getPosicionY(),
      origen.getMueble().getLargo(),
      referencia.getLargo(),
      ambiente.getLargo(),
      ambiente.getAnchoMinimoDePasillo()
    );
    if (frente == Muro.SUPERIOR) {
      posicionY = 0.0;
    } else if (frente == Muro.INFERIOR) {
      posicionY = ambiente.getLargo() - referencia.getLargo();
    } else if (frente == Muro.IZQUIERDO) {
      posicionX = 0.0;
    } else {
      posicionX = ambiente.getAncho() - referencia.getAncho();
    }
    return crearPlano(plano, indice, posicionX, posicionY);
  }

  private Plano crearPropuestaConDistancia(
    Plano plano,
    MuebleUbicado origen,
    Muro frente,
    int indice,
    double distancia
  ) {
    Mueble referencia = plano.getMuebles().get(indice).getMueble();
    double posicionX = centrar(
      origen.getPosicionX(),
      origen.getMueble().getAncho(),
      referencia.getAncho(),
      plano.getAmbiente().getAncho(),
      plano.getAmbiente().getAnchoMinimoDePasillo()
    );
    double posicionY = centrar(
      origen.getPosicionY(),
      origen.getMueble().getLargo(),
      referencia.getLargo(),
      plano.getAmbiente().getLargo(),
      plano.getAmbiente().getAnchoMinimoDePasillo()
    );
    if (frente == Muro.SUPERIOR) {
      posicionY = origen.getPosicionY() - distancia - referencia.getLargo();
    } else if (frente == Muro.INFERIOR) {
      posicionY = origen.getPosicionY() + origen.getMueble().getLargo() + distancia;
    } else if (frente == Muro.IZQUIERDO) {
      posicionX = origen.getPosicionX() - distancia - referencia.getAncho();
    } else {
      posicionX = origen.getPosicionX() + origen.getMueble().getAncho() + distancia;
    }
    return crearPlano(plano, indice, posicionX, posicionY);
  }

  private Plano crearPlano(Plano plano, int indice, double posicionX, double posicionY) {
    Ambiente ambiente = plano.getAmbiente();
    Mueble referencia = plano.getMuebles().get(indice).getMueble();
    List<MuebleUbicado> muebles = new ArrayList<>();
    for (int actual = 0; actual < plano.getMuebles().size(); actual++) {
      MuebleUbicado ubicado = plano.getMuebles().get(actual);
      if (actual == indice) {
        muebles.add(new MuebleUbicado(referencia, posicionX, posicionY));
      } else {
        muebles.add(
          new MuebleUbicado(ubicado.getMueble(), ubicado.getPosicionX(), ubicado.getPosicionY())
        );
      }
    }
    return new Plano(ambiente, muebles, new ArrayList<>(), new ArrayList<>());
  }

  private double centrar(
    double posicion,
    double anchoOrigen,
    double anchoReferencia,
    double anchoAmbiente,
    double minimo
  ) {
    double disponible = anchoAmbiente - anchoReferencia;
    double centrado = posicion + (anchoOrigen - anchoReferencia) / 2;
    if (disponible >= 2 * minimo) {
      return Math.max(minimo, Math.min(disponible - minimo, centrado));
    }
    return Math.max(0.0, Math.min(disponible, centrado));
  }

  private String categoriaDeReferencia(MuebleUbicado ubicado) {
    String categoria = categoriaDe(ubicado);
    if (categoria.startsWith("Sofá") || categoria.startsWith("Sofa")) {
      return CATEGORIA_RACK;
    }
    if (categoria.startsWith("Silla")) {
      return "Mesa";
    }
    return "";
  }

  private String categoriaDe(MuebleUbicado ubicado) {
    CategoriaDeMueble categoria = ubicado.getMueble().getCategoriaDeMueble();
    return categoria == null || categoria.getNombre() == null ? "" : categoria.getNombre();
  }
}
