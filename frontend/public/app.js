'use strict';

const OPS = {
  add:       { symbol: '+',  label: 'Cộng',      arity: 2, a: 'Số thứ nhất',    b: 'Số thứ hai' },
  subtract:  { symbol: '−',  label: 'Trừ',       arity: 2, a: 'Số bị trừ',      b: 'Số trừ' },
  multiply:  { symbol: '×',  label: 'Nhân',      arity: 2, a: 'Số thứ nhất',    b: 'Số thứ hai' },
  divide:    { symbol: '÷',  label: 'Chia',      arity: 2, a: 'Số bị chia',     b: 'Số chia' },
  sqrt:      { symbol: '√',  label: 'Căn bậc 2', arity: 1, a: 'Số cần khai căn' },
  power:     { symbol: 'xʸ', label: 'Lũy thừa',  arity: 2, a: 'Cơ số',          b: 'Số mũ' },
  factorial: { symbol: 'n!', label: 'Giai thừa', arity: 1, a: 'n (số nguyên ≥ 0)' },
};
const HISTORY_KEY = 'calc.history.v1';
const HISTORY_MAX = 20;
const $ = (id) => document.getElementById(id);

let currentOp = 'add';
let lastFull = '';

/* ---------- dựng phím chọn phép tính ---------- */
const keys = $('keys');
Object.entries(OPS).forEach(([id, o]) => {
  const lab = document.createElement('label');
  lab.className = 'key';
  lab.innerHTML = `<input type="radio" name="op" value="${id}" ${id === currentOp ? 'checked' : ''}><span><b>${o.symbol}</b><small>${o.label}</small></span>`;
  keys.appendChild(lab);
});
keys.addEventListener('change', (e) => { if (e.target.name === 'op') setOp(e.target.value); });

function setOp(op) {
  currentOp = op;
  const o = OPS[op];
  $('label-a').textContent = o.a;
  $('label-b').textContent = o.b || '';
  $('field-b').hidden = o.arity === 1;
  $('op-sign').textContent = o.symbol;
  $('op-sign').style.display = o.arity === 1 ? 'none' : '';
  $('t-req').textContent = `calc.${op}.requests`;
  $('t-service-name').textContent = `${op}-service`;
  resetTrace();
  const input = $('a'); input.focus();
}

/* ---------- định dạng ---------- */
const clampText = (s, max = 14) => (s.length > max ? s.slice(0, max) + '…' : s);
const normalise = (s) => String(s).trim().replace(',', '.');

function expression(op, a, b) {
  switch (op) {
    case 'sqrt': return `√(${clampText(a)})`;
    case 'factorial': return `${clampText(a)}!`;
    case 'power': return `${clampText(a)} ^ ${clampText(b)}`;
    default: return `${clampText(a)} ${OPS[op].symbol} ${clampText(b)}`;
  }
}
function shortResult(str) {
  return str.length > 40 ? `${str.slice(0, 20)}…${str.slice(-8)}` : str;
}

/* ---------- đường đi ---------- */
const nodes = [...document.querySelectorAll('#trace .node')];
function resetTrace() {
  $('trace').classList.remove('busy');
  nodes.forEach((n) => n.classList.remove('done', 'err'));
  $('t-browser').textContent = 'Gửi POST /api/calculate';
  $('t-gateway').textContent = 'Ghi yêu cầu vào Kafka';
  $('t-service').textContent = 'Đọc yêu cầu, tính, ghi kết quả';
  $('t-back').textContent = 'Gateway ghép kết quả theo mã yêu cầu';
}
function traceBusy() { resetTrace(); $('trace').classList.add('busy'); }
function traceDone(r) {
  $('trace').classList.remove('busy');
  const bad = r && r.ok === false;
  nodes.forEach((n) => n.classList.add('done'));
  if (bad) nodes.find((n) => n.dataset.n === 'service').classList.replace('done', 'err');
  if (r.gateway) $('t-gateway').textContent = `pod ${r.gateway}`;
  if (r.topic) $('t-req').textContent = r.topic;
  if (r.instance) $('t-service').textContent = `pod ${r.instance} · xử lý ${r.processedMs.toFixed(2)} ms`;
  $('t-back').textContent = `tổng ${r.totalMs} ms`;
}
function traceFail(msg) {
  $('trace').classList.remove('busy');
  nodes.slice(0, 2).forEach((n) => n.classList.add('done'));
  nodes[1].classList.replace('done', 'err');
  $('t-gateway').textContent = msg;
}

/* ---------- tính toán ---------- */
$('calc-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const o = OPS[currentOp];
  const a = normalise($('a').value);
  const b = o.arity === 2 ? normalise($('b').value) : undefined;

  const bad = [];
  if (a === '' || !Number.isFinite(Number(a))) bad.push('a');
  if (o.arity === 2 && (b === '' || !Number.isFinite(Number(b)))) bad.push('b');
  ['a', 'b'].forEach((k) => $(k).setAttribute('aria-invalid', String(bad.includes(k))));
  if (bad.length) {
    showError(expression(currentOp, a || '?', b || '?'), 'Hãy nhập số hợp lệ vào ô được đánh dấu.');
    $(bad[0]).focus();
    return;
  }

  const btn = $('go');
  btn.disabled = true; btn.textContent = 'Đang tính…';
  traceBusy();
  $('expr').textContent = expression(currentOp, a, b);
  $('value').textContent = '…'; $('value').className = 'value';
  $('meta').textContent = 'Đang chờ service phản hồi';
  $('copy').hidden = true;

  try {
    const res = await fetch('/api/calculate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ op: currentOp, a, b }),
    });
    const r = await res.json();
    if (!res.ok && r.ok === undefined) throw new Error('Phản hồi không hợp lệ');
    if (!res.ok) {
      traceFail(r.error || `HTTP ${res.status}`);
      showError(expression(currentOp, a, b), r.error || 'Có lỗi xảy ra.');
    } else if (r.ok) {
      traceDone(r);
      showResult(r, a, b);
    } else {
      traceDone(r);
      showError(expression(currentOp, a, b), r.error, r);
    }
    if (res.ok) addHistory({ op: currentOp, a, b, ok: r.ok, result: r.ok ? String(r.result) : r.error });
  } catch (err) {
    traceFail('Không kết nối được gateway');
    showError(expression(currentOp, a, b), 'Không kết nối được gateway. Kiểm tra lại các container hoặc pod.');
  } finally {
    btn.disabled = false; btn.textContent = 'Tính';
  }
});

function showResult(r, a, b) {
  const full = String(r.result);
  lastFull = full;
  $('expr').textContent = `${expression(currentOp, a, b)} =`;
  $('value').className = 'value';
  $('value').textContent = shortResult(full);
  const digits = /^\d+$/.test(full) && full.length > 40 ? ` · ${full.length} chữ số` : '';
  $('meta').textContent = `Tính bởi ${r.service} · ${r.processedMs.toFixed(2)} ms trong service, ${r.totalMs} ms tổng cộng${digits}`;
  $('copy').hidden = full.length <= 40;
}
function showError(exprText, msg, r) {
  lastFull = '';
  $('expr').textContent = exprText;
  $('value').className = 'value err';
  $('value').textContent = msg;
  $('meta').textContent = r && r.service ? `Phản hồi từ ${r.service} · ${r.totalMs} ms` : '';
  $('copy').hidden = true;
}
$('copy').addEventListener('click', async () => {
  try { await navigator.clipboard.writeText(lastFull); $('copy').textContent = 'Đã sao chép'; }
  catch { $('copy').textContent = 'Trình duyệt không cho sao chép'; }
  setTimeout(() => { $('copy').textContent = 'Sao chép kết quả đầy đủ'; }, 1800);
});

/* ---------- lịch sử (localStorage) ---------- */
function loadHistory() {
  try { return JSON.parse(localStorage.getItem(HISTORY_KEY)) || []; } catch { return []; }
}
function saveHistory(list) {
  try { localStorage.setItem(HISTORY_KEY, JSON.stringify(list)); } catch { /* bỏ qua */ }
}
let history = loadHistory();
function addHistory(item) {
  history = [item, ...history].slice(0, HISTORY_MAX);
  saveHistory(history);
  renderHistory();
}
function renderHistory() {
  const ul = $('history-list');
  ul.innerHTML = '';
  $('history-empty').hidden = history.length > 0;
  $('clear').hidden = history.length === 0;
  history.forEach((h) => {
    const li = document.createElement('li');
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.title = 'Nạp lại phép tính này';
    const l = document.createElement('span'); l.className = 'h-expr'; l.textContent = expression(h.op, h.a, h.b);
    const r = document.createElement('span'); r.className = 'h-res' + (h.ok ? '' : ' bad'); r.textContent = h.ok ? shortResult(h.result) : h.result;
    btn.append(l, r);
    btn.addEventListener('click', () => {
      document.querySelector(`input[name="op"][value="${h.op}"]`).checked = true;
      setOp(h.op);
      $('a').value = h.a; $('b').value = h.b || '';
    });
    li.appendChild(btn); ul.appendChild(li);
  });
}
$('clear').addEventListener('click', () => { history = []; saveHistory(history); renderHistory(); });

/* ---------- trạng thái service ---------- */
async function refreshServices() {
  const gw = $('gw');
  try {
    const res = await fetch('/api/services', { cache: 'no-store' });
    if (!res.ok) throw new Error();
    const data = await res.json();
    const up = data.services.filter((s) => s.status === 'up').length;
    gw.className = 'gw ' + (up === data.services.length ? 'ok' : up ? '' : 'bad');
    $('gw-text').textContent = `Gateway ${data.gateway} · ${up}/${data.services.length} service đang chạy`;
    $('svc-list').innerHTML = data.services.map((s) => `
      <li class="${s.status}"><span class="dot"></span>
        <span class="name">${s.service}</span>
        <span class="count">${s.status === 'up' ? `${s.instances} bản sao` : 'không phản hồi'}</span></li>`).join('');
  } catch {
    gw.className = 'gw bad';
    $('gw-text').textContent = 'Không kết nối được gateway';
    $('svc-list').innerHTML = '<li class="down"><span class="dot"></span><span class="name">gateway</span><span class="count">không phản hồi</span></li>';
  }
}

/* ---------- khởi động ---------- */
setOp('add');
renderHistory();
refreshServices();
setInterval(refreshServices, 4000);
