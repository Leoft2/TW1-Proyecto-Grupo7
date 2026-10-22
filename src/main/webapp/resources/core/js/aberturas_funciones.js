const PREFIJO_DE_ABERTURAS = "aberturas";

const CAMPOS_DE_LA_ABERTURA = {
  __TIPO__: "tipo",
  __MURO__: "muro",
  __POSICION__: "posicion",
  __ANCHO__: "ancho",
  __RADIO__: "radioDeGiro",
};

export function agregarFilaAbertura(cuerpo, filaPlantilla) {
  const prefijo = `${PREFIJO_DE_ABERTURAS}[${cuerpo.querySelectorAll("tr").length}]`;
  const fila = filaPlantilla.cloneNode(true);
  fila.querySelectorAll("input, select").forEach((campo) => {
    const nombreDelCampo = CAMPOS_DE_LA_ABERTURA[campo.name];
    if (nombreDelCampo) {
      campo.name = `${prefijo}.${nombreDelCampo}`;
    }
  });
  cuerpo.appendChild(fila);
}

export function eliminarFilaAbertura(fila, cuerpo) {
  fila.remove();
  renumerarAberturas(cuerpo);
}

export function renumerarAberturas(cuerpo) {
  const patron = new RegExp(`${PREFIJO_DE_ABERTURAS}\\[\\d+\\]`);
  cuerpo.querySelectorAll("tr").forEach((fila, indice) => {
    fila.querySelectorAll("input, select").forEach((campo) => {
      if (campo.name) {
        campo.name = campo.name.replace(patron, `${PREFIJO_DE_ABERTURAS}[${indice}]`);
      }
    });
  });
}
