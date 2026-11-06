'use strict';
document.getElementById('agregar-compra')?.addEventListener('click', () => {
    const filas = document.getElementById('lineas-compra');
    if (filas.rows.length >= 20) return;
    const fila = filas.rows[0].cloneNode(true);
    fila.querySelector('select').value = '';
    fila.querySelector('[name="cantidad"]').value = '1';
    fila.querySelector('[name="costo"]').value = '';
    filas.appendChild(fila);
    fila.querySelector('select').focus();
    document.getElementById('agregar-compra').disabled = filas.rows.length >= 20;
});
