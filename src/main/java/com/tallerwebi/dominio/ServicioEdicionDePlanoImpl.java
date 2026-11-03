package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioEdicionDePlanoImpl implements ServicioEdicionDePlano {

  @Override
  public boolean quitarMueble(Plano plano, Integer indice) {
    if (!esIndiceValido(plano, indice)) {
      return false;
    }

    MuebleUbicado muebleUbicado = plano.getMuebles().get(indice);
    if (muebleUbicado == null || muebleUbicado.getMueble() == null) {
      return false;
    }

    agregarMueblesDelPlanoAlAmbiente(plano);
    plano.getMuebles().remove(indice.intValue());
    if (!elMuebleSigueEnElPlano(plano.getMuebles(), muebleUbicado.getMueble())) {
      quitarMuebleDelAmbiente(plano.getAmbiente().getMuebles(), muebleUbicado.getMueble());
    }
    return true;
  }

  private boolean esIndiceValido(Plano plano, Integer indice) {
    return (
      plano != null &&
      plano.getAmbiente() != null &&
      plano.getMuebles() != null &&
      indice != null &&
      indice >= 0 &&
      indice < plano.getMuebles().size()
    );
  }

  private void agregarMueblesDelPlanoAlAmbiente(Plano plano) {
    List<Mueble> mueblesDelAmbiente = plano.getAmbiente().getMuebles();
    for (MuebleUbicado ubicado : plano.getMuebles()) {
      if (ubicado != null && ubicado.getMueble() != null) {
        if (!contieneMueble(mueblesDelAmbiente, ubicado.getMueble())) {
          mueblesDelAmbiente.add(ubicado.getMueble());
        }
      }
    }
  }

  private boolean contieneMueble(List<Mueble> muebles, Mueble candidato) {
    for (Mueble mueble : muebles) {
      if (sonElMismoMueble(mueble, candidato)) {
        return true;
      }
    }
    return false;
  }

  private boolean elMuebleSigueEnElPlano(List<MuebleUbicado> ubicados, Mueble mueble) {
    for (MuebleUbicado ubicado : ubicados) {
      if (
        ubicado != null &&
        ubicado.getMueble() != null &&
        sonElMismoMueble(ubicado.getMueble(), mueble)
      ) {
        return true;
      }
    }
    return false;
  }

  private void quitarMuebleDelAmbiente(List<Mueble> muebles, Mueble muebleAEliminar) {
    for (int indice = muebles.size() - 1; indice >= 0; indice--) {
      if (sonElMismoMueble(muebles.get(indice), muebleAEliminar)) {
        muebles.remove(indice);
      }
    }
  }

  private boolean sonElMismoMueble(Mueble primero, Mueble segundo) {
    if (primero != null && primero.equals(segundo)) {
      return true;
    }
    return (
      primero != null &&
      segundo != null &&
      primero.getId() > 0 &&
      primero.getId() == segundo.getId()
    );
  }
}
