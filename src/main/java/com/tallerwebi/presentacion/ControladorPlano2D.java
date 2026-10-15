package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.ValidacionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorPlano2D {

    private ServicioPlano2D servicioPlano2D;
    private ServicioCategoriaDeMueble servicioCategoriaDeMueble;

    @Autowired
    public ControladorPlano2D(
            ServicioPlano2D servicioPlano2D,
            ServicioCategoriaDeMueble servicioCategoriaDeMueble
    ) {
        this.servicioPlano2D = servicioPlano2D;
        this.servicioCategoriaDeMueble = servicioCategoriaDeMueble;
    }

    //Review 2 : Usar model Map que vinimos haciendo en clase.
    @RequestMapping(path = "/plano/ambiente", method = RequestMethod.GET)
    public ModelAndView irAConfigurarAmbiente() {
        return new ModelAndView(
                "ambiente-config",
                "categorias",
                servicioCategoriaDeMueble.obtenerCategorias()
        );
    }

    public ModelAndView generarPlano(Ambiente ambiente, List<Mueble> muebles) {
        try {
            Plano plano = servicioPlano2D.generarPlano(ambiente, muebles);
            return new ModelAndView("plano-interactivo", "plano", plano);
        } catch (ValidacionException e) {
            return planoConError(e);
        }
    }

    private ModelAndView planoConError(ValidacionException error) {
        Map<String, Object> modelo = new ModelMap();
        modelo.put("error", error.getMessage());
        return new ModelAndView("plano-interactivo", modelo);
    }

    /**
     * Entrada HTTP del formulario. Spring entrega aca el {@link DatosPlano} ya bindeado desde el
     * request; este metodo arma los muebles con el servicio de categorías (HU-08: completa las
     * medidas vacías con el promedio) y delega en {@link #generarPlano}, que es quien decide la
     * vista y el modelo.
     */
    @RequestMapping(path = "/plano/generar", method = RequestMethod.POST)
    public ModelAndView generarPlanoDesdeFormulario(DatosPlano datosDelFormulario) {
        Ambiente ambiente = datosDelFormulario.aAmbiente();
        List<Mueble> muebles = new ArrayList<>();
        try {
            for (DatosMueble datosMueble : datosDelFormulario.getMuebles()) {
                muebles.add(
                        servicioCategoriaDeMueble.crearMueble(
                                datosMueble.getNombre(),
                                datosMueble.getCategoria(),
                                datosMueble.getAncho(),
                                datosMueble.getProfundidad()
                        )
                );
            }
        } catch (ValidacionException e) {
            return planoConError(e);
        }
        return generarPlano(ambiente, muebles);
    }
}
