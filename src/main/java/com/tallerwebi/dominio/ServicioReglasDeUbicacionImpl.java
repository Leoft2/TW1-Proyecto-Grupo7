package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Decide si un mueble puede ocupar una posicion del ambiente y si cumple las reglas de su
 * categoria. Trabaja en metros, con origen en la esquina superior izquierda del plano: X crece
 * hacia la derecha e Y hacia abajo.
 *
 * <p>Las reglas de ubicacion son eliminatorias y no suman puntaje: una ubicacion que tapa una
 * puerta, choca con un obstaculo, se superpone con otro mueble o deja un pasillo o un despeje
 * menor al minimo se descarta. El puntaje lo calcula el ranking de distribuciones.
 */
@Service
public class ServicioReglasDeUbicacionImpl implements ServicioReglasDeUbicacion {

  // Las categorias se reconocen por el prefijo del nombre porque el catalogo las carga como
  // "Cama 2 plazas", "Placard 3 puertas", etc.
  private static final String CATEGORIA_CAMA = "Cama";
  private static final String CATEGORIA_PLACARD = "Placard";
  // Espacio libre que necesita un placard frente a sus puertas. Valor provisorio hasta que se
  // defina la configuracion del pasillo minimo (FU14).
  private static final double DESPEJE_FRONTAL_MINIMO = 0.40;

  private final ServicioZonaExclusion servicioZonaExclusion;
  // Se instancia directo porque no tiene dependencias propias; no se inyecta por constructor.
  private final ServicioColisiones servicioColisiones = new ServicioColisionesImpl();

  public ServicioReglasDeUbicacionImpl(ServicioZonaExclusion servicioZonaExclusion) {
    this.servicioZonaExclusion = servicioZonaExclusion;
  }

  /**
   * Calcula las zonas de exclusion del ambiente una sola vez y delega en la version que las
   * recibe, para no recalcularlas en cada validacion.
   */
  @Override
  public boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados
  ) {
    return esUbicacionValida(
      candidato,
      ambiente,
      mueblesUbicados,
      servicioZonaExclusion.calcularZonas(ambiente)
    );
  }

  /**
   * Reglas eliminatorias: la ubicacion se descarta si tapa una puerta o su radio de giro, si choca
   * con un obstaculo fijo o si deja un pasillo menor al minimo. No suman puntaje.
   *
   * <p>El frente solo se calcula para un placard: es la cara desde la que se abren sus puertas y
   * necesita un despeje propio, distinto del pasillo de circulacion.
   */
  @Override
  public boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas
  ) {
    Muro frente = esPlacard(candidato.getMueble()) ? muroFrontalDe(candidato, ambiente) : null;
    return (
      !tapaUnaPuerta(candidato, zonas) &&
      !tapaUnObstaculo(candidato, ambiente) &&
      !servicioColisiones.hayColision(candidato, mueblesUbicados) &&
      !dejaUnPasilloMenorAlMinimo(candidato, ambiente, mueblesUbicados, frente) &&
      !dejaUnDespejeFrontalMenorAlMinimo(candidato, ambiente, mueblesUbicados, zonas, frente)
    );
  }

  /**
   * Devuelve el despeje frontal del placard en metros, o null si el mueble no es un placard o no
   * tiene un muro de apoyo. Acepta zonas nulas: en ese caso no considera el barrido de puertas.
   */
  @Override
  public Double despejeFrontalDe(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas
  ) {
    if (!esPlacard(candidato.getMueble())) {
      return null;
    }
    Muro frente = muroFrontalDe(candidato, ambiente);
    if (frente == null) {
      return null;
    }
    return despejeHacia(candidato, ambiente, mueblesUbicados, zonas, frente);
  }

  /**
   * Solo la cama tiene una regla de categoria: debe apoyarse en el muro opuesto a una ventana. Un
   * mueble de cualquier otra categoria cumple automaticamente.
   */
  @Override
  public boolean cumpleReglasDeCategoria(MuebleUbicado candidato, Ambiente ambiente) {
    if (!esCama(candidato.getMueble())) {
      return true;
    }
    return estaOpuestaALaVentana(candidato, ambiente);
  }

  /**
   * Devuelve un aviso cuando el mueble no tiene categoria, o null cuando si la tiene y no hay nada
   * que informar.
   */
  @Override
  public String avisoDeCategoria(Mueble mueble) {
    if (mueble.getCategoriaDeMueble() != null) {
      return null;
    }
    return (
      "El mueble " +
      mueble.getNombre() +
      " no tiene categoria. Asignarle una categoria mejora el resultado de la distribucion"
    );
  }

  /** Una puerta bloquea la ubicacion si el mueble se solapa con su zona de exclusion. */
  private boolean tapaUnaPuerta(MuebleUbicado candidato, List<ZonaExclusion> zonas) {
    for (ZonaExclusion zona : zonas) {
      if (zona.getTipo() == TipoAbertura.PUERTA && zona.seSolapaCon(candidato)) {
        return true;
      }
    }
    return false;
  }

  /** Un obstaculo fijo bloquea la ubicacion si el mueble choca con el. */
  private boolean tapaUnObstaculo(MuebleUbicado candidato, Ambiente ambiente) {
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      if (obstaculo.colisionaCon(candidato, ambiente)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Un mueble pegado a otro mueble o a un muro no deja pasillo. Si queda separado, la separacion
   * es un pasillo y debe alcanzar el minimo.
   *
   * <p>La cara frontal de un placard no se evalua como pasillo: ese espacio lo controla el despeje
   * frontal.
   */
  private boolean dejaUnPasilloMenorAlMinimo(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    Muro frente
  ) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();
    double minimo = ambiente.getAnchoMinimoDePasillo();

    if (
      dejaUnPasilloEstrechoConLosMuros(
        izquierda,
        arriba,
        ambiente.getAncho() - derecha,
        ambiente.getLargo() - abajo,
        minimo,
        frente
      )
    ) {
      return true;
    }
    for (MuebleUbicado otro : mueblesUbicados) {
      if (dejaUnPasilloEstrechoCon(candidato, otro, minimo, frente)) {
        return true;
      }
    }
    return false;
  }

  /** Revisa la separacion del mueble con cada uno de los cuatro muros. */
  private boolean dejaUnPasilloEstrechoConLosMuros(
    double izquierda,
    double arriba,
    double derecha,
    double abajo,
    double minimo,
    Muro frente
  ) {
    return (
      esPasilloEstrechoEnLaCara(Muro.IZQUIERDO, izquierda, frente, minimo) ||
      esPasilloEstrechoEnLaCara(Muro.SUPERIOR, arriba, frente, minimo) ||
      esPasilloEstrechoEnLaCara(Muro.DERECHO, derecha, frente, minimo) ||
      esPasilloEstrechoEnLaCara(Muro.INFERIOR, abajo, frente, minimo)
    );
  }

  /** La cara frontal del placard no cuenta como pasillo aunque quede angosta. */
  private boolean esPasilloEstrechoEnLaCara(
    Muro muro,
    double separacion,
    Muro frente,
    double minimo
  ) {
    return muro != frente && esPasilloEstrecho(separacion, minimo);
  }

  /**
   * Compara dos muebles. Si se solapan en X, el hueco entre ellos esta arriba o abajo; si se
   * solapan en Y, esta a izquierda o derecha. Como no hay colision, solo una de las dos
   * separaciones puede ser positiva, por eso Math.max toma el hueco real.
   */
  private boolean dejaUnPasilloEstrechoCon(
    MuebleUbicado candidato,
    MuebleUbicado otro,
    double minimo,
    Muro frente
  ) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();
    double otroIzquierda = otro.getPosicionX();
    double otroArriba = otro.getPosicionY();
    double otroDerecha = otroIzquierda + otro.getMueble().getAncho();
    double otroAbajo = otroArriba + otro.getMueble().getLargo();
    if (seSuperponen(izquierda, derecha, otroIzquierda, otroDerecha)) {
      double separacionInferior = otroArriba - abajo;
      double separacionSuperior = arriba - otroAbajo;
      if (
        esLaCaraFrontal(frente, Muro.INFERIOR, separacionInferior) ||
        esLaCaraFrontal(frente, Muro.SUPERIOR, separacionSuperior)
      ) {
        return false;
      }
      return esPasilloEstrecho(Math.max(separacionInferior, separacionSuperior), minimo);
    }
    if (seSuperponen(arriba, abajo, otroArriba, otroAbajo)) {
      double separacionDerecha = otroIzquierda - derecha;
      double separacionIzquierda = izquierda - otroDerecha;
      if (
        esLaCaraFrontal(frente, Muro.DERECHO, separacionDerecha) ||
        esLaCaraFrontal(frente, Muro.IZQUIERDO, separacionIzquierda)
      ) {
        return false;
      }
      return esPasilloEstrecho(Math.max(separacionDerecha, separacionIzquierda), minimo);
    }
    return false;
  }

  /** Indica si el hueco esta sobre la cara frontal del placard y no sobre un lateral. */
  private boolean esLaCaraFrontal(Muro frente, Muro muro, double separacion) {
    return frente == muro && separacion > -ZonaNoDisponible.TOLERANCIA;
  }

  /**
   * Un hueco pegado (dentro de la tolerancia) no es pasillo: cuenta solo si es positivo y menor al
   * minimo.
   */
  private boolean esPasilloEstrecho(double separacion, double minimo) {
    return (
      separacion > ZonaNoDisponible.TOLERANCIA && separacion < minimo - ZonaNoDisponible.TOLERANCIA
    );
  }

  /**
   * Solo aplica a un placard (frente distinto de null): verifica que quede el espacio minimo libre
   * frente a sus puertas.
   */
  private boolean dejaUnDespejeFrontalMenorAlMinimo(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas,
    Muro frente
  ) {
    if (frente == null) {
      return false;
    }
    return (
      despejeHacia(candidato, ambiente, mueblesUbicados, zonas, frente) <
      DESPEJE_FRONTAL_MINIMO - ZonaNoDisponible.TOLERANCIA
    );
  }

  /**
   * Calcula el despeje real frente al placard: el minimo entre el muro, los demas muebles, los
   * obstaculos y las zonas de puerta.
   */
  private double despejeHacia(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas,
    Muro frente
  ) {
    double despeje = despejeAlMuro(candidato, ambiente, frente);
    for (MuebleUbicado otro : mueblesUbicados) {
      double despejeConOtro = despejeARectangulo(
        candidato,
        otro.getPosicionX(),
        otro.getPosicionY(),
        otro.getMueble().getAncho(),
        otro.getMueble().getLargo(),
        frente
      );
      despeje = Math.min(despeje, despejeConOtro);
    }
    for (Obstaculo obstaculo : ambiente.getObstaculos()) {
      ZonaNoDisponible zona = obstaculo.zonaNoDisponible(ambiente);
      double despejeConObstaculo = despejeARectangulo(
        candidato,
        zona.getPosicionX(),
        zona.getPosicionY(),
        zona.getAncho(),
        zona.getLargo(),
        frente
      );
      despeje = Math.min(despeje, despejeConObstaculo);
    }
    if (zonas != null) {
      for (ZonaExclusion zona : zonas) {
        if (zona.getTipo() != TipoAbertura.PUERTA) {
          continue;
        }
        double despejeConZona = despejeARectangulo(
          candidato,
          zona.getPosicionX(),
          zona.getPosicionY(),
          zona.getAncho(),
          zona.getProfundidad(),
          frente
        );
        despeje = Math.min(despeje, despejeConZona);
      }
    }
    return despeje;
  }

  /** Distancia desde el mueble hasta el muro que tiene enfrente. */
  private double despejeAlMuro(MuebleUbicado candidato, Ambiente ambiente, Muro frente) {
    return switch (frente) {
      case INFERIOR -> ambiente.getLargo() -
      (candidato.getPosicionY() + candidato.getMueble().getLargo());
      case SUPERIOR -> candidato.getPosicionY();
      case DERECHO -> ambiente.getAncho() -
      (candidato.getPosicionX() + candidato.getMueble().getAncho());
      case IZQUIERDO -> candidato.getPosicionX();
    };
  }

  /**
   * Distancia desde el mueble hasta un rectangulo medida sobre el eje perpendicular al frente. Si
   * el rectangulo esta detras o no se proyecta sobre la misma banda, devuelve Double.MAX_VALUE
   * para que no limite el despeje minimo.
   *
   * <p>El eje 1 es vertical (frente superior/inferior) y el 0 horizontal (frente
   * izquierdo/derecho); el otro eje comprueba que ambos se crucen en la banda.
   */
  private double despejeARectangulo(
    MuebleUbicado candidato,
    double izquierda,
    double arriba,
    double ancho,
    double largo,
    Muro frente
  ) {
    double[] candidatoInicio = { candidato.getPosicionX(), candidato.getPosicionY() };
    double[] candidatoTamano = {
      candidato.getMueble().getAncho(),
      candidato.getMueble().getLargo(),
    };
    double[] rectanguloInicio = { izquierda, arriba };
    double[] rectanguloTamano = { ancho, largo };
    int eje = (frente == Muro.SUPERIOR || frente == Muro.INFERIOR) ? 1 : 0;
    int ejeDeProyeccion = 1 - eje;
    // Segun el muro de enfrente, la separacion se mide hacia abajo/derecha o hacia arriba/izquierda.
    boolean haciaElFinal = frente == Muro.INFERIOR || frente == Muro.DERECHO;
    double candidatoFin = candidatoInicio[eje] + candidatoTamano[eje];
    double rectanguloFin = rectanguloInicio[eje] + rectanguloTamano[eje];
    double separacion = haciaElFinal
      ? rectanguloInicio[eje] - candidatoFin
      : candidatoInicio[eje] - rectanguloFin;
    if (
      separacion < -ZonaNoDisponible.TOLERANCIA ||
      !seSuperponen(
        candidatoInicio[ejeDeProyeccion],
        candidatoInicio[ejeDeProyeccion] + candidatoTamano[ejeDeProyeccion],
        rectanguloInicio[ejeDeProyeccion],
        rectanguloInicio[ejeDeProyeccion] + rectanguloTamano[ejeDeProyeccion]
      )
    ) {
      return Double.MAX_VALUE;
    }
    return separacion;
  }

  /** Dos intervalos se superponen solo si el solape supera la tolerancia (tocarse no cuenta). */
  private boolean seSuperponen(double desdeA, double hastaA, double desdeB, double hastaB) {
    return (
      desdeA < hastaB - ZonaNoDisponible.TOLERANCIA && desdeB < hastaA - ZonaNoDisponible.TOLERANCIA
    );
  }

  /** Reconoce la categoria por el prefijo del nombre (ej.: "Cama 2 plazas"). */
  private boolean esCama(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria != null && categoria.getNombre().startsWith(CATEGORIA_CAMA);
  }

  /** Reconoce la categoria por el prefijo del nombre (ej.: "Placard 3 puertas"). */
  private boolean esPlacard(Mueble mueble) {
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    return categoria != null && categoria.getNombre().startsWith(CATEGORIA_PLACARD);
  }

  /** El frente del placard mira al muro opuesto al que lo sostiene. */
  private Muro muroFrontalDe(MuebleUbicado candidato, Ambiente ambiente) {
    Muro muroDeApoyo = muroDeApoyoDe(candidato, ambiente);
    return muroDeApoyo == null ? null : muroOpuestoA(muroDeApoyo);
  }

  /**
   * Devuelve el muro con el que el mueble tiene mas contacto. Un mueble puede tocar dos muros a la
   * vez (por ejemplo, en una esquina) y el de mayor contacto define su apoyo.
   */
  private Muro muroDeApoyoDe(MuebleUbicado candidato, Ambiente ambiente) {
    Muro deApoyo = null;
    double mayorContacto = 0.0;
    for (Muro muro : Muro.values()) {
      if (!tocaElMuro(candidato, muro, ambiente)) {
        continue;
      }
      double contacto = muro == Muro.SUPERIOR || muro == Muro.INFERIOR
        ? candidato.getMueble().getAncho()
        : candidato.getMueble().getLargo();
      if (deApoyo == null || contacto > mayorContacto + ZonaNoDisponible.TOLERANCIA) {
        deApoyo = muro;
        mayorContacto = contacto;
      }
    }
    return deApoyo;
  }

  /**
   * La cama debe apoyarse en el muro opuesto al de una ventana. Si el ambiente no tiene ventanas
   * no hay a que oponerse y la regla se cumple.
   */
  private boolean estaOpuestaALaVentana(MuebleUbicado candidato, Ambiente ambiente) {
    boolean hayVentanas = false;
    for (Abertura abertura : ambiente.getAberturas()) {
      if (abertura.getTipo() == TipoAbertura.VENTANA) {
        hayVentanas = true;
        if (tocaElMuro(candidato, muroOpuestoA(abertura.getMuro()), ambiente)) {
          return true;
        }
      }
    }
    return !hayVentanas;
  }

  /** Devuelve el muro de enfrente: superior-inferior e izquierdo-derecho. */
  private Muro muroOpuestoA(Muro muro) {
    return switch (muro) {
      case SUPERIOR -> Muro.INFERIOR;
      case INFERIOR -> Muro.SUPERIOR;
      case DERECHO -> Muro.IZQUIERDO;
      case IZQUIERDO -> Muro.DERECHO;
    };
  }

  /** Indica si el mueble esta apoyado contra el muro, con margen para el error de redondeo. */
  private boolean tocaElMuro(MuebleUbicado candidato, Muro muro, Ambiente ambiente) {
    double izquierda = candidato.getPosicionX();
    double arriba = candidato.getPosicionY();
    double derecha = izquierda + candidato.getMueble().getAncho();
    double abajo = arriba + candidato.getMueble().getLargo();
    return switch (muro) {
      case SUPERIOR -> arriba <= ZonaNoDisponible.TOLERANCIA;
      case INFERIOR -> abajo >= ambiente.getLargo() - ZonaNoDisponible.TOLERANCIA;
      case IZQUIERDO -> izquierda <= ZonaNoDisponible.TOLERANCIA;
      case DERECHO -> derecha >= ambiente.getAncho() - ZonaNoDisponible.TOLERANCIA;
    };
  }
}
