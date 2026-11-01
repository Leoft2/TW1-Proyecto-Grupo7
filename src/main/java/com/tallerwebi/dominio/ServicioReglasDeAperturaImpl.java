package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

/***
Servicio que determina las reglas de apertura de una puerta según su posición y el ambiente en el que se encuentra.
 */

@Service
public class ServicioReglasDeAperturaImpl implements ServicioReglasDeApertura {

  @Override
  public ReglaDeApertura determinarApertura(Abertura abertura, Ambiente ambiente) {
    // Este servicio calcula reglas únicamente para puertas, no para ventanas.
    if (abertura.getTipo() != TipoAbertura.PUERTA) {
      throw new IllegalArgumentException("La abertura debe ser una puerta");
    }

    // Se mide cuánto espacio queda antes y después de la abertura sobre su muro.
    Double radio = radioEfectivo(abertura);
    Double distanciaInicio = abertura.getPosicion();
    Double distanciaFin =
      abertura.getMuro().largoEn(ambiente) - (abertura.getPosicion() + abertura.getAncho());

    // Un lado es libre cuando puede contener el giro completo de la puerta.
    boolean inicioLibre = distanciaInicio >= radio;
    boolean finLibre = distanciaFin >= radio;
    boolean hayParedLibre = inicioLibre || finLibre;

    // Se elige el lado de la bisagra según el espacio disponible.
    LadoDeLaHoja ladoElegido = elegirLado(distanciaInicio, distanciaFin, inicioLibre, finLibre);
    Muro sentido = sentidoDeApertura(abertura, ladoElegido);
    LadoDeLaHoja ladoDeLaManilla = ladoOpuesto(ladoElegido);

    // La zona de barrido representa el sector de 90 grados que ocupa la hoja al abrirse.
    Double centroX = centroX(abertura, ambiente, ladoElegido);
    Double anguloInicial = anguloInicial(abertura, ladoElegido);
    Double anguloFinal = anguloInicial + 90.0;
    ZonaDeBarrido zona = new ZonaDeBarrido(
      centroX,
      centroY(abertura, ambiente, ladoElegido),
      radioEfectivo(abertura),
      anguloInicial,
      anguloFinal
    );
    return new ReglaDeApertura(sentido, ladoElegido, ladoDeLaManilla, zona, hayParedLibre);
  }

  private Double centroX(Abertura abertura, Ambiente ambiente, LadoDeLaHoja lado) {
    if (abertura.getMuro() == Muro.DERECHO) {
      return ambiente.getAncho();
    }
    if (abertura.getMuro() == Muro.IZQUIERDO) {
      return 0.0;
    }
    return abertura.getPosicion() + (lado == LadoDeLaHoja.FIN ? abertura.getAncho() : 0.0);
  }

  private Muro sentidoDeApertura(Abertura abertura, LadoDeLaHoja lado) {
    if (abertura.getMuro() == Muro.DERECHO || abertura.getMuro() == Muro.IZQUIERDO) {
      return lado == LadoDeLaHoja.INICIO ? Muro.SUPERIOR : Muro.INFERIOR;
    }
    return lado == LadoDeLaHoja.INICIO ? Muro.IZQUIERDO : Muro.DERECHO;
  }

  private Double anguloInicial(Abertura abertura, LadoDeLaHoja lado) {
    if (abertura.getMuro() == Muro.INFERIOR) {
      return lado == LadoDeLaHoja.INICIO ? 270.0 : 180.0;
    }
    if (abertura.getMuro() == Muro.DERECHO) {
      return lado == LadoDeLaHoja.INICIO ? 90.0 : 180.0;
    }
    if (abertura.getMuro() == Muro.IZQUIERDO) {
      return lado == LadoDeLaHoja.INICIO ? 0.0 : 270.0;
    }
    return lado == LadoDeLaHoja.INICIO ? 0.0 : 90.0;
  }

  private Double centroY(Abertura abertura, Ambiente ambiente, LadoDeLaHoja lado) {
    if (abertura.getMuro() == Muro.INFERIOR) {
      return ambiente.getLargo();
    }
    if (abertura.getMuro() == Muro.DERECHO || abertura.getMuro() == Muro.IZQUIERDO) {
      return abertura.getPosicion() + (lado == LadoDeLaHoja.FIN ? abertura.getAncho() : 0.0);
    }
    return 0.0;
  }

  private LadoDeLaHoja ladoOpuesto(LadoDeLaHoja lado) {
    return lado == LadoDeLaHoja.INICIO ? LadoDeLaHoja.FIN : LadoDeLaHoja.INICIO;
  }

  private LadoDeLaHoja elegirLado(
    Double distanciaInicio,
    Double distanciaFin,
    boolean inicioLibre,
    boolean finLibre
  ) {
    // Si ningún lado alcanza el radio, se elige el que tenga más espacio como alternativa.
    if (!inicioLibre && !finLibre) {
      return distanciaInicio >= distanciaFin ? LadoDeLaHoja.INICIO : LadoDeLaHoja.FIN;
    }

    // Si ambos lados son posibles, se deja libre el lado con más espacio.
    if (inicioLibre && finLibre) {
      return distanciaInicio <= distanciaFin ? LadoDeLaHoja.INICIO : LadoDeLaHoja.FIN;
    }

    // Si solo un lado es posible, la bisagra se coloca en ese lado.
    return inicioLibre ? LadoDeLaHoja.INICIO : LadoDeLaHoja.FIN;
  }

  private Double radioEfectivo(Abertura abertura) {
    // El radio configurado tiene prioridad; si falta, se toma el ancho de la puerta.
    if (abertura.getRadioDeGiro() != null) {
      return abertura.getRadioDeGiro();
    }
    return abertura.getAncho();
  }
}
