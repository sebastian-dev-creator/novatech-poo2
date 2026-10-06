"use strict";
const cerrar = document.getElementById("cerrar-ventana");
if (cerrar) {
  cerrar.hidden = false;
  cerrar.addEventListener("click", () => window.close());
}
// No se manipula el historial para forzar las restricciones del navegador.
window.setTimeout(() => window.close(), 1500);
