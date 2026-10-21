package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.CategoriaDeMueble;
import com.tallerwebi.dominio.Estilo;
import com.tallerwebi.dominio.Material;
import com.tallerwebi.dominio.Mueble;
import com.tallerwebi.dominio.ServicioCatalogo;
import com.tallerwebi.dominio.ServicioCategoriaDeMueble;
import com.tallerwebi.dominio.ServicioMaterial;
import com.tallerwebi.dominio.TipoDeAmbiente;
import com.tallerwebi.dominio.excepcion.MuebleNoEncontrado;
import com.tallerwebi.dominio.excepcion.PresupuestoNegativoException;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import com.tallerwebi.presentacion.excepcion.FiltroDeMueblesInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCatalogoDeMuebles {

  private static final String ROL_ADMIN = "ADMIN";
  private static final String REDIRECCION_AL_CATALOGO = "redirect:/catalogo";
  private static final String VISTA_CREAR_MUEBLE = "crearMueble";
  private static final String VISTA_EDITAR_MUEBLE = "editarMueble";
  private static final String ATTR_DATOS_MUEBLE = "datosMuebleCatalogo";
  private static final String ATTR_ID_MUEBLE = "idMueble";

  private final ServicioCatalogo servicioCatalogo;
  private final ServicioCategoriaDeMueble servicioCategoriaDeMueble;
  private final ServicioMaterial servicioMaterial;

  @Autowired
  public ControladorCatalogoDeMuebles(
    ServicioCatalogo servicioCatalogo,
    ServicioCategoriaDeMueble servicioCategoriaDeMueble,
    ServicioMaterial servicioMaterial
  ) {
    this.servicioCatalogo = servicioCatalogo;
    this.servicioCategoriaDeMueble = servicioCategoriaDeMueble;
    this.servicioMaterial = servicioMaterial;
  }

  @RequestMapping(path = "/catalogo", method = RequestMethod.GET)
  public ModelAndView catalogo(
    @RequestParam(name = "estilo", required = false) String estilo,
    @RequestParam(name = "precioMax", required = false) String precioMaximo,
    @RequestParam(name = "precioMin", required = false) String precioMinimo,
    @RequestParam(name = "tipoDeAmbiente", required = false) String tipoDeAmbiente,
    @RequestParam(name = "orden", required = false) String orden,
    @RequestParam(name = "soloSostenibles", required = false) String soloSostenibles,
    @RequestParam(name = "material", required = false) String material
  ) {
    FiltroDeMuebles filtro;

    try {
      filtro =
        FiltroDeMuebles.delCatalogo(
          estilo,
          precioMaximo,
          precioMinimo,
          tipoDeAmbiente,
          orden,
          soloSostenibles,
          material
        );
    } catch (FiltroDeMueblesInvalidoException e) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }

    return new ModelAndView("catalogo", this.modeloDelCatalogo(filtro));
  }

  @RequestMapping(path = "/muebles", method = RequestMethod.GET)
  public ModelAndView muebles() {
    return new ModelAndView("muebles");
  }

  @RequestMapping(path = "/muebles/crear", method = RequestMethod.GET)
  public ModelAndView crearMueble(HttpServletRequest request) {
    if (!this.esAdministrador(request)) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    return new ModelAndView(VISTA_CREAR_MUEBLE, this.modeloDelFormularioDeCreacion());
  }

  @RequestMapping(path = "/muebles/crear", method = RequestMethod.POST)
  public ModelAndView crearMuebleDelCatalogo(
    @ModelAttribute(ATTR_DATOS_MUEBLE) DTONuevoMuebleCatalogo datos,
    HttpServletRequest request
  ) {
    if (!this.esAdministrador(request)) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    try {
      this.servicioCatalogo.RegistrarMuebleDelCatalogo(this.aMuebleDelCatalogo(datos));
    } catch (ValidacionException e) {
      Map<String, Object> modelo = this.modeloDelFormularioDeCreacion();
      modelo.put("error", e.getMessage());
      return new ModelAndView(VISTA_CREAR_MUEBLE, modelo);
    }
    return new ModelAndView(REDIRECCION_AL_CATALOGO);
  }

  @RequestMapping(path = "/muebles/editar/{id}", method = RequestMethod.GET)
  public ModelAndView editarMueble(@PathVariable("id") int id, HttpServletRequest request) {
    if (!this.esAdministrador(request)) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    Mueble mueble;
    try {
      mueble = this.servicioCatalogo.ObtenerMueblePorId(id);
    } catch (MuebleNoEncontrado e) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    Map<String, Object> modelo = this.modeloDelFormularioDeCreacion();
    modelo.put("muebleAEliminar", mueble);
    modelo.put(ATTR_ID_MUEBLE, mueble.getId());
    modelo.put(ATTR_DATOS_MUEBLE, this.aDatosDelCatalogo(mueble));
    return new ModelAndView(VISTA_EDITAR_MUEBLE, modelo);
  }

  @RequestMapping(path = "/muebles/editar/{id}", method = RequestMethod.POST)
  public ModelAndView modificarMuebleDelCatalogo(
    @PathVariable("id") int id,
    @ModelAttribute(ATTR_DATOS_MUEBLE) DTONuevoMuebleCatalogo datos,
    HttpServletRequest request
  ) {
    if (!this.esAdministrador(request)) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    try {
      this.servicioCatalogo.modificarMuebleDelCatalogo(id, this.aMuebleDelCatalogo(datos));
    } catch (ValidacionException e) {
      Map<String, Object> modelo = this.modeloDelFormularioDeEdicion();
      modelo.put(ATTR_ID_MUEBLE, id);
      modelo.put(ATTR_DATOS_MUEBLE, datos);
      modelo.put("error", e.getMessage());
      return new ModelAndView(VISTA_EDITAR_MUEBLE, modelo);
    } catch (MuebleNoEncontrado e) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    return new ModelAndView(REDIRECCION_AL_CATALOGO);
  }

  @RequestMapping(path = "/muebles/eliminar/{id}", method = RequestMethod.POST)
  public ModelAndView eliminarMuebleDelCatalogo(
    @PathVariable("id") int id,
    HttpServletRequest request
  ) {
    if (!this.esAdministrador(request)) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    try {
      this.servicioCatalogo.eliminarMuebleDelCatalogo(id);
    } catch (MuebleNoEncontrado e) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }
    return new ModelAndView(REDIRECCION_AL_CATALOGO);
  }

  @RequestMapping(path = "/mueble/{id}", method = RequestMethod.GET)
  public ModelAndView detalleMueble(@PathVariable("id") int id) {
    Mueble mueble;

    try {
      mueble = this.servicioCatalogo.ObtenerMueblePorId(id);
    } catch (MuebleNoEncontrado e) {
      return new ModelAndView(REDIRECCION_AL_CATALOGO);
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("mueble", mueble);
    modelo.put("categoriaDeMueble", mueble.getCategoriaDeMueble());

    return new ModelAndView("mueble", modelo);
  }

  private Map<String, Object> modeloDelCatalogo(FiltroDeMuebles filtro) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("estilos", Estilo.values());
    modelo.put("estiloSeleccionado", filtro.getEstilo());
    modelo.put("precioMaximo", filtro.getPrecioMaximo());
    modelo.put("precioMinimo", filtro.getPrecioMinimo());
    modelo.put("tipoDeAmbiente", filtro.getTipoDeAmbiente());
    modelo.put("orden", filtro.getOrden());
    modelo.put("soloSostenibles", filtro.getSoloSostenibles());
    modelo.put("materiales", this.servicioMaterial.obtenerMateriales());
    modelo.put("materialSeleccionado", filtro.getMaterialId());
    List<Mueble> muebles = null;
    TipoDeAmbiente tipoDeAmbiente = this.aTipoDeAmbiente(filtro.getTipoDeAmbiente());

    try {
      muebles =
        this.servicioCatalogo.ObtenerMueblesFiltrados(
            filtro.getPrecioMinimo(),
            filtro.getPrecioMaximo(),
            filtro.getEstilo(),
            tipoDeAmbiente,
            filtro.getOrden(),
            filtro.getSoloSostenibles(),
            filtro.getMaterialId()
          );
    } catch (PresupuestoNegativoException e) {
      modelo.put("error", "El presupuesto no puede ser negativo");
    }
    modelo.put("muebles", muebles);

    return modelo;
  }

  private TipoDeAmbiente aTipoDeAmbiente(String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
      return null;
    }
    return new TipoDeAmbiente(nombre);
  }

  private boolean esAdministrador(HttpServletRequest request) {
    HttpSession sesion = request.getSession();
    boolean esAdmin = sesion != null && ROL_ADMIN.equals(sesion.getAttribute("ROL"));
    return esAdmin;
  }

  private Map<String, Object> modeloDelFormularioDeCreacion() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("estilos", Estilo.values());
    modelo.put("categorias", this.servicioCategoriaDeMueble.obtenerCategorias());
    modelo.put("materiales", this.servicioMaterial.obtenerMateriales());
    modelo.put("tiposDeAmbiente", TipoDeAmbiente.nombresConocidos());
    return modelo;
  }

  private Map<String, Object> modeloDelFormularioDeEdicion() {
    return this.modeloDelFormularioDeCreacion();
  }

  private DTONuevoMuebleCatalogo aDatosDelCatalogo(Mueble mueble) {
    DTONuevoMuebleCatalogo datos = new DTONuevoMuebleCatalogo();
    datos.setNombre(mueble.getNombre());
    datos.setDescripcion(mueble.getDescripcion());
    datos.setPrecio(mueble.getPrecio());
    Estilo estilo = mueble.getEstilo();
    if (estilo == null) {
      datos.setEstilo(null);
    } else {
      datos.setEstilo(estilo.name());
    }
    CategoriaDeMueble categoria = mueble.getCategoriaDeMueble();
    if (categoria == null) {
      datos.setCategoria(null);
    } else {
      datos.setCategoria(categoria.getId());
    }
    Material material = mueble.getMaterial();
    if (material == null) {
      datos.setMaterial(null);
    } else {
      datos.setMaterial(material.getId());
    }
    datos.setAncho(mueble.getAncho());
    datos.setLargo(mueble.getLargo());
    List<String> nombresDeTipo = new ArrayList<>();
    for (TipoDeAmbiente tipo : mueble.getTiposDeAmbiente()) {
      nombresDeTipo.add(tipo.getNombre());
    }
    datos.setTiposDeAmbiente(nombresDeTipo);
    return datos;
  }

  private Mueble aMuebleDelCatalogo(DTONuevoMuebleCatalogo datos) {
    Mueble mueble = new Mueble();
    mueble.setNombre(datos.getNombre());
    mueble.setDescripcion(datos.getDescripcion());
    mueble.setPrecio(datos.getPrecio() == null ? 0.0 : datos.getPrecio());
    mueble.setEstilo(this.aEstilo(datos.getEstilo()));
    mueble.setCategoriaDeMueble(this.aCategoria(datos.getCategoria()));
    mueble.setMaterial(this.servicioMaterial.buscarPorId(datos.getMaterial()));
    mueble.setAncho(datos.getAncho());
    mueble.setLargo(datos.getLargo());
    this.agregarTiposDeAmbienteElegidos(mueble, datos.getTiposDeAmbiente());
    return mueble;
  }

  private Estilo aEstilo(String nombreDeEstilo) {
    if (nombreDeEstilo == null || nombreDeEstilo.trim().isEmpty()) {
      return null;
    }
    return Estilo.valueOf(nombreDeEstilo);
  }

  private CategoriaDeMueble aCategoria(Long categoriaId) {
    if (categoriaId == null) {
      return null;
    }
    return this.servicioCategoriaDeMueble.buscarPorId(categoriaId);
  }

  private void agregarTiposDeAmbienteElegidos(Mueble mueble, List<String> nombresDeTipo) {
    for (String nombre : nombresDeTipo) {
      TipoDeAmbiente tipoDeAmbiente = this.servicioCatalogo.buscarTipoDeAmbientePorNombre(nombre);
      if (tipoDeAmbiente != null) {
        mueble.agregarTipoDeAmbiente(tipoDeAmbiente);
      }
    }
  }
}
