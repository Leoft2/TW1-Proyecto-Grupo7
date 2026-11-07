package com.tallerwebi.infraestructura;

import org.jsoup.nodes.Document;

@FunctionalInterface
public interface ConexionColorshop {
  Document conectar(String url) throws Exception;
}
