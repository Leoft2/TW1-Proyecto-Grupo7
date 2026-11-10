const plano = document.querySelector(".plano-svg");
const alerta = document.getElementById("plano-alerta-movimiento");
const botonQuitarMueble = document.getElementById("boton-quitar-mueble");
const indiceMuebleSeleccionado = document.getElementById("indice-mueble-seleccionado");
const ayudaSeleccionMueble = document.getElementById("ayuda-seleccion-mueble");

if (plano && alerta) {
  const anchoAmbiente = Number(plano.dataset.ancho);
  const largoAmbiente = Number(plano.dataset.largo);
  let muebleEnMovimiento = null;

  function posicionEnPlano(evento) {
    const punto = plano.createSVGPoint();
    punto.x = evento.clientX;
    punto.y = evento.clientY;
    return punto.matrixTransform(plano.getScreenCTM().inverse());
  }

  function mostrarAdvertencia(mensaje) {
    alerta.textContent = mensaje;
    alerta.classList.remove("d-none");
  }

  function limpiarAdvertencia() {
    alerta.textContent = "";
    alerta.classList.add("d-none");
  }

  function seleccionarMueble(mueble) {
    plano.querySelectorAll(".plano-mueble-seleccionado").forEach((seleccionado) => {
      seleccionado.classList.remove("plano-mueble-seleccionado");
      seleccionado.setAttribute("aria-pressed", "false");
    });
    mueble.classList.add("plano-mueble-seleccionado");
    mueble.setAttribute("aria-pressed", "true");
    indiceMuebleSeleccionado.value = mueble.dataset.indice;
    botonQuitarMueble.disabled = false;
    ayudaSeleccionMueble.textContent = "Mueble seleccionado: " +
      mueble.querySelector(".plano-mueble-nombre").textContent.trim();
  }

  function estaDentroDelAmbiente(x, y, mueble) {
    const cuerpo = mueble.querySelector(".plano-mueble-cuerpo");
    const ancho = Number(cuerpo.getAttribute("width"));
    const largo = Number(cuerpo.getAttribute("height"));
    return x >= 0 && y >= 0 && x + ancho <= anchoAmbiente && y + largo <= largoAmbiente;
  }

  function actualizarTabla(indice, posicionX, posicionY) {
    const fila = document.querySelector(`tr[data-indice="${indice}"]`);
    if (fila) {
      const formato = new Intl.NumberFormat("es-AR", {
        minimumFractionDigits: 1,
        maximumFractionDigits: 1,
      });
      fila.querySelector(".plano-posicion-x").textContent = formato.format(posicionX);
      fila.querySelector(".plano-posicion-y").textContent = formato.format(posicionY);
    }
  }

  function actualizarOrientacion(mueble, frente) {
    const indicador = mueble.querySelector(".plano-orientacion");
    const cuerpo = mueble.querySelector(".plano-mueble-cuerpo");
    const ancho = Number(cuerpo.getAttribute("width"));
    const largo = Number(cuerpo.getAttribute("height"));
    let posicionX = Number(cuerpo.getAttribute("x")) + ancho / 2;
    let posicionY = Number(cuerpo.getAttribute("y")) + largo / 2;
    const angulos = { SUPERIOR: 0, DERECHO: 90, INFERIOR: 180, IZQUIERDO: 270 };
    if (frente === "DERECHO") posicionX += ancho * 0.32;
    if (frente === "IZQUIERDO") posicionX -= ancho * 0.32;
    if (frente === "INFERIOR") posicionY += largo * 0.32;
    if (frente === "SUPERIOR") posicionY -= largo * 0.32;
    indicador.setAttribute("transform", `translate(${posicionX} ${posicionY}) rotate(${angulos[frente] || 0})`);
    indicador.setAttribute("visibility", frente ? "visible" : "hidden");
    indicador.setAttribute("aria-label", frente ? `Frente hacia ${frente}` : "Orientación no determinada");
  }

  function guardarMovimiento(mueble, posicionNueva, desplazamiento) {
    const datos = new URLSearchParams();
    datos.set("indice", mueble.dataset.indice);
    datos.set("posicionX", posicionNueva.x);
    datos.set("posicionY", posicionNueva.y);

    fetch(plano.dataset.url, {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: datos,
    })
      .then((respuesta) => {
        if (!respuesta.ok) {
          return respuesta.text().then((mensaje) => {
            throw new Error(mensaje);
          });
        }

        mueble.querySelectorAll("[x][y]").forEach((elemento) => {
          elemento.setAttribute("x", Number(elemento.getAttribute("x")) + desplazamiento.x);
          elemento.setAttribute("y", Number(elemento.getAttribute("y")) + desplazamiento.y);
        });
        mueble.removeAttribute("transform");
        mueble.dataset.posicionX = posicionNueva.x;
        mueble.dataset.posicionY = posicionNueva.y;
        actualizarOrientacion(mueble, respuesta.headers.get("X-Orientacion"));
        actualizarTabla(mueble.dataset.indice, posicionNueva.x, posicionNueva.y);
        limpiarAdvertencia();
      })
      .catch((error) => {
        mueble.removeAttribute("transform");
        mostrarAdvertencia(error.message);
      })
      .finally(() => {
        muebleEnMovimiento = null;
      });
  }

  plano.querySelectorAll(".plano-mueble-arrastrable").forEach((mueble) => {
    mueble.addEventListener("click", () => seleccionarMueble(mueble));
    mueble.addEventListener("keydown", (evento) => {
      if (evento.key === "Enter" || evento.key === " ") {
        evento.preventDefault();
        seleccionarMueble(mueble);
      }
    });

    mueble.addEventListener("pointerdown", (evento) => {
      if (mueble.dataset.anclado === "true") {
        mostrarAdvertencia("El mueble está anclado. Desanclalo antes de moverlo.");
        return;
      }
      if (muebleEnMovimiento || evento.button !== 0) {
        return;
      }

      evento.preventDefault();
      muebleEnMovimiento = mueble;
      mueble.setPointerCapture(evento.pointerId);
      const puntoInicial = posicionEnPlano(evento);
      const posicionInicial = {
        x: Number(mueble.dataset.posicionX),
        y: Number(mueble.dataset.posicionY),
      };

      const moverMueble = (movimiento) => {
        const puntoActual = posicionEnPlano(movimiento);
        const desplazamiento = {
          x: puntoActual.x - puntoInicial.x,
          y: puntoActual.y - puntoInicial.y,
        };
        const posicionNueva = {
          x: posicionInicial.x + desplazamiento.x,
          y: posicionInicial.y + desplazamiento.y,
        };
        mueble.setAttribute(
          "transform",
          `translate(${desplazamiento.x} ${desplazamiento.y})`
        );

        if (!estaDentroDelAmbiente(posicionNueva.x, posicionNueva.y, mueble)) {
          mostrarAdvertencia(
            "El mueble debe permanecer dentro de los límites del ambiente."
          );
        } else {
          limpiarAdvertencia();
        }
      };

      const cancelarMovimiento = (cancelacion) => {
        mueble.removeEventListener("pointermove", moverMueble);
        mueble.removeEventListener("pointerup", finalizarMovimiento);
        mueble.removeEventListener("pointercancel", cancelarMovimiento);
        mueble.removeAttribute("transform");
        muebleEnMovimiento = null;
        if (cancelacion.type === "pointercancel") {
          limpiarAdvertencia();
        }
      };

      const finalizarMovimiento = (final) => {
        mueble.removeEventListener("pointermove", moverMueble);
        mueble.removeEventListener("pointerup", finalizarMovimiento);
        mueble.removeEventListener("pointercancel", cancelarMovimiento);

        const puntoFinal = posicionEnPlano(final);
        const desplazamiento = {
          x: puntoFinal.x - puntoInicial.x,
          y: puntoFinal.y - puntoInicial.y,
        };
        const posicionNueva = {
          x: posicionInicial.x + desplazamiento.x,
          y: posicionInicial.y + desplazamiento.y,
        };

        if (Math.abs(desplazamiento.x) < 0.005 && Math.abs(desplazamiento.y) < 0.005) {
          mueble.removeAttribute("transform");
          muebleEnMovimiento = null;
          return;
        }

        if (!estaDentroDelAmbiente(posicionNueva.x, posicionNueva.y, mueble)) {
          mueble.removeAttribute("transform");
          mostrarAdvertencia(
            "El mueble debe permanecer dentro de los límites del ambiente. Se mantuvo su posición anterior."
          );
          muebleEnMovimiento = null;
          return;
        }

        guardarMovimiento(mueble, posicionNueva, desplazamiento);
      };

      mueble.addEventListener("pointermove", moverMueble);
      mueble.addEventListener("pointerup", finalizarMovimiento);
      mueble.addEventListener("pointercancel", cancelarMovimiento);
    });
  });
}
