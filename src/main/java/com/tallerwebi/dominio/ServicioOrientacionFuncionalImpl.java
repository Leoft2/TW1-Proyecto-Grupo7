package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServicioOrientacionFuncionalImpl implements ServicioOrientacionFuncional {

  private static final String CATEGORIA_CAMA = "Cama";
  private static final String CATEGORIA_SILLA = "Silla";

  private final ServicioOrientacionDeMuebles orientacion = new ServicioOrientacionDeMueblesImpl();
  private final ServicioAccesoLateralCama acceso = new ServicioAccesoLateralCama();
  private final RelacionSofaTelevisor relacionSofaTelevisor = new RelacionSofaTelevisor();
  private final RelacionSillaMesa relacionSillaMesa = new RelacionSillaMesa();

  @Override
  public boolean cumpleOrientacion(Plano plano) {
    if (!cumpleCapacidadDeMesas(plano)) {
      return false;
    }
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
    int sillas = 0;
    int mesas = 0;
    int total = 0;
    for (Mueble mueble : muebles) {
      String categoria = categoriaDeMueble(mueble);
      if (categoria.startsWith(CATEGORIA_CAMA) || (esSofa(categoria) && hayRack)) {
        total++;
      }
      if (relacionSillaMesa.esSilla(mueble)) {
        sillas++;
      }
      if (relacionSillaMesa.esMesa(mueble)) {
        mesas++;
      }
    }
    return total + Math.min(sillas, mesas * AsignacionSillas.MAXIMO_POR_MESA);
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
    if (
      categoria.startsWith(CATEGORIA_SILLA) &&
      relacionSillaMesa.frente(ubicado, plano.getMuebles()).isPresent()
    ) {
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
    if (
      categoria.startsWith(CATEGORIA_SILLA) &&
      relacionSillaMesa.frente(ubicado, plano.getMuebles()).isPresent()
    ) {
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
    return true;
  }

  private boolean cumpleCapacidadDeMesas(Plano plano) {
    List<MuebleUbicado> muebles = plano.getMuebles();
    int sillas = 0;
    int mesas = 0;
    for (MuebleUbicado ubicado : muebles) {
      if (relacionSillaMesa.esSilla(ubicado.getMueble())) {
        sillas++;
      }
      if (relacionSillaMesa.esMesa(ubicado.getMueble())) {
        mesas++;
      }
    }
    if (mesas == 0) {
      return true;
    }
    Map<MuebleUbicado, MuebleUbicado> asignaciones = relacionSillaMesa.asignaciones(muebles);
    if (asignaciones.size() != Math.min(sillas, mesas * AsignacionSillas.MAXIMO_POR_MESA)) {
      return false;
    }
    return sillasExcedentesSinMesa(muebles, asignaciones);
  }

  private boolean sillasExcedentesSinMesa(
    List<MuebleUbicado> muebles,
    Map<MuebleUbicado, MuebleUbicado> asignaciones
  ) {
    for (MuebleUbicado ubicado : muebles) {
      if (
        relacionSillaMesa.esSilla(ubicado.getMueble()) &&
        !asignaciones.containsKey(ubicado) &&
        relacionSillaMesa.tieneMesaCercana(ubicado, muebles)
      ) {
        return false;
      }
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

  private String categoriaDe(MuebleUbicado ubicado) {
    return categoriaDeMueble(ubicado.getMueble());
  }

  private String categoriaDeMueble(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria == null || categoria.getNombre() == null ? "" : categoria.getNombre();
  }
}
