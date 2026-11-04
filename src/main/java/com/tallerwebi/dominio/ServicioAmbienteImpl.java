package com.tallerwebi.dominio;

import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioAmbiente")
@Transactional
public class ServicioAmbienteImpl implements ServicioAmbiente {

  private final RepositorioAmbiente repositorioAmbiente;

  @Autowired
  public ServicioAmbienteImpl(RepositorioAmbiente repositorioAmbiente) {
    this.repositorioAmbiente = repositorioAmbiente;
  }

  @Override
  public List<Ambiente> obtenerAmbientes() {
    return repositorioAmbiente.obtenerTodos();
  }

  @Override
  public List<Ambiente> obtenerAmbientesDe(String email) {
    return repositorioAmbiente.obtenerAmbientesDe(email);
  }

  @Override
  public Ambiente buscarAmbiente(Long id) {
    if (id == null) {
      return null;
    }

    Ambiente ambiente = repositorioAmbiente.buscarPorId(id);
    if (ambiente != null) {
      // La recomendación de pintura necesita las aberturas fuera de esta transacción.
      Hibernate.initialize(ambiente.getAberturas());
    }
    return ambiente;
  }

  @Override
  public void agregarAbertura(Long ambienteId, Abertura abertura) {
    Ambiente ambiente = repositorioAmbiente.buscarPorId(ambienteId);
    if (ambiente == null) {
      throw new RuntimeException("Ambiente no encontrado");
    }
    ambiente.agregarAbertura(abertura);
  }
}
