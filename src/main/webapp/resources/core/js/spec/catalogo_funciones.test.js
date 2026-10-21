/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { JSDOM } from "jsdom";
import {
  conectarConfirmacionDeEliminacion,
  MENSAJE_CONFIRMAR_ELIMINACION,
  TITULO_CONFIRMAR_ELIMINACION,
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
  let formularioEliminar;

  beforeEach(function() {
    documento = new JSDOM(PAGINA, { runScripts: "outside-only" }).window.document;
    contenedor = documento.querySelector("main");
    formularioEliminar = documento.getElementById("eliminar");
    conectarConfirmacionDeEliminacion(contenedor);
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

  function clickEn(accion) {
    documento
      .querySelector(`#hb-modal-confirmacion [data-accion='${accion}']`)
      .dispatchEvent(new documento.defaultView.Event("click", { bubbles: true }));
  }

  it("crea el modal una sola vez", function() {
    expect(documento.querySelectorAll("#hb-modal-confirmacion").length).toBe(1);
  });

  it("abre el modal con el mensaje al enviar un formulario marcado para eliminar", function() {
    const evento = enviar("eliminar");

    expect(evento.defaultPrevented).toBe(true);
    expect(documento.getElementById("hb-modal-confirmacion").hidden).toBe(false);
    expect(documento.querySelector(".hb-modal-titulo").textContent).toBe(
      TITULO_CONFIRMAR_ELIMINACION
    );

    expect(documento.querySelector(".hb-modal-mensaje").textContent).toBe(
      MENSAJE_CONFIRMAR_ELIMINACION
    );
  });

  it("no abre el modal para formularios que no eliminan", function() {
    enviar("otro");

    expect(documento.getElementById("hb-modal-confirmacion").hidden).toBe(true);
  });

  it("envía el formulario al confirmar", function() {
    const enviados = [];
    formularioEliminar.submit = function() {
      enviados.push(this.id);
    };

    enviar("eliminar");
    clickEn("confirmar");

    expect(enviados).toEqual(["eliminar"]);
    expect(documento.getElementById("hb-modal-confirmacion").hidden).toBe(true);
  });

  it("no envía el formulario al cancelar", function() {
    let enviado = false;
    formularioEliminar.submit = function() {
      enviado = true;
    };

    enviar("eliminar");
    clickEn("cancelar");

    expect(enviado).toBe(false);
    expect(documento.getElementById("hb-modal-confirmacion").hidden).toBe(true);
  });

  it("cierra el modal al presionar Escape", function() {
    enviar("eliminar");

    documento.dispatchEvent(
      new documento.defaultView.KeyboardEvent("keydown", { key: "Escape" })
    );

    expect(documento.getElementById("hb-modal-confirmacion").hidden).toBe(true);
  });

  it("usa un mensaje propio del formulario si lo define", function() {
    formularioEliminar.dataset.mensajeConfirmacion = "¿Borrar de verdad?";

    enviar("eliminar");

    expect(documento.querySelector(".hb-modal-mensaje").textContent).toBe("¿Borrar de verdad?");
  });

  it("no falla si el contenedor no existe", function() {
    expect(function() {
      conectarConfirmacionDeEliminacion(null);
    }).not.toThrow();
  });
});
