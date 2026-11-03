(() => {
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const fmt = s => String(Math.floor(s / 60)).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');

  /* ---------- Menú mobile ---------- */
  const menuToggle = document.querySelector('.cp-menu-toggle');
  const mainNav = document.getElementById('cp-main-nav');
  if (menuToggle && mainNav) {
    menuToggle.addEventListener('click', () => {
      const isOpen = mainNav.classList.toggle('is-open');
      menuToggle.setAttribute('aria-expanded', isOpen);
      menuToggle.textContent = isOpen ? '✕' : '☰';
      menuToggle.setAttribute('aria-label', isOpen ? 'Cerrar menú' : 'Abrir menú');
    });
    // Cerrar al hacer clic en un enlace
    mainNav.querySelectorAll('a').forEach(a => {
      a.addEventListener('click', () => {
        mainNav.classList.remove('is-open');
        menuToggle.setAttribute('aria-expanded', 'false');
        menuToggle.textContent = '☰';
      });
    });
  }

  /* ---------- Filtros de pedidos ---------- */
  const filterBtns = $$('.filter-btn');
  if (filterBtns.length) {
    const orders = $$('.order[data-estado]');
    filterBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        filterBtns.forEach(b => b.classList.remove('is-active'));
        btn.classList.add('is-active');
        const filter = btn.dataset.filter;
        orders.forEach(order => {
          if (filter === 'todos' || order.dataset.estado === filter) {
            order.style.display = '';
            order.style.animation = 'fadeInUp .3s ease-out both';
          } else {
            order.style.display = 'none';
          }
        });
      });
    });
  }

  /* ---------- Temporizador: el servidor manda la hora de vencimiento ---------- */
  const timers = $$('[data-timer]');
  if (timers.length) {
    const skew = (Number(document.body.dataset.ahora) || Date.now()) - Date.now(); // corrige reloj del dispositivo
    const live = document.getElementById('cp-live');
    const tick = () => {
      const now = Date.now() + skew;
      timers.forEach(t => {
        const total = Number(t.dataset.total) || 900;
        const left = Math.max(0, Math.ceil((Number(t.dataset.expira) - now) / 1000));
        const pct = Math.min(1, left / total);
        $$('[data-time]', t).forEach(e => (e.textContent = fmt(left)));
        $$('[data-ring]', t).forEach(c => (c.style.strokeDashoffset = 1 - pct));
        $$('[data-bar]', t).forEach(b => (b.style.transform = `scaleX(${pct})`));
        t.classList.toggle('is-warn', left <= 300 && left > 60);
        t.classList.toggle('is-crit', left <= 60 && left > 0);
        t.classList.toggle('is-expired', left === 0);
        if (t.hasAttribute('data-announce') && live) {
          if (left === 300) live.textContent = 'Quedan 5 minutos para completar tu pago.';
          if (left === 60) live.textContent = 'Queda 1 minuto para que venza tu reserva.';
        }
        if (left === 0 && !t.dataset.done) {
          t.dataset.done = '1';
          if (live && t.hasAttribute('data-announce')) live.textContent = 'Tu reserva venció.';
          document.dispatchEvent(new CustomEvent('cp:expired', { detail: t }));
        }
      });
    };
    tick();
    setInterval(tick, 1000);
    document.addEventListener('visibilitychange', tick);
  }

  /* ---------- Copiar y mostrar/ocultar datos ---------- */
  document.addEventListener('click', e => {
    const c = e.target.closest('[data-copy]');
    if (c) {
      const txt = c.dataset.copy, old = c.textContent;
      (navigator.clipboard ? navigator.clipboard.writeText(txt) : Promise.reject()).catch(() => {}).finally(() => {
        c.textContent = 'Copiado'; setTimeout(() => (c.textContent = old), 1600);
      });
    }
    const r = e.target.closest('[data-reveal]');
    if (r) {
      const box = r.closest('.cred'), hidden = box.classList.toggle('is-shown');
      $$('.mask', box).forEach(m => (m.style.filter = hidden ? 'none' : ''));
      r.textContent = hidden ? 'Ocultar datos' : 'Mostrar datos';
    }
  });

  /* ---------- Formulario de comprobante ---------- */
  const form = document.getElementById('form-comprobante');
  if (!form) return;
  const file = form.elements.comprobante, drop = $$('.drop', form)[0], prev = $$('.preview', form)[0];
  const submit = form.querySelector('[type=submit]');
  const rules = {
    titular: v => v.trim().length >= 3 || 'Escribe el nombre que aparece en tu Yape o Plin.',
    operacion: v => /^\d{6,12}$/.test(v) || 'Ingresa el número de operación: solo dígitos, entre 6 y 12.',
    comprobante: () => {
      const f = file.files[0];
      if (!f) return 'Adjunta la captura de tu pago.';
      if (!/^image\/|^application\/pdf$/.test(f.type)) return 'Usa una imagen (JPG o PNG) o un PDF.';
      if (f.size > 5 * 1024 * 1024) return 'El archivo pesa más de 5 MB. Sube una captura más liviana.';
      return true;
    }
  };
  const show = (name, force) => {
    const el = form.elements[name], res = rules[name](el.value), err = document.getElementById('err-' + name);
    const target = name === 'comprobante' ? drop : el;
    if (force || el.dataset.touched) {
      target.setAttribute('aria-invalid', res !== true);
      err.textContent = res === true ? '' : res;
    }
    return res === true;
  };
  const validate = force => Object.keys(rules).map(n => show(n, force)).every(Boolean);
  // El botón solo se bloquea al vencer; si faltan datos, se explican los errores al enviar.
  const refresh = () => { validate(false); submit.disabled = document.body.classList.contains('is-expired'); };

  ['titular', 'operacion'].forEach(n => {
    const el = form.elements[n];
    el.addEventListener('blur', () => { el.dataset.touched = '1'; show(n); });
    el.addEventListener('input', () => {
      if (n === 'operacion') el.value = el.value.replace(/\D/g, '').slice(0, 12);
      show(n); refresh();
    });
  });

  const setPreview = () => {
    const f = file.files[0];
    prev.classList.toggle('is-on', !!f);
    if (f) {
      const img = prev.querySelector('img');
      img.hidden = !f.type.startsWith('image/');
      if (!img.hidden) img.src = URL.createObjectURL(f);
      prev.querySelector('.name').textContent = f.name + ' · ' + (f.size / 1024 / 1024).toFixed(1) + ' MB';
    }
    drop.dataset.touched = '1'; file.dataset.touched = '1'; show('comprobante'); refresh();
  };
  file.addEventListener('change', setPreview);
  ['dragenter', 'dragover'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.add('is-over'); }));
  ['dragleave', 'drop'].forEach(ev => drop.addEventListener(ev, e => { e.preventDefault(); drop.classList.remove('is-over'); }));
  drop.addEventListener('drop', e => { if (e.dataTransfer.files.length) { file.files = e.dataTransfer.files; setPreview(); } });
  prev.querySelector('[data-remove]').addEventListener('click', () => { file.value = ''; setPreview(); file.focus(); });

  form.addEventListener('submit', e => {
    if (!validate(true)) {
      e.preventDefault();
      const bad = form.querySelector('[aria-invalid=true]');
      if (bad) (bad === drop ? file : bad).focus();
      return;
    }
    submit.textContent = 'Enviando comprobante…';
    setTimeout(() => (submit.disabled = true));
  });

  document.addEventListener('cp:expired', () => {
    document.body.classList.add('is-expired');
    form.querySelector('fieldset').disabled = true;
    submit.disabled = true;
    document.getElementById('expired-note')?.focus();
  });
  refresh();
})();
