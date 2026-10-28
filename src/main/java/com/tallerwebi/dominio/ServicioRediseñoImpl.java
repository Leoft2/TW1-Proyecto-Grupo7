package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioRediseño")
public class ServicioRediseñoImpl implements ServicioRediseño {

  private static final double ALTURA_POR_DEFECTO = 2.7;
  private static final String COLOR_BLANCO_ROTO = "Blanco roto";
  private static final String HEX_BLANCO_ROTO = "#F2F0EB";

  private static final String ZONA_TODO_AMBIENTE = "Todo el ambiente";
  private static final String ZONA_RESTO_AMBIENTE = "Resto del ambiente";

  private RepositorioBaldePintura repositorioBaldePintura;

  @Autowired
  public void setRepositorioBaldePintura(RepositorioBaldePintura repositorioBaldePintura) {
    this.repositorioBaldePintura = repositorioBaldePintura;
  }

  public ServicioRediseñoImpl() {}

  @Override
  public Recomendacion recomendar(Estilo estilo) {
    return switch (estilo) {
      case Retro -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Mostaza", "#D4A017"),
        new Color("Oliva", "#6B7B3A"),
        new Color("Terracota", "#C1663F")
      );
      case Minimalista -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        new Color("Blanco", "#F5F5F2"),
        new Color("Gris perla", "#C9C9C4"),
        new Color("Carbón", "#2B2B2B")
      );
      case Japandi -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Arena", "#D9CBB8"),
        new Color("Madera clara", "#C8A87C"),
        new Color("Salvia", "#9CAF94")
      );
      case Industrial -> crearRecomendacion(
        estilo,
        Iluminacion.FRIA,
        new Color("Cemento", "#8C8C86"),
        new Color("Hierro", "#33322E"),
        new Color("Ladrillo", "#A0522D")
      );
      case Boho -> crearRecomendacion(
        estilo,
        Iluminacion.CALIDA,
        new Color("Terracota", "#C1663F"),
        new Color("Mostaza arena", "#E0B252"),
        new Color("Turquesa", "#4FA3A5")
      );
      case Escandinavo -> crearRecomendacion(
        estilo,
        Iluminacion.INTERMEDIA,
        new Color(COLOR_BLANCO_ROTO, HEX_BLANCO_ROTO),
        new Color("Celeste", "#A8C3D1"),
        new Color("Gris cálido", "#C4B8A8")
      );
    };
  }

  private Recomendacion crearRecomendacion(
    Estilo estilo,
    Iluminacion iluminacion,
    Color... colores
  ) {
    return new Recomendacion(estilo, new ArrayList<>(Arrays.asList(colores)), iluminacion);
  }

  @Override
  public RecomendacionPintura recomendarPintura(Ambiente ambiente) {
    if (ambiente == null) {
      return new RecomendacionPintura();
    }

    Double ancho = obtenerDimension(ambiente.getAncho(), 0.0);
    Double largo = obtenerDimension(ambiente.getLargo(), 0.0);
    Double altura = obtenerDimension(ambiente.getAltura(), ALTURA_POR_DEFECTO);

    RecomendacionPintura recomendacion;

    if (dimensionesInvalidas(ancho, largo)) {
      recomendacion = crearRecomendacionCompacta(ancho, largo, altura);
    } else if (esAmbienteAlargado(ancho, largo)) {
      recomendacion = recomendarParaAmbienteAlargado(ancho, largo, altura);
    } else if (esAmbienteAncho(ancho, largo)) {
      recomendacion = recomendarParaAmbienteAncho(largo, altura);
    } else {
      recomendacion = recomendarParaAmbienteCuadrado(ancho, largo, altura);
    }

    cargarColoresSuperficie(recomendacion);

    return recomendacion;
  }

  private Double obtenerDimension(Double dimension, Double valorDefecto) {
    return dimension == null ? valorDefecto : dimension;
  }

  private boolean dimensionesInvalidas(Double ancho, Double largo) {
    return ancho <= 0 || largo <= 0;
  }

  private boolean esAmbienteAlargado(Double ancho, Double largo) {
    return largo > ancho * 1.5;
  }

  private boolean esAmbienteAncho(Double ancho, Double largo) {
    return ancho > largo * 1.5;
  }

  private RecomendacionPintura crearRecomendacionCompacta(
    Double ancho,
    Double largo,
    Double altura
  ) {
    RecomendacionPintura recomendacion = new RecomendacionPintura();

    recomendacion.setFormaPintura(FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR);
    recomendacion.setDescripcion("Pintar todo de un color para compactar el ambiente.");
    recomendacion.setImagenSugerida("pintar-todo-un-color");

    Double area = 2.0 * (ancho + largo) * altura + ancho * largo;

    agregarItemRecomendado(
      recomendacion,
      "Todo el ambiente",
      area,
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );

    return recomendacion;
  }

  private RecomendacionPintura recomendarParaAmbienteAlargado(
    Double ancho,
    Double largo,
    Double altura
  ) {
    RecomendacionPintura recomendacion = new RecomendacionPintura();

    recomendacion.setFormaPintura(
      FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO
    );
    recomendacion.setDescripcion("Pintar techo y pared opuesta para alargar el ambiente.");
    recomendacion.setImagenSugerida("techo-y-pared-opuesta-alargar");

    agregarItemRecomendado(recomendacion, "Pared opuesta", ancho * altura, "Salvia", "#9CAF94");

    agregarItemRecomendado(
      recomendacion,
      "Techo",
      ancho * largo,
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );

    return recomendacion;
  }

  private RecomendacionPintura recomendarParaAmbienteAncho(Double largo, Double altura) {
    RecomendacionPintura recomendacion = new RecomendacionPintura();

    recomendacion.setFormaPintura(FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE);
    recomendacion.setDescripcion("Pintar paredes opuestas para estrechar el ambiente.");
    recomendacion.setImagenSugerida("paredes-opuestas-estrechar");

    agregarItemRecomendado(recomendacion, "Pared opuesta 1", largo * altura, "Celeste", "#A8C3D1");

    agregarItemRecomendado(recomendacion, "Pared opuesta 2", largo * altura, "Celeste", "#A8C3D1");

    return recomendacion;
  }

  private RecomendacionPintura recomendarParaAmbienteCuadrado(
    Double ancho,
    Double largo,
    Double altura
  ) {
    RecomendacionPintura recomendacion = new RecomendacionPintura();

    recomendacion.setFormaPintura(FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA);
    recomendacion.setDescripcion("Pintar todo menos una pared para destacarla.");
    recomendacion.setImagenSugerida("todo-menos-una-pared-destacar");

    Double perimetro = 2.0 * (ancho + largo);
    Double areaParedes = perimetro * altura;
    Double areaTecho = ancho * largo;
    Double areaADestacar = Math.max(ancho, largo) * altura;

    Double areaARestar = Math.max(0.0, areaParedes + areaTecho - areaADestacar);

    agregarItemRecomendado(
      recomendacion,
      "Resto del ambiente",
      areaARestar,
      COLOR_BLANCO_ROTO,
      HEX_BLANCO_ROTO
    );

    agregarItemRecomendado(
      recomendacion,
      "Pared a destacar",
      areaADestacar,
      "Terracota",
      "#C1663F"
    );

    return recomendacion;
  }

  private void agregarItemRecomendado(
    RecomendacionPintura recomendacion,
    String zona,
    Double metrosCuadrados,
    String colorNombre,
    String hex
  ) {
    if (metrosCuadrados == null || metrosCuadrados <= 0) {
      return;
    }

    RecomendacionPintura.ItemPintura item = new RecomendacionPintura.ItemPintura();

    item.setZona(zona);
    item.setColorNombre(colorNombre);
    item.setHex(hex);
    item.setMetrosCuadrados(metrosCuadrados);

    recomendacion.agregarItem(item);
  }

  private void cargarColoresSuperficie(RecomendacionPintura recomendacion) {
    String[] colores = new String[4];

    List<RecomendacionPintura.ItemPintura> items = recomendacion.getItems();

    if (items == null) {
      return;
    }

    for (RecomendacionPintura.ItemPintura item : items) {
      if (item != null) {
        asignarColorZona(colores, item.getZona(), item.getHex());
      }
    }

    recomendacion.setColorSuperficie(
      new ColorSuperficie(colores[0], colores[1], colores[2], colores[3])
    );
  }

  private void asignarColorZona(String[] colores, String zona, String hex) {
    if (zona == null || hex == null) {
      return;
    }

    if (ZONA_TODO_AMBIENTE.equals(zona)) {
      asignarColorATodasLasSuperficies(colores, hex);
      return;
    }

    if (ZONA_RESTO_AMBIENTE.equals(zona)) {
      asignarColorRestoAmbiente(colores, hex);
      return;
    }

    int indice = obtenerIndiceZona(zona);

    if (indice >= 0) {
      colores[indice] = hex;
    }
  }

  private int obtenerIndiceZona(String zona) {
    switch (zona) {
      case "Techo":
        return 0;
      case "Pared opuesta 1":
        return 1;
      case "Pared opuesta 2":
        return 3;
      case "Pared opuesta":
      case "Pared a destacar":
        return 2;
      default:
        return -1;
    }
  }

  private void asignarColorRestoAmbiente(String[] colores, String hex) {
    colores[0] = hex;
    colores[1] = hex;
    colores[3] = hex;
  }

  private void asignarColorATodasLasSuperficies(String[] colores, String hex) {
    for (int i = 0; i < colores.length; i++) {
      colores[i] = hex;
    }
  }
}
