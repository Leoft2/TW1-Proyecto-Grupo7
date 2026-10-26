package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioReglasDeUbicacion {
  boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados
  );

  boolean cumpleReglasDeCategoria(MuebleUbicado candidato, Ambiente ambiente);

  String avisoDeCategoria(Mueble mueble);
}
