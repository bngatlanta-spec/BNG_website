/* ==========================================================
   Bikes & Barrels - menu.js  (menu page standalone)
   ========================================================== */

const API_BASE = 'http://localhost:8080';
const TAX_RATE = 0.07; // Cobb County, GA

/* ---- Loader ---- */
(function () {
  function hideLoader() {
    const el = document.getElementById('loader');
    if (el) el.classList.add('hidden');
  }
  window.addEventListener('load', () => setTimeout(hideLoader, 900));
  setTimeout(hideLoader, 3000);
})();

/* ---- Sticky navbar ---- */
const navbar = document.getElementById('navbar');
if (navbar) {
  window.addEventListener('scroll', () => {
    navbar.classList.toggle('scrolled', window.scrollY > 60);
  }, { passive: true });
}

/* ---- Mobile menu ---- */
const hamburger = document.getElementById('hamburger');
const navLinksEl = document.getElementById('navLinks');
if (hamburger && navLinksEl) {
  hamburger.addEventListener('click', () => {
    hamburger.classList.toggle('open');
    navLinksEl.classList.toggle('open');
  });
  navLinksEl.querySelectorAll('.nav-link').forEach(a => {
    a.addEventListener('click', () => {
      hamburger.classList.remove('open');
      navLinksEl.classList.remove('open');
    });
  });
}

/* ============================================================
   Menu Data — loaded from API
   ============================================================ */
let menuBySlug = {};   // { slug: [items] }
let allItemsById = {}; // { cloverItemId: item }
let categories = [];   // [{ id, name, slug }]

function slugify(name) {
  return name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
}

/* ============================================================
   Cart State
   ============================================================ */
const cart = {}; // { cloverItemId: { name, img, priceNum, qty } }

function cartCount() {
  return Object.values(cart).reduce((s, i) => s + i.qty, 0);
}

function cartSubtotal() {
  return Object.values(cart).reduce((s, i) => s + i.priceNum * i.qty, 0);
}

/* ---- Badge ---- */
function refreshBadge() {
  const badge = document.getElementById('cartBadge');
  if (!badge) return;
  const count = cartCount();
  badge.hidden = count === 0;
  badge.textContent = count;
  if (count > 0) {
    badge.classList.add('bump');
    setTimeout(() => badge.classList.remove('bump'), 300);
  }
}

/* ---- Cart drawer ---- */
function refreshCartDrawer() {
  const emptyEl    = document.getElementById('cartEmpty');
  const listEl     = document.getElementById('cartItemsList');
  const footerEl   = document.getElementById('cartFooter');
  const subtotalEl = document.getElementById('cartSubtotal');
  const taxEl      = document.getElementById('cartTax');
  const totalEl    = document.getElementById('cartTotal');
  if (!listEl) return;

  const entries = Object.entries(cart).filter(([, v]) => v.qty > 0);
  const isEmpty = entries.length === 0;

  if (emptyEl)  emptyEl.hidden  = !isEmpty;
  if (footerEl) footerEl.hidden =  isEmpty;
  listEl.hidden = isEmpty;

  if (!isEmpty) {
    listEl.innerHTML = entries.map(([key, item]) => `
      <li class="cart-item" data-key="${key}">
        <div class="cart-item-img">
          ${item.img ? `<img src="${item.img}" alt="${item.name}" loading="lazy" />` : ''}
        </div>
        <div class="cart-item-body">
          <div class="cart-item-name">${item.name}</div>
          <div class="cart-item-price">$${(item.priceNum * item.qty).toFixed(2)}</div>
          <div class="cart-item-controls">
            <button class="qty-btn cart-dec" data-key="${key}" aria-label="Remove one">&#8722;</button>
            <span class="qty-val">${item.qty}</span>
            <button class="qty-btn cart-inc" data-key="${key}" aria-label="Add one">+</button>
          </div>
        </div>
      </li>
    `).join('');

    listEl.querySelectorAll('.cart-dec').forEach(btn =>
      btn.addEventListener('click', () => changeQty(btn.dataset.key, -1))
    );
    listEl.querySelectorAll('.cart-inc').forEach(btn =>
      btn.addEventListener('click', () => changeQty(btn.dataset.key, +1))
    );

    const subtotal = cartSubtotal();
    const tax      = subtotal * TAX_RATE;
    const total    = subtotal + tax;

    if (subtotalEl) subtotalEl.textContent = `$${subtotal.toFixed(2)}`;
    if (taxEl)      taxEl.textContent      = `$${tax.toFixed(2)}`;
    if (totalEl)    totalEl.textContent    = `$${total.toFixed(2)}`;
  }
}

/* ---- Quantity helpers ---- */
function changeQty(key, delta) {
  if (!cart[key]) return;
  cart[key].qty += delta;
  if (cart[key].qty <= 0) {
    delete cart[key];
    refreshCardCtrl(key, 0);
  } else {
    refreshCardCtrl(key, cart[key].qty);
  }
  refreshBadge();
  refreshCartDrawer();
}

function addToCart(id) {
  const item = allItemsById[id];
  if (!item) return;
  if (cart[id]) {
    cart[id].qty++;
  } else {
    cart[id] = { name: item.name, img: item.imageUrl || '', priceNum: item.price, qty: 1 };
  }
  refreshCardCtrl(id, cart[id].qty);
  refreshBadge();
  refreshCartDrawer();
  const cartBtn = document.getElementById('cartBtn');
  if (cartBtn) {
    cartBtn.classList.add('cart-btn-bump');
    setTimeout(() => cartBtn.classList.remove('cart-btn-bump'), 380);
  }
}

/* ---- Update the per-card control (Add button <-> qty widget) ---- */
function refreshCardCtrl(id, qty) {
  document.querySelectorAll(`.menu-card[data-key="${id}"] .menu-card-foot`).forEach(foot => {
    const addBtn  = foot.querySelector('.menu-card-add');
    const qtyCtrl = foot.querySelector('.card-qty-ctrl');

    if (qty > 0) {
      if (addBtn) {
        addBtn.replaceWith(buildQtyCtrl(id, qty));
      } else if (qtyCtrl) {
        qtyCtrl.querySelector('.card-qty-val').textContent = qty;
      }
    } else {
      if (qtyCtrl) {
        const btn = document.createElement('button');
        btn.className = 'menu-card-add';
        btn.textContent = '+ Add';
        btn.addEventListener('click', () => addToCart(id));
        qtyCtrl.replaceWith(btn);
      }
    }
  });
}

function buildQtyCtrl(id, qty) {
  const wrap = document.createElement('div');
  wrap.className = 'card-qty-ctrl';
  wrap.innerHTML = `
    <button class="card-qty-btn card-minus" data-key="${id}" aria-label="Remove one">&#8722;</button>
    <span class="card-qty-val">${qty}</span>
    <button class="card-qty-btn card-plus"  data-key="${id}" aria-label="Add one">+</button>
  `;
  wrap.querySelector('.card-minus').addEventListener('click', e => { e.stopPropagation(); changeQty(id, -1); });
  wrap.querySelector('.card-plus' ).addEventListener('click', e => { e.stopPropagation(); changeQty(id, +1); });
  return wrap;
}

/* ============================================================
   Menu Rendering
   ============================================================ */
const menuGrid = document.getElementById('menuGrid');

function renderMenu(slug) {
  if (!menuGrid) return;
  const items = menuBySlug[slug] || [];
  menuGrid.innerHTML = '';
  items.forEach((item, i) => {
    const id     = item.id;
    const inCart = cart[id];
    const card   = document.createElement('article');
    card.className = 'menu-card';
    card.dataset.key = id;
    card.style.animationDelay = `${i * 0.07}s`;

    const footCtrl = inCart
      ? `<div class="card-qty-ctrl">
           <button class="card-qty-btn card-minus" data-key="${id}" aria-label="Remove one">&#8722;</button>
           <span class="card-qty-val">${inCart.qty}</span>
           <button class="card-qty-btn card-plus"  data-key="${id}" aria-label="Add one">+</button>
         </div>`
      : `<button class="menu-card-add">+ Add</button>`;

    const imgHtml = item.imageUrl
      ? `<img src="${item.imageUrl}" alt="${item.name}" loading="lazy" />`
      : `<div class="menu-card-no-img"></div>`;

    card.innerHTML = `
      <div class="menu-card-img">
        ${imgHtml}
      </div>
      <div class="menu-card-body">
        <h3>${item.name}</h3>
        <p>${item.description || ''}</p>
        <div class="menu-card-foot">
          <div class="menu-card-price">$${item.price.toFixed(2)}</div>
          ${footCtrl}
        </div>
      </div>
    `;

    const addBtn   = card.querySelector('.menu-card-add');
    const minusBtn = card.querySelector('.card-minus');
    const plusBtn  = card.querySelector('.card-plus');
    if (addBtn)   addBtn.addEventListener('click', () => addToCart(id));
    if (minusBtn) minusBtn.addEventListener('click', e => { e.stopPropagation(); changeQty(id, -1); });
    if (plusBtn)  plusBtn.addEventListener('click',  e => { e.stopPropagation(); changeQty(id, +1); });

    menuGrid.appendChild(card);
  });
}

/* ---- Build tabs dynamically from category list ---- */
function buildTabs(cats) {
  const tabsEl = document.querySelector('.menu-tabs');
  if (!tabsEl) return;
  tabsEl.innerHTML = '';
  cats.forEach((cat, i) => {
    const btn = document.createElement('button');
    btn.className = 'tab' + (i === 0 ? ' active' : '');
    btn.dataset.cat = cat.slug;
    btn.textContent = cat.name;
    btn.addEventListener('click', () => {
      tabsEl.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      btn.classList.add('active');
      renderMenu(cat.slug);
    });
    tabsEl.appendChild(btn);
  });
}

/* ---- Show loading / error states ---- */
function showGridMessage(html) {
  if (menuGrid) menuGrid.innerHTML = `<p class="menu-loading">${html}</p>`;
}

/* ============================================================
   Bootstrap: fetch menu from API
   ============================================================ */
async function loadMenu() {
  showGridMessage('Loading menu&hellip;');
  try {
    const res = await fetch(`${API_BASE}/api/menu`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    const data = json.data;

    categories = data.categories || [];

    categories.forEach(cat => {
      menuBySlug[cat.slug] = cat.items || [];
      (cat.items || []).forEach(item => {
        allItemsById[item.id] = item;
      });
    });

    buildTabs(categories);

    if (categories.length > 0) {
      renderMenu(categories[0].slug);
    } else {
      showGridMessage('No menu items available right now.');
    }
  } catch (err) {
    console.error('Failed to load menu:', err);
    showGridMessage('Unable to load menu. Please refresh or call us at <a href="tel:+16782935779">(678) 293-5779</a>.');
  }
}

loadMenu();

/* ============================================================
   Cart Drawer Open / Close
   ============================================================ */
function openCart() {
  document.getElementById('cartDrawer').classList.add('open');
  document.getElementById('cartOverlay').classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeCart() {
  document.getElementById('cartDrawer').classList.remove('open');
  document.getElementById('cartOverlay').classList.remove('open');
  document.body.style.overflow = '';
}

document.getElementById('cartBtn').addEventListener('click', openCart);
document.getElementById('cartDrawerClose').addEventListener('click', closeCart);
document.getElementById('cartOverlay').addEventListener('click', closeCart);

/* ---- Clear all ---- */
document.getElementById('cartClear').addEventListener('click', () => {
  const keys = Object.keys(cart);
  keys.forEach(k => { delete cart[k]; refreshCardCtrl(k, 0); });
  refreshBadge();
  refreshCartDrawer();
});

/* ============================================================
   Clover.js — online payment
   ============================================================ */
let cloverInstance = null;

async function initCloverJs() {
  if (cloverInstance) return; // already initialized
  try {
    const res  = await fetch(`${API_BASE}/api/config/clover-key`);
    const json = await res.json();
    const { pakmsKey, sdkUrl } = json.data;

    // Dynamically load Clover.js SDK
    await new Promise((resolve, reject) => {
      const s = document.createElement('script');
      s.src = sdkUrl;
      s.onload = resolve;
      s.onerror = reject;
      document.head.appendChild(s);
    });

    cloverInstance = new window.Clover(pakmsKey);
    const elements = cloverInstance.elements();

    const inputStyles = {
      input: {
        color: '#f5f0e8',
        fontFamily: "'Poppins', sans-serif",
        fontSize: '14px',
        '::placeholder': { color: 'rgba(245,240,232,0.35)' }
      }
    };

    elements.create('CARD_NUMBER',      inputStyles).mount('#card-number');
    elements.create('CARD_DATE',        inputStyles).mount('#card-date');
    elements.create('CARD_CVV',         inputStyles).mount('#card-cvv');
    elements.create('CARD_POSTAL_CODE', inputStyles).mount('#card-postal-code');

    // Show card validation errors inline
    ['CARD_NUMBER','CARD_DATE','CARD_CVV','CARD_POSTAL_CODE'].forEach(type => {
      const el = elements.create(type, inputStyles);
      el.addEventListener('change', e => {
        const errEl = document.getElementById('card-errors');
        const err   = e[type]?.error?.message;
        if (errEl) errEl.textContent = err || '';
      });
    });

  } catch (err) {
    console.error('Failed to init Clover.js:', err);
  }
}

// Toggle card fields when payment type changes
document.addEventListener('change', e => {
  if (e.target?.name === 'paymentType') {
    const section = document.getElementById('cardFieldsSection');
    if (!section) return;
    if (e.target.value === 'PREPAID') {
      section.hidden = false;
      initCloverJs();
    } else {
      section.hidden = true;
    }
  }
});

/* ---- Checkout modal ---- */
function openCheckout() {
  closeCart();

  // Populate order summary
  const entries = Object.entries(cart).filter(([, v]) => v.qty > 0);
  const listEl  = document.getElementById('checkoutItemsList');
  if (listEl) {
    listEl.innerHTML = entries.map(([, item]) => `
      <li class="co-item">
        <span class="co-item-name">${item.name}${item.qty > 1 ? ` <em>x${item.qty}</em>` : ''}</span>
        <span class="co-item-price">$${(item.priceNum * item.qty).toFixed(2)}</span>
      </li>
    `).join('');
  }

  const subtotal = cartSubtotal();
  const tax      = subtotal * TAX_RATE;
  const total    = subtotal + tax;
  const s = id => document.getElementById(id);
  if (s('coSubtotal')) s('coSubtotal').textContent = `$${subtotal.toFixed(2)}`;
  if (s('coTax'))      s('coTax').textContent      = `$${tax.toFixed(2)}`;
  if (s('coTotal'))    s('coTotal').textContent     = `$${total.toFixed(2)}`;

  // Clear previous errors and reset submit button
  const errEl = document.getElementById('checkoutError');
  if (errEl) { errEl.hidden = true; errEl.textContent = ''; }
  const submitBtn = document.getElementById('checkoutSubmit');
  if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = 'Place Order'; }

  const modal = document.getElementById('checkoutModal');
  if (modal) { modal.hidden = false; document.body.style.overflow = 'hidden'; }
}

function closeCheckout() {
  const modal = document.getElementById('checkoutModal');
  if (modal) { modal.hidden = true; document.body.style.overflow = ''; }
}

document.getElementById('cartCheckout').addEventListener('click', openCheckout);
document.getElementById('checkoutClose').addEventListener('click', closeCheckout);

/* ---- Form submission ---- */
document.getElementById('checkoutForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const name  = document.getElementById('custName').value.trim();
  const phone = document.getElementById('custPhone').value.trim();
  const email = document.getElementById('custEmail').value.trim();
  const notes = document.getElementById('custNotes').value.trim();
  const orderType   = document.querySelector('input[name="orderType"]:checked')?.value   || 'TAKEOUT';
  const paymentType = document.querySelector('input[name="paymentType"]:checked')?.value || 'PAY_AT_RESTAURANT';

  const errEl = document.getElementById('checkoutError');

  if (!name) {
    errEl.textContent = 'Please enter your name.'; errEl.hidden = false; return;
  }
  if (!phone) {
    errEl.textContent = 'Please enter your phone number.'; errEl.hidden = false; return;
  }

  const items = Object.entries(cart)
    .filter(([, v]) => v.qty > 0)
    .map(([id, v]) => ({
      itemId:   id,
      name:     v.name,
      price:    Math.round(v.priceNum * 100), // cents
      quantity: v.qty
    }));

  const submitBtn = document.getElementById('checkoutSubmit');
  submitBtn.disabled = true;

  // Tokenize card if paying online
  let cardToken = null;
  if (paymentType === 'PREPAID') {
    if (!cloverInstance) {
      errEl.textContent = 'Card payment is not ready yet. Please try again.';
      errEl.hidden = false;
      submitBtn.disabled = false;
      return;
    }
    submitBtn.textContent = 'Securing card…';
    try {
      const tokenResult = await cloverInstance.createToken();
      if (tokenResult.errors) {
        const msg = Object.values(tokenResult.errors)[0] || 'Invalid card details';
        errEl.textContent = msg; errEl.hidden = false;
        submitBtn.disabled = false; submitBtn.textContent = 'Place Order';
        return;
      }
      cardToken = tokenResult.token;
    } catch (err) {
      errEl.textContent = 'Could not secure card. Please check your details.';
      errEl.hidden = false;
      submitBtn.disabled = false; submitBtn.textContent = 'Place Order';
      return;
    }
  }

  submitBtn.textContent = 'Placing order…';

  try {
    const res = await fetch(`${API_BASE}/api/orders`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerName: name, customerPhone: phone, customerEmail: email,
                             orderType, paymentType, notes, items, cardToken })
    });
    const json = await res.json();
    if (!res.ok || !json.success) throw new Error(json.message || `HTTP ${res.status}`);

    // Success — clear cart and show success modal
    closeCheckout();
    const keys = Object.keys(cart);
    keys.forEach(k => { delete cart[k]; refreshCardCtrl(k, 0); });
    refreshBadge();
    refreshCartDrawer();
    document.getElementById('checkoutForm').reset();
    const successModal = document.getElementById('orderSuccessModal');
    if (successModal) { successModal.hidden = false; document.body.style.overflow = 'hidden'; }

  } catch (err) {
    errEl.textContent = `Order failed: ${err.message}. Please try again or call us.`;
    errEl.hidden = false;
    submitBtn.disabled = false;
    submitBtn.textContent = 'Place Order';
  }
});

document.getElementById('orderSuccessClose').addEventListener('click', () => {
  const modal = document.getElementById('orderSuccessModal');
  if (modal) { modal.hidden = true; document.body.style.overflow = ''; }
});

/* ---- Reveal on scroll ---- */
const revObs = new IntersectionObserver(entries => {
  entries.forEach(e => {
    if (e.isIntersecting) { e.target.classList.add('visible'); revObs.unobserve(e.target); }
  });
}, { threshold: 0.12, rootMargin: '0px 0px -60px 0px' });
document.querySelectorAll('.reveal').forEach(el => revObs.observe(el));
