package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioRecomendacionPintura {
  RecomendacionPintura recomendar(Ambiente ambiente);
}
