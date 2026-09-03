/**
 * BNG — Combined Apps Script (Specials + Reservations + Catering)
 *
 * HOW TO UPDATE YOUR EXISTING SCRIPT:
 *   1. Go to script.google.com → open your existing project
 *   2. Delete everything in Code.gs and paste this entire file
 *   3. Deploy → Manage deployments → Edit (pencil) on your existing deployment
 *      → Version: "New version" → Deploy
 *   Same URL — no other changes needed.
 *
 * doGet  → returns today's specials  (existing feature, unchanged)
 * doPost → routes by "type" field:
 *            type: "reservation" → saves to Reservations sheet + emails
 *            type: "catering"    → saves to Catering sheet + emails
 */

const OWNER_EMAIL       = 'saikiran31520@gmail.com';
const SPECIALS_SHEET    = 'Specials';
const RESERVATION_SHEET = 'Reservations';
const CATERING_SHEET    = 'Catering Enquiries';
const CONTACT_SHEET     = 'Contact Messages';

// ── Specials (GET) ────────────────────────────────────────────────────────────
function doGet(e) {
  try {
    const ss    = SpreadsheetApp.getActiveSpreadsheet();
    const sheet = ss.getSheetByName(SPECIALS_SHEET);
    if (!sheet) return json({ items: [] });

    const data    = sheet.getDataRange().getValues();
    const headers = data[0].map(h => String(h).toLowerCase().trim());
    const rows    = data.slice(1).map(row => {
      const obj = {};
      headers.forEach((h, i) => obj[h] = row[i]);
      return obj;
    });
    return json({ items: rows });
  } catch (err) {
    return json({ items: [], error: err.toString() });
  }
}

// ── POST router ───────────────────────────────────────────────────────────────
function doPost(e) {
  try {
    const p = JSON.parse(e.postData.contents);
    if (p.type === 'catering') return handleCatering(p);
    if (p.type === 'contact')  return handleContact(p);
    return handleReservation(p);
  } catch (err) {
    return json({ success: false, error: err.toString() });
  }
}

// ── Reservation ───────────────────────────────────────────────────────────────
function handleReservation(p) {
  const ss    = SpreadsheetApp.getActiveSpreadsheet();
  let   sheet = ss.getSheetByName(RESERVATION_SHEET);
  if (!sheet) {
    sheet = ss.insertSheet(RESERVATION_SHEET);
    const h = ['Timestamp','Name','Phone','Email','Date','Time','Party Size','Special Requests'];
    sheet.appendRow(h);
    sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
    sheet.setFrozenRows(1);
  }

  const ts = now();
  sheet.appendRow([ts, p.customerName, p.phone, p.email,
                   p.reservationDate, p.reservationTime,
                   p.partySize, p.specialRequests || '']);

  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: `New Reservation: ${p.customerName} — ${p.reservationDate} at ${p.reservationTime}`,
    body:
      `New table reservation from biryani-n-grill.com\n\n` +
      `Name:     ${p.customerName}\n` +
      `Phone:    ${p.phone}\n` +
      `Email:    ${p.email}\n` +
      `Date:     ${p.reservationDate}\n` +
      `Time:     ${p.reservationTime}\n` +
      `Party:    ${p.partySize}\n` +
      `Requests: ${p.specialRequests || 'None'}\n\n` +
      `Submitted: ${ts} ET`
  });

  if (p.email) {
    MailApp.sendEmail({
      to:      p.email,
      subject: 'Reservation Received — Bikes & Barrels Biryani N Grill',
      body:
        `Hi ${p.customerName},\n\n` +
        `We've received your reservation request:\n\n` +
        `  Date:   ${p.reservationDate}\n` +
        `  Time:   ${p.reservationTime}\n` +
        `  Guests: ${p.partySize}\n` +
        (p.specialRequests ? `  Notes:  ${p.specialRequests}\n` : '') +
        `\nWe'll confirm your table shortly.\n` +
        `Questions? Call (678) 293-5779.\n\n` +
        `See you soon!\n` +
        `Bikes & Barrels — Biryani N Grill\n` +
        `2590 Spring Rd SE, Smyrna, GA 30080`
    });
  }

  return json({ success: true });
}

// ── Catering ──────────────────────────────────────────────────────────────────
function handleCatering(p) {
  const ss    = SpreadsheetApp.getActiveSpreadsheet();
  let   sheet = ss.getSheetByName(CATERING_SHEET);
  if (!sheet) {
    sheet = ss.insertSheet(CATERING_SHEET);
    const h = ['Timestamp','Name','Email','Subject','Event Details'];
    sheet.appendRow(h);
    sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
    sheet.setFrozenRows(1);
  }

  const ts = now();
  sheet.appendRow([ts, p.name, p.email, p.subject, p.eventDetails]);

  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: `New Catering Enquiry: ${p.subject} — ${p.name}`,
    body:
      `New catering enquiry from biryani-n-grill.com\n\n` +
      `Name:    ${p.name}\n` +
      `Email:   ${p.email}\n` +
      `Subject: ${p.subject}\n\n` +
      `Details:\n${p.eventDetails}\n\n` +
      `Submitted: ${ts} ET`
  });

  if (p.email) {
    MailApp.sendEmail({
      to:      p.email,
      subject: 'Catering Enquiry Received — Bikes & Barrels Biryani N Grill',
      body:
        `Hi ${p.name},\n\n` +
        `Thank you for reaching out about catering!\n\n` +
        `We've received your enquiry for: ${p.subject}\n\n` +
        `Our team will review your request and get back to you with a custom quote shortly.\n` +
        `For urgent requests call (678) 293-5779.\n\n` +
        `Bikes & Barrels — Biryani N Grill\n` +
        `2590 Spring Rd SE, Smyrna, GA 30080`
    });
  }

  return json({ success: true });
}

// ── Contact message ───────────────────────────────────────────────────────────
function handleContact(p) {
  const ss    = SpreadsheetApp.getActiveSpreadsheet();
  let   sheet = ss.getSheetByName(CONTACT_SHEET);
  if (!sheet) {
    sheet = ss.insertSheet(CONTACT_SHEET);
    const h = ['Timestamp','Name','Email','Subject','Message'];
    sheet.appendRow(h);
    sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
    sheet.setFrozenRows(1);
  }

  const ts = now();
  sheet.appendRow([ts, p.name, p.email, p.subject, p.message]);

  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: `New Message: ${p.subject} — ${p.name}`,
    body:
      `New contact message from biryani-n-grill.com\n\n` +
      `Name:    ${p.name}\n` +
      `Email:   ${p.email}\n` +
      `Subject: ${p.subject}\n\n` +
      `Message:\n${p.message}\n\n` +
      `Submitted: ${ts} ET`
  });

  return json({ success: true });
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function now() {
  return new Date().toLocaleString('en-US', { timeZone: 'America/New_York' });
}
function json(data) {
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
