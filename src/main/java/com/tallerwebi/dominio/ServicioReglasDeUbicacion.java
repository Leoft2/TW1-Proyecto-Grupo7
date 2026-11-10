package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioReglasDeUbicacion {
  boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados
  );

  boolean esUbicacionValida(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas
  );

  Double despejeFrontalDe(
    MuebleUbicado candidato,
    Ambiente ambiente,
    List<MuebleUbicado> mueblesUbicados,
    List<ZonaExclusion> zonas
  );

  boolean cumpleReglasDeCategoria(MuebleUbicado candidato, Ambiente ambiente);

  String avisoDeCategoria(Mueble mueble);
}
