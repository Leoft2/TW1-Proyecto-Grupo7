export const MENSAJE_CONFIRMAR_ELIMINACION = "¿Seguro que querés eliminar este mueble del catálogo?";
export const TITULO_CONFIRMAR_ELIMINACION = "Eliminar mueble del catálogo";

const ID_MODAL = "hb-modal-confirmacion";

// Crea (una sola vez) el modal de confirmación con el estilo de la web y lo agrega al body.
function obtenerModal(documento) {
  const existente = documento.getElementById(ID_MODAL);
  if (existente) {
    return existente;
  }

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

// Conecta la confirmación a todos los formularios de eliminación del contenedor. En vez del
// confirm() nativo (que rompe con el estilo de la web) se muestra un modal propio. El atributo
// de datos evita pisar un mensaje propio si algún formulario lo define.
export function conectarConfirmacionDeEliminacion(contenedor) {
  if (!contenedor) {
    return;
  }

  const documento = contenedor.ownerDocument;
  const modal = obtenerModal(documento);
  let formularioPendiente = null;

  const cerrar = () => {
    formularioPendiente = null;
    cerrarModal(modal);
  };

  contenedor.addEventListener("submit", (evento) => {
    const formulario = evento.target;
    if (!formulario.matches("form[data-confirmar-eliminacion]")) {
      return;
    }
    evento.preventDefault();
    formularioPendiente = formulario;
    const mensaje = formulario.dataset.mensajeConfirmacion || MENSAJE_CONFIRMAR_ELIMINACION;
    abrirModal(modal, TITULO_CONFIRMAR_ELIMINACION, mensaje);
  });

  modal.addEventListener("click", (evento) => {
    const accion = evento.target.closest("[data-accion]")?.dataset.accion;
    if (accion === "cancelar") {
      cerrar();
    }
    if (accion === "confirmar" && formularioPendiente) {
      const formulario = formularioPendiente;
      formularioPendiente = null;
      cerrarModal(modal);
      formulario.submit();
    }
  });

  documento.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape" && !modal.hidden) {
      cerrar();
    }
  });
}
