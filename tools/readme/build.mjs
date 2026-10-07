// Генерирует SVG-баннеры README в стиле темы «Цветная» (мягкий тональный Material You)
// со встроенным урезанным шрифтом Nunito.
// Запуск: cd tools/readme && npm install && node build.mjs
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import subsetFont from 'subset-font';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const OUT = path.join(REPO, 'assets/readme');
const FONT = path.join(REPO, 'app/src/main/res/font/nunito.ttf');

// Палитра темы «Цветная» — те же значения, что ColorPalette в Theme.kt
const P = {
  bg: '#F7F2FA', ink: '#1D1B20', muted: '#49454F', surface: '#FFFFFF', surfaceHi: '#ECE6F0',
  primary: '#4A2FC0', onPrimary: '#FFFFFF', container: '#E8DEFF', onContainer: '#21005D',
  pink: '#FFD8E4', onPink: '#31111D', green: '#D7F5E1', onGreen: '#0B3B22',
  peach: '#FFE3CC', onPeach: '#4A2208', stroke: '#E4DAEE',
};

const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
let allText = '';
const T = (s) => { allText += s; return esc(s); };

const style = (font) => `<style>
@font-face{font-family:"NUN";src:url(data:font/woff2;base64,${font}) format("woff2");font-weight:200 1000}
text{font-family:"NUN",ui-rounded,"Segoe UI",sans-serif}
.r{animation:in .7s cubic-bezier(.2,.8,.2,1) backwards}
@keyframes in{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:none}}
@media (prefers-reduced-motion:reduce){.r{animation:none}}
</style>`;

const frame = (w, h) => `<rect x=".5" y=".5" width="${w - 1}" height="${h - 1}" rx="32" fill="${P.bg}" stroke="${P.stroke}"/>`;

const svg = (w, h, label, body) => (font) =>
  `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}" role="img" aria-label="${esc(label)}">\n${style(font)}\n${frame(w, h)}\n${body}\n</svg>\n`;

const spans = (parts) => parts.map(([s, c]) => `<tspan fill="${c}">${T(s)}</tspan>`).join('');
const delay = (i) => `style="animation-delay:${(i * 0.08).toFixed(2)}s"`;
const card = (x, y, w, h, fill, rx = 24) => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${rx}" fill="${fill}"/>`;

/** Пилюля-чип, как фильтры в приложении. Ширина — по тексту. */
function chip(x, y, text, { fill = P.container, color = P.onContainer, check = false } = {}) {
  const w = Math.round(text.length * 7.4 + (check ? 46 : 28));
  let s = `<rect x="${x}" y="${y}" width="${w}" height="30" rx="10" fill="${fill}"/>`;
  let tx = x + 14;
  if (check) {
    s += `<path d="M${x + 13} ${y + 15.5}l4 4 8-8.5" fill="none" stroke="${color}" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>`;
    tx = x + 32;
  }
  s += `<text x="${tx}" y="${y + 20}" font-size="13" font-weight="800" fill="${color}">${T(text)}</text>`;
  return { s, w };
}

const sectionHead = (label, parts) => {
  const c = chip(40, 34, label, { check: true });
  return `<g class="r">${c.s}</g>
<g class="r" ${delay(1)}><text x="40" y="104" font-size="30" font-weight="900" letter-spacing="-.4">${spans(parts)}</text></g>`;
};

// ---------- телефон «Цветная» (как превью темы в приложении) ----------
function phoneTonal(x, y, k = 1) {
  const g = (s) => `<g transform="translate(${x} ${y}) scale(${k})">${s}</g>`;
  let s = `<rect width="150" height="228" rx="20" fill="${P.bg}" stroke="${P.stroke}"/>`;
  s += `<rect x="10" y="14" width="100" height="16" rx="8" fill="${P.surfaceHi}"/><circle cx="130" cy="22" r="8" fill="${P.primary}"/>`;
  s += `<rect x="10" y="40" width="74" height="9" rx="3" fill="${P.ink}"/>`;
  s += `<rect x="10" y="58" width="130" height="40" rx="12" fill="${P.container}"/>`;
  for (let i = 0; i < 7; i++) {
    const fill = i < 2 ? P.primary : i === 2 ? P.pink : P.bg;
    s += `<rect x="${16 + i * 17.5}" y="76" width="14" height="14" rx="5" fill="${fill}"/>`;
  }
  s += `<rect x="10" y="106" width="62" height="40" rx="10" fill="#fff"/><rect x="78" y="106" width="62" height="28" rx="10" fill="${P.pink}"/>`;
  s += `<rect x="10" y="152" width="62" height="28" rx="10" fill="${P.green}"/><rect x="78" y="140" width="62" height="34" rx="10" fill="#fff"/>`;
  s += `<rect x="98" y="180" width="42" height="18" rx="7" fill="${P.primary}"/>`;
  s += `<path d="M0 204h150v4a20 20 0 0 1-20 20H20A20 20 0 0 1 0 208z" fill="#EFE8F7"/><rect x="28" y="210" width="24" height="10" rx="5" fill="${P.container}"/>`;
  return g(s);
}
function phoneOkto(x, y, p) {
  let s = `<rect x="${x}" y="${y}" width="150" height="228" rx="20" fill="${p.bg}"/>`;
  s += `<circle cx="${x + 16}" cy="${y + 18}" r="3" fill="${p.key}"/><rect x="${x + 24}" y="${y + 15}" width="34" height="6" rx="2" fill="${p.ink}" fill-opacity=".8"/><rect x="${x + 100}" y="${y + 15}" width="38" height="6" rx="2" fill="${p.muted}" fill-opacity=".6"/>`;
  s += `<rect x="${x + 10}" y="${y + 32}" width="70" height="11" rx="2" fill="${p.ink}"/>`;
  for (let i = 0; i < 2; i++) {
    const wx = x + 10 + i * 67;
    s += `<rect x="${wx}" y="${y + 52}" width="63" height="44" rx="6" fill="${p.well}"/>`;
    for (let d = 0; d < 2; d++) s += `<rect x="${wx + 8 + d * 14}" y="${y + 66}" width="11" height="20" rx="2" fill="${p.wellInk}"/>`;
  }
  for (let i = 0; i < 4; i++) {
    s += `<rect x="${x + 10}" y="${y + 104 + i * 22}" width="130" height="18" rx="5" fill="${i === 0 ? p.tint : p.row}"/>`;
    s += `<rect x="${x + 16}" y="${y + 110 + i * 22}" width="${60 - i * 8}" height="5" rx="2" fill="${p.ink}" fill-opacity=".85"/><rect x="${x + 118}" y="${y + 110 + i * 22}" width="16" height="5" rx="2" fill="${p.muted}" fill-opacity=".7"/>`;
  }
  s += `<rect x="${x + 8}" y="${y + 202}" width="32" height="18" rx="4" fill="${p.row}"/><rect x="${x + 44}" y="${y + 202}" width="32" height="18" rx="4" fill="${p.key}"/><rect x="${x + 80}" y="${y + 202}" width="62" height="18" rx="4" fill="${p.primary}"/>`;
  return s;
}

// ---------- HERO ----------
function hero() {
  const W = 880, H = 480;
  let b = '';
  b += `<g class="r"><circle cx="54" cy="56" r="14" fill="${P.primary}"/><circle cx="54" cy="56" r="5" fill="#fff"/>
<text x="78" y="64" font-size="24" font-weight="900" letter-spacing="-.4" fill="${P.ink}">${T('Okto Notes')}</text></g>`;
  const v = chip(220, 41, 'v1.1', { fill: P.surfaceHi, color: P.muted });
  b += `<g class="r" ${delay(1)}>${v.s}</g>`;
  b += `<g class="r" ${delay(2)}><text x="40" y="146" font-size="40" font-weight="900" letter-spacing="-1">${spans([['Лучшее приложение', P.ink]])}</text>
<text x="40" y="194" font-size="40" font-weight="900" letter-spacing="-1">${spans([['для ', P.ink], ['заметок', P.primary], [' и дневника', P.ink]])}</text></g>`;
  b += `<g class="r" ${delay(3)}><text x="40" y="234" font-size="17" font-weight="600" fill="${P.muted}">${T('Пиши мысли, веди дневник и отмечай настроение —')}</text>
<text x="40" y="258" font-size="17" font-weight="600" fill="${P.muted}">${T('быстро, офлайн и без рекламы.')}</text></g>`;

  const feats = [
    ['Мгновенно', 'Автосохранение', '#fff', P.ink, P.muted],
    ['Офлайн', 'Без аккаунтов', P.green, P.onGreen, '#24533A'],
    ['Три темы', 'И своя палитра', P.pink, P.onPink, '#633B48'],
    ['Бесплатно', 'Без рекламы', P.container, P.onContainer, '#4F4566'],
  ];
  feats.forEach(([t, d, fill, c1, c2], i) => {
    const x = 40 + (i % 2) * 262, y = 290 + Math.floor(i / 2) * 86;
    b += `<g class="r" ${delay(4 + i)}>${card(x, y, 250, 74, fill)}
<text x="${x + 20}" y="${y + 32}" font-size="17" font-weight="900" fill="${c1}">${T(t)}</text>
<text x="${x + 20}" y="${y + 54}" font-size="14" font-weight="700" fill="${c2}">${T(d)}</text></g>`;
  });

  b += `<g class="r" ${delay(3)}>${phoneTonal(588, 44, 1.7)}</g>`;
  return svg(W, H, 'Okto Notes — лучшее приложение для заметок и дневника на Android', b);
}

// ---------- КНОПКИ ----------
const androidIcon = (c) => `<g transform="translate(18 14)" fill="none" stroke="${c}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M7 10.5a5 5 0 0 1 10 0v.5H7z"/><path d="M8.6 6.6 7.4 4.8M15.4 6.6l1.2-1.8"/><rect x="7" y="12.5" width="10" height="7" rx="1.6"/></g>`;
const sparkIcon = (c) => `<g transform="translate(18 14)" fill="none" stroke="${c}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 4l1.8 4.6L18.5 10l-4.7 1.4L12 16l-1.8-4.6L5.5 10l4.7-1.4z"/></g>`;

function button(label, icon, fill, color) {
  return (font) => {
    const w = Math.round(52 + label.length * 9.4 + 22);
    return `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="52" viewBox="0 0 ${w} 52" role="img" aria-label="${esc(label)}">
<style>@font-face{font-family:"NUN";src:url(data:font/woff2;base64,${font}) format("woff2");font-weight:200 1000}text{font-family:"NUN",ui-rounded,"Segoe UI",sans-serif}</style>
<rect width="${w}" height="52" rx="18" fill="${fill}"/>
${icon(color)}
<text x="50" y="32" font-size="16" font-weight="800" fill="${color}">${T(label)}</text>
</svg>
`;
  };
}

// ---------- ВОЗМОЖНОСТИ ----------
function features() {
  const W = 880, H = 470;
  let b = sectionHead('Возможности', [['Всё для записей — ', P.ink], ['и ничего лишнего', P.primary]]);
  const items = [
    ['1', 'Заметки', ['Заголовок, текст и 7 цветных меток.', 'Избранное и поиск по всему тексту.'], '#fff', P.ink, P.muted],
    ['2', 'Дневник', ['Настроение 1–5, серия дней подряд', 'и тепловая карта за 4 недели.'], P.container, P.onContainer, '#4F4566'],
    ['3', 'Редактор', ['Автосохранение на каждой букве.', 'Список, задача и время в одно касание.'], P.green, P.onGreen, '#24533A'],
    ['4', 'Приватность', ['Всё хранится только на телефоне.', 'Ноль разрешений, ни облака, ни трекеров.'], P.pink, P.onPink, '#633B48'],
  ];
  items.forEach(([n, t, lines, fill, c1, c2], i) => {
    const x = 40 + (i % 2) * 408, y = 132 + Math.floor(i / 2) * 156;
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, 392, 140, fill, 28)}
<circle cx="${x + 40}" cy="${y + 42}" r="18" fill="${P.primary}"/><text x="${x + 40}" y="${y + 48}" text-anchor="middle" font-size="16" font-weight="900" fill="#fff">${T(n)}</text>
<text x="${x + 70}" y="${y + 49}" font-size="21" font-weight="900" fill="${c1}">${T(t)}</text>
${lines.map((l, k) => `<text x="${x + 24}" y="${y + 92 + k * 22}" font-size="15" font-weight="600" fill="${c2}">${T(l)}</text>`).join('\n')}</g>`;
  });
  return svg(W, H, 'Возможности: заметки, дневник, редактор, приватность', b);
}

// ---------- ТЕМЫ ----------
function themes() {
  const W = 880, H = 520;
  let b = sectionHead('Темы оформления', [['Три темы — ', P.ink], ['выбери свою', P.primary]]);
  const okto = { bg: '#141414', key: '#262626', ink: '#EDEDED', muted: '#8E8E8E', well: '#0A0A0A', wellInk: '#F2F2F2', row: '#1D1D1D', tint: '#2A1E1F', primary: '#EDEDED' };
  const amber = { bg: '#120f0a', key: '#231d12', ink: '#f3e7cf', muted: '#9a8a6a', well: '#050402', wellInk: '#ffb000', row: '#1a160e', tint: '#2a2010', primary: '#ffb000' };
  const cols = [
    ['Цветная', 'Основная тема', (x, y) => phoneTonal(x, y)],
    ['Okto', 'Графит и табло', (x, y) => phoneOkto(x, y, okto)],
    ['Своя', 'Любой цвет', (x, y) => phoneOkto(x, y, amber)],
  ];
  cols.forEach(([t, d, phone], i) => {
    const x = 40 + i * 272, y = 132;
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, 256, 320, '#fff', 28)}${phone(x + 53, y + 20)}
<text x="${x + 24}" y="${y + 284}" font-size="20" font-weight="900" fill="${P.ink}">${T(t)}</text>
<text x="${x + 232}" y="${y + 284}" text-anchor="end" font-size="14" font-weight="700" fill="${P.muted}">${T(d)}</text></g>`;
  });
  const sw = ['#8a8a8a', '#7c5cff', '#3d7bff', '#2ec4d6', '#2fbf8a', '#5cc85a', '#a6d93a', '#ffb000', '#ff7a2e', '#ef5a5f', '#ff5c9a', '#b05cff'];
  sw.forEach((c, i) => { b += `<circle class="r" ${delay(6 + i * 0.3)} cx="${52 + i * 26}" cy="${H - 34}" r="9" fill="${c}"/>`; });
  b += `<text x="${52 + 12 * 26 + 2}" y="${H - 29}" font-size="14" font-weight="700" fill="${P.muted}">${T('12 акцентов + свой оттенок и насыщенность')}</text>`;
  return svg(W, H, 'Темы: Цветная, Okto и Своя', b);
}

// ---------- В ЦИФРАХ ----------
function numbers() {
  const W = 880, H = 290;
  let b = sectionHead('В цифрах', [['Лёгкое, быстрое ', P.ink], ['и приватное', P.primary]]);
  const items = [
    ['1.8', ' МБ', ['размер APK'], P.primary, '#fff', '#E1D9FF'],
    ['0', '', ['разрешений', 'системы'], '#fff', P.ink, P.muted],
    ['0', '', ['рекламы', 'и трекеров'], P.green, P.onGreen, '#24533A'],
    ['3', '', ['темы', 'оформления'], P.pink, P.onPink, '#633B48'],
    ['12', '', ['акцентных', 'цветов'], P.container, P.onContainer, '#4F4566'],
  ];
  const w = (800 - 4 * 12) / 5;
  items.forEach(([n, unit, lines, fill, c1, c2], i) => {
    const x = 40 + i * (w + 12), y = 132;
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, w, 124, fill, 24)}
<text x="${x + 20}" y="${y + 56}" font-size="40" font-weight="900" letter-spacing="-1" fill="${c1}">${T(n)}<tspan font-size="18">${T(unit)}</tspan></text>
${lines.map((l, k) => `<text x="${x + 20}" y="${y + 86 + k * 18}" font-size="14" font-weight="700" fill="${c2}">${T(l)}</text>`).join('\n')}</g>`;
  });
  return svg(W, H, 'В цифрах: 1.8 МБ, 0 разрешений, 0 рекламы, 3 темы, 12 акцентов', b);
}

// ---------- сборка ----------
const files = {
  'hero.svg': hero(),
  'btn-android.svg': button('Скачать для Android', androidIcon, P.primary, '#fff'),
  'btn-new.svg': button('Что нового в 1.1', sparkIcon, P.container, P.onContainer),
  'features.svg': features(),
  'themes.svg': themes(),
  'numbers.svg': numbers(),
};

for (const f of Object.values(files)) f(''); // собрать весь текст для урезания шрифта
const chars = [...new Set(allText + '0123456789')].join('');
const font = (await subsetFont(fs.readFileSync(FONT), chars, { targetFormat: 'woff2' })).toString('base64');

fs.mkdirSync(OUT, { recursive: true });
for (const [name, render] of Object.entries(files)) {
  const out = render(font);
  fs.writeFileSync(path.join(OUT, name), out);
  console.log(name, (out.length / 1024).toFixed(1) + ' KB');
}
console.log('glyphs:', chars.length);
