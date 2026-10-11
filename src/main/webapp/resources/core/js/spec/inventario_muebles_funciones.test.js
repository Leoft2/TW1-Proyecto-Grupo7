import { JSDOM } from "jsdom";
import { conectarAutocompletadoDeFormulario } from "../dimensiones_funciones.js";

const FORMULARIO = `
  <form id="agregar-mueble">
    <select data-categoria>
      <option value="">Sin categoría</option>
      <option data-ancho="1.4" data-profundidad="1.9">Cama doble</option>
      <option data-ancho="2.1" data-profundidad="0.9">Sofá 3 cuerpos</option>
    </select>
    <input type="number" id="agregar-ancho">
    <input type="number" id="agregar-largo">
  </form>`;

describe("Autocompletado de dimensiones por categoría en el formulario de alta", function() {
  let documento;
  let formulario;

  beforeEach(function() {
    documento = new JSDOM(FORMULARIO).window.document;
    formulario = documento.getElementById("agregar-mueble");
    conectarAutocompletadoDeFormulario(formulario, "#agregar-ancho", "#agregar-largo");
  });

  function campo(selector) {
    return formulario.querySelector(selector);
  }

  function elegirCategoria(nombre) {
    const selector = formulario.querySelector("select[data-categoria]");
    selector.selectedIndex = [...selector.options].findIndex((opcion) => opcion.text === nombre);
    selector.dispatchEvent(new documento.defaultView.Event("change", { bubbles: true }));
  }

  function escribir(selector, valor) {
    const campoDeMedida = campo(selector);
    campoDeMedida.value = valor;
    campoDeMedida.dispatchEvent(new documento.defaultView.Event("input", { bubbles: true }));
  }

  it("al elegir una categoría completa ancho y profundidad con sus medidas promedio", function() {
    elegirCategoria("Cama doble");

    expect(campo("#agregar-ancho").value).toBe("1.4");
    expect(campo("#agregar-largo").value).toBe("1.9");
  });

  it("respeta el ancho que el usuario escribió a mano y completa solo la profundidad", function() {
    escribir("#agregar-ancho", "1.6");

    elegirCategoria("Cama doble");

    expect(campo("#agregar-ancho").value).toBe("1.6");
    expect(campo("#agregar-largo").value).toBe("1.9");
  });

  it("respeta las dos medidas si el usuario escribió ambas", function() {
    escribir("#agregar-ancho", "1.6");
    escribir("#agregar-largo", "2.05");

    elegirCategoria("Cama doble");

    expect(campo("#agregar-ancho").value).toBe("1.6");
    expect(campo("#agregar-largo").value).toBe("2.05");
  });

  it("al cambiar de categoría reemplaza las medidas que había autocompletado", function() {
    elegirCategoria("Cama doble");

    elegirCategoria("Sofá 3 cuerpos");

    expect(campo("#agregar-ancho").value).toBe("2.1");
    expect(campo("#agregar-largo").value).toBe("0.9");
  });

  it("al volver a 'Sin categoría' no modifica las medidas", function() {
    elegirCategoria("Cama doble");

    elegirCategoria("Sin categoría");

    expect(campo("#agregar-ancho").value).toBe("1.4");
    expect(campo("#agregar-largo").value).toBe("1.9");
  });

  it("si el usuario borró una medida, vuelve a autocompletarla", function() {
    escribir("#agregar-ancho", "1.6");
    escribir("#agregar-ancho", "");

    elegirCategoria("Cama doble");

    expect(campo("#agregar-ancho").value).toBe("1.4");
  });
});