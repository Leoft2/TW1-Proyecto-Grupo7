package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioRankingDeDistribucionesImpl implements ServicioRankingDeDistribuciones {

  private static final int PUNTOS_POR_CUMPLIR_REGLA = 1;
  private static final int PUNTOS_POR_INCUMPLIR_REGLA = -1;
  private static final int CANTIDAD_MAXIMA_DE_OPCIONES = 3;
  private static final int CANTIDAD_MAXIMA_DE_DESPLAZAMIENTOS = 8;
  private static final double DESPLAZAMIENTO_MINIMO = 0.25;
  private static final String CATEGORIA_CAMA = "Cama";

  private final ServicioPlano2D servicioPlano2D;
  private final ServicioReglasDeUbicacion servicioReglasDeUbicacion;
  private final ServicioZonaExclusion servicioZonaExclusion;
  private final SimetriaDeAmbiente simetria = new SimetriaDeAmbiente();

  @Autowired
  public ServicioRankingDeDistribucionesImpl(
    ServicioPlano2D servicioPlano2D,
    ServicioReglasDeUbicacion servicioReglasDeUbicacion,
    ServicioZonaExclusion servicioZonaExclusion
  ) {
    this.servicioPlano2D = servicioPlano2D;
    this.servicioReglasDeUbicacion = servicioReglasDeUbicacion;
    this.servicioZonaExclusion = servicioZonaExclusion;
  }

  public ServicioRankingDeDistribucionesImpl(
    ServicioPlano2D servicioPlano2D,
    ServicioReglasDeUbicacion servicioReglasDeUbicacion
  ) {
    this(servicioPlano2D, servicioReglasDeUbicacion, ambiente -> null);
  }

  @Override
  public List<OpcionDeDistribucion> generarRanking(Ambiente ambiente, List<Mueble> muebles) {
    List<OpcionDeDistribucion> ranking = new ArrayList<>();
    List<ZonaExclusion> zonasAmbienteOriginal = calcularZonas(ambiente);
    int puntajeMaximo = calcularPuntajeMaximo(muebles);
    for (int indiceSimetria = 0; indiceSimetria < 4; indiceSimetria++) {
      boolean reflejarHorizontalmente = indiceSimetria == 1 || indiceSimetria == 3;
      boolean reflejarVerticalmente = indiceSimetria == 2 || indiceSimetria == 3;
      Ambiente ambienteTransformado = simetria.transformarAmbiente(
        ambiente,
        reflejarHorizontalmente,
        reflejarVerticalmente
      );
      List<ZonaExclusion> zonasAmbienteTransformado = calcularZonas(ambienteTransformado);
      Plano planoTransformado = servicioPlano2D.generarPlano(ambienteTransformado, muebles);
      if (
        !esDistribucionValida(
          planoTransformado,
          muebles.size(),
          ambienteTransformado,
          zonasAmbienteTransformado
        )
      ) {
        continue;
      }
      Plano plano = simetria.transformarPlanoAlAmbienteOriginal(
        planoTransformado,
        ambiente,
        reflejarHorizontalmente,
        reflejarVerticalmente
      );
      agregarSiEsUnaOpcionNueva(ranking, puntuar(plano, ambiente));
      agregarVariantes(ranking, plano, ambiente, zonasAmbienteOriginal, puntajeMaximo);
    }
    ranking.sort(Comparator.comparingInt(OpcionDeDistribucion::getPuntajeTotal).reversed());
    while (ranking.size() > CANTIDAD_MAXIMA_DE_OPCIONES) {
      ranking.remove(ranking.size() - 1);
    }
    return ranking;
  }

  private List<ZonaExclusion> calcularZonas(Ambiente ambiente) {
    return servicioZonaExclusion.calcularZonas(ambiente);
  }

  private boolean esDistribucionValida(
    Plano plano,
    int cantidadMuebles,
    Ambiente ambiente,
    List<ZonaExclusion> zonas
  ) {
    if (plano.getMuebles().size() != cantidadMuebles || !plano.getMueblesExcluidos().isEmpty()) {
      return false;
    }
    List<MuebleUbicado> mueblesValidados = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      if (
        ubicado.isEnConflicto() ||
        !servicioPlano2D.estaDentroDelPerimetro(ubicado, ambiente) ||
        seSuperponeConAlguno(ubicado, mueblesValidados) ||
        !(zonas == null
            ? servicioReglasDeUbicacion.esUbicacionValida(ubicado, ambiente, mueblesValidados)
            : servicioReglasDeUbicacion.esUbicacionValida(
              ubicado,
              ambiente,
              mueblesValidados,
              zonas
            ))
      ) {
        return false;
      }
      mueblesValidados.add(ubicado);
    }
    return true;
  }

  private void agregarVariantes(
    List<OpcionDeDistribucion> ranking,
    Plano plano,
    Ambiente ambiente,
    List<ZonaExclusion> zonas,
    int puntajeMaximo
  ) {
    double desplazamiento = Math.max(ambiente.getAnchoMinimoDePasillo(), DESPLAZAMIENTO_MINIMO);
    for (int indiceMueble = 0; indiceMueble < plano.getMuebles().size(); indiceMueble++) {
      for (int eje = 0; eje < 2; eje++) {
        for (int sentido = -1; sentido <= 1; sentido += 2) {
          if (estaCompleto(ranking, puntajeMaximo)) {
            return;
          }
          agregarVariantesEnEje(
            ranking,
            plano,
            ambiente,
            indiceMueble,
            eje,
            sentido,
            desplazamiento,
            zonas
          );
        }
      }
    }
  }

  private void agregarVariantesEnEje(
    List<OpcionDeDistribucion> ranking,
    Plano plano,
    Ambiente ambiente,
    int indiceMueble,
    int eje,
    int sentido,
    double desplazamiento,
    List<ZonaExclusion> zonas
  ) {
    double anchoDelEje = eje == 0 ? ambiente.getAncho() : ambiente.getLargo();
    for (
      int indiceDesplazamiento = 1;
      indiceDesplazamiento <= CANTIDAD_MAXIMA_DE_DESPLAZAMIENTOS;
      indiceDesplazamiento++
    ) {
      double distancia = desplazamiento * indiceDesplazamiento * sentido;
      Plano variante = desplazarMueble(plano, indiceMueble, eje, distancia);
      if (esVarianteValida(variante, indiceMueble, ambiente, zonas)) {
        agregarSiEsUnaOpcionNueva(ranking, puntuar(variante, ambiente));
      }
      if (Math.abs(distancia) >= anchoDelEje) {
        break;
      }
    }
  }

  // El resto de la distribucion ya era valida: solo se revisa el mueble desplazado.
  private boolean esVarianteValida(
    Plano variante,
    int indiceMueble,
    Ambiente ambiente,
    List<ZonaExclusion> zonas
  ) {
    MuebleUbicado desplazado = variante.getMuebles().get(indiceMueble);
    List<MuebleUbicado> otros = new ArrayList<>();
    for (int indice = 0; indice < variante.getMuebles().size(); indice++) {
      if (indice != indiceMueble) {
        otros.add(variante.getMuebles().get(indice));
      }
    }
    if (
      !servicioPlano2D.estaDentroDelPerimetro(desplazado, ambiente) ||
      seSuperponeConAlguno(desplazado, otros)
    ) {
      return false;
    }
    return zonas == null
      ? servicioReglasDeUbicacion.esUbicacionValida(desplazado, ambiente, otros)
      : servicioReglasDeUbicacion.esUbicacionValida(desplazado, ambiente, otros, zonas);
  }

  private int calcularPuntajeMaximo(List<Mueble> muebles) {
    int puntajeMaximo = 0;
    for (Mueble mueble : muebles) {
      if (mueble.getCategoriaDeMueble() != null && esCama(mueble)) {
        puntajeMaximo += PUNTOS_POR_CUMPLIR_REGLA;
      }
    }
    return puntajeMaximo;
  }

  private boolean estaCompleto(List<OpcionDeDistribucion> ranking, int puntajeMaximo) {
    int cantidadConPuntajeMaximo = 0;
    for (OpcionDeDistribucion opcion : ranking) {
      if (opcion.getPuntajeTotal() >= puntajeMaximo) {
        cantidadConPuntajeMaximo++;
      }
    }
    return cantidadConPuntajeMaximo >= CANTIDAD_MAXIMA_DE_OPCIONES;
  }

  private Plano desplazarMueble(Plano plano, int indiceMueble, int eje, double distancia) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    for (int indice = 0; indice < plano.getMuebles().size(); indice++) {
      MuebleUbicado ubicado = plano.getMuebles().get(indice);
      double posicionX = ubicado.getPosicionX();
      double posicionY = ubicado.getPosicionY();
      if (indice == indiceMueble && eje == 0) {
        posicionX += distancia;
      }
      if (indice == indiceMueble && eje == 1) {
        posicionY += distancia;
      }
      muebles.add(new MuebleUbicado(ubicado.getMueble(), posicionX, posicionY));
    }
    return new Plano(plano.getAmbiente(), muebles, new ArrayList<>(), new ArrayList<>());
  }

  private boolean seSuperponeConAlguno(
    MuebleUbicado candidato,
    List<MuebleUbicado> mueblesUbicados
  ) {
    for (MuebleUbicado otro : mueblesUbicados) {
      boolean seSuperponeEnX =
        candidato.getPosicionX() <
          otro.getPosicionX() + otro.getMueble().getAncho() - ZonaNoDisponible.TOLERANCIA &&
        candidato.getPosicionX() + candidato.getMueble().getAncho() >
          otro.getPosicionX() + ZonaNoDisponible.TOLERANCIA;
      boolean seSuperponeEnY =
        candidato.getPosicionY() <
          otro.getPosicionY() + otro.getMueble().getLargo() - ZonaNoDisponible.TOLERANCIA &&
        candidato.getPosicionY() + candidato.getMueble().getLargo() >
          otro.getPosicionY() + ZonaNoDisponible.TOLERANCIA;
      if (seSuperponeEnX && seSuperponeEnY) {
        return true;
      }
    }
    return false;
  }

  private OpcionDeDistribucion puntuar(Plano plano, Ambiente ambiente) {
    int puntaje = 0;
    List<String> desglose = new ArrayList<>();
    List<String> avisos = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      Mueble mueble = ubicado.getMueble();
      if (mueble.getCategoriaDeMueble() == null) {
        String aviso = servicioReglasDeUbicacion.avisoDeCategoria(mueble);
        if (aviso != null) {
          avisos.add(aviso);
        }
      } else if (esCama(mueble)) {
        if (servicioReglasDeUbicacion.cumpleReglasDeCategoria(ubicado, ambiente)) {
          puntaje += PUNTOS_POR_CUMPLIR_REGLA;
          desglose.add(mueble.getNombre() + ": cama opuesta a la ventana (+1)");
        } else {
          puntaje += PUNTOS_POR_INCUMPLIR_REGLA;
          desglose.add(mueble.getNombre() + ": cama no opuesta a la ventana (-1)");
        }
      }
    }
    return new OpcionDeDistribucion(plano, puntaje, desglose, avisos);
  }

  private boolean esCama(Mueble mueble) {
    return mueble.getCategoriaDeMueble().getNombre().startsWith(CATEGORIA_CAMA);
  }

  private void agregarSiEsUnaOpcionNueva(
    List<OpcionDeDistribucion> ranking,
    OpcionDeDistribucion candidata
  ) {
    for (OpcionDeDistribucion opcion : ranking) {
      if (sonEquivalentes(opcion, candidata)) {
        return;
      }
    }
    ranking.add(candidata);
  }

  private boolean sonEquivalentes(OpcionDeDistribucion primera, OpcionDeDistribucion segunda) {
    Ambiente ambiente = primera.getPlano().getAmbiente();
    if (mismasPosiciones(primera, segunda, ambiente, false, false)) {
      return true;
    }
    if (primera.getPuntajeTotal() != segunda.getPuntajeTotal()) {
      return false;
    }
    return (
      mismasPosiciones(primera, segunda, ambiente, true, false) ||
      mismasPosiciones(primera, segunda, ambiente, false, true) ||
      mismasPosiciones(primera, segunda, ambiente, true, true)
    );
  }

  private boolean mismasPosiciones(
    OpcionDeDistribucion primera,
    OpcionDeDistribucion segunda,
    Ambiente ambiente,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    List<MuebleUbicado> mueblesPrimera = primera.getPlano().getMuebles();
    List<MuebleUbicado> mueblesSegunda = segunda.getPlano().getMuebles();
    if (mueblesPrimera.size() != mueblesSegunda.size()) {
      return false;
    }
    for (int indice = 0; indice < mueblesPrimera.size(); indice++) {
      MuebleUbicado uno = mueblesPrimera.get(indice);
      MuebleUbicado dos = mueblesSegunda.get(indice);
      if (!uno.getMueble().equals(dos.getMueble())) {
        return false;
      }
      double posicionX = reflejarHorizontalmente
        ? ambiente.getAncho() - uno.getPosicionX() - uno.getMueble().getAncho()
        : uno.getPosicionX();
      double posicionY = reflejarVerticalmente
        ? ambiente.getLargo() - uno.getPosicionY() - uno.getMueble().getLargo()
        : uno.getPosicionY();
      if (
        Math.abs(posicionX - dos.getPosicionX()) > ZonaNoDisponible.TOLERANCIA ||
        Math.abs(posicionY - dos.getPosicionY()) > ZonaNoDisponible.TOLERANCIA
      ) {
        return false;
      }
    }
    return true;
  }
}
