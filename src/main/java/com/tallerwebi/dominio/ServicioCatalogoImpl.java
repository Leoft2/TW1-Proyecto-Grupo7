package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.dominio.excepcion.PresupuestoNuloException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioCatalogo")
@Transactional
public class ServicioCatalogoImpl implements ServicioCatalogo {

  private RepositorioCatalogo repositorioCatalogo;

  @Autowired
  public ServicioCatalogoImpl(RepositorioCatalogo repositorioCatalogo) {
    this.repositorioCatalogo = repositorioCatalogo;
  }

  @Override
  public List<Mueble> ObtenerMueblesConUnPrecioMenorAl(Double precioPresupuestoMaximo)
    throws PresupuestoNegativoException, PresupuestoNuloException {
    if (precioPresupuestoMaximo == null) {
      throw new PresupuestoNuloException("El presupuesto máximo no puede ser nulo");
    }

    if (precioPresupuestoMaximo < 0) {
      throw new PresupuestoNegativoException("El presupuesto máximo no puede ser negativo");
    }

    return this.repositorioCatalogo.ObtenerMueblesConPrecioMenorAl(precioPresupuestoMaximo);
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilos(List<Estilo> estilos) {
    return this.repositorioCatalogo.ObtenerMueblesDeEstilos(estilos);
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilo(Estilo estilo) {
    return this.repositorioCatalogo.ObtenerMueblesDeEstilo(estilo);
  }

  @Override
  public Mueble ObtenerMuebleMasBaratoDeCategoria(Estilo categoria) {
    return this.repositorioCatalogo.ObtenerMuebleMasBaratoDeEstilo(categoria);
  }

  @Override
  public void RegistrarMueble(Mueble mueble) {
    this.repositorioCatalogo.guardarMueble(mueble);
  }

  @Override
  public void RegistrarMuebleDelCatalogo(Mueble mueble) throws ValidacionException {
    this.validarMuebleDelCatalogo(mueble);
    mueble.setEnCatalogo(true);
    this.repositorioCatalogo.guardarMueble(mueble);
  }

  @Override
  public TipoDeAmbiente buscarTipoDeAmbientePorNombre(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
      return null;
    }
    TipoDeAmbiente tipoDeAmbiente = this.repositorioCatalogo.buscarTipoDeAmbientePorNombre(nombre);
    if (tipoDeAmbiente == null) {
      tipoDeAmbiente = new TipoDeAmbiente(nombre);
      this.repositorioCatalogo.guardarTipoDeAmbiente(tipoDeAmbiente);
    }
    return tipoDeAmbiente;
  }

  @Override
  public List<Mueble> ObtenerMueblesOrdenadosPorPrecioAsc() {
    return this.repositorioCatalogo.ObtenerMueblesDelCatalogo(null, null, null, null, null, null);
  }

  @Override
  public List<Mueble> ObtenerMueblesOrdenadosPorPrecioDesc() {
    return this.repositorioCatalogo.ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
        null,
        null,
        null,
        null,
        null,
        null
      );
  }

  @Override
  public List<Mueble> ObtenerMueblesQueCumplan(
    TipoDeAmbiente tipoDeAmbiente,
    Estilo estilo,
    Double precioMaximo,
    Boolean soloSostenibles,
    Long materialId
  ) throws PresupuestoNegativoException {
    this.validarPresupuestos(null, precioMaximo);
    return this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
        null,
        precioMaximo,
        estilo,
        this.nombreDelTipoDeAmbiente(tipoDeAmbiente),
        soloSostenibles,
        materialId
      );
  }

  @Override
  public List<Mueble> ObtenerMueblesFiltrados(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    TipoDeAmbiente tipoDeAmbiente,
    OrdenDeMuebles orden,
    Boolean soloSostenibles,
    Long materialId
  ) throws PresupuestoNegativoException {
    this.validarPresupuestos(precioMinimo, precioMaximo);
    if (orden == OrdenDeMuebles.PRECIO_DESC) {
      return this.repositorioCatalogo.ObtenerMueblesDelCatalogoOrdenadosPorPrecioDesc(
          precioMinimo,
          precioMaximo,
          estilo,
          this.nombreDelTipoDeAmbiente(tipoDeAmbiente),
          soloSostenibles,
          materialId
        );
    }
    return this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
        precioMinimo,
        precioMaximo,
        estilo,
        this.nombreDelTipoDeAmbiente(tipoDeAmbiente),
        soloSostenibles,
        materialId
      );
  }

  @Override
  public Mueble ObtenerMueblePorId(int id) throws MuebleNoEncontrado {
    Mueble mueble = this.repositorioCatalogo.ObtenerMueblePorId(id);
    if (mueble == null) {
      throw new MuebleNoEncontrado("Mueble no encontrado");
    }
    return mueble;
  }

  @Override
  public void modificarMuebleDelCatalogo(int id, Mueble datos)
    throws ValidacionException, MuebleNoEncontrado {
    Mueble mueble = this.ObtenerMueblePorId(id);
    this.validarMuebleDelCatalogo(datos);
    this.copiarDatosDelCatalogo(mueble, datos);
    this.repositorioCatalogo.guardarMueble(mueble);
  }

  @Override
  public void eliminarMuebleDelCatalogo(int id) throws MuebleNoEncontrado {
    this.ObtenerMueblePorId(id);
    this.repositorioCatalogo.eliminar(id);
  }

  private String nombreDelTipoDeAmbiente(TipoDeAmbiente tipoDeAmbiente) {
    if (tipoDeAmbiente == null) {
      return null;
    }
    return tipoDeAmbiente.getNombre();
  }

  private void copiarDatosDelCatalogo(Mueble mueble, Mueble datos) {
    mueble.setNombre(datos.getNombre());
    mueble.setDescripcion(datos.getDescripcion());
    mueble.setPrecio(datos.getPrecio());
    mueble.setEstilo(datos.getEstilo());
    mueble.setCategoriaDeMueble(datos.getCategoriaDeMueble());
    mueble.setAncho(datos.getAncho());
    mueble.setLargo(datos.getLargo());
    mueble.setTiposDeAmbiente(datos.getTiposDeAmbiente());
    mueble.setMaterial(datos.getMaterial());
  }

  private void validarMuebleDelCatalogo(Mueble mueble) throws ValidacionException {
    this.validarNombreDelCatalogo(mueble.getNombre());
    this.validarPrecioDelCatalogo(mueble.getPrecio());
    this.validarEstiloDelCatalogo(mueble.getEstilo());
    this.validarMedidaDelCatalogo(mueble.getAncho(), "ancho");
    this.validarMedidaDelCatalogo(mueble.getLargo(), "largo");
    if (mueble.getCategoriaDeMueble() == null) {
      throw new ValidacionException("El mueble debe tener una categoría");
    }
    if (mueble.getTiposDeAmbiente().isEmpty()) {
      throw new ValidacionException("El mueble debe indicar al menos un tipo de ambiente");
    }
  }

  private void validarMedidaDelCatalogo(Double medida, String nombreDeLaMedida)
    throws ValidacionException {
    if (medida == null || medida <= 0) {
      throw new ValidacionException("El " + nombreDeLaMedida + " del mueble debe ser mayor a cero");
    }
  }

  private void validarNombreDelCatalogo(String nombre) throws ValidacionException {
    if (nombre == null || nombre.trim().isEmpty()) {
      throw new ValidacionException("El nombre del mueble no puede estar vacío");
    }
  }

  private void validarPrecioDelCatalogo(double precio) throws ValidacionException {
    if (precio <= 0) {
      throw new ValidacionException("El precio del mueble debe ser mayor a cero");
    }
  }

  private void validarEstiloDelCatalogo(Estilo estilo) throws ValidacionException {
    if (estilo == null) {
      throw new ValidacionException("El mueble debe tener un estilo");
    }
  }

  private void validarPresupuestos(Double precioMinimo, Double precioMaximo)
    throws PresupuestoNegativoException {
    if ((precioMaximo != null && precioMaximo < 0) || (precioMinimo != null && precioMinimo < 0)) {
      throw new PresupuestoNegativoException("El presupuesto no puede ser negativo");
    }
  }
}
