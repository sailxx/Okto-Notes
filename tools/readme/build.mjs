// Генерирует SVG-баннеры README в стиле Okto с встроенным (урезанным) JetBrains Mono.
// Запуск: cd tools/readme && npm install && node build.mjs
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import subsetFont from 'subset-font';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const OUT = path.join(REPO, 'assets/readme');
const FONT = path.join(REPO, 'app/src/main/res/font/jbmono.ttf');

const ACC = '#c8f560';      // лаймовый акцент Okto Notes
const MUTED = '#8b8b87';
const BODY = '#bdbdb8';
const DIM = '#a8a8a3';

const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
let allText = '';
const T = (s) => { allText += s; return esc(s); };

// ---------- общие куски ----------
const style = (font) => `<style>
@font-face{font-family:"JBM";src:url(data:font/woff2;base64,${font}) format("woff2");font-weight:100 800}
text{font-family:"JBM",ui-monospace,Consolas,monospace}
.lb{font-size:11px;font-weight:600;letter-spacing:2.4px}
.r{animation:in .8s cubic-bezier(.2,.8,.2,1) backwards}
.blink{animation:blink 1s steps(1) infinite}
@keyframes in{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:none}}
@keyframes blink{50%{opacity:.2}}
@media (prefers-reduced-motion:reduce){.r,.blink{animation:none}}
</style>`;

const frame = (w, h) => `<defs><clipPath id="card"><rect width="${w}" height="${h}" rx="22"/></clipPath>
<pattern id="grid" width="22" height="22" patternUnits="userSpaceOnUse"><circle cx="1.5" cy="1.5" r="1" fill="#ffffff" fill-opacity=".06"/></pattern></defs>
<g clip-path="url(#card)"><rect width="${w}" height="${h}" fill="#111111"/><rect width="${w}" height="${h}" fill="url(#grid)"/></g>
<rect x=".5" y=".5" width="${w - 1}" height="${h - 1}" rx="21.5" fill="none" stroke="#ffffff" stroke-opacity=".1"/>`;

const svg = (w, h, label, body) => (font) =>
  `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}" role="img" aria-label="${esc(label)}">\n${style(font)}\n${frame(w, h)}\n${body}\n</svg>\n`;

/** Строка из кусков [текст, цвет]. */
const spans = (parts) => parts.map(([s, c]) => `<tspan fill="${c}">${T(s)}</tspan>`).join('');
const delay = (i) => `style="animation-delay:${(i * 0.1).toFixed(2)}s"`;
const panel = (x, y, w, h, rx = 12) => `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${rx}" fill="#ffffff" fill-opacity=".04" stroke="#ffffff" stroke-opacity=".1"/>`;
const sectionHead = (label, parts) => `<g class="r"><rect x="40" y="38" width="9" height="9" rx="2" fill="${ACC}"/><text x="58" y="47" class="lb" fill="${MUTED}">${T(label)}</text></g>
<g class="r" font-weight="800" letter-spacing="-.5" ${delay(1)}><text x="40" y="84" font-size="22">${spans(parts)}</text></g>`;

// ---------- HERO ----------
function hero() {
  const W = 880, H = 481;
  let b = '';
  b += `<g class="r"><circle cx="48" cy="43" r="7" fill="#e9e9e6"/><text x="64" y="50" font-size="24" font-weight="800" letter-spacing="-.8" fill="#ffffff">${T('okto notes')}</text><text x="840" y="48" text-anchor="end" class="lb" fill="${MUTED}">${T('v1.1')}</text></g>`;
  b += `<text x="40" y="98" class="lb r" fill="${MUTED}" ${delay(1)}>${T('ЗАМЕТКИ · ДНЕВНИК · НАСТРОЕНИЕ · ТЕМЫ')}</text>`;
  b += `<g class="r" font-weight="800" letter-spacing="-.8" ${delay(2)}>
<text x="40" y="142" font-size="28">${spans([['Лучшее приложение', '#ffffff']])}</text>
<text x="40" y="180" font-size="28">${spans([['для ', '#ffffff'], ['заметок', ACC], [' и дневника.', '#ffffff']])}</text></g>`;
  b += `<g class="r" ${delay(3)}><text x="40" y="224" font-size="14" fill="${BODY}">${T('Пиши мысли, веди дневник и отмечай')}</text>
<text x="40" y="246" font-size="14" fill="${BODY}">${T('настроение — быстро, офлайн, без рекламы.')}</text></g>`;

  // Табло дневника справа
  const wx = 500, wy = 104, ww = 340, wh = 218;
  b += `<g class="r" ${delay(3.5)}><rect x="${wx}" y="${wy}" width="${ww}" height="${wh}" rx="12" fill="#000" stroke="#ffffff" stroke-opacity=".12"/>`;
  b += `<circle cx="${wx + 18}" cy="${wy + 20}" r="3.5" fill="${ACC}"/><text x="${wx + 28}" y="${wy + 24}" font-size="9.5" font-weight="700" letter-spacing="2" fill="${MUTED}">${T('ДНЕВНИК · СЕРИЯ')}</text>`;
  b += `<text x="${wx + 18}" y="${wy + 84}" font-size="56" font-weight="800" letter-spacing="-2" fill="#ffffff" fill-opacity=".06">${T('88')}</text>`;
  b += `<text x="${wx + 18}" y="${wy + 84}" font-size="56" font-weight="800" letter-spacing="-2" fill="${ACC}">${T('05')}</text>`;
  b += `<text x="${wx + 18}" y="${wy + 106}" font-size="9" letter-spacing="1.6" fill="${MUTED}">${T('ДНЕЙ ПОДРЯД')}</text>`;
  // тепловая карта 4×7
  const heat = '0110100' + '1011010' + '0101111' + '1110000'; // последний ряд — текущая неделя (СР — сегодня)
  for (let r = 0; r < 4; r++) {
    for (let cIdx = 0; cIdx < 7; cIdx++) {
      const i = r * 7 + cIdx;
      const x = wx + 196 + cIdx * 18, y = wy + 40 + r * 18;
      const future = r === 3 && cIdx > 2;
      const on = heat[i] === '1';
      if (future) b += `<rect x="${x}" y="${y}" width="14" height="14" rx="3" fill="none" stroke="#ffffff" stroke-opacity=".06"/>`;
      else b += `<rect x="${x}" y="${y}" width="14" height="14" rx="3" fill="${on ? ACC : '#ffffff'}" fill-opacity="${on ? 1 : 0.06}"/>`;
      if (r === 3 && cIdx === 2) b += `<rect x="${x - 2.5}" y="${y - 2.5}" width="19" height="19" rx="4.5" fill="none" stroke="${MUTED}"/>`;
    }
  }
  // настроение за неделю
  b += `<line x1="${wx + 18}" y1="${wy + 124}" x2="${wx + ww - 18}" y2="${wy + 124}" stroke="#ffffff" stroke-opacity=".08"/>`;
  b += `<text x="${wx + 18}" y="${wy + 142}" font-size="9.5" font-weight="700" letter-spacing="2" fill="${MUTED}">${T('НАСТРОЕНИЕ · НЕДЕЛЯ')}</text><text x="${wx + ww - 18}" y="${wy + 142}" text-anchor="end" font-size="9.5" font-weight="700" letter-spacing="1" fill="${MUTED}">${T('4/5')}</text>`;
  const moods = [3, 4, 5, 0, 0, 0, 0];
  const days = ['ПН', 'ВТ', 'СР', 'ЧТ', 'ПТ', 'СБ', 'ВС'];
  const base = wy + 196;
  moods.forEach((m, i) => {
    const x = wx + 18 + i * 44;
    const h = m ? m * 8 : 3;
    const fill = m === 0 ? '#ffffff' : (i === 2 ? ACC : ACC);
    const op = m === 0 ? 0.08 : (i === 2 ? 1 : 0.35);
    b += `<rect x="${x}" y="${base - h}" width="34" height="${h}" rx="2" fill="${fill}" fill-opacity="${op}"/>`;
    b += `<text x="${x + 17}" y="${base + 14}" text-anchor="middle" font-size="9" ${i === 2 ? `font-weight="800" fill="${ACC}"` : `fill="${MUTED}"`}>${T(days[i])}</text>`;
  });
  b += `</g>`;

  // карточки преимуществ
  const cards = [
    ['Мгновенно', 'Открыл — пишешь.', 'Автосохранение'],
    ['Офлайн', 'Без интернета,', 'аккаунтов и облака'],
    ['Три темы', 'Цветная, Okto', 'и своя палитра'],
    ['Бесплатно', 'Без рекламы,', 'подписок и трекеров'],
  ];
  cards.forEach(([t, l1, l2], i) => {
    const x = 40 + i * 203;
    b += `<g class="r" ${delay(4.5 + i)}>${panel(x, 346, 191, 95)}<text x="${x + 16}" y="376" font-size="14" font-weight="700" fill="${ACC}">${T(t)}</text>
<text x="${x + 16}" y="403" font-size="12" fill="${DIM}">${T(l1)}</text><text x="${x + 16}" y="420" font-size="12" fill="${DIM}">${T(l2)}</text></g>`;
  });
  return svg(W, H, 'Okto Notes — лучшее приложение для заметок и дневника на Android', b);
}

// ---------- КНОПКИ ----------
const androidIcon = (color) => `<g transform="translate(14 12)" fill="none" stroke="${color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M7 10.5a5 5 0 0 1 10 0v.5H7z"/><path d="M8.6 6.6 7.4 4.8M15.4 6.6l1.2-1.8"/><rect x="7" y="12.5" width="10" height="7" rx="1.6"/></g>`;
const sparkIcon = (color) => `<g transform="translate(14 12)" fill="none" stroke="${color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5 18 18M6 18l2.5-2.5M15.5 8.5 18 6"/></g>`;

function button(label, icon, fill, iconColor, textColor = '#141414') {
  return (font) => {
    const w = Math.round(44 + label.length * 9 + 18);
    return `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="48" viewBox="0 0 ${w} 48" role="img" aria-label="${esc(label)}">
<style>@font-face{font-family:"JBM";src:url(data:font/woff2;base64,${font}) format("woff2");font-weight:100 800}text{font-family:"JBM",ui-monospace,Consolas,monospace}</style>
<rect x=".75" y=".75" width="${w - 1.5}" height="46.5" rx="12" fill="${fill}" stroke="#141414" stroke-width="1.5"/>
${icon(iconColor)}
<text x="44" y="30" font-size="15" font-weight="800" letter-spacing="-.2" fill="${textColor}">${T(label)}</text>
</svg>
`;
  };
}

// ---------- ВОЗМОЖНОСТИ ----------
function features() {
  const W = 880, H = 430;
  let b = sectionHead('ВОЗМОЖНОСТИ', [['Всё для записей', '#ffffff'], [' — и ничего', ACC], [' лишнего.', '#ffffff']]);
  const items = [
    ['01', 'Заметки', ['Заголовок, текст и 7 цветных меток.', 'Избранное — долгим нажатием, поиск', 'по всему тексту.']],
    ['02', 'Дневник', ['Запись на каждый день, настроение', '1–5, серия дней подряд и тепловая', 'карта за 4 недели.']],
    ['03', 'Редактор', ['Автосохранение на каждой букве.', 'Быстрые вставки: • список, ☐ задача,', 'текущее время; счётчик слов.']],
    ['04', 'Приватность', ['Всё хранится только на телефоне.', 'Ноль разрешений, ни облака,', 'ни аккаунтов, ни трекеров.']],
  ];
  items.forEach(([n, t, lines], i) => {
    const col = i % 2, row = Math.floor(i / 2);
    const x = 40 + col * 408, y = 114 + row * 148;
    b += `<g class="r" ${delay(2 + i)}>${panel(x, y, 392, 132, 14)}
<text x="${x + 20}" y="${y + 34}" font-size="11" font-weight="700" letter-spacing="2" fill="${MUTED}">${T(n)}</text>
<text x="${x + 52}" y="${y + 35}" font-size="17" font-weight="800" letter-spacing="-.4" fill="#ffffff">${T(t)}</text>
${lines.map((l, k) => `<text x="${x + 20}" y="${y + 66 + k * 19}" font-size="12" fill="${BODY}">${T(l)}</text>`).join('\n')}</g>`;
  });
  return svg(W, H, 'Возможности: заметки, дневник, редактор, приватность', b);
}

// ---------- ТЕМЫ ----------
function phoneTonal(x, y) {
  let s = `<rect x="${x}" y="${y}" width="150" height="228" rx="18" fill="#F7F2FA"/>`;
  s += `<rect x="${x + 10}" y="${y + 14}" width="100" height="16" rx="8" fill="#ECE6F0"/><circle cx="${x + 130}" cy="${y + 22}" r="8" fill="#4A2FC0"/>`;
  s += `<rect x="${x + 10}" y="${y + 40}" width="74" height="9" rx="3" fill="#1D1B20"/>`;
  s += `<rect x="${x + 10}" y="${y + 58}" width="130" height="40" rx="12" fill="#E8DEFF"/>`;
  for (let i = 0; i < 7; i++) {
    const fill = i < 2 ? '#4A2FC0' : i === 2 ? '#FFD8E4' : '#F7F2FA';
    s += `<rect x="${x + 16 + i * 17.5}" y="${y + 76}" width="14" height="14" rx="5" fill="${fill}"/>`;
  }
  s += `<rect x="${x + 10}" y="${y + 106}" width="62" height="40" rx="10" fill="#ffffff"/><rect x="${x + 78}" y="${y + 106}" width="62" height="28" rx="10" fill="#FFD8E4"/>`;
  s += `<rect x="${x + 10}" y="${y + 152}" width="62" height="28" rx="10" fill="#D7F5E1"/><rect x="${x + 78}" y="${y + 140}" width="62" height="34" rx="10" fill="#ffffff"/>`;
  s += `<rect x="${x + 98}" y="${y + 180}" width="42" height="18" rx="7" fill="#4A2FC0"/>`;
  s += `<rect x="${x}" y="${y + 204}" width="150" height="24" rx="18" fill="#EFE8F7"/><rect x="${x}" y="${y + 204}" width="150" height="12" fill="#EFE8F7"/><rect x="${x + 28}" y="${y + 210}" width="24" height="10" rx="5" fill="#E8DEFF"/>`;
  return s;
}
function phoneOkto(x, y, p) {
  let s = `<rect x="${x}" y="${y}" width="150" height="228" rx="18" fill="${p.bg}" stroke="#ffffff" stroke-opacity=".08"/>`;
  s += `<circle cx="${x + 16}" cy="${y + 18}" r="3" fill="${p.key}"/><rect x="${x + 24}" y="${y + 15}" width="34" height="6" rx="2" fill="${p.ink}" fill-opacity=".8"/><rect x="${x + 100}" y="${y + 15}" width="38" height="6" rx="2" fill="${p.muted}" fill-opacity=".6"/>`;
  s += `<rect x="${x + 10}" y="${y + 32}" width="70" height="11" rx="2" fill="${p.ink}"/>`;
  for (let i = 0; i < 2; i++) {
    const wx = x + 10 + i * 67;
    s += `<rect x="${wx}" y="${y + 52}" width="63" height="44" rx="6" fill="${p.well}" stroke="#ffffff" stroke-opacity=".06"/>`;
    s += `<text x="${wx + 7}" y="${y + 86}" font-size="20" font-weight="800" fill="${p.wellInk}" fill-opacity=".08">${T('88')}</text><text x="${wx + 7}" y="${y + 86}" font-size="20" font-weight="800" fill="${p.wellInk}">${T(i ? '05' : '12')}</text>`;
  }
  for (let i = 0; i < 4; i++) {
    s += `<rect x="${x + 10}" y="${y + 104 + i * 22}" width="130" height="18" rx="5" fill="${i === 0 ? p.tint : p.row}" stroke="#ffffff" stroke-opacity=".05"/>`;
    s += `<rect x="${x + 16}" y="${y + 110 + i * 22}" width="${60 - i * 8}" height="5" rx="2" fill="${p.ink}" fill-opacity=".85"/><rect x="${x + 118}" y="${y + 110 + i * 22}" width="16" height="5" rx="2" fill="${p.muted}" fill-opacity=".7"/>`;
  }
  s += `<line x1="${x}" y1="${y + 196}" x2="${x + 150}" y2="${y + 196}" stroke="#ffffff" stroke-opacity=".08"/>`;
  s += `<rect x="${x + 8}" y="${y + 202}" width="32" height="18" rx="4" fill="${p.row}"/><rect x="${x + 44}" y="${y + 202}" width="32" height="18" rx="4" fill="${p.key}"/><rect x="${x + 80}" y="${y + 202}" width="62" height="18" rx="4" fill="${p.primary}"/>`;
  return s;
}
function themes() {
  const W = 880, H = 470;
  let b = sectionHead('ТЕМЫ ОФОРМЛЕНИЯ', [['Три темы', '#ffffff'], [' — выбери свою.', ACC]]);
  const okto = { bg: '#141414', key: '#262626', ink: '#EDEDED', muted: '#8E8E8E', well: '#0A0A0A', wellInk: '#F2F2F2', row: '#1D1D1D', tint: '#2A1E1F', primary: '#EDEDED' };
  const amber = { bg: '#120f0a', key: '#231d12', ink: '#f3e7cf', muted: '#9a8a6a', well: '#050402', wellInk: '#ffb000', row: '#1a160e', tint: '#2a2010', primary: '#ffb000' };
  const cols = [
    ['Цветная', ['Основная тема:', 'пастельные карточки'], (x, y) => phoneTonal(x, y)],
    ['Okto', ['Графит, табло', 'с цифрами и клавиши'], (x, y) => phoneOkto(x, y, okto)],
    ['Своя', ['Любой цвет, светлая', 'или тёмная, 2 стиля'], (x, y) => phoneOkto(x, y, amber)],
  ];
  cols.forEach(([t, lines, phone], i) => {
    const x = 40 + i * 272, y = 114;
    b += `<g class="r" ${delay(2 + i)}>${panel(x, y, 256, 316, 14)}${phone(x + 53, y + 18)}
<text x="${x + 20}" y="${y + 272}" font-size="15" font-weight="800" fill="#ffffff">${T(t)}</text>
<text x="${x + 236}" y="${y + 272}" text-anchor="end" font-size="11" fill="${MUTED}">${T(lines[0])}</text>
<text x="${x + 236}" y="${y + 290}" text-anchor="end" font-size="11" fill="${MUTED}">${T(lines[1])}</text></g>`;
  });
  // палитра акцентов своей темы
  const sw = ['#8a8a8a', '#7c5cff', '#3d7bff', '#2ec4d6', '#2fbf8a', '#5cc85a', '#c8f560', '#ffb000', '#ff7a2e', '#ef5a5f', '#ff5c9a', '#b05cff'];
  sw.forEach((c, i) => { b += `<circle class="r" ${delay(5 + i * 0.2)} cx="${46 + i * 22}" cy="${H - 24}" r="7" fill="${c}"/>`; });
  b += `<text x="${46 + 12 * 22 + 4}" y="${H - 20}" font-size="11" fill="${MUTED}">${T('12 акцентов + свой оттенок и насыщенность')}</text>`;
  return svg(W, H, 'Темы: Цветная, Okto и Своя', b);
}

// ---------- В ЦИФРАХ ----------
function numbers() {
  const W = 880, H = 262;
  let b = sectionHead('В ЦИФРАХ', [['Лёгкое,', '#ffffff'], [' быстрое', ACC], [' и приватное.', '#ffffff']]);
  const items = [
    ['1.8', ' МБ', ['размер APK']],
    ['0', '', ['разрешений', 'системы']],
    ['0', '', ['рекламы', 'и трекеров']],
    ['3', '', ['темы', 'оформления']],
    ['12', '', ['акцентных', 'цветов']],
  ];
  const w = (800 - 4 * 12) / 5;
  items.forEach(([n, unit, lines], i) => {
    const x = 40 + i * (w + 12);
    b += `<g class="r" ${delay(2 + i)}>${panel(x, 114, w, 112)}
<text x="${x + 16}" y="${114 + 50}" font-size="32" font-weight="800" letter-spacing="-1" fill="${i === 0 ? ACC : '#ffffff'}">${T(n)}<tspan font-size="15" fill="#ffffff">${T(unit)}</tspan></text>
${lines.map((l, k) => `<text x="${x + 16}" y="${114 + 76 + k * 15}" font-size="11" fill="${BODY}">${T(l)}</text>`).join('\n')}</g>`;
  });
  return svg(W, H, 'В цифрах: 1.8 МБ, 0 разрешений, 0 рекламы, 3 темы, 12 акцентов', b);
}

// ---------- сборка ----------
const files = {
  'hero.svg': hero(),
  'btn-android.svg': button('Скачать для Android ↓', androidIcon, '#141414', ACC, '#ffffff'),
  'btn-new.svg': button('Что нового в 1.1 →', sparkIcon, '#ffffff', '#141414'),
  'features.svg': features(),
  'themes.svg': themes(),
  'numbers.svg': numbers(),
};

// Сначала собираем текст (функции уже вызваны выше и наполнили allText), кнопки — при рендере, поэтому прогоняем вхолостую.
for (const f of Object.values(files)) f('');
const chars = [...new Set(allText + '0123456789')].join('');
const font = (await subsetFont(fs.readFileSync(FONT), chars, { targetFormat: 'woff2' })).toString('base64');

fs.mkdirSync(OUT, { recursive: true });
for (const [name, render] of Object.entries(files)) {
  const out = render(font);
  fs.writeFileSync(path.join(OUT, name), out);
  console.log(name, (out.length / 1024).toFixed(1) + ' KB');
}
console.log('glyphs:', chars.length);
