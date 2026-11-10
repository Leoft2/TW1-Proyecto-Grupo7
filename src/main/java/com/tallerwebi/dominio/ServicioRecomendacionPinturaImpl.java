package com.tallerwebi.dominio;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioRecomendacionPintura")
@Transactional(readOnly = true)
public class ServicioRecomendacionPinturaImpl implements ServicioRecomendacionPintura {

  private final SeleccionadorColoresPorPrecio seleccionadorColores;
  private final CalculadorLitros calculadorLitros;
  private final ServicioRediseño servicioRediseño;
  private final RepositorioBaldePintura repositorioBaldePintura;
  private final Map<FormaPintura, EstrategiaAplicacionPintura> estrategias;

  @Autowired
  public ServicioRecomendacionPinturaImpl(
    ServicioRediseño servicioRediseño,
    RepositorioBaldePintura repositorioBaldePintura
  ) {
    this.servicioRediseño = servicioRediseño;
    this.repositorioBaldePintura = repositorioBaldePintura;
    this.seleccionadorColores = new SeleccionadorColoresPorPrecio(repositorioBaldePintura);
    this.calculadorLitros = new CalculadorLitros();
    this.estrategias = new EnumMap<>(FormaPintura.class);
    inicializarEstrategias();
  }

  public ServicioRecomendacionPinturaImpl() {
    this.servicioRediseño = null;
    this.repositorioBaldePintura = null;
    this.seleccionadorColores = null;
    this.calculadorLitros = null;
    this.estrategias = new EnumMap<>(FormaPintura.class);
    inicializarEstrategias();
  }

  private void inicializarEstrategias() {
    this.estrategias.put(
        FormaPintura.PINTAR_PARED_OPUESTA_A_PUERTA_PARA_ACORTAR_AMBIENTE,
        new EstrategiaParedOpuesta()
      );
    this.estrategias.put(
        FormaPintura.PINTAR_TODAS_LAS_PAREDES_DE_UN_COLOR_PARA_ESTRECHAR_VERTICALMENTE,
        new EstrategiaTodasLasParedes(false)
      );
    this.estrategias.put(FormaPintura.PINTAR_TECHO_DE_UN_COLOR_PARA_BAJARLO, new EstrategiaTecho());
    this.estrategias.put(
        FormaPintura.PINTAR_PAREDES_OPUESTAS_PARA_ESTRECHAR_AMBIENTE,
        new EstrategiaParedesOpuestas()
      );
    this.estrategias.put(
        FormaPintura.PINTAR_TECHO_Y_PARED_OPUESTA_DE_UN_AMBIENTE_PARA_ALARGARLO,
        new EstrategiaTechoYParedOpuesta()
      );
    this.estrategias.put(
        FormaPintura.PINTAR_TODAS_LAS_PAREDES_POR_LA_MITAD_PARA_ACORTAR_LAS_PAREDES,
        new EstrategiaTodasLasParedes(true)
      );
    this.estrategias.put(
        FormaPintura.PINTAR_TODO_DE_UN_COLOR_PARA_COMPACTAR,
        new EstrategiaTodoUnColor()
      );
    this.estrategias.put(
        FormaPintura.PINTAR_TODO_MENOS_UNA_PARED_PARA_DESTACARLA,
        new EstrategiaTodoMenosUnaPared()
      );
  }

  @Override
  public RecomendacionPintura recomendar(Ambiente ambiente, Estilo estilo) {
    return recomendar(ambiente, estilo, null);
  }

  @Override
  public RecomendacionPintura recomendar(
    Ambiente ambiente,
    Estilo estilo,
    Integer colorOverrideIndex
  ) {
    if (ambiente == null) {
      return new RecomendacionPintura();
    }
    return recomendarPara(AnalisisAmbiente.de(ambiente), estilo, colorOverrideIndex);
  }

  private RecomendacionPintura recomendarPara(
    AnalisisAmbiente analisis,
    Estilo estilo,
    Integer colorOverrideIndex
  ) {
    FormaPintura forma = new SelectorFormaPintura().seleccionar(analisis);

    RecomendacionPintura recomendacion = new RecomendacionPintura();
    recomendacion.setFormaPintura(forma);
    recomendacion.setDescripcion(forma.getDescripcion());

    List<Color> colores = coloresOrdenadosPorPrecio(estilo);
    List<Color> coloresSecundarios = coloresSecundariosPara(estilo);
    String neutralHex = hexNeutral(coloresSecundarios);

    ContextoSuperficies ctx = new ContextoSuperficies(
      analisis.areaTecho(),
      analisis.areaParedIzquierda(),
      analisis.areaParedFondo(),
      analisis.areaParedDerecha(),
      indiceParedOpuesta(analisis.muroPuerta())
    );
    ctx.hexes[0] = neutralHex;
    ctx.hexes[1] = neutralHex;
    ctx.hexes[2] = neutralHex;
    ctx.hexes[3] = neutralHex;

    Color override = colorActivo(colores, colorOverrideIndex);
    EstrategiaAplicacionPintura estrategia = estrategias.get(forma);
    if (estrategia != null) {
      estrategia.aplicar(recomendacion, colores, override, ctx);
    } else {
      new EstrategiaTodoUnColor().aplicar(recomendacion, colores, override, ctx);
    }

    for (int i = 0; i < 4; i++) {
      if (ctx.hexes[i].equals(neutralHex) && ctx.areas[i] > 0) {
        UtilidadesPintura.agregarItem(
          recomendacion,
          UtilidadesPintura.nombreSuperficie(i),
          ctx.areas[i],
          UtilidadesPintura.NOMBRE_NEUTRO,
          neutralHex
        );
      }
    }

    recomendacion.setColorSuperficie(
      new ColorSuperficie(ctx.hexes[0], ctx.hexes[1], ctx.hexes[2], ctx.hexes[3])
    );

    agruparItemsPorColor(recomendacion);

    resolverBaldes(recomendacion);

    recomendacion.calcularTotal();
    return recomendacion;
  }

  @Override
  public List<Color> coloresOrdenadosPorPrecio(Estilo estilo) {
    if (this.seleccionadorColores == null || estilo == null) {
      return new ArrayList<>();
    }
    List<Color> colores = coloresPara(estilo);
    return this.seleccionadorColores.coloresOrdenadosPorPrecio(colores);
  }

  private List<Color> coloresPara(Estilo estilo) {
    if (this.servicioRediseño != null && estilo != null) {
      List<Color> colores = this.servicioRediseño.recomendar(estilo).getColores();
      if (colores != null && !colores.isEmpty()) {
        return colores;
      }
    }

    return new ArrayList<>();
  }

  private List<Color> coloresSecundariosPara(Estilo estilo) {
    if (this.servicioRediseño != null && estilo != null) {
      List<Color> secundarios = this.servicioRediseño.recomendar(estilo).getColoresSecundarios();
      if (secundarios != null && !secundarios.isEmpty()) {
        return secundarios;
      }
    }
    List<Color> colores = new ArrayList<>();
    colores.add(new Color("Neutro", UtilidadesPintura.HEX_NEUTRO_FALLBACK));
    return colores;
  }

  private String hexNeutral(List<Color> coloresSecundarios) {
    if (coloresSecundarios != null && !coloresSecundarios.isEmpty()) {
      return coloresSecundarios.get(0).getHex();
    }
    return UtilidadesPintura.HEX_NEUTRO_FALLBACK;
  }

  private Color colorActivo(List<Color> colores, Integer colorOverrideIndex) {
    if (
      colorOverrideIndex != null &&
      colores != null &&
      !colores.isEmpty() &&
      colorOverrideIndex >= 0 &&
      colorOverrideIndex < colores.size()
    ) {
      return colores.get(colorOverrideIndex);
    }
    return null;
  }

  private void resolverBaldes(RecomendacionPintura recomendacion) {
    if (this.repositorioBaldePintura == null || recomendacion.getItems() == null) {
      return;
    }

    for (RecomendacionPintura.ItemPintura item : recomendacion.getItems()) {
      if (item == null || item.getBaldePintura() != null) {
        continue;
      }

      BaldePintura balde = resolverBalde(item);
      if (balde == null) {
        continue;
      }

      item.setBaldePintura(balde);
      this.calculadorLitros.elegirLitroMasBarato(item, balde);
      establecerCantidadYPrecio(item, balde);
    }
  }

  private BaldePintura resolverBalde(RecomendacionPintura.ItemPintura item) {
    String hex = item.getHex();
    BaldePintura balde = null;

    if (hex != null && !hex.isBlank()) {
      balde = this.repositorioBaldePintura.buscarPorHex(hex);
    }

    if (balde == null) {
      String nombre = item.getColorNombre();
      if (nombre != null && !nombre.isBlank()) {
        balde = this.repositorioBaldePintura.buscarPorColorNombre(nombre);
      }
    }

    return balde;
  }

  private void establecerCantidadYPrecio(
    RecomendacionPintura.ItemPintura item,
    BaldePintura balde
  ) {
    Double litros = item.getLitrosSeleccionados();
    if (litros == null || litros <= 0) {
      litros = balde.getCantidadLitros();
    }
    Double metros = item.getMetrosCuadrados();

    if (litros == null || litros <= 0 || metros == null) {
      return;
    }

    int cantidad = (int) Math.ceil(metros / litros);
    item.setCantidadBaldes(cantidad);

    Double precio = balde.getPrecio();
    if (precio != null) {
      item.setPrecioTotal(cantidad * precio);
    }
  }

  private int indiceParedOpuesta(Muro muroPuerta) {
    if (muroPuerta == null) {
      return UtilidadesPintura.INDICE_PARED_FONDO;
    }

    return switch (muroPuerta) {
      case IZQUIERDO -> UtilidadesPintura.INDICE_PARED_DERECHA;
      case DERECHO -> UtilidadesPintura.INDICE_PARED_IZQUIERDA;
      case SUPERIOR, INFERIOR -> UtilidadesPintura.INDICE_PARED_FONDO;
    };
  }

  private void agruparItemsPorColor(RecomendacionPintura recomendacion) {
    List<RecomendacionPintura.ItemPintura> items = recomendacion.getItems();
    if (items == null || items.isEmpty()) {
      return;
    }

    Map<String, RecomendacionPintura.ItemPintura> gruposPorColor = new java.util.LinkedHashMap<>();
    List<RecomendacionPintura.ItemPintura> itemsSinAgrupar = new ArrayList<>();

    for (RecomendacionPintura.ItemPintura item : items) {
      if (item == null) {
        continue;
      }
      String hex = item.getHex();
      if (hex == null || hex.isBlank()) {
        itemsSinAgrupar.add(item);
        continue;
      }

      RecomendacionPintura.ItemPintura existente = gruposPorColor.get(hex);
      if (existente != null) {
        existente.setMetrosCuadrados(
          UtilidadesPintura.redondear(existente.getMetrosCuadrados() + item.getMetrosCuadrados())
        );
        existente.setZona(existente.getZona() + " + " + item.getZona());
      } else {
        RecomendacionPintura.ItemPintura copia = new RecomendacionPintura.ItemPintura();
        copia.setZona(item.getZona());
        copia.setColorNombre(item.getColorNombre());
        copia.setHex(item.getHex());
        copia.setMetrosCuadrados(item.getMetrosCuadrados());
        copia.setLitrosSeleccionados(item.getLitrosSeleccionados());
        gruposPorColor.put(hex, copia);
      }
    }

    List<RecomendacionPintura.ItemPintura> resultado = new ArrayList<>();
    resultado.addAll(itemsSinAgrupar);
    resultado.addAll(gruposPorColor.values());

    recomendacion.setItems(resultado);
  }
}
