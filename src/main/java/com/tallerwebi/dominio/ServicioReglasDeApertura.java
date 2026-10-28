package com.tallerwebi.dominio;

@FunctionalInterface
public interface ServicioReglasDeApertura {
  ReglaDeApertura determinarApertura(Abertura abertura, Ambiente ambiente);
}
