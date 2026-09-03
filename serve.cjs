const http = require('http');
const fs = require('fs');
const path = require('path');
const mime = {
  '.html':'text/html','.css':'text/css','.js':'application/javascript',
  '.png':'image/png','.jpg':'image/jpeg','.webp':'image/webp',
  '.svg':'image/svg+xml','.ico':'image/x-icon','.json':'application/json',
  '.xml':'application/xml','.woff2':'font/woff2','.ttf':'font/ttf','.gif':'image/gif'
};
const PORT = 3001;
http.createServer((req, res) => {
  let url = req.url.split('?')[0];
  if (url === '/') url = '/index.html';
  const file = path.join(__dirname, url);
  try {
    const data = fs.readFileSync(file);
    const ext = path.extname(file);
    res.writeHead(200, {'Content-Type': mime[ext] || 'application/octet-stream'});
    res.end(data);
  } catch {
    res.writeHead(404); res.end('Not found');
  }
}).listen(PORT, () => {
  console.log('');
  console.log('  BNG site running at:');
  console.log('  http://localhost:' + PORT);
  console.log('');
  console.log('  Press Ctrl+C to stop.');
  console.log('');
});
