package com.tallerwebi.dominio;

public enum FormaPintura {
  //Techo, izquierda, fondo, derecha

  PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE(0, 0, 1, 0),

  PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE(0, 1, 1, 1),

  PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO(1, 0, 0, 0),

  PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE(0, 1, 0, 1),

  PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO(1, 0, 1, 0),

  PINTAR_TODAS_LAS_PAREDES_POR_LA_PITAD_PARA_ACORTAR_LAS_PAREDES(0, 1, 1, 1),

  PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR(1, 1, 1, 1),

  PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA(1, 1, 0, 1);

  private final int[] superficies;

  FormaPintura(int... superficies) {
    this.superficies = superficies.clone();
  }

  public int[] getSuperficies() {
    return superficies.clone();
  }
}
