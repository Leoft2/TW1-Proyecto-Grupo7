package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Dos constructores a proposito: el de {@link RepositorioCatalogo} lo usa Spring
 * (marcado con @Autowired), y el de lista en memoria lo usan los tests unitarios del
 * servicio (AGENTS.md: no mockear repositorios en tests de servicio).
 * <p>
 * No hay @Transactional: el controlador invoca los metodos desde una peticion web y
 * la SessionFactory se encarga de la sesion por peticion.
 */
@Service("servicioEdicionInventarioMuebles")
public class ServicioEdicionInventarioMueblesImpl implements ServicioEdicionInventarioMuebles {

  private final List<Mueble> inventario;
  private final RepositorioCatalogo repositorioCatalogo;

  public ServicioEdicionInventarioMueblesImpl(List<Mueble> inventario) {
    this.inventario = inventario;
    this.repositorioCatalogo = null;
  }

  @Autowired
  public ServicioEdicionInventarioMueblesImpl(RepositorioCatalogo repositorioCatalogo) {
    this.repositorioCatalogo = repositorioCatalogo;
    this.inventario = null;
  }

  @Override
  public void actualizar(int id, Mueble datos) throws MuebleNoEncontrado {
    Mueble mueble = this.buscarPorId(id);
    mueble.setNombre(datos.getNombre());
    mueble.setAncho(datos.getAncho());
    mueble.setLargo(datos.getLargo());
    mueble.setDescripcion(datos.getDescripcion());
    mueble.setPrecio(datos.getPrecio());
    mueble.setEstilo(datos.getEstilo());
    if (this.repositorioCatalogo != null) {
      this.repositorioCatalogo.guardarMueble(mueble);
    }
  }

  @Override
  public void eliminar(int id) throws MuebleNoEncontrado {
    this.buscarPorId(id);
    if (this.repositorioCatalogo != null) {
      this.repositorioCatalogo.eliminar(id);
    } else {
      this.inventario.removeIf(mueble -> mueble.getId() == id);
    }
  }

  @Override
  public List<Mueble> listar() {
    if (this.repositorioCatalogo != null) {
      return this.repositorioCatalogo.ObtenerTodosLosMuebles();
    }
    return this.inventario;
  }

  @Override
  public void crear(Mueble mueble) {
    if (this.repositorioCatalogo != null) {
      this.repositorioCatalogo.guardarMueble(mueble);
    } else {
      this.inventario.add(mueble);
    }
  }

  @Override
  public Mueble buscarPorId(int id) throws MuebleNoEncontrado {
    if (this.repositorioCatalogo != null) {
      Mueble mueble = this.repositorioCatalogo.ObtenerMueblePorId(id);
      if (mueble != null) {
        return mueble;
      }
      throw new MuebleNoEncontrado("No se encontro un mueble con el id " + id);
    }
    for (Mueble mueble : this.inventario) {
      if (mueble.getId() == id) {
        return mueble;
      }
    }
    throw new MuebleNoEncontrado("No se encontro un mueble con el id " + id);
  }
}
