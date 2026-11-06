'use strict';
const contenedor = document.getElementById('grafico-reporte');
if (contenedor && window.echarts) {
    const etiqueta = Number(contenedor.dataset.etiqueta);
    const medida = Number(contenedor.dataset.medida);
    const tabla = document.getElementById('tabla-reporte');
    const grupos = new Map();
    for (const fila of tabla.tBodies[0].rows) {
        const nombre = fila.cells[etiqueta].textContent;
        const celda = medida < 0 ? null : fila.cells[medida];
        if (celda && !celda.hasAttribute('data-numero')) continue;
        const valor = celda ? Number(celda.dataset.numero) : 1;
        grupos.set(nombre, (grupos.get(nombre) || 0) + valor);
    }
    const datos = Array.from(grupos.entries()).slice(0, 20);
    if (!datos.length) {
        contenedor.textContent = 'Completa los datos pendientes para visualizar el gráfico.';
    } else {
        const grafico = echarts.init(contenedor, null, {renderer: 'canvas'});
        grafico.setOption({
            aria: {enabled: true},
            tooltip: {trigger: 'axis', renderMode: 'richText'},
            grid: {left: 55, right: 20, bottom: 100, top: 30, containLabel: true},
            xAxis: {type: 'category', data: datos.map(fila => fila[0]), axisLabel: {rotate: 35, width: 130, overflow: 'truncate'}},
            yAxis: {type: 'value'},
            series: [{name: medida < 0 ? 'Registros' : tabla.tHead.rows[0].cells[medida].textContent,
                type: 'bar', data: datos.map(fila => fila[1]), itemStyle: {color: '#2463eb'}}]
        });
        new ResizeObserver(() => grafico.resize()).observe(contenedor);
    }
}
