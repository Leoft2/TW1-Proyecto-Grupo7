/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { JSDOM } from "jsdom";
import { agregarFilaAbertura, eliminarFilaAbertura } from "../aberturas_funciones.js";

const SELECTOR_DE_TIPO = `
  <select name="__TIPO__">
    <option value="PUERTA">PUERTA</option>
    <option value="VENTANA">VENTANA</option>
  </select>`;

const SELECTOR_DE_MURO = `
  <select name="__MURO__">
    <option value="SUPERIOR">SUPERIOR</option>
    <option value="INFERIOR">INFERIOR</option>
  </select>`;

function crearFila(indice) {
  return `
    <tr>
      <td>${SELECTOR_DE_TIPO}</td>
      <td>${SELECTOR_DE_MURO}</td>
      <td><input type="number" name="aberturas[${indice}].posicion"></td>
      <td><input type="number" name="aberturas[${indice}].ancho"></td>
      <td><input type="number" name="aberturas[${indice}].radioDeGiro"></td>
      <td><button type="button" class="btn-eliminar-abertura">Eliminar</button></td>
    </tr>`;
}

const PLANTILLA_DE_ABERTURA = `
  <tr>
    <td>${SELECTOR_DE_TIPO}</td>
    <td>${SELECTOR_DE_MURO}</td>
    <td><input type="number" name="__POSICION__"></td>
    <td><input type="number" name="__ANCHO__"></td>
    <td><input type="number" name="__RADIO__"></td>
    <td><button type="button" class="btn-eliminar-abertura">Eliminar</button></td>
  </tr>`;

describe("Alta y baja de filas de aberturas", function() {
  let cuerpo;
  let filaPlantilla;

  beforeEach(function() {
    const documento = new JSDOM(`
      <table><tbody id="filas-aberturas">${crearFila(0)}</tbody></table>
      <template id="plantilla-abertura">${PLANTILLA_DE_ABERTURA}</template>
    `).window.document;
    cuerpo = documento.getElementById("filas-aberturas");
    filaPlantilla = documento.getElementById("plantilla-abertura").content.firstElementChild;
  });

  function filas() {
    return cuerpo.querySelectorAll("tr");
  }

  function nombreDeCampo(indice, campo) {
    return filas()[indice].querySelector(`[name$='.${campo}']`).name;
  }

  it("agrega una fila nueva al final de la tabla", function() {
    agregarFilaAbertura(cuerpo, filaPlantilla);

    expect(filas().length).toBe(2);
  });

  it("nombra los campos de la fila nueva con el indice que sigue al de las que estan cargadas", function() {
    agregarFilaAbertura(cuerpo, filaPlantilla);

    expect(nombreDeCampo(1, "tipo")).toBe("aberturas[1].tipo");
  });

  it("elimina la fila que se le indica", function() {
    agregarFilaAbertura(cuerpo, filaPlantilla);

    eliminarFilaAbertura(filas()[0], cuerpo);

    expect(filas().length).toBe(1);
  });

  it("renumera los campos de las filas que quedan", function() {
    agregarFilaAbertura(cuerpo, filaPlantilla);

    eliminarFilaAbertura(filas()[0], cuerpo);

    expect(nombreDeCampo(0, "ancho")).toBe("aberturas[0].ancho");
  });

  it("deja la tabla sin filas cuando se elimina la ultima", function() {
    eliminarFilaAbertura(filas()[0], cuerpo);

    expect(filas().length).toBe(0);
  });
});
