const CAMPO_ANCHO = "input[name$='.ancho']";
const CAMPO_PROFUNDIDAD = "input[name$='.profundidad']";
const CAMPOS_DE_MEDIDA = `${CAMPO_ANCHO}, ${CAMPO_PROFUNDIDAD}`;

export function conectarAutocompletado(contenedor, selectorDelDisparador = "select[data-categoria]") {
  contenedor.addEventListener("input", (evento) => {
    if (evento.target.matches(CAMPOS_DE_MEDIDA)) {
      evento.target.dataset.editadoAMano = "true";
    }
  });

  contenedor.addEventListener("change", (evento) => {
    if (!evento.target.matches(selectorDelDisparador)) {
      return;
    }
    const selector = evento.target;
    const opcion = selector.options[selector.selectedIndex];
    if (!opcion?.dataset.ancho) {
      return;
    }
    const fila = selector.closest("tr");
    autocompletar(fila.querySelector(CAMPO_ANCHO), opcion.dataset.ancho);
    autocompletar(fila.querySelector(CAMPO_PROFUNDIDAD), opcion.dataset.profundidad);
  });
}

// Variante para un formulario de alta simple (no una tabla): los campos se buscan
// por selector dentro del mismo contenedor del select de categoría.
export function conectarAutocompletadoDeFormulario(
  contenedor,
  selectorAncho,
  selectorProfundidad
) {
  const camposDeMedida = `${selectorAncho}, ${selectorProfundidad}`;

  contenedor.addEventListener("input", (evento) => {
    if (evento.target.matches(camposDeMedida)) {
      evento.target.dataset.editadoAMano = "true";
    }
  });

  contenedor.addEventListener("change", (evento) => {
    if (!evento.target.matches("select[data-categoria]")) {
      return;
    }
    const selector = evento.target;
    const opcion = selector.options[selector.selectedIndex];
    if (!opcion?.dataset.ancho) {
      return;
    }
    autocompletar(contenedor.querySelector(selectorAncho), opcion.dataset.ancho);
    autocompletar(contenedor.querySelector(selectorProfundidad), opcion.dataset.profundidad);
  });
}

function autocompletar(campo, valorPromedio) {
  const fueEscritoPorElUsuario = campo.dataset.editadoAMano === "true" && campo.value !== "";
  if (!fueEscritoPorElUsuario) {
    campo.value = valorPromedio;
  }
}
