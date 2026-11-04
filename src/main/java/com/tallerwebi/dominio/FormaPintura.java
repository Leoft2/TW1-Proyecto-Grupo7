package com.tallerwebi.dominio;

public enum FormaPintura {
  //Techo, izquierda, fondo, derecha

  PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE(
    "Pared opuesta a la puerta",
    "Pintá la pared opuesta a la puerta de un color más oscuro para acortar visualmente el ambiente.",
    0,
    0,
    1,
    0
  ),

  PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE(
    "Todas las paredes de un color",
    "Pintá las tres paredes del mismo color para reducir la sensación de altura y dar continuidad.",
    0,
    1,
    1,
    1
  ),

  PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO(
    "Techo de un color",
    "Pintá el techo de un tono más oscuro que las paredes para bajarlo visualmente.",
    1,
    0,
    0,
    0
  ),

  PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE(
    "Paredes opuestas",
    "Pintá las dos paredes opuestas del mismo color para estrechar el ambiente.",
    0,
    1,
    0,
    1
  ),

  PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO(
    "Techo y pared opuesta",
    "Pintá el techo y la pared del fondo del mismo tono para alargar el ambiente.",
    1,
    0,
    1,
    0
  ),

  PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES(
    "Paredes por la mitad",
    "Pintá la mitad inferior de las paredes de un color para acortar visualmente su altura.",
    0,
    1,
    1,
    1
  ),

  PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR(
    "Todo de un color",
    "Pintá todo el ambiente de un mismo color para compactarlo y unificar el espacio.",
    1,
    1,
    1,
    1
  ),

  PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA(
    "Todo menos una pared",
    "Pintá todo el ambiente de un color neutro y destacá una sola pared con un tono protagonista.",
    1,
    1,
    0,
    1
  );

  private final String etiqueta;
  private final String descripcion;
  private final int[] superficies;

  FormaPintura(String etiqueta, String descripcion, int... superficies) {
    this.etiqueta = etiqueta;
    this.descripcion = descripcion;
    this.superficies = superficies.clone();
  }

  public String getEtiqueta() {
    return etiqueta;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public int[] getSuperficies() {
    return superficies.clone();
  }
}
