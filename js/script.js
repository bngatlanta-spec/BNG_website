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

// ── Google Review link — change this one value to update every button & QR code
const REVIEW_URL = 'https://customersreviewforus.com/bikes-barrels-biryani-n-grill/';

// Wire all review buttons to REVIEW_URL
document.querySelectorAll('.review-link').forEach(el => { el.href = REVIEW_URL; });

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

  const ORDER_URL = 'https://food.orders.co/bikesbarrels-biryaningrill/menu';
  list.innerHTML = items.map((s, i) => `
    <li class="notif-item" style="animation-delay:${i * 0.08}s;cursor:pointer" onclick="window.open('${ORDER_URL}','_blank','noopener')">
      <span class="notif-item-num">${String(i + 1).padStart(2, '0')}</span>
      <div class="notif-item-body">
        <span class="notif-item-cat">${s.category}</span>
        <span class="notif-item-name">${s.item}</span>
      </div>
      <span class="notif-item-arrow">&#8594;</span>
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
const navBackdrop = document.getElementById('navBackdrop');

function closeNav() {
  hamburger.classList.remove('open');
  navLinks.classList.remove('open');
  navBackdrop.classList.remove('open');
}

hamburger.addEventListener('click', () => {
  const opening = !navLinks.classList.contains('open');
  hamburger.classList.toggle('open');
  navLinks.classList.toggle('open');
  navBackdrop.classList.toggle('open', opening);
});

navBackdrop.addEventListener('click', closeNav);

document.querySelectorAll('.nav-link, .nav-mobile-reserve, .nav-mobile-review').forEach(a => {
  a.addEventListener('click', closeNav);
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
}, { threshold: 0.05, rootMargin: '0px 0px -20px 0px' });
document.querySelectorAll('.reveal').forEach(el => observer.observe(el));

/* ---------- Menu rendering handled by js/menu-static.js ---------- */

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

/* ---------- Reservation form ---------- */

// Paste your deployed Apps Script URL here (see reservation-apps-script.js for setup steps)
const RESERVATION_SCRIPT_URL = 'https://script.google.com/macros/s/AKfycby0SuBSdRwbCsqC-DrsGHtaz0FEL-U31KnpwwV4paF8gvY6udX-SrgYSvuao8CzlhGMUg/exec';

// Set minimum date to today
const resDateInput = document.getElementById('resDate');
if (resDateInput) {
  resDateInput.setAttribute('min', new Date().toISOString().split('T')[0]);
}

const reservationForm = document.getElementById('reservationForm');
if (reservationForm) {
  reservationForm.addEventListener('submit', async function (e) {
    e.preventDefault();

    const name     = document.getElementById('resName').value.trim();
    const phone    = document.getElementById('resPhone').value.trim();
    const email    = document.getElementById('resEmail').value.trim();
    const date     = document.getElementById('resDate').value;
    const time     = document.getElementById('resTime').value;
    const party    = document.getElementById('resParty').value;
    const requests = document.getElementById('resRequests').value.trim();

    const btn      = document.getElementById('resSubmitBtn');
    const label    = document.getElementById('resSubmitLabel');
    const errEl    = document.getElementById('resError');

    // Loading state
    btn.disabled   = true;
    label.textContent = 'Sending…';
    errEl.hidden   = true;

    const formattedDate = date
      ? new Date(date + 'T12:00:00').toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })
      : date;

    const partySuffix = (party === '1') ? '1 guest' : `${party} guests`;

    try {
      // POST to Apps Script — doPost handles both reservation and catering by type
      fetch(RESERVATION_SCRIPT_URL, {
        method: 'POST',
        mode:   'no-cors',
        body:   JSON.stringify({ type: 'reservation', customerName: name, phone, email,
                                 reservationDate: date, reservationTime: time,
                                 partySize: party, specialRequests: requests })
      });
      // Can't read a no-cors response — add a small delay so the request fires
      await new Promise(r => setTimeout(r, 600));

      // Fade form out, then swap to confirmation and scroll into view
      document.getElementById('resSuccessName').textContent   = name;
      document.getElementById('resSuccessDetail').textContent = `${formattedDate} · ${time} · ${partySuffix}`;

      const formWrap  = document.getElementById('resFormWrap');
      const successEl = document.getElementById('resSuccess');

      formWrap.classList.add('fading');
      setTimeout(() => {
        formWrap.hidden   = true;
        successEl.hidden  = false;
        successEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 260);

    } catch {
      // still show success — request was fired, script processes async
    } finally {
      btn.disabled      = false;
      label.textContent = 'Confirm Reservation';
    }
  });
}

document.getElementById('resResetBtn')?.addEventListener('click', function () {
  const form     = document.getElementById('reservationForm');
  const formWrap = document.getElementById('resFormWrap');
  form.reset();
  document.getElementById('resSuccess').hidden = true;
  document.getElementById('resError').hidden   = true;
  formWrap.classList.remove('fading');
  formWrap.hidden = false;
  formWrap.scrollIntoView({ behavior: 'smooth', block: 'start' });
});

/* ---------- Catering enquiry form ---------- */
const cateringForm = document.getElementById('cateringInquiryForm');
if (cateringForm) {
  cateringForm.addEventListener('submit', async function (e) {
    e.preventDefault();

    const name        = document.getElementById('catName').value.trim();
    const email       = document.getElementById('catEmail').value.trim();
    const subject     = document.getElementById('catSubject').value.trim();
    const eventDetails = document.getElementById('catDetails').value.trim();

    const btn   = document.getElementById('catSubmitBtn');
    const label = document.getElementById('catSubmitLabel');
    const errEl = document.getElementById('catError');

    btn.disabled      = true;
    label.textContent = 'Sending…';
    errEl.hidden      = true;

    try {
      fetch(RESERVATION_SCRIPT_URL, {
        method: 'POST',
        mode:   'no-cors',
        body:   JSON.stringify({ type: 'catering', name, email, subject, eventDetails })
      });
      await new Promise(r => setTimeout(r, 600));

      document.getElementById('catSuccessName').textContent = name;

      const wrap      = document.getElementById('catFormWrap');
      const successEl = document.getElementById('catSuccess');
      wrap.classList.add('fading');
      setTimeout(() => {
        wrap.hidden      = true;
        successEl.hidden = false;
        successEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 260);

    } catch {
      // still show success — request was fired
    } finally {
      btn.disabled      = false;
      label.textContent = 'Send Enquiry';
    }
  });
}


document.getElementById('catResetBtn')?.addEventListener('click', function () {
  const form = document.getElementById('cateringInquiryForm');
  const wrap = document.getElementById('catFormWrap');
  form.reset();
  document.getElementById('catSuccess').hidden = true;
  document.getElementById('catError').hidden   = true;
  wrap.classList.remove('fading');
  wrap.hidden = false;
  wrap.scrollIntoView({ behavior: 'smooth', block: 'start' });
});
