/* ==========================================================
   Bikes & Barrels - Static Menu (no backend required)
   Reads menu-response.json — no cart, no Clover, no API
   ========================================================== */

const ORDER_URL  = 'https://food.orders.co/bikesbarrels-biryaningrill/menu';

async function loadStaticMenu() {
  const tabsEl = document.getElementById('menuTabs');
  const gridEl = document.getElementById('menuGrid');
  if (!tabsEl || !gridEl) return;

  gridEl.innerHTML = '<p class="menu-loading">Loading menu&hellip;</p>';

  let categories = [];
  try {
    const res = await fetch('menu-response.json');
    if (!res.ok) throw new Error('fetch');
    const json = await res.json();
    categories = (json.data && json.data.categories) ? json.data.categories : [];
  } catch (_) {
    gridEl.innerHTML = `<p class="menu-loading">Unable to load menu right now. <a href="tel:+16782935779">Call&nbsp;us</a> or <a href="${ORDER_URL}" target="_blank" rel="noopener">order&nbsp;online</a>.</p>`;
    return;
  }

  if (categories.length === 0) {
    gridEl.innerHTML = `<p class="menu-loading">Menu coming soon. <a href="tel:+16782935779">Call us</a>.</p>`;
    return;
  }

  /* ---- Build tabs ---- */
  tabsEl.innerHTML = '';
  categories.forEach((cat, i) => {
    const btn = document.createElement('button');
    btn.className = 'tab' + (i === 0 ? ' active' : '');
    btn.dataset.cat = cat.slug;
    btn.textContent = cat.name.trim();
    btn.addEventListener('click', () => {
      tabsEl.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      btn.classList.add('active');
      renderCategory(cat, gridEl);
      /* smooth scroll to grid on mobile */
      if (window.innerWidth <= 768) {
        document.getElementById('menu').scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
    tabsEl.appendChild(btn);
  });

  /* ---- Render first category ---- */
  renderCategory(categories[0], gridEl);
}

function renderCategory(cat, gridEl) {
  gridEl.innerHTML = '';
  const items = cat.items || [];

  if (items.length === 0) {
    gridEl.innerHTML = '<p class="menu-loading">No items in this category.</p>';
    return;
  }

  items.forEach((item, i) => {
    const card = document.createElement('article');
    card.className = 'menu-card';
    card.style.animationDelay = `${i * 0.05}s`;

    const imgHtml = item.imageUrl
      ? `<img src="${item.imageUrl}" alt="${item.name.trim()}" loading="lazy" />`
      : `<div class="menu-card-no-img"></div>`;

    card.innerHTML = `
      <div class="menu-card-img">${imgHtml}</div>
      <div class="menu-card-body">
        <h3>${item.name.trim()}</h3>
        ${item.description ? `<p>${item.description}</p>` : ''}
        <div class="menu-card-foot">
          <div class="menu-card-price">$${item.price.toFixed(2)}</div>
          <a href="${ORDER_URL}" target="_blank" rel="noopener" class="menu-card-add">Order</a>
        </div>
      </div>
    `;

    gridEl.appendChild(card);
  });
}

loadStaticMenu();
