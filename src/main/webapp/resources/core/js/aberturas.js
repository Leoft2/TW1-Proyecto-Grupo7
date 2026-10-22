import { agregarFilaAbertura, eliminarFilaAbertura } from "./aberturas_funciones.js";

const cuerpo = document.getElementById("filas-aberturas");
const filaPlantilla = document.getElementById("plantilla-abertura").content.firstElementChild;

document.getElementById("agregar-abertura").addEventListener("click", () => {
  agregarFilaAbertura(cuerpo, filaPlantilla);
});

cuerpo.addEventListener("click", (evento) => {
  if (evento.target.classList.contains("btn-eliminar-abertura")) {
    eliminarFilaAbertura(evento.target.closest("tr"), cuerpo);
  }
});
