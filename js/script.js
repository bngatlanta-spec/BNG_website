/* ==========================================================
   Bikes & Barrels - Biryani N Grill
   ========================================================== */

/* ---------- Today's Specials (Google Sheets CSV) ----------
   SETUP:
   1. Create a Google Sheet with 3 columns in row 1:
      Date | Category | Item
   2. Fill rows like:
      2026-07-30 | Biryani        | Chicken Dum Biryani
      2026-07-30 | Chicken Starter| Chicken 65
      2026-07-30 | Veg Starter    | Paneer Tikka Kebab
      2026-07-30 | Curry          | Butter Chicken
      2026-07-30 | Dessert        | Rose Panna Cotta
   3. File > Share > Publish to web > Comma-separated values (.csv) > Publish
   4. Copy the generated URL and paste it into SPECIALS_SHEET_URL below.
   Date can be YYYY-MM-DD, M/D/YYYY, or D/M/YYYY - all supported.
   ============================================================ */
const SPECIALS_SHEET_URL = 'https://script.google.com/macros/s/AKfycby0SuBSdRwbCsqC-DrsGHtaz0FEL-U31KnpwwV4paF8gvY6udX-SrgYSvuao8CzlhGMUg/exec';

function parseCSV(text) {
  const lines = text.replace(/\r/g, '').trim().split('\n');
  if (lines.length < 2) return [];
  const parseLine = (line) => {
    const cells = []; let cur = ''; let inQ = false;
    for (let i = 0; i < line.length; i++) {
      const c = line[i];
      if (c === '"') { inQ = !inQ; continue; }
      if (c === ',' && !inQ) { cells.push(cur.trim()); cur = ''; continue; }
      cur += c;
    }
    cells.push(cur.trim());
    return cells;
  };
  const headers = parseLine(lines[0]).map(h => h.toLowerCase());
  return lines.slice(1).map(line => {
    const cells = parseLine(line);
    const obj = {};
    headers.forEach((h, i) => obj[h] = cells[i] || '');
    return obj;
  });
}

function parseSheetDate(s) {
  if (!s) return null;
  s = s.trim();
  // ISO YYYY-MM-DD
  let m = s.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/);
  if (m) return new Date(+m[1], +m[2] - 1, +m[3]);
  // M/D/YYYY or D/M/YYYY - assume M/D/YYYY (US)
  m = s.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})/);
  if (m) return new Date(+m[3], +m[1] - 1, +m[2]);
  // D-M-YYYY
  m = s.match(/^(\d{1,2})-(\d{1,2})-(\d{4})/);
  if (m) return new Date(+m[3], +m[2] - 1, +m[1]);
  const d = new Date(s);
  return isNaN(d.getTime()) ? null : d;
}

function isoDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function friendlyDate(d) {
  return d.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });
}

async function loadTodaysSpecials() {
  const today = new Date();
  let specials = [];
  let isDemo = false;

  if (!SPECIALS_SHEET_URL || SPECIALS_SHEET_URL.includes('PASTE_YOUR')) {
    // Placeholder preview when sheet URL isn't configured yet
    specials = [
      { category: 'Biryani',         item: 'Chicken Dum Biryani' },
      { category: 'Chicken Starter', item: 'Chicken 65' },
      { category: 'Veg Starter',     item: 'Paneer Tikka Kebab' },
      { category: 'Curry',           item: 'Butter Chicken Masala' },
      { category: 'Dessert',         item: 'Rose Panna Cotta' }
    ];
    isDemo = true;
  } else {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);
      const res = await fetch(SPECIALS_SHEET_URL, { cache: 'no-store', signal: controller.signal });
      clearTimeout(timeoutId);
      if (!res.ok) throw new Error('fetch');
      const text = await res.text();
      let rows;
      // Support JSON (Apps Script) or CSV
      try {
        const json = JSON.parse(text);
        const arr = json.items || json.data || (Array.isArray(json) ? json : []);
        rows = arr.map(i => ({
          date: i.date || i.Date,
          category: i.category || i.Category,
          item: i.item || i.Item
        }));
      } catch {
        rows = parseCSV(text);
      }
      const todayIso = isoDate(today);
      specials = rows
        .filter(r => r.date && r.item)
        .filter(r => {
          const d = parseSheetDate(r.date);
          return d && isoDate(d) === todayIso;
        })
        .map(r => ({ category: r.category || 'Special', item: r.item }));
    } catch (e) {
      specials = [];
    }
  }

  // No specials for today = no notification appears at all
  if (specials.length === 0) return;

  renderNotification(specials, isDemo, today);
}

function renderNotification(items, isDemo, today) {
  const bell = document.getElementById('notifBell');
  const card = document.getElementById('notifCard');
  const list = document.getElementById('notifList');
  const badge = document.getElementById('notifBellBadge');
  const dateEl = document.getElementById('notifDate');
  const preview = document.getElementById('notifPreviewBadge');
  if (!bell || !card || !list) return;

  list.innerHTML = items.map((s, i) => `
    <li class="notif-item" style="animation-delay:${i * 0.08}s">
      <span class="notif-item-num">${String(i + 1).padStart(2, '0')}</span>
      <div class="notif-item-body">
        <span class="notif-item-cat">${s.category}</span>
        <span class="notif-item-name">${s.item}</span>
      </div>
    </li>
  `).join('');

  badge.textContent = items.length;
  dateEl.textContent = today.toLocaleDateString(undefined, {
    weekday: 'long', month: 'short', day: 'numeric'
  });
  preview.hidden = !isDemo;

  bell.hidden = false;
  requestAnimationFrame(() => bell.classList.add('notif-bell-in'));

  // Auto-open once per day per browser session
  const dismissedKey = 'bng_specials_dismissed_' + isoDate(today);
  const dismissed = sessionStorage.getItem(dismissedKey);
  if (!dismissed) {
    setTimeout(() => {
      openNotifCard();
      setTimeout(() => {
        if (card.classList.contains('open') && !card.dataset.userInteracted) {
          closeNotifCard();
        }
      }, 15000);
    }, 1600);
  }

  document.getElementById('notifClose').addEventListener('click', () => {
    closeNotifCard();
    sessionStorage.setItem(dismissedKey, '1');
  });
  bell.addEventListener('click', () => {
    if (card.classList.contains('open')) closeNotifCard();
    else openNotifCard();
  });
  card.addEventListener('mouseenter', () => { card.dataset.userInteracted = '1'; });
  card.addEventListener('touchstart', () => { card.dataset.userInteracted = '1'; }, { passive: true });
}

function openNotifCard() {
  const card = document.getElementById('notifCard');
  const bell = document.getElementById('notifBell');
  card.hidden = false;
  requestAnimationFrame(() => card.classList.add('open'));
  bell.classList.add('bell-active');
}
function closeNotifCard() {
  const card = document.getElementById('notifCard');
  const bell = document.getElementById('notifBell');
  card.classList.remove('open');
  bell.classList.remove('bell-active');
  setTimeout(() => { card.hidden = true; }, 400);
}

loadTodaysSpecials();



/* ---------- Loader ---------- */
function hideLoader() {
  const loader = document.getElementById('loader');
  if (loader) loader.classList.add('hidden');
}
window.addEventListener('load', () => setTimeout(hideLoader, 900));
setTimeout(hideLoader, 3000); // fallback: never block longer than 3s

/* ---------- Sticky nav ---------- */
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 60);
});

/* ---------- Mobile menu ---------- */
const hamburger = document.getElementById('hamburger');
const navLinks = document.getElementById('navLinks');
hamburger.addEventListener('click', () => {
  hamburger.classList.toggle('open');
  navLinks.classList.toggle('open');
});
document.querySelectorAll('.nav-link').forEach(a => {
  a.addEventListener('click', () => {
    hamburger.classList.remove('open');
    navLinks.classList.remove('open');
  });
});

/* ---------- Active nav on scroll ---------- */
const sections = document.querySelectorAll('section[id]');
const navLinksAll = document.querySelectorAll('.nav-link');
window.addEventListener('scroll', () => {
  const scrollY = window.scrollY + 120;
  sections.forEach(sec => {
    if (scrollY >= sec.offsetTop && scrollY < sec.offsetTop + sec.offsetHeight) {
      navLinksAll.forEach(l => l.classList.remove('active'));
      const active = document.querySelector(`.nav-link[href="#${sec.id}"]`);
      if (active) active.classList.add('active');
    }
  });
});

/* ---------- Hero slideshow ---------- */
const slides = document.querySelectorAll('.hero-slide');
let currentSlide = 0;
setInterval(() => {
  slides[currentSlide].classList.remove('active');
  currentSlide = (currentSlide + 1) % slides.length;
  slides[currentSlide].classList.add('active');
}, 5000);

/* ---------- Reveal on scroll ---------- */
const observer = new IntersectionObserver((entries) => {
  entries.forEach(e => {
    if (e.isIntersecting) {
      e.target.classList.add('visible');
      observer.unobserve(e.target);
    }
  });
}, { threshold: 0.12, rootMargin: '0px 0px -60px 0px' });
document.querySelectorAll('.reveal').forEach(el => observer.observe(el));

/* ---------- Menu data ---------- */
const menuData = {
  popular: [
    { name:'Chicken Tikka Masala', desc:'Tandoor-roasted chicken in a silky tomato-butter gravy, kissed with fenugreek.', price:'$16', tag:'POPULAR', img:'images/chicken-tikka-masala.jpg' },
    { name:'Chicken Dum Biryani', desc:'Long-grain basmati layered with marinated chicken, slow-cooked on dum for aromatic perfection.', price:'$17', tag:'SIGNATURE', img:'images/chicken-dum-biryani-pot.webp' },
    { name:'Paneer Biryani', desc:'Cottage cheese cubes layered with saffron rice, cashews and warming whole spices.', price:'$15', tag:'VEG', img:'images/paneer-biryani.png' },
    { name:'Gongura Lamb Curry', desc:'Tender lamb slow-simmered with tangy Andhra sorrel leaves and green chilies.', price:'$22', tag:'BOLD', img:'images/gongura-lamb-curry.jpg' },
    { name:'Bullet Naan Chilli Garlic', desc:'Fluffy naan blistered in the tandoor with fiery green chili and roasted garlic.', price:'$6', tag:'FIERY', img:'images/bullet-naan-chilli-garlic.png' },
    { name:'B&B Clay Pot Biryani', desc:'Our signature clay pot biryani &mdash; branded with the B&amp;B seal.', price:'$20', tag:'CHEF', img:'images/clay-pot-signature-biryani.webp' },
    { name:'Butter Chicken Masala', desc:'Tender chicken in a creamy tomato-cashew makhani gravy.', price:'$17', tag:'POPULAR', img:'images/butter-chicken-masala.jpg' },
    { name:'Apollo Fish', desc:'Crispy fried fish tossed in a South Indian tangy chili glaze &mdash; coastal favorite.', price:'$16', tag:'COASTAL', img:'images/apollo-fish.jpg' },
    { name:'Tandoori Half Chicken', desc:'Half chicken marinated in yogurt and spices, chargrilled in the tandoor.', price:'$18', tag:'SMOKY', img:'images/tandoori-half-chicken.webp' },
    { name:'Lamb Chops', desc:'Slow-braised lamb chops in a rich onion-tomato masala with garam masala warmth.', price:'$24', tag:'PREMIUM', img:'images/lamb-chops.jpg' }
  ],
  biryani: [
    { name:'Chicken Dum Biryani', desc:'Long-grain basmati layered with marinated chicken, slow-cooked on dum.', price:'$17', tag:'SIGNATURE', img:'images/chicken-dum-biryani-pot.webp' },
    { name:'Goat Dum Biryani', desc:'Tender goat meat layered with saffron rice, slow-cooked to perfection.', price:'$22', tag:'CLASSIC', img:'images/goat-dum-biryani.jpg' },
    { name:'Paneer Biryani', desc:'Cottage cheese with saffron rice, cashews and whole spices.', price:'$15', tag:'VEG', img:'images/paneer-biryani.png' },
    { name:'Egg Biryani', desc:'Hyderabadi-style egg biryani with soft boiled eggs and fragrant rice.', price:'$14', tag:'HOMESTYLE', img:'images/egg-biryani.jpg' },
    { name:'B&B Clay Pot Biryani', desc:'Our signature clay pot biryani &mdash; sealed and slow-cooked with house-ground spices.', price:'$20', tag:'HOUSE SPECIAL', img:'images/clay-pot-signature-biryani.webp' },
    { name:'MLA Potlam Bucket Biryani', desc:'Slow-cooked bucket biryani wrapped and steamed &mdash; feeds 2-3 people.', price:'$32', tag:'FAMILY', img:'images/mla-potlam-bucket-biryani.png' },
    { name:'Mutton Mandi', desc:'Slow-cooked mutton served over aromatic rice with pickled onions.', price:'$24', tag:'ARABIAN', img:'images/mutton-mandi.webp' },
    { name:'Family Mandi Feast', desc:'A grand mandi platter &mdash; perfect for a family gathering or celebration.', price:'$45', tag:'FEAST', img:'images/family-mandi-feast.webp' },
    { name:'Raju Gari Kodi Pulav', desc:'Country chicken cooked with rice in an aromatic village-style pulav.', price:'$18', tag:'RUSTIC', img:'images/raju-gari-kodi-pulav.webp' }
  ],
  curry: [
    { name:'Chicken Tikka Masala', desc:'Tandoor-roasted chicken in silky tomato-butter gravy.', price:'$16', tag:'POPULAR', img:'images/chicken-tikka-masala.jpg' },
    { name:'Butter Chicken Masala', desc:'Tender chicken in a creamy tomato-cashew makhani gravy.', price:'$17', tag:'CREAMY', img:'images/butter-chicken-masala.jpg' },
    { name:'Butter Paneer Masala', desc:'Cottage cheese cubes in a rich tomato-cashew butter gravy.', price:'$15', tag:'CLASSIC', img:'images/butter-paneer.webp' },
    { name:'Gongura Lamb Curry', desc:'Andhra-style lamb curry with tangy sorrel leaves.', price:'$22', tag:'BOLD', img:'images/gongura-lamb-curry.jpg' },
    { name:'Nalli Gosht', desc:'Slow-cooked lamb shank in an aromatic pulav &mdash; a Hyderabadi classic.', price:'$26', tag:'PREMIUM', img:'images/nalli-gosht.webp' },
    { name:'Chilly Paneer', desc:'Indo-Chinese cottage cheese in a spicy garlic soy glaze.', price:'$14', tag:'VEG', img:'images/chilly-paneer.jpg' },
    { name:'Ghee Roast Chicken', desc:'Chicken dry-tossed with ghee, curry leaves and roasted spices.', price:'$17', tag:'GHEE ROAST', img:'images/ghee-roast-chicken.webp' },
    { name:'Ghee Roast Mutton Fry', desc:'Tender mutton pieces dry-fried in ghee with black pepper and curry leaves.', price:'$21', tag:'HOUSE FAV', img:'images/ghee-roast-mutton-fry.jpg' }
  ],
  starters: [
    { name:'Chicken 65', desc:'Boneless chicken tossed in a fiery Andhra red spice glaze.', price:'$12', tag:'CROWD FAV', img:'images/chicken-65-plate.jpg' },
    { name:'Chicken Lollipop', desc:'Crispy fried chicken lollipops in a fiery Indo-Chinese glaze.', price:'$12', tag:'CROWD FAV', img:'images/chicken-lollipop.jpg' },
    { name:'Chicken Tikka Kebab', desc:'Marinated chicken chunks charred to perfection in the tandoor.', price:'$14', tag:'SMOKY', img:'images/chicken-tikka-kebab-official.jpg' },
    { name:'Chicken Sheekh Kebab', desc:'Ground chicken skewers spiced with garam masala, grilled in the tandoor.', price:'$14', tag:'GRILLED', img:'images/chicken-sheekh-kebab.jpg' },
    { name:'Paneer Tikka Kebab', desc:'Paneer cubes and bell peppers marinated in yogurt spices, tandoor-grilled.', price:'$13', tag:'VEG', img:'images/paneer-tikka-kebab.jpg' },
    { name:'Punjabi Samosa', desc:'Crispy triangular pastries stuffed with spiced potatoes and onions.', price:'$7', tag:'CLASSIC', img:'images/punjabi-samosa.jpg' },
    { name:'Apollo Fish', desc:'Crispy fried fish in a tangy chili glaze.', price:'$16', tag:'COASTAL', img:'images/apollo-fish.jpg' },
    { name:'BNG Tandoori Wings', desc:'Chicken wings glazed with our house tandoori sauce, chargrilled to a caramel finish.', price:'$13', tag:'STICKY', img:'images/bng-tandoori-grilled-wings.jpg' },
    { name:'Manchow Soup', desc:'Spicy Indo-Chinese soup with crispy noodle topping.', price:'$8', tag:'WARM', img:'images/manchow-soup.webp' },
    { name:'Sweet Corn Soup', desc:'Silky sweet corn soup with delicate herbs.', price:'$7', tag:'MILD', img:'images/sweet-corn-soup.webp' }
  ],
  south: [
    { name:'Crispy Masala Dosa', desc:'Golden rice-lentil crepe folded around spiced potato masala, served with sambar and chutneys.', price:'$12', tag:'CROWD FAV', img:'images/dosa.webp' },
    { name:'Idly with Chutney', desc:'Fluffy steamed rice cakes with coconut chutney, tomato chutney and hot sambar.', price:'$8', tag:'CLASSIC', img:'images/idly.webp' },
    { name:'Idly Platter', desc:'A big platter of soft idlys with three chutneys and sambar &mdash; the perfect share.', price:'$12', tag:'SHARE', img:'images/idly-2.webp' },
    { name:'Street Chaat', desc:'Crispy puris, tangy tamarind, mint chutney and yogurt &mdash; a hit of Indian street food.', price:'$9', tag:'STREET STYLE', img:'images/chaat.webp' },
    { name:'Chicken Fried Rice', desc:'Wok-tossed rice with chicken, spring onions and Indo-Chinese soy glaze.', price:'$13', tag:'INDO CHINESE', img:'images/chicken-fried-rice.webp' },
    { name:'Egg Bhurji Special', desc:'Spiced scrambled eggs cooked Hyderabadi-style with fresh cilantro and green chilies.', price:'$10', tag:'BREAKFAST', img:'images/egg-dish.webp' }
  ],
  breads: [
    { name:'Butter Naan', desc:'Soft, pillowy naan brushed with butter, straight from the tandoor.', price:'$4', tag:'CLASSIC', img:'images/butter-naan.webp' },
    { name:'Bullet Naan Chilli Garlic', desc:'Fluffy naan blistered with fiery green chili and roasted garlic.', price:'$6', tag:'FIERY', img:'images/bullet-naan-chilli-garlic.png' },
    { name:'Rumali Roti', desc:'Handkerchief-thin bread cooked on an inverted tawa.', price:'$4', tag:'THIN', img:'images/rumali-roti.jpg' },
    { name:'Mango Lassi (Big)', desc:'Creamy yogurt drink blended with sweet Alphonso mango pulp.', price:'$6', tag:'REFRESHING', img:'images/mango-lassi.jpg' },
    { name:'Rose Panna Cotta', desc:'House-made rose panna cotta with basil seeds and fresh strawberry.', price:'$8', tag:'HOUSE MADE', img:'images/panna-cotta-dessert.webp' },
    { name:'Kulfi', desc:'Traditional Indian ice cream slow-churned with cardamom and pistachios.', price:'$6', tag:'ICE COLD', img:'images/kulfi.webp' },
    { name:'House Dessert', desc:'Ask about our seasonal special &mdash; something sweet is always in rotation.', price:'$7', tag:'SEASONAL', img:'images/dessert-2.webp' },
    { name:'Masala Chai', desc:'Traditional spiced Indian tea brewed with cardamom, ginger and cloves.', price:'$3', tag:'WARM', img:'images/masala-chai.webp' }
  ]
};

/* ---------- Render menu ---------- */
const menuGrid = document.getElementById('menuGrid');
function renderMenu(category) {
  const items = menuData[category] || [];
  menuGrid.innerHTML = '';
  items.forEach((item, i) => {
    const card = document.createElement('article');
    card.className = 'menu-card';
    card.style.animationDelay = `${i * 0.08}s`;
    card.innerHTML = `
      <div class="menu-card-img">
        <img src="${item.img}" alt="${item.name}" loading="lazy" />
        <span class="menu-card-tag">${item.tag}</span>
      </div>
      <div class="menu-card-body">
        <h3>${item.name}</h3>
        <p>${item.desc}</p>
        <div class="menu-card-foot">
          <div class="menu-card-price">${item.price}</div>
          <button class="menu-card-add">ADD +</button>
        </div>
      </div>
    `;
    menuGrid.appendChild(card);
  });
}
renderMenu('popular');

document.querySelectorAll('.tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    renderMenu(tab.dataset.cat);
  });
});

/* ---------- Gallery filter + mobile limit ---------- */
const GALLERY_MOBILE_LIMIT = 6;
const GALLERY_MOBILE_BREAKPOINT = 768;
let galleryShowAll = false;

function isGalleryMobile() {
  return window.innerWidth <= GALLERY_MOBILE_BREAKPOINT;
}

function applyGalleryFilter() {
  const activeTab = document.querySelector('.g-tab.active');
  const cat = activeTab ? activeTab.dataset.gcat : 'all';
  const items = document.querySelectorAll('.g-item');
  const mobile = isGalleryMobile();
  let matchingTotal = 0;
  let shown = 0;

  items.forEach(item => {
    const matches = cat === 'all' || item.dataset.gcat === cat;
    if (!matches) {
      item.style.display = 'none';
      return;
    }
    matchingTotal++;
    if (mobile && !galleryShowAll && shown >= GALLERY_MOBILE_LIMIT) {
      item.style.display = 'none';
    } else {
      item.style.display = '';
      shown++;
    }
  });

  // Toggle the "See all" button
  const btn = document.getElementById('galleryShowAllBtn');
  const btnText = document.getElementById('galleryShowAllText');
  if (btn) {
    if (mobile && matchingTotal > GALLERY_MOBILE_LIMIT) {
      btn.style.display = 'inline-flex';
      btnText.textContent = galleryShowAll
        ? 'Show fewer'
        : `See all ${matchingTotal} photos`;
    } else {
      btn.style.display = 'none';
    }
  }
}

document.querySelectorAll('.g-tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.g-tab').forEach(t => t.classList.remove('active'));
    tab.classList.add('active');
    galleryShowAll = false; // reset when filter changes
    applyGalleryFilter();
    // scroll gallery back into view so users don't lose position
    if (isGalleryMobile()) {
      document.getElementById('gallery').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
});

const gAllBtn = document.getElementById('galleryShowAllBtn');
if (gAllBtn) {
  gAllBtn.addEventListener('click', () => {
    galleryShowAll = !galleryShowAll;
    applyGalleryFilter();
    if (!galleryShowAll) {
      document.getElementById('gallery').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
}

// Re-apply on load and resize (debounced)
applyGalleryFilter();
let galleryResizeTimer;
window.addEventListener('resize', () => {
  clearTimeout(galleryResizeTimer);
  galleryResizeTimer = setTimeout(applyGalleryFilter, 200);
});

/* ---------- Reviews carousel ---------- */
const revTrack = document.getElementById('revTrack');
document.getElementById('revNext').addEventListener('click', () => {
  revTrack.scrollBy({ left: 420, behavior: 'smooth' });
});
document.getElementById('revPrev').addEventListener('click', () => {
  revTrack.scrollBy({ left: -420, behavior: 'smooth' });
});

/* auto-scroll reviews */
let revAutoTimer = setInterval(autoScrollReviews, 5000);
function autoScrollReviews() {
  const maxScroll = revTrack.scrollWidth - revTrack.clientWidth;
  if (revTrack.scrollLeft >= maxScroll - 10) {
    revTrack.scrollTo({ left: 0, behavior: 'smooth' });
  } else {
    revTrack.scrollBy({ left: 420, behavior: 'smooth' });
  }
}
revTrack.addEventListener('mouseenter', () => clearInterval(revAutoTimer));
revTrack.addEventListener('mouseleave', () => {
  revAutoTimer = setInterval(autoScrollReviews, 5000);
});

/* ---------- Parallax on hero content ---------- */
const heroContent = document.querySelector('.hero-content');
window.addEventListener('scroll', () => {
  const y = window.scrollY;
  if (y < window.innerHeight) {
    heroContent.style.transform = `translateY(${y * 0.3}px)`;
    heroContent.style.opacity = 1 - y / 600;
  }
});

/* ---------- Rating bars animate when in view ---------- */
const barsSection = document.querySelector('.rating-bars');
if (barsSection) {
  const barObs = new IntersectionObserver((entries) => {
    entries.forEach(e => {
      if (e.isIntersecting) {
        barsSection.querySelectorAll('.bar i').forEach(bar => {
          const w = bar.style.width;
          bar.style.width = '0';
          setTimeout(() => bar.style.width = w, 100);
        });
        barObs.unobserve(barsSection);
      }
    });
  }, { threshold: 0.4 });
  barObs.observe(barsSection);
}
