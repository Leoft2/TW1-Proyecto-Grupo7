import {
  conectarConfirmacionDeEliminacion,
  conectarFiltrosDinamicos,
} from "./catalogo_funciones.js";

const principal = document.querySelector("main");
conectarConfirmacionDeEliminacion(principal);
conectarFiltrosDinamicos(principal);
