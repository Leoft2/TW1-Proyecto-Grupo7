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

  @Autowired
  public ServicioRankingDeDistribucionesImpl(
    ServicioPlano2D servicioPlano2D,
    ServicioReglasDeUbicacion servicioReglasDeUbicacion
  ) {
    this.servicioPlano2D = servicioPlano2D;
    this.servicioReglasDeUbicacion = servicioReglasDeUbicacion;
  }

  @Override
  public List<OpcionDeDistribucion> generarRanking(Ambiente ambiente, List<Mueble> muebles) {
    List<OpcionDeDistribucion> ranking = new ArrayList<>();
    for (int indiceSimetria = 0; indiceSimetria < 4; indiceSimetria++) {
      boolean reflejarHorizontalmente = indiceSimetria == 1 || indiceSimetria == 3;
      boolean reflejarVerticalmente = indiceSimetria == 2 || indiceSimetria == 3;
      Ambiente ambienteTransformado = transformarAmbiente(
        ambiente,
        reflejarHorizontalmente,
        reflejarVerticalmente
      );
      Plano planoTransformado = servicioPlano2D.generarPlano(ambienteTransformado, muebles);
      if (!esDistribucionValida(planoTransformado, muebles, ambienteTransformado)) {
        continue;
      }
      Plano plano = transformarPlanoAlAmbienteOriginal(
        planoTransformado,
        ambiente,
        reflejarHorizontalmente,
        reflejarVerticalmente
      );
      agregarSiEsUnaOpcionNueva(ranking, puntuar(plano, ambiente));
      agregarVariantes(ranking, plano, ambiente);
    }
    ranking.sort(Comparator.comparingInt(OpcionDeDistribucion::getPuntajeTotal).reversed());
    while (ranking.size() > CANTIDAD_MAXIMA_DE_OPCIONES) {
      ranking.remove(ranking.size() - 1);
    }
    return ranking;
  }

  private Ambiente transformarAmbiente(
    Ambiente ambiente,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Ambiente transformado = new Ambiente(
      ambiente.getAncho(),
      ambiente.getLargo(),
      ambiente.getTipoDeAmbiente()
    );
    transformado.setAnchoMinimoDePasillo(ambiente.getAnchoMinimoDePasillo());
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      transformado.agregarObstaculo(
        transformarObstaculo(ambiente, obstaculo, reflejarHorizontalmente, reflejarVerticalmente)
      );
    }
    for (Abertura abertura : ambiente.getAberturas()) {
      transformado.agregarAbertura(
        transformarAbertura(ambiente, abertura, reflejarHorizontalmente, reflejarVerticalmente)
      );
    }
    return transformado;
  }

  private Obstaculo transformarObstaculo(
    Ambiente ambiente,
    Obstaculo obstaculo,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro muro = transformarMuro(
      obstaculo.getMuro(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    double posicion = transformarPosicionEnMuro(
      ambiente,
      obstaculo.getMuro(),
      obstaculo.getPosicion(),
      obstaculo.getAncho(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    return new Obstaculo(
      obstaculo.getTipo(),
      muro,
      posicion,
      obstaculo.getAncho(),
      obstaculo.getProfundidad()
    );
  }

  private Abertura transformarAbertura(
    Ambiente ambiente,
    Abertura abertura,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro muro = transformarMuro(abertura.getMuro(), reflejarHorizontalmente, reflejarVerticalmente);
    double posicion = transformarPosicionEnMuro(
      ambiente,
      abertura.getMuro(),
      abertura.getPosicion(),
      abertura.getAncho(),
      reflejarHorizontalmente,
      reflejarVerticalmente
    );
    return new Abertura(
      abertura.getTipo(),
      muro,
      posicion,
      abertura.getAncho(),
      abertura.getRadioDeGiro()
    );
  }

  private Muro transformarMuro(
    Muro muro,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    Muro resultado = muro;
    if (reflejarHorizontalmente) {
      resultado =
        switch (resultado) {
          case DERECHO -> Muro.IZQUIERDO;
          case IZQUIERDO -> Muro.DERECHO;
          default -> resultado;
        };
    }
    if (reflejarVerticalmente) {
      resultado =
        switch (resultado) {
          case SUPERIOR -> Muro.INFERIOR;
          case INFERIOR -> Muro.SUPERIOR;
          default -> resultado;
        };
    }
    return resultado;
  }

  private double transformarPosicionEnMuro(
    Ambiente ambiente,
    Muro muro,
    Double posicion,
    Double ancho,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    double resultado = posicion;
    if (reflejarHorizontalmente && (muro == Muro.SUPERIOR || muro == Muro.INFERIOR)) {
      resultado = ambiente.getAncho() - resultado - ancho;
    }
    if (reflejarVerticalmente && (muro == Muro.DERECHO || muro == Muro.IZQUIERDO)) {
      resultado = ambiente.getLargo() - resultado - ancho;
    }
    return resultado;
  }

  private boolean esDistribucionValida(Plano plano, List<Mueble> muebles, Ambiente ambiente) {
    if (plano.getMuebles().size() != muebles.size() || !plano.getMueblesExcluidos().isEmpty()) {
      return false;
    }
    List<MuebleUbicado> mueblesValidados = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      if (
        ubicado.isEnConflicto() ||
        !servicioPlano2D.estaDentroDelPerimetro(ubicado, ambiente) ||
        seSuperponeConAlguno(ubicado, mueblesValidados) ||
        !servicioReglasDeUbicacion.esUbicacionValida(ubicado, ambiente, mueblesValidados)
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
    Ambiente ambiente
  ) {
    double desplazamiento = Math.max(ambiente.getAnchoMinimoDePasillo(), DESPLAZAMIENTO_MINIMO);
    for (int indiceMueble = 0; indiceMueble < plano.getMuebles().size(); indiceMueble++) {
      for (int eje = 0; eje < 2; eje++) {
        for (int sentido = -1; sentido <= 1; sentido += 2) {
          agregarVariantesEnEje(
            ranking,
            plano,
            ambiente,
            indiceMueble,
            eje,
            sentido,
            desplazamiento
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
    double desplazamiento
  ) {
    double anchoDelEje = eje == 0 ? ambiente.getAncho() : ambiente.getLargo();
    for (
      int indiceDesplazamiento = 1;
      indiceDesplazamiento <= CANTIDAD_MAXIMA_DE_DESPLAZAMIENTOS;
      indiceDesplazamiento++
    ) {
      double distancia = desplazamiento * indiceDesplazamiento * sentido;
      Plano variante = desplazarMueble(plano, indiceMueble, eje, distancia);
      if (esDistribucionValida(variante, obtenerMuebles(variante), ambiente)) {
        agregarSiEsUnaOpcionNueva(ranking, puntuar(variante, ambiente));
      }
      if (Math.abs(distancia) >= anchoDelEje) {
        break;
      }
    }
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

  private List<Mueble> obtenerMuebles(Plano plano) {
    List<Mueble> muebles = new ArrayList<>();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      muebles.add(ubicado.getMueble());
    }
    return muebles;
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

  private Plano transformarPlanoAlAmbienteOriginal(
    Plano planoTransformado,
    Ambiente ambienteOriginal,
    boolean reflejarHorizontalmente,
    boolean reflejarVerticalmente
  ) {
    List<MuebleUbicado> muebles = new ArrayList<>();
    for (MuebleUbicado ubicado : planoTransformado.getMuebles()) {
      double posicionX = ubicado.getPosicionX();
      double posicionY = ubicado.getPosicionY();
      if (reflejarHorizontalmente) {
        posicionX = ambienteOriginal.getAncho() - posicionX - ubicado.getMueble().getAncho();
      }
      if (reflejarVerticalmente) {
        posicionY = ambienteOriginal.getLargo() - posicionY - ubicado.getMueble().getLargo();
      }
      muebles.add(new MuebleUbicado(ubicado.getMueble(), posicionX, posicionY));
    }
    Plano plano = new Plano(ambienteOriginal, muebles, new ArrayList<>(), new ArrayList<>());
    plano.setMensaje(planoTransformado.getMensaje());
    return plano;
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
