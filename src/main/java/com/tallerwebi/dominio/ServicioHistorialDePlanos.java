package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioHistorialDePlanos {
  List<Plano> listarVersiones(String usuarioEmail);
  Plano restaurarVersion(Long planoId, String usuarioEmail);
}
