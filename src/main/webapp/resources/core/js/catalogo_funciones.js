export const MENSAJE_CONFIRMAR_ELIMINACION = "¿Seguro que querés eliminar este mueble del catálogo?";

// Conecta la confirmación a todos los formularios de eliminación del contenedor.
// El atributo de datos evita pisar un mensaje propio si algún formulario lo define.
export function conectarConfirmacionDeEliminacion(contenedor, ventana = globalThis.window) {
  if (!contenedor) {
    return;
  }
  contenedor.addEventListener("submit", (evento) => {
    const formulario = evento.target;
    if (!formulario.matches("form[data-confirmar-eliminacion]")) {
      return;
    }
    const mensaje = formulario.dataset.mensajeConfirmacion || MENSAJE_CONFIRMAR_ELIMINACION;
    if (!ventana.confirm(mensaje)) {
      evento.preventDefault();
    }
  });
}
