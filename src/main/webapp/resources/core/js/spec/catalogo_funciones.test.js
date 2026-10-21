/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { JSDOM } from "jsdom";
import {
  conectarConfirmacionDeEliminacion,
  MENSAJE_CONFIRMAR_ELIMINACION,
} from "../catalogo_funciones.js";

const PAGINA = `
  <main>
    <form id="eliminar" action="/muebles/eliminar/1" method="post" data-confirmar-eliminacion>
      <button type="submit">Eliminar</button>
    </form>
    <form id="otro" action="/muebles/editar/1" method="post">
      <button type="submit">Guardar</button>
    </form>
  </main>`;

describe("Confirmación de eliminación de muebles del catálogo", function() {
  let documento;
  let contenedor;

  beforeEach(function() {
    documento = new JSDOM(PAGINA).window.document;
    contenedor = documento.querySelector("main");
    conectarConfirmacionDeEliminacion(contenedor, documento.defaultView);
  });

  function enviar(id) {
    const formulario = documento.getElementById(id);
    const evento = new documento.defaultView.Event("submit", {
      bubbles: true,
      cancelable: true,
    });
    formulario.dispatchEvent(evento);
    return evento;
  }

  it("pide confirmación al enviar un formulario marcado para eliminar", function() {
    spyOn(documento.defaultView, "confirm").and.returnValue(true);

    enviar("eliminar");

    expect(documento.defaultView.confirm).toHaveBeenCalledWith(MENSAJE_CONFIRMAR_ELIMINACION);
  });

  it("deja enviar el formulario cuando el usuario confirma", function() {
    spyOn(documento.defaultView, "confirm").and.returnValue(true);

    const evento = enviar("eliminar");

    expect(evento.defaultPrevented).toBe(false);
  });

  it("cancela el envío cuando el usuario rechaza la confirmación", function() {
    spyOn(documento.defaultView, "confirm").and.returnValue(false);

    const evento = enviar("eliminar");

    expect(evento.defaultPrevented).toBe(true);
  });

  it("no pide confirmación para formularios que no eliminan", function() {
    spyOn(documento.defaultView, "confirm").and.returnValue(true);

    const evento = enviar("otro");

    expect(documento.defaultView.confirm).not.toHaveBeenCalled();
    expect(evento.defaultPrevented).toBe(false);
  });

  it("no falla si el contenedor no existe", function() {
    expect(function() {
      conectarConfirmacionDeEliminacion(null);
    }).not.toThrow();
  });
});
