const API = {
  get: (url) => fetch(url, {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  }).then(r => { if (r.status === 401) { location.href = '../index.html'; throw new Error('Unauth'); } return r.json(); }),

  post: (url, body) => fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
    body: JSON.stringify(body)
  }).then(r => r.json()),

  put: (url, body) => fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' },
    body: JSON.stringify(body)
  }).then(r => r.json()),

  delete: (url) => fetch(url, {
    method: 'DELETE',
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  }).then(r => r.json())
};

function toast(msg, type = 'success') {
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.classList.add('show'), 10);
  setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 400); }, 3500);
}

function showModal(id) { document.getElementById(id).style.display = 'flex'; }
function closeModal(id) { document.getElementById(id).style.display = 'none'; }
function logout() { window.location.href = '../auth'; }
function logoutRoot() { window.location.href = 'auth'; }

function gradeToPoints(g) {
  return { 'O': 10, 'A+': 9, 'A': 8, 'B+': 7, 'B': 6, 'C': 5, 'F': 0 }[g] ?? 0;
}

function gradeClass(g) {
  return { 'O':'grade-O','A+':'grade-Ap','A':'grade-A','B+':'grade-Bp','B':'grade-B','C':'grade-C','F':'grade-F' }[g] ?? '';
}

function statusBadge(s) {
  const map = { enrolled:'bg-blue', completed:'bg-green', dropped:'bg-red' };
  return `<span class="badge ${map[s]||'bg-gray'}">${s}</span>`;
}

function setUserInfo(name, role) {
  const ni = document.getElementById('user-name');
  const ri = document.getElementById('user-role');
  const av = document.getElementById('user-avatar');
  if (ni) ni.textContent = name;
  if (ri) ri.textContent = role;
  if (av) av.textContent = name ? name[0].toUpperCase() : 'U';
}

function initSidebar() {
  const current = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-item').forEach(item => {
    const href = item.getAttribute('href');
    if (href && href === current) item.classList.add('active');
  });
}

function tableEmpty(cols, msg = 'No data found') {
  return `<tr><td colspan="${cols}" style="text-align:center;padding:32px;color:#718096">${msg}</td></tr>`;
}
