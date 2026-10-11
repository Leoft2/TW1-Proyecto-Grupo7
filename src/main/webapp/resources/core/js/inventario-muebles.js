import { conectarAutocompletadoDeFormulario } from "./dimensiones_funciones.js";

const formularioDeAlta = document.getElementById("agregar-mueble");
conectarAutocompletadoDeFormulario(formularioDeAlta, "#agregar-ancho", "#agregar-largo");