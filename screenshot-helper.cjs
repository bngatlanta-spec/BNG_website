const { chromium } = require('./Handy/node_modules/playwright-core');
(async () => {
  const browser = await chromium.launch({
    executablePath: 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
    args: ['--no-sandbox']
  });
  const page = await browser.newPage();
  await page.setViewportSize({ width: 1400, height: 900 });
  await page.goto('http://localhost:3000/menu.html', { waitUntil: 'networkidle' });
  await page.waitForSelector('.loader.hidden', { timeout: 8000 });
  await page.waitForTimeout(600);

  // Screenshot 1: full menu page
  await page.screenshot({ path: 'ss-1-menu.png', fullPage: true });

  // Screenshot 2: click Add on first 3 items then open cart
  const addBtns = await page.$$('.menu-card-add');
  for (let i = 0; i < Math.min(3, addBtns.length); i++) {
    await addBtns[i].click();
    await page.waitForTimeout(200);
  }
  await page.click('#cartBtn');
  await page.waitForTimeout(600);
  await page.screenshot({ path: 'ss-2-cart.png' });

  console.log('done');
  await browser.close();
})();
