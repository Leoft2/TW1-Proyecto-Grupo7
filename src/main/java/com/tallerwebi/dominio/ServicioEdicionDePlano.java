package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioEdicionDePlano {
  boolean quitarMueble(Plano plano, Integer indice);
}
