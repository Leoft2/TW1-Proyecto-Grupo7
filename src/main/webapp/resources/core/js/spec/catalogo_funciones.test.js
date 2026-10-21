/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import { JSDOM } from "jsdom";
import {
  conectarConfirmacionDeEliminacion,
  conectarFiltrosDinamicos,
  filtrosActivos,
  limpiarFiltros,
  MS_ESPERA_PARA_FILTRAR,
  parametrosActivos,
  urlConLosFiltros,
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

const PAGINA_DEL_CATALOGO = `
  <main>
    <form action="/catalogo" method="get" data-filtros-catalogo>
      <select id="estilo" name="estilo" data-etiqueta="Estilo">
        <option value="">Todos los estilos</option>
        <option value="Retro" selected>Retro</option>
        <option value="Japandi">Japandi</option>
      </select>
      <select id="tipoDeAmbiente" name="tipoDeAmbiente" data-etiqueta="Tipo de ambiente">
        <option value="">Todos los ambientes</option>
        <option value="Living">Living</option>
      </select>
      <input type="number" id="precioMin" name="precioMin" data-etiqueta="Precio mínimo" data-formato="precio">
      <input type="checkbox" id="soloSostenibles" name="soloSostenibles" value="true" data-etiqueta="Solo sostenibles">
      <label for="soloSostenibles">Solo sostenibles</label>
      <select id="orden" name="orden">
        <option value="">Sin ordenar</option>
        <option value="PRECIO_ASC">Menor a mayor</option>
      </select>
      <button type="submit">Filtrar</button>
      <a href="/catalogo" data-limpiar-filtros>Limpiar filtros</a>
    </form>
    <div data-resultados-catalogo>
      <div id="hb-resultados">
        <div data-filtros-activos hidden></div>
        Muebles originales
      </div>
    </div>
  </main>`;

function unaRespuesta(html, ok) {
  return {
    ok: ok === undefined ? true : ok,
    text: function() {
      return Promise.resolve(html);
    },
  };
}

// Deja que se resuelvan las promesas encadenadas de la búsqueda.
async function esperar() {
  for (let vuelta = 0; vuelta < 10; vuelta++) {
    await Promise.resolve();
  }
}

describe("Filtros dinámicos del catálogo", function() {
  let ventana;
  let documento;
  let formulario;
  let resultados;

  beforeEach(function() {
    ventana = new JSDOM(PAGINA_DEL_CATALOGO, {
      runScripts: "outside-only",
      url: "http://localhost/catalogo",
    }).window;
    documento = ventana.document;
    formulario = documento.querySelector("[data-filtros-catalogo]");
    resultados = documento.querySelector("[data-resultados-catalogo]");
  });

  function control(nombre) {
    return formulario.elements.namedItem(nombre);
  }

  describe("los parámetros del formulario", function() {
    it("descarta los filtros que no tienen valor", function() {
      const parametros = parametrosActivos(formulario);

      expect(parametros.get("estilo")).toBe("Retro");
      expect(parametros.has("precioMin")).toBe(false);
      expect(parametros.has("orden")).toBe(false);
    });

    it("incluye el checkbox solo cuando está marcado", function() {
      expect(parametrosActivos(formulario).has("soloSostenibles")).toBe(false);

      control("soloSostenibles").checked = true;

      expect(parametrosActivos(formulario).get("soloSostenibles")).toBe("true");
    });

    it("arma la url con los filtros que están puestos", function() {
      expect(urlConLosFiltros(formulario, "/catalogo")).toBe("/catalogo?estilo=Retro");
    });

    it("deja la url sin parámetros cuando no hay filtros", function() {
      limpiarFiltros(formulario);

      expect(urlConLosFiltros(formulario, "/catalogo")).toBe("/catalogo");
    });
  });

  describe("los filtros activos", function() {
    it("devuelve el nombre, la etiqueta y el valor de cada filtro", function() {
      const filtros = filtrosActivos(formulario);

      expect(filtros.length).toBe(1);
      expect(filtros[0].campo).toBe("estilo");
      expect(filtros[0].etiqueta).toBe("Estilo");
      expect(filtros[0].valor).toBe("Retro");
    });

    it("no incluye el orden porque no es un filtro", function() {
      control("orden").value = "PRECIO_ASC";

      const campos = filtrosActivos(formulario).map((filtro) => filtro.campo);

      expect(campos).not.toContain("orden");
    });

    it("muestra los precios con separador de miles", function() {
      control("precioMin").value = "50000";

      const filtro = filtrosActivos(formulario).find((item) => item.campo === "precioMin");

      expect(filtro.valor).toBe("50,000");
    });
  });

  describe("la limpieza de los filtros", function() {
    it("vacía los selects, los números y desmarca el checkbox", function() {
      control("precioMin").value = "100";
      control("soloSostenibles").checked = true;

      limpiarFiltros(formulario);

      expect(control("estilo").value).toBe("");
      expect(control("precioMin").value).toBe("");
      expect(control("soloSostenibles").checked).toBe(false);
    });
  });

  describe("la conexión de los filtros", function() {
    let buscando;
    let navegado;

    beforeEach(function() {
      buscando = jasmine.createSpy("buscar").and.callFake((url) =>
        Promise.resolve(
          unaRespuesta(
            `<div id="hb-resultados"><div data-filtros-activos hidden></div>${url}</div>`
          )
        )
      );
      navegado = jasmine.createSpy("navegar");
    });

    function conectar() {
      conectarFiltrosDinamicos(documento.querySelector("main"), {
        buscar: buscando,
        navegar: navegado,
      });
    }

    function cambiar(nombre, valor) {
      const elemento = control(nombre);
      if (elemento.type === "checkbox") {
        elemento.checked = valor;
      } else {
        elemento.value = valor;
      }
      elemento.dispatchEvent(new ventana.Event("change", { bubbles: true }));
    }

    it("busca los resultados cuando cambia un filtro", async function() {
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      expect(buscando).toHaveBeenCalledWith("/catalogo?estilo=Japandi");
      expect(resultados.querySelector("#hb-resultados").textContent).toContain("Japandi");
    });

    it("actualiza la url de la página con los filtros aplicados", async function() {
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      expect(ventana.location.search).toBe("?estilo=Japandi");
    });

    it("muestra los filtros aplicados como chips", async function() {
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      const chips = documento.querySelectorAll("[data-filtros-activos] .hb-chip-filtro");

      expect(chips.length).toBe(1);
      expect(chips[0].textContent).toContain("Estilo");
      expect(chips[0].textContent).toContain("Japandi");
    });

    it("quita un filtro cuando se hace clic en la cruz del chip", async function() {
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      const boton = documento.querySelector("[data-filtros-activos] [data-quitar-filtro]");
      boton.dispatchEvent(new ventana.Event("click", { bubbles: true }));
      await esperar();

      expect(control("estilo").value).toBe("");
      expect(buscando).toHaveBeenCalledWith("/catalogo");
    });

    it("limpia todos los filtros cuando se hace clic en limpiar", async function() {
      conectar();

      control("precioMin").value = "100";
      cambiar("estilo", "Japandi");
      await esperar();

      const limpiar = documento.querySelector("[data-limpiar-filtros]");
      limpiar.dispatchEvent(new ventana.Event("click", { bubbles: true }));
      await esperar();

      expect(control("estilo").value).toBe("");
      expect(control("precioMin").value).toBe("");
      expect(buscando).toHaveBeenCalledWith("/catalogo");
    });

    it("no busca dos veces si el filtro no cambió", async function() {
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();
      cambiar("estilo", "Japandi");
      await esperar();

      expect(buscando.calls.count()).toBe(1);
    });

    it("espera a que el usuario termine de escribir el precio", function() {
      jasmine.clock().install();
      try {
        conectar();

        const precio = control("precioMin");
        precio.value = "5";
        precio.dispatchEvent(new ventana.Event("input", { bubbles: true }));

        expect(buscando).not.toHaveBeenCalled();

        jasmine.clock().tick(MS_ESPERA_PARA_FILTRAR + 10);

        expect(buscando).toHaveBeenCalledWith("/catalogo?estilo=Retro&precioMin=5");
      } finally {
        jasmine.clock().uninstall();
      }
    });

    it("busca al enviar el formulario", async function() {
      conectar();

      const evento = new ventana.Event("submit", { bubbles: true, cancelable: true });
      formulario.dispatchEvent(evento);
      await esperar();

      expect(evento.defaultPrevented).toBe(true);
      expect(buscando).toHaveBeenCalledWith("/catalogo?estilo=Retro");
    });

    it("si la búsqueda falla navega a la página con los filtros", async function() {
      buscando.and.callFake(() => Promise.reject(new Error("sin red")));
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      expect(navegado).toHaveBeenCalledWith("/catalogo?estilo=Japandi");
    });

    it("si la respuesta no es exitosa navega a la página con los filtros", async function() {
      buscando.and.callFake(() => Promise.resolve(unaRespuesta("", false)));
      conectar();

      cambiar("estilo", "Japandi");
      await esperar();

      expect(navegado).toHaveBeenCalledWith("/catalogo?estilo=Japandi");
    });

    it("no falla si el contenedor de los filtros no existe", function() {
      expect(function() {
        conectarFiltrosDinamicos(null, { buscar: buscando, navegar: navegado });
      }).not.toThrow();
    });
  });
});
