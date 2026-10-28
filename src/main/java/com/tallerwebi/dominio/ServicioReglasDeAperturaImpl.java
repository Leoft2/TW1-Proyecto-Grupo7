package com.tallerwebi.dominio;

import org.springframework.stereotype.Service;

@Service
public class ServicioReglasDeAperturaImpl implements ServicioReglasDeApertura {

  @Override
  public ReglaDeApertura determinarApertura(Abertura abertura, Ambiente ambiente) {
    if (abertura.getTipo() != TipoAbertura.PUERTA) {
      throw new IllegalArgumentException("La abertura debe ser una puerta");
    }
    LadoDeLaHoja ladoElegido = elegirLadoLibre(abertura, ambiente);
    Muro sentido = sentidoDeApertura(abertura, ladoElegido);
    LadoDeLaHoja ladoDeLaManilla = ladoOpuesto(ladoElegido);
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
    return new ReglaDeApertura(sentido, ladoElegido, ladoDeLaManilla, zona);
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

  private LadoDeLaHoja elegirLadoLibre(Abertura abertura, Ambiente ambiente) {
    Double radio = radioEfectivo(abertura);
    Double distanciaInicio = abertura.getPosicion();
    Double largoDelMuro = abertura.getMuro().largoEn(ambiente);
    Double distanciaFin = largoDelMuro - (abertura.getPosicion() + abertura.getAncho());
    boolean inicioLibre = distanciaInicio >= radio;
    boolean finLibre = distanciaFin >= radio;
    if (!inicioLibre && !finLibre) {
      throw new IllegalArgumentException("La puerta no tiene ninguna pared libre para abrir");
    }
    if (inicioLibre && finLibre) {
      return distanciaInicio <= distanciaFin ? LadoDeLaHoja.INICIO : LadoDeLaHoja.FIN;
    }
    return inicioLibre ? LadoDeLaHoja.INICIO : LadoDeLaHoja.FIN;
  }

  private Double radioEfectivo(Abertura abertura) {
    if (abertura.getRadioDeGiro() != null) {
      return abertura.getRadioDeGiro();
    }
    return abertura.getAncho();
  }
}
