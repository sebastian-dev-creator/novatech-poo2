'use strict';
function actualizarLineasCompra() {
    const filas = document.getElementById('lineas-compra');
    if (!filas) return;
    Array.from(filas.rows).forEach((fila, i) => {
        let quitar = fila.querySelector('.quitar-compra');
        if (!quitar) {
            quitar = document.createElement('button'); quitar.type = 'button';
            quitar.className = 'secondary quitar-compra';
            fila.cells[0].appendChild(quitar);
            quitar.addEventListener('click', () => {
                if (filas.rows.length > 1) { fila.remove(); actualizarLineasCompra(); }
            });
        }
        quitar.textContent = 'Retirar línea ' + (i + 1); quitar.disabled = filas.rows.length === 1;
        for (const control of fila.querySelectorAll('select,[role="combobox"]')) control.setAttribute('aria-label','Artículo de compra ' + (i + 1));
        fila.querySelector('[name="cantidad"]').setAttribute('aria-label','Cantidad ' + (i + 1));
        fila.querySelector('[name="costo"]').setAttribute('aria-label','Costo unitario ' + (i + 1));
    });
    document.getElementById('agregar-compra').disabled = filas.rows.length >= 20;
}
document.addEventListener('DOMContentLoaded', actualizarLineasCompra);
document.getElementById('agregar-compra')?.addEventListener('click', () => {
    const filas = document.getElementById('lineas-compra');
    if (filas.rows.length >= 20) return;
    const fila = filas.rows[0].cloneNode(true);
    fila.querySelector('.quitar-compra')?.remove();
    const mejorado = fila.querySelector('.buscador-remoto');
    if (mejorado) { const select = mejorado.querySelector('select'); select.hidden = false; select.required = true; delete select.dataset.iniciado; mejorado.replaceWith(select); }
    fila.querySelector('select').value = '';
    fila.querySelector('[name="cantidad"]').value = '1';
    fila.querySelector('[name="costo"]').value = '';
    filas.appendChild(fila);
    fila.dispatchEvent(new CustomEvent('novatech:linea', {bubbles:true}));
    (fila.querySelector('[role="combobox"]') || fila.querySelector('select')).focus();
    actualizarLineasCompra();
});
