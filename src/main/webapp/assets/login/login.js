'use strict';
const password = document.getElementById('password');
const toggle = document.getElementById('toggle-password');
if (password && toggle) {
  toggle.hidden = false;
  toggle.addEventListener('click', () => {
    const visible = password.type === 'password';
    password.type = visible ? 'text' : 'password';
    toggle.setAttribute('aria-pressed', String(visible));
    toggle.setAttribute('aria-label', visible ? 'Ocultar contraseña' : 'Mostrar contraseña');
  });
}

// La ventana se abre por una acción del usuario; el navegador permite cerrarla.
const formAcceso = document.getElementById('form-acceso');
const abrirPanel = document.getElementById('abrir-panel');
const abrirAcceso = document.getElementById('abrir-acceso');
const estadoVentana = document.getElementById('estado-ventana');
if (formAcceso && abrirPanel && window.opener && window.name === 'novatech-acceso') {
  formAcceso.hidden = false;
  abrirPanel.hidden = true;
}
if (abrirAcceso) {
  abrirAcceso.addEventListener('click', () => {
    const acceso = window.open(abrirAcceso.dataset.url, 'novatech-acceso', 'popup,width=1100,height=800,resizable=yes,scrollbars=yes');
    if (!acceso) {
      estadoVentana.textContent = 'Permite las ventanas emergentes de NovaTech y vuelve a pulsar el botón.';
      return;
    }
    acceso.focus();
    estadoVentana.textContent = 'Continúa en la ventana de acceso.';
  });
}
