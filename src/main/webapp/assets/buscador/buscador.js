'use strict';
(() => {
    let serie = 0;
    function iniciar(select) {
        if (select.dataset.iniciado) return;
        select.dataset.iniciado = '1';
        const caja = document.createElement('div');
        caja.className = 'buscador-remoto';
        select.before(caja); caja.append(select);
        const input = document.createElement('input');
        input.type = 'text'; input.maxLength = 100; input.autocomplete = 'off';
        input.placeholder = select.dataset.buscar === 'productos' ? 'Escribe nombre o código del artículo…' : 'Escribe nombre o documento…';
        input.required = select.required; select.required = false; select.hidden = true;
        input.setAttribute('role', 'combobox'); input.setAttribute('aria-autocomplete', 'list');
        input.setAttribute('aria-expanded', 'false');
        const etiqueta = select.getAttribute('aria-label');
        if (etiqueta) input.setAttribute('aria-label', etiqueta);
        input.value = select.value ? select.selectedOptions[0].textContent : '';
        const lista = document.createElement('div'); lista.className = 'buscador-resultados';
        lista.id = 'busqueda-' + (++serie); lista.setAttribute('role', 'listbox'); lista.hidden = true;
        input.id = lista.id + '-entrada';
        const label = select.closest('label');
        if (label) {
            label.htmlFor = input.id;
            input.setAttribute('aria-label', Array.from(label.childNodes).filter(n => n.nodeType === Node.TEXT_NODE).map(n => n.textContent).join(' ').trim());
        }
        input.setAttribute('aria-controls', lista.id);
        const estado = document.createElement('span'); estado.className = 'buscador-estado';
        estado.id = lista.id + '-estado'; estado.setAttribute('role', 'status');
        input.setAttribute('aria-describedby', estado.id);
        const mas = document.createElement('button'); mas.type = 'button'; mas.className = 'secondary buscador-mas';
        mas.textContent = 'Mostrar más coincidencias'; mas.hidden = true;
        caja.append(input, estado, lista, mas);
        let temporizador, peticion, secuencia = 0, pagina = 0, indice = -1, resultados = [];
        function cerrar() { lista.hidden = true; mas.hidden = true; input.setAttribute('aria-expanded', 'false'); input.removeAttribute('aria-activedescendant'); }
        function seleccionar(item) {
            select.replaceChildren(new Option(item.texto, item.id, true, true));
            input.value = item.texto; input.setCustomValidity('');
            secuencia++; peticion?.abort(); clearTimeout(temporizador);
            cerrar(); estado.textContent = 'Seleccionado'; input.focus();
        }
        function activar(n) {
            indice = n;
            Array.from(lista.children).forEach((op, i) => op.setAttribute('aria-selected', String(i === n)));
            if (n >= 0) { input.setAttribute('aria-activedescendant', lista.children[n].id); lista.children[n].scrollIntoView({block:'nearest'}); }
        }
        async function buscar(anexar = false) {
            const texto = input.value.trim();
            if (texto.length < 2) { cerrar(); estado.textContent = 'Escribe al menos 2 caracteres.'; return; }
            const turno = ++secuencia; peticion?.abort(); peticion = new AbortController();
            const paginaSolicitada = anexar ? pagina + 1 : 0;
            if (!anexar) { pagina = 0; resultados = []; lista.replaceChildren(); indice = -1; }
            mas.hidden = true; estado.textContent = 'Buscando…';
            try {
                const url = new URL(select.dataset.url, location.origin);
                url.search = new URLSearchParams({tipo:select.dataset.buscar, contexto:select.dataset.contexto, q:texto, pagina:paginaSolicitada});
                const respuesta = await fetch(url, {signal:peticion.signal, headers:{Accept:'application/json'}});
                if (respuesta.redirected || !respuesta.headers.get('content-type')?.includes('application/json')) throw new Error('La sesión terminó. Recarga e inicia sesión.');
                const datos = await respuesta.json();
                if (!respuesta.ok) throw new Error(datos.error || 'No se pudo buscar. Intenta nuevamente.');
                if (turno !== secuencia || input.value.trim() !== texto) return;
                pagina = paginaSolicitada;
                for (const item of datos.resultados) {
                    const op = document.createElement('div'); op.className = 'buscador-opcion'; op.setAttribute('role','option');
                    op.id = lista.id + '-' + resultados.length; op.setAttribute('aria-selected','false'); op.textContent = item.texto;
                    op.addEventListener('mousedown', e => e.preventDefault());
                    op.addEventListener('click', () => seleccionar(item));
                    resultados.push(item); lista.append(op);
                }
                lista.hidden = !resultados.length || (!anexar && !caja.contains(document.activeElement));
                input.setAttribute('aria-expanded', String(!lista.hidden));
                mas.hidden = !datos.mas || lista.hidden;
                estado.textContent = resultados.length ? `${resultados.length} ${resultados.length === 1 ? 'coincidencia' : 'coincidencias'}${datos.mas ? '. Hay más; precisa el nombre o muestra más.' : '.'} Usa ↑ ↓ y Enter para seleccionar.` : 'Sin coincidencias. Prueba otro nombre o código.';
            } catch (error) {
                if (error.name !== 'AbortError' && turno === secuencia) { cerrar(); estado.textContent = error.message; }
            }
        }
        input.addEventListener('input', () => {
            select.value = ''; input.setCustomValidity('Selecciona una coincidencia de la búsqueda.');
            secuencia++; peticion?.abort(); cerrar(); clearTimeout(temporizador);
            temporizador = setTimeout(() => buscar(), 300);
        });
        input.addEventListener('keydown', e => {
            if (e.key === 'Escape') { secuencia++; peticion?.abort(); clearTimeout(temporizador); cerrar(); }
            if (!lista.hidden && ['ArrowDown','ArrowUp'].includes(e.key)) {
                e.preventDefault(); activar(e.key === 'ArrowDown' ? Math.min(indice+1,resultados.length-1) : Math.max(indice-1,0));
            }
            if (e.key === 'Enter' && !select.value) {
                e.preventDefault(); if (!lista.hidden && indice >= 0) seleccionar(resultados[indice]); else buscar();
            }
        });
        input.addEventListener('focus', () => { if (!select.value && input.value.trim().length >= 2) buscar(); });
        caja.addEventListener('focusout', () => setTimeout(() => { if (!caja.contains(document.activeElement)) cerrar(); }, 0));
        mas.addEventListener('click', () => buscar(true));
    }
    function preparar(raiz) { raiz.querySelectorAll('select[data-buscar]').forEach(iniciar); }
    preparar(document);
    document.addEventListener('novatech:linea', e => preparar(e.target));
})();
