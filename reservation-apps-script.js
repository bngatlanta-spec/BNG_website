/**
 * BNG — Combined Apps Script (Specials + Reservations + Catering + Reviews)
 *
 * HOW TO UPDATE YOUR EXISTING SCRIPT:
 *   1. Go to script.google.com → open your existing project
 *   2. Delete everything in Code.gs and paste this entire file
 *   3. Save (Ctrl+S)
 *   4. Deploy → Manage deployments → Edit (pencil) → New version → Deploy
 *   Same URL — no other changes needed.
 */

const OWNER_EMAIL = 'Bngatlanta@gmail.com';

// ── Specials (GET) ────────────────────────────────────────────────────────────
function doGet(e) {
  try {
    const ss    = SpreadsheetApp.getActiveSpreadsheet();
    const sheet = ss.getSheetByName('Specials');
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
  const ts = now();

  // Send email first — always
  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: 'New Reservation: ' + p.customerName + ' — ' + p.reservationDate + ' at ' + p.reservationTime,
    body:
      'New table reservation from biryani-n-grill.com\n\n' +
      'Name:     ' + p.customerName + '\n' +
      'Phone:    ' + p.phone + '\n' +
      'Email:    ' + p.email + '\n' +
      'Date:     ' + p.reservationDate + '\n' +
      'Time:     ' + p.reservationTime + '\n' +
      'Party:    ' + p.partySize + '\n' +
      'Requests: ' + (p.specialRequests || 'None') + '\n\n' +
      'Submitted: ' + ts + ' ET'
  });

  if (p.email) {
    MailApp.sendEmail({
      to:      p.email,
      subject: 'Reservation Received — Bikes & Barrels Biryani N Grill',
      body:
        'Hi ' + p.customerName + ',\n\n' +
        "We've received your reservation request:\n\n" +
        '  Date:   ' + p.reservationDate + '\n' +
        '  Time:   ' + p.reservationTime + '\n' +
        '  Guests: ' + p.partySize + '\n' +
        (p.specialRequests ? '  Notes:  ' + p.specialRequests + '\n' : '') +
        "\nWe'll confirm your table shortly.\n" +
        'Questions? Call (678) 293-5779.\n\n' +
        'See you soon!\n' +
        'Bikes & Barrels — Biryani N Grill\n' +
        '2590 Spring Rd SE, Smyrna, GA 30080'
    });
  }

  // Save to sheet — separately so a sheet error never blocks the email
  try {
    const ss    = SpreadsheetApp.getActiveSpreadsheet();
    let   sheet = ss.getSheetByName('Reservations');
    if (!sheet) {
      sheet = ss.insertSheet('Reservations');
      const h = ['Timestamp','Name','Phone','Email','Date','Time','Party Size','Special Requests'];
      sheet.appendRow(h);
      sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
      sheet.setFrozenRows(1);
    }
    sheet.appendRow([ts, p.customerName, p.phone, p.email,
                     p.reservationDate, p.reservationTime,
                     p.partySize, p.specialRequests || '']);
  } catch (sheetErr) {
    // Sheet save failed but email already sent — log and continue
    Logger.log('Sheet error (reservation): ' + sheetErr.toString());
  }

  return json({ success: true });
}

// ── Catering / Review ─────────────────────────────────────────────────────────
function handleCatering(p) {
  const ts = now();

  // Send email first — always
  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: p.subject + ' — ' + (p.name || 'Anonymous'),
    body:
      'New submission from biryani-n-grill.com\n\n' +
      'Name:    ' + (p.name || 'Anonymous') + '\n' +
      'Email:   ' + (p.email || 'Not provided') + '\n' +
      'Subject: ' + p.subject + '\n\n' +
      'Details:\n' + p.eventDetails + '\n\n' +
      'Submitted: ' + ts + ' ET'
  });

  // Save to sheet — separately
  try {
    const ss    = SpreadsheetApp.getActiveSpreadsheet();
    let   sheet = ss.getSheetByName('Catering Enquiries');
    if (!sheet) {
      sheet = ss.insertSheet('Catering Enquiries');
      const h = ['Timestamp','Name','Email','Subject','Event Details'];
      sheet.appendRow(h);
      sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
      sheet.setFrozenRows(1);
    }
    sheet.appendRow([ts, p.name || '', p.email || '', p.subject, p.eventDetails]);
  } catch (sheetErr) {
    Logger.log('Sheet error (catering): ' + sheetErr.toString());
  }

  return json({ success: true });
}

// ── Contact message ───────────────────────────────────────────────────────────
function handleContact(p) {
  const ts = now();

  // Send email first — always
  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: 'New Message: ' + p.subject + ' — ' + p.name,
    body:
      'New contact message from biryani-n-grill.com\n\n' +
      'Name:    ' + p.name + '\n' +
      'Email:   ' + p.email + '\n' +
      'Subject: ' + p.subject + '\n\n' +
      'Message:\n' + p.message + '\n\n' +
      'Submitted: ' + ts + ' ET'
  });

  // Save to sheet — separately
  try {
    const ss    = SpreadsheetApp.getActiveSpreadsheet();
    let   sheet = ss.getSheetByName('Contact Messages');
    if (!sheet) {
      sheet = ss.insertSheet('Contact Messages');
      const h = ['Timestamp','Name','Email','Subject','Message'];
      sheet.appendRow(h);
      sheet.getRange(1,1,1,h.length).setFontWeight('bold').setBackground('#f5c46b');
      sheet.setFrozenRows(1);
    }
    sheet.appendRow([ts, p.name, p.email, p.subject, p.message]);
  } catch (sheetErr) {
    Logger.log('Sheet error (contact): ' + sheetErr.toString());
  }

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

// ── Quick test — run this manually from the editor to verify email works ──────
function testEmail() {
  MailApp.sendEmail({
    to:      OWNER_EMAIL,
    subject: 'BNG Apps Script — Test Email',
    body:    'If you received this, the script is authorized and email is working.\n\nSent: ' + now()
  });
}
