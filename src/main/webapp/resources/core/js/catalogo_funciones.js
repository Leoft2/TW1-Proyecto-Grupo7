export const MENSAJE_CONFIRMAR_ELIMINACION = "¿Seguro que querés eliminar este mueble del catálogo?";
export const TITULO_CONFIRMAR_ELIMINACION = "Eliminar mueble del catálogo";

export const MS_ESPERA_PARA_FILTRAR = 350;
export const SELECTOR_FORMULARIO_DE_FILTROS = "[data-filtros-catalogo]";
export const SELECTOR_RESULTADOS = "[data-resultados-catalogo]";
export const SELECTOR_FILTROS_ACTIVOS = "[data-filtros-activos]";
export const SELECTOR_INDICADOR_DE_CARGA = "[data-filtros-cargando]";
export const SELECTOR_LIMPIAR = "[data-limpiar-filtros]";
export const ATRIBUTO_QUITAR_FILTRO = "data-quitar-filtro";
export const ID_DE_RESULTADOS = "hb-resultados";

const ID_MODAL = "hb-modal-confirmacion";

export function conectarConfirmacionDeEliminacion(contenedor) {
  if (!contenedor) return;

  const documento = contenedor.ownerDocument;
  const modal = obtenerModal(documento);
  let formularioPendiente = null;

  function cancelar() {
    formularioPendiente = null;
    cerrarModal(modal);
  }

  function confirmar() {
    const formulario = formularioPendiente;
    formularioPendiente = null;
    cerrarModal(modal);
    formulario.submit();
  }

  contenedor.addEventListener("submit", (evento) => {
    const formulario = evento.target;
    if (!formulario.matches("form[data-confirmar-eliminacion]")) return;

    evento.preventDefault();
    formularioPendiente = formulario;

    const mensaje = formulario.dataset.mensajeConfirmacion || MENSAJE_CONFIRMAR_ELIMINACION;
    abrirModal(modal, TITULO_CONFIRMAR_ELIMINACION, mensaje);
  });

  modal.addEventListener("click", (evento) => {
    const accion = evento.target.closest("[data-accion]")?.dataset.accion;

    if (accion === "cancelar") cancelar();
    if (accion === "confirmar" && formularioPendiente) confirmar();
  });

  documento.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape" && !modal.hidden) cancelar();
  });
}

function obtenerModal(documento) {
  const existente = documento.getElementById(ID_MODAL);
  if (existente) return existente;

  const modal = documento.createElement("div");
  modal.id = ID_MODAL;
  modal.className = "hb-modal";
  modal.hidden = true;
  modal.innerHTML = `
    <div class="hb-modal-backdrop" data-accion="cancelar"></div>
    <div class="hb-modal-dialog" role="dialog" aria-modal="true">
      <div class="hb-modal-content">
        <div class="hb-modal-icon"><i class="bi bi-trash3"></i></div>
        <h2 class="hb-modal-titulo"></h2>
        <p class="hb-modal-mensaje"></p>
        <div class="hb-modal-acciones">
          <button type="button" class="btn btn-secondary" data-accion="cancelar">Cancelar</button>
          <button type="button" class="btn btn-danger" data-accion="confirmar">Eliminar</button>
        </div>
      </div>
    </div>`;

  documento.body.appendChild(modal);
  return modal;
}

function abrirModal(modal, titulo, mensaje) {
  modal.querySelector(".hb-modal-titulo").textContent = titulo;
  modal.querySelector(".hb-modal-mensaje").textContent = mensaje;
  modal.hidden = false;
  modal.classList.add("hb-modal-abierto");
  modal.querySelector("[data-accion='confirmar']").focus();
}

function cerrarModal(modal) {
  modal.hidden = true;
  modal.classList.remove("hb-modal-abierto");
}

export function conectarFiltrosDinamicos(contenedor, opciones = {}) {
  if (!contenedor) return;

  const documento = contenedor.ownerDocument;
  const ventana = documento.defaultView;
  const formulario = contenedor.querySelector(SELECTOR_FORMULARIO_DE_FILTROS);
  const resultados = contenedor.querySelector(SELECTOR_RESULTADOS);

  if (!formulario || !resultados) return;

  const base = formulario.getAttribute("action") || "/catalogo";
  const espera = opciones.espera ?? MS_ESPERA_PARA_FILTRAR;

  const buscar =
    opciones.buscar ||
    ((url) => ventana.fetch(url, { headers: { "X-Requested-With": "XMLHttpRequest" } }));

  const navegar = opciones.navegar || ((url) => ventana.location.assign(url));

  let temporizador = null; // para el debounce
  let ultimaUrl = null;    // para no repetir la misma búsqueda

  async function pedirResultados(url) {
    const respuesta = await buscar(url);
    if (!respuesta.ok) {
      throw new Error(`El servidor respondió ${respuesta.status}`);
    }

    const html = await respuesta.text();
    const nuevosResultados = extraerResultados(html, documento);
    if (!nuevosResultados) {
      throw new Error(`La respuesta no contiene #${ID_DE_RESULTADOS}`);
    }

    return nuevosResultados;
  }

  async function aplicarFiltros() {
    const url = urlConLosFiltros(formulario, base);
    if (url === ultimaUrl) return; // nada cambió
    ultimaUrl = url;

    marcarCargando(true);
    try {
      const nuevosResultados = await pedirResultados(url);
      resultados.innerHTML = nuevosResultados.outerHTML;
      pintarFiltrosActivos();
      actualizarLaUrl(ventana, url);
    } catch {
      navegar(url); 
    } finally {
      marcarCargando(false);
    }
  }

  function marcarCargando(cargando) {
    resultados.classList.toggle("hb-cargando", cargando);
    resultados.setAttribute("aria-busy", String(cargando));

    const indicador = contenedor.querySelector(SELECTOR_INDICADOR_DE_CARGA);
    if (indicador) indicador.hidden = !cargando;
  }

  function pintarFiltrosActivos() {
    const destino = contenedor.querySelector(SELECTOR_FILTROS_ACTIVOS);
    if (!destino) return;

    const filtros = filtrosActivos(formulario);
    destino.textContent = "";
    destino.hidden = filtros.length === 0;

    for (const filtro of filtros) {
      destino.appendChild(crearChipDeFiltro(documento, filtro));
    }
  }

  function programarBusqueda(milisegundos) {
    clearTimeout(temporizador);
    temporizador = null;

    if (milisegundos === 0) {
      aplicarFiltros();
      return;
    }

    temporizador = setTimeout(() => {
      temporizador = null;
      aplicarFiltros();
    }, milisegundos);
  }

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    programarBusqueda(0);
  });

  formulario.addEventListener("change", () => {
    programarBusqueda(0);
  });

  formulario.addEventListener("input", (evento) => {
    if (esUnCampoDeTexto(evento.target)) {
      programarBusqueda(espera);
    }
  });

  contenedor.addEventListener("click", (evento) => {
    const botonLimpiar = evento.target.closest(SELECTOR_LIMPIAR);
    if (botonLimpiar) {
      evento.preventDefault();
      limpiarFiltros(formulario);
      programarBusqueda(0);
      return;
    }

    const botonQuitar = evento.target.closest(`[${ATRIBUTO_QUITAR_FILTRO}]`);
    if (botonQuitar) {
      evento.preventDefault();
      limpiarFiltro(formulario, botonQuitar.getAttribute(ATRIBUTO_QUITAR_FILTRO));
      programarBusqueda(0);
    }
  });

  pintarFiltrosActivos();
}

export function parametrosActivos(formulario) {
  const parametros = new URLSearchParams();

  for (const control of controlesDe(formulario)) {
    if (control.disabled) continue;

    const valor = valorDe(control);
    if (valor !== "") {
      parametros.append(control.name, valor);
    }
  }

  return parametros;
}

export function urlConLosFiltros(formulario, base) {
  const consulta = parametrosActivos(formulario).toString();
  const baseSinConsulta = base.split("?")[0];

  return consulta ? `${baseSinConsulta}?${consulta}` : baseSinConsulta;
}

export function filtrosActivos(formulario) {
  const filtros = [];

  for (const control of controlesDe(formulario)) {
    if (!control.dataset.etiqueta) continue;

    const valor = valorDe(control);
    if (valor === "") continue;

    filtros.push({
      campo: control.name,
      etiqueta: control.dataset.etiqueta,
      valor: textoDelValor(control, valor),
    });
  }

  return filtros;
}

function crearChipDeFiltro(documento, filtro) {
  const chip = documento.createElement("span");
  chip.className = "hb-chip hb-chip-filtro";

  chip.appendChild(crearTexto(documento, "hb-chip-filtro-etiqueta", filtro.etiqueta));

  if (filtro.valor && filtro.valor !== filtro.etiqueta) {
    chip.appendChild(crearTexto(documento, "hb-chip-filtro-valor", filtro.valor));
  }

  const boton = documento.createElement("button");
  boton.type = "button";
  boton.className = "hb-chip-filtro-quitar";
  boton.setAttribute(ATRIBUTO_QUITAR_FILTRO, filtro.campo);
  boton.setAttribute("aria-label", `Quitar el filtro ${filtro.etiqueta}`);
  boton.textContent = "×";
  chip.appendChild(boton);

  return chip;
}

function crearTexto(documento, clase, texto) {
  const elemento = documento.createElement("span");
  elemento.className = clase;
  elemento.textContent = texto;
  return elemento;
}

export function limpiarFiltros(formulario) {
  for (const control of controlesDe(formulario)) {
    vaciarControl(control);
  }
}

function limpiarFiltro(formulario, nombre) {
  const control = formulario.elements.namedItem(nombre);
  if (control) vaciarControl(control);
}

function vaciarControl(control) {
  if (esCasilla(control)) {
    control.checked = false;
  } else {
    control.value = "";
  }
}

//Para lectura del form: 
function controlesDe(formulario) {
  return Array.from(formulario.elements).filter((control) => control.name);
}

function esCasilla(control) {
  return control.type === "checkbox" || control.type === "radio";
}

function esUnCampoDeTexto(control) {
  return control.type === "number" || control.type === "text" || control.type === "search";
}

function valorDe(control) {
  if (esCasilla(control)) {
    return control.checked ? control.value || "true" : "";
  }
  return (control.value || "").trim();
}

function textoDelValor(control, valor) {
  if (control.dataset.formato === "precio") {
    const numero = Number(valor);
    if (!Number.isNaN(numero)) {
      return new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 }).format(numero);
    }
  }

  if (control.tagName === "SELECT") {
    const opcion = control.options[control.selectedIndex];
    const texto = opcion?.textContent.trim();
    return texto || valor;
  }

  if (esCasilla(control) && control.form) {
    const etiqueta = control.form.querySelector(`label[for='${control.id}']`);
    if (etiqueta) return etiqueta.textContent.trim();
  }

  return valor;
}

function extraerResultados(html, documento) {
  const ventana = documento.defaultView;
  if (!ventana || !ventana.DOMParser) return null;

  const documentoNuevo = new ventana.DOMParser().parseFromString(html, "text/html");
  return documentoNuevo.getElementById(ID_DE_RESULTADOS);
}

function actualizarLaUrl(ventana, url) {
  if (ventana.history && typeof ventana.history.replaceState === "function") {
    ventana.history.replaceState(null, "", url);
  }
}