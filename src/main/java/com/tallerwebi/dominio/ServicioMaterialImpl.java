package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioMaterial")
@Transactional
public class ServicioMaterialImpl implements ServicioMaterial {

  private final RepositorioMaterial repositorioMaterial;

  @Autowired
  public ServicioMaterialImpl(RepositorioMaterial repositorioMaterial) {
    this.repositorioMaterial = repositorioMaterial;
  }

  @Override
  public List<Material> obtenerMateriales() {
    return repositorioMaterial.obtenerTodos();
  }

  @Override
  public Material buscarPorId(Long id) {
    if (id == null) {
      return null;
    }
    return repositorioMaterial.buscarPorId(id);
  }
}
