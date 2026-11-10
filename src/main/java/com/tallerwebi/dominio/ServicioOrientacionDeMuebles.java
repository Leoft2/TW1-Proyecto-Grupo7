package com.tallerwebi.dominio;

import java.util.Optional;

@FunctionalInterface
public interface ServicioOrientacionDeMuebles {
  Optional<Muro> calcularFrente(MuebleUbicado ubicado, Ambiente ambiente);
}
