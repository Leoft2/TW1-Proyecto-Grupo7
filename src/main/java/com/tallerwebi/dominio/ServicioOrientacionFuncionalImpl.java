package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServicioOrientacionFuncionalImpl implements ServicioOrientacionFuncional {

  private static final String CATEGORIA_CAMA = "Cama";
  private static final String CATEGORIA_SILLA = "Silla";

  private final ServicioOrientacionDeMuebles orientacion = new ServicioOrientacionDeMueblesImpl();
  private final ServicioAccesoLateralCama acceso = new ServicioAccesoLateralCama();
  private final RelacionSofaTelevisor relacionSofaTelevisor = new RelacionSofaTelevisor();

  @Override
  public boolean cumpleOrientacion(Plano plano) {
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      if (!cumpleRegla(ubicado, plano)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<String> desgloseOrientacion(Plano plano) {
    List<String> desglose = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      String regla = descripcionDeRegla(ubicado, plano);
      if (regla != null && cumpleRegla(ubicado, plano)) {
        desglose.add(ubicado.getMueble().getNombre() + ": " + regla + " (+1)");
      }
    }
    return desglose;
  }

  @Override
  public int puntajeMaximo(List<Mueble> muebles) {
    boolean hayRack = hayCategoria(muebles, "Rack de TV");
    boolean hayMesa = hayCategoria(muebles, "Mesa");
    int total = 0;
    for (Mueble mueble : muebles) {
      String categoria = categoriaDeMueble(mueble);
      if (
        categoria.startsWith(CATEGORIA_CAMA) ||
        (esSofa(categoria) && hayRack) ||
        (categoria.startsWith(CATEGORIA_SILLA) && hayMesa)
      ) {
        total++;
      }
    }
    return total;
  }

  private boolean hayCategoria(List<Mueble> muebles, String prefijo) {
    for (Mueble mueble : muebles) {
      if (categoriaDeMueble(mueble).startsWith(prefijo)) {
        return true;
      }
    }
    return false;
  }

  private String descripcionDeRegla(MuebleUbicado ubicado, Plano plano) {
    String categoria = categoriaDe(ubicado);
    if (categoria.startsWith(CATEGORIA_CAMA)) {
      return "acceso libre a ambos laterales";
    }
    if (esSofa(categoria) && hayReferencia(plano, "Rack de TV")) {
      return "sofa orientado hacia el rack de TV";
    }
    if (categoria.startsWith(CATEGORIA_SILLA) && hayReferencia(plano, "Mesa")) {
      return "silla orientada hacia la mesa";
    }
    return null;
  }

  private boolean hayReferencia(Plano plano, String prefijo) {
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      if (categoriaDe(ubicado).startsWith(prefijo)) {
        return true;
      }
    }
    return false;
  }

  private boolean cumpleRegla(MuebleUbicado ubicado, Plano plano) {
    String categoria = categoriaDe(ubicado);
    if (!necesitaOrientacion(categoria)) {
      return true;
    }
    Optional<Muro> frente = orientacion.calcularFrente(ubicado, plano.getAmbiente());
    if (frente.isEmpty()) {
      return false;
    }
    if (categoria.startsWith(CATEGORIA_CAMA)) {
      return acceso.hayAcceso(ubicado, frente.get(), plano);
    }
    if (esSofa(categoria)) {
      return miraTelevisor(ubicado, frente.get(), plano.getMuebles());
    }
    if (categoria.startsWith(CATEGORIA_SILLA)) {
      return miraReferencia(ubicado, frente.get(), plano.getMuebles(), "Mesa");
    }
    return true;
  }

  private boolean necesitaOrientacion(String categoria) {
    return (
      categoria.startsWith(CATEGORIA_CAMA) ||
      esSofa(categoria) ||
      categoria.startsWith(CATEGORIA_SILLA) ||
      categoria.startsWith("Escritorio")
    );
  }

  private boolean esSofa(String categoria) {
    return categoria.startsWith("Sofá") || categoria.startsWith("Sofa");
  }

  private boolean miraTelevisor(MuebleUbicado sofa, Muro frente, List<MuebleUbicado> muebles) {
    boolean hayRack = false;
    for (MuebleUbicado rack : muebles) {
      if (!categoriaDe(rack).startsWith("Rack de TV")) {
        continue;
      }
      hayRack = true;
      if (relacionSofaTelevisor.esValida(sofa, rack, frente, muebles)) {
        return true;
      }
    }
    return !hayRack;
  }

  private boolean miraReferencia(
    MuebleUbicado ubicado,
    Muro frente,
    List<MuebleUbicado> muebles,
    String categoria
  ) {
    boolean hayReferencia = false;
    for (MuebleUbicado otro : muebles) {
      if (!categoriaDe(otro).startsWith(categoria)) {
        continue;
      }
      hayReferencia = true;
      if (estaDelante(ubicado, otro, frente)) {
        return true;
      }
    }
    return !hayReferencia;
  }

  private boolean estaDelante(MuebleUbicado ubicado, MuebleUbicado otro, Muro frente) {
    double izquierda = ubicado.getPosicionX();
    double derecha = izquierda + ubicado.getMueble().getAncho();
    double arriba = ubicado.getPosicionY();
    double abajo = arriba + ubicado.getMueble().getLargo();
    double otroIzquierda = otro.getPosicionX();
    double otroDerecha = otroIzquierda + otro.getMueble().getAncho();
    double otroArriba = otro.getPosicionY();
    double otroAbajo = otroArriba + otro.getMueble().getLargo();
    return switch (frente) {
      case SUPERIOR -> otroAbajo <= arriba + ZonaNoDisponible.TOLERANCIA &&
      seCruzan(izquierda, derecha, otroIzquierda, otroDerecha);
      case INFERIOR -> otroArriba >= abajo - ZonaNoDisponible.TOLERANCIA &&
      seCruzan(izquierda, derecha, otroIzquierda, otroDerecha);
      case IZQUIERDO -> otroDerecha <= izquierda + ZonaNoDisponible.TOLERANCIA &&
      seCruzan(arriba, abajo, otroArriba, otroAbajo);
      case DERECHO -> otroIzquierda >= derecha - ZonaNoDisponible.TOLERANCIA &&
      seCruzan(arriba, abajo, otroArriba, otroAbajo);
    };
  }

  private boolean seCruzan(double inicio, double fin, double otroInicio, double otroFin) {
    return (
      inicio < otroFin - ZonaNoDisponible.TOLERANCIA &&
      otroInicio < fin - ZonaNoDisponible.TOLERANCIA
    );
  }

  private String categoriaDe(MuebleUbicado ubicado) {
    return categoriaDeMueble(ubicado.getMueble());
  }

  private String categoriaDeMueble(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria == null || categoria.getNombre() == null ? "" : categoria.getNombre();
  }
}
