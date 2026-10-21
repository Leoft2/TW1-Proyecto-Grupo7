package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.dominio.excepcion.PresupuestoNuloException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import java.util.ArrayList;
import java.util.Comparator;
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

    List<Mueble> mueblesFiltrados = new ArrayList<>();
    List<Mueble> catalogo = repositorioCatalogo.ObtenerTodosLosMuebles();

    for (Mueble mueble : catalogo) {
      if (mueble.getPrecio() <= precioPresupuestoMaximo) {
        mueblesFiltrados.add(mueble);
      }
    }

    return mueblesFiltrados;
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilos(List<Estilo> estilos) {
    List<Mueble> mueblesFiltrados = new ArrayList<>();
    List<Mueble> catalogo = repositorioCatalogo.ObtenerTodosLosMuebles();
    for (Mueble mueble : catalogo) {
      if (estilos.contains(mueble.getEstilo())) {
        mueblesFiltrados.add(mueble);
      }
    }
    return mueblesFiltrados;
  }

  @Override
  public List<Mueble> ObtenerMueblesDeEstilo(Estilo estilo) {
    List<Mueble> mueblesFiltrados = new ArrayList<>();
    List<Mueble> catalogo = repositorioCatalogo.ObtenerTodosLosMuebles();
    for (Mueble mueble : catalogo) {
      if (mueble.getEstilo() == estilo) {
        mueblesFiltrados.add(mueble);
      }
    }
    return mueblesFiltrados;
  }

  @Override
  public Mueble ObtenerMuebleMasBaratoDeCategoria(Estilo categoria) {
    Mueble muebleMasBarato = null;
    List<Mueble> catalogo = repositorioCatalogo.ObtenerTodosLosMuebles();
    for (Mueble mueble : catalogo) {
      if (mueble.getEstilo() == categoria) {
        if (muebleMasBarato == null || mueble.getPrecio() < muebleMasBarato.getPrecio()) {
          muebleMasBarato = mueble;
        }
      }
    }
    return muebleMasBarato;
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
    List<Mueble> mueblesFiltradorPorPrecioAsc =
      this.filtrarYOrdenar(null, null, null, null, OrdenDeMuebles.PRECIO_ASC, null);
    return mueblesFiltradorPorPrecioAsc;
  }

  @Override
  public List<Mueble> ObtenerMueblesOrdenadosPorPrecioDesc() {
    List<Mueble> mueblesFiltradorPorPrecioDesc =
      this.filtrarYOrdenar(null, null, null, null, OrdenDeMuebles.PRECIO_DESC, null);
    return mueblesFiltradorPorPrecioDesc;
  }

  @Override
  public List<Mueble> ObtenerMueblesQueCumplan(
    TipoDeAmbiente tipoDeAmbiente,
    Estilo estilo,
    Double precioMaximo,
    Boolean soloSostenibles
  ) throws PresupuestoNegativoException {
    this.validarPresupuestos(null, precioMaximo);
    List<Mueble> mueblesFiltrados =
      this.filtrarYOrdenar(null, precioMaximo, estilo, tipoDeAmbiente, null, soloSostenibles);
    return mueblesFiltrados;
  }

  @Override
  public List<Mueble> ObtenerMueblesFiltrados(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    TipoDeAmbiente tipoDeAmbiente,
    OrdenDeMuebles orden,
    Boolean soloSostenibles
  ) throws PresupuestoNegativoException {
    this.validarPresupuestos(precioMinimo, precioMaximo);
    List<Mueble> mueblesFiltrados =
      this.filtrarYOrdenar(
          precioMinimo,
          precioMaximo,
          estilo,
          tipoDeAmbiente,
          orden,
          soloSostenibles
        );
    return mueblesFiltrados;
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

  private void copiarDatosDelCatalogo(Mueble mueble, Mueble datos) {
    mueble.setNombre(datos.getNombre());
    mueble.setDescripcion(datos.getDescripcion());
    mueble.setPrecio(datos.getPrecio());
    mueble.setEstilo(datos.getEstilo());
    mueble.setCategoriaDeMueble(datos.getCategoriaDeMueble());
    mueble.setAncho(datos.getAncho());
    mueble.setLargo(datos.getLargo());
    mueble.setTiposDeAmbiente(datos.getTiposDeAmbiente());
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

  private List<Mueble> filtrarYOrdenar(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    TipoDeAmbiente tipoDeAmbiente,
    OrdenDeMuebles orden,
    Boolean soloSostenibles
  ) {
    List<Mueble> mueblesFiltrados = new ArrayList<>();
    for (Mueble mueble : this.mueblesCandidatos(
        precioMinimo,
        precioMaximo,
        estilo,
        soloSostenibles
      )) {
      if (
        cumpleElFiltroDeTipoDeAmbiente(mueble, tipoDeAmbiente) &&
        cumpleElFiltroDeEstilo(mueble, estilo) &&
        cumpleElFiltroDePrecioMinimo(mueble, precioMinimo) &&
        cumpleElFiltroDePrecioMaximo(mueble, precioMaximo)
      ) {
        mueblesFiltrados.add(mueble);
      }
    }

    mueblesFiltrados = ordenarPorPrecio(mueblesFiltrados, orden);
    return mueblesFiltrados;
  }

  private List<Mueble> mueblesCandidatos(
    Double precioMinimo,
    Double precioMaximo,
    Estilo estilo,
    Boolean soloSostenibles
  ) {
    if (Boolean.TRUE.equals(soloSostenibles)) {
      return this.repositorioCatalogo.ObtenerMueblesDelCatalogo(
          estilo,
          precioMinimo,
          precioMaximo,
          true
        );
    }
    return this.repositorioCatalogo.ObtenerTodosLosMuebles();
  }

  private List<Mueble> ordenarPorPrecio(List<Mueble> muebles, OrdenDeMuebles orden) {
    if (orden == OrdenDeMuebles.PRECIO_ASC) {
      return muebles.stream().sorted(Comparator.comparing(Mueble::getPrecio)).toList();
    }
    if (orden == OrdenDeMuebles.PRECIO_DESC) {
      return muebles.stream().sorted(Comparator.comparing(Mueble::getPrecio).reversed()).toList();
    }
    return muebles;
  }

  private boolean cumpleElFiltroDeEstilo(Mueble mueble, Estilo estilo) {
    return estilo == null || mueble.getEstilo() == estilo;
  }

  private boolean cumpleElFiltroDePrecioMinimo(Mueble mueble, Double precioMinimo) {
    return precioMinimo == null || mueble.getPrecio() >= precioMinimo;
  }

  private boolean cumpleElFiltroDePrecioMaximo(Mueble mueble, Double precioMaximo) {
    return precioMaximo == null || mueble.getPrecio() <= precioMaximo;
  }

  private boolean cumpleElFiltroDeTipoDeAmbiente(Mueble mueble, TipoDeAmbiente tipoDeAmbiente) {
    if (tipoDeAmbiente == null || tipoDeAmbiente.getNombre() == null) {
      return true;
    }
    for (TipoDeAmbiente tipoDelMueble : mueble.getTiposDeAmbiente()) {
      if (tipoDeAmbiente.getNombre().equals(tipoDelMueble.getNombre())) {
        return true;
      }
    }
    return false;
  }
}
