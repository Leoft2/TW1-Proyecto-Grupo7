package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de edicion y eliminacion de muebles del inventario (HU-23).
 */
@Service
@Transactional
public class ServicioEdicionInventarioMueblesImpl implements ServicioEdicionInventarioMuebles {

  private final RepositorioInventarioMueble repositorioInventarioMueble;

  @Autowired
  public ServicioEdicionInventarioMueblesImpl(
    RepositorioInventarioMueble repositorioInventarioMueble
  ) {
    this.repositorioInventarioMueble = repositorioInventarioMueble;
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
    mueble.setCategoriaDeMueble(datos.getCategoriaDeMueble());
    mueble.setMaterial(datos.getMaterial());
    this.repositorioInventarioMueble.guardar(mueble);
  }

  @Override
  public void eliminar(int id) throws MuebleNoEncontrado {
    this.buscarPorId(id);
    this.repositorioInventarioMueble.eliminar(id);
  }

  @Override
  public List<Mueble> listar() {
    return this.repositorioInventarioMueble.listarTodos();
  }

  @Override
  public void crear(Mueble mueble) {
    this.repositorioInventarioMueble.guardar(mueble);
  }

  @Override
  public Mueble buscarPorId(int id) throws MuebleNoEncontrado {
    Mueble mueble = this.repositorioInventarioMueble.buscarPorId(id);
    if (mueble == null) {
      throw new MuebleNoEncontrado("No se encontro un mueble con el id " + id);
    }
    return mueble;
  }

  @Override
  // Se suprime PMD.CompareObjectsWithEquals: el chequeo por identidad (==) es intencional y
  // forma parte del patrón canónico de equals/sonIguales (atajo eficiente cuando es la misma instancia).
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public boolean sonIguales(Mueble muebleUno, Mueble muebleDos) {
    if (muebleUno == muebleDos) {
      return true;
    }
    if (muebleUno == null || muebleDos == null) {
      return false;
    }
    return (
      Objects.equals(muebleUno.getAncho(), muebleDos.getAncho()) &&
      Objects.equals(muebleUno.getLargo(), muebleDos.getLargo()) &&
      Double.compare(muebleUno.getPrecio(), muebleDos.getPrecio()) == 0 &&
      Objects.equals(muebleUno.getNombre(), muebleDos.getNombre()) &&
      Objects.equals(muebleUno.getDescripcion(), muebleDos.getDescripcion()) &&
      muebleUno.getEstilo() == muebleDos.getEstilo()
    );
  }
}
