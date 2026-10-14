// Генерирует SVG-плашки README в стиле Okto Calc: чёрный фон, свечения и акцент жёлтого цвета Apple Notes.
// Шрифты Golos Text и JetBrains Mono встраиваются урезанными под каждый язык (тексты — strings.json).
// Запуск: cd tools/readme && npm install && node build.mjs
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createKit, ICONS } from './kit.mjs';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const OUT = path.join(REPO, 'assets/readme');
const VERSION = fs.readFileSync(path.join(REPO, 'app/build.gradle.kts'), 'utf8').match(/versionName = "([^"]+)"/)[1];
const STR = JSON.parse(fs.readFileSync(path.join(REPO, 'tools/readme/strings.json'), 'utf8'));

const Y = '#FFD60A'; // жёлтый Apple Notes (systemYellow, тёмная тема)
const K = createKit({
  golos: path.join(REPO, 'app/src/main/res/font/golos.ttf'),
  mono: path.join(REPO, 'app/src/main/res/font/jbmono.ttf'),
  accent: { hi: Y, grad: ['#FFE566', '#FFC300'], on: '#1A1400', glow: Y, glow2: '#FF9F0A', btn: ['#FFE04D', '#F5C400'] },
});
const { P, T, card, chip, sectionHead, badge, lines, wrap, one, fitSize, fitText, delay, svg } = K;

// Логотип — тот же рисунок, что у иконки приложения (ic_launcher): жёлтая плитка с линовкой и осьминог с карандашом
export const logo = (x, y, s) => `<g transform="translate(${x} ${y}) scale(${s / 104})">
<rect width="104" height="104" rx="24" fill="url(#ly)"/><path d="M14 80h76M14 90h76" stroke="#000" stroke-opacity=".1" stroke-width="2"/>
<g transform="translate(2 4)"><path d="M38,52q-6,12 -14,14M46,56q-2,14 -8,20M54,56q2,14 8,20" fill="none" stroke="#161616" stroke-width="6" stroke-linecap="round"/>
<g transform="translate(58 48) rotate(45)"><path d="M3,-4h27v8h-27z" fill="#fff"/><path d="M3,-1h27v2h-27z" fill="#E6E6E6"/><path d="M30,-4L38,0L30,4z" fill="#F5D0A9"/><path d="M35,-1.5L38,0L35,1.5z" fill="#161616"/></g>
<circle cx="50" cy="38" r="18" fill="#161616"/><circle cx="44" cy="36" r="3.2" fill="${Y}"/><circle cx="56" cy="36" r="3.2" fill="${Y}"/></g></g>`;
const LOGO_DEFS = '<linearGradient id="ly" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#FFE15A"/><stop offset="1" stop-color="#FFCC00"/></linearGradient>';

// ---------- телефон: тема «Своя» в оформлении Okto с жёлтым акцентом ----------
function phoneNotes(x, y, t, k = 1) {
  const notes = t.features.items[0][0], diary = t.features.items[1][0];
  let s = `<rect width="150" height="228" rx="20" fill="#141414" stroke="#2e2e2e"/>`;
  s += `<text x="12" y="30" font-size="15" font-weight="800" fill="#F2F2F2">${T(notes)}</text>`;
  s += `<circle cx="133" cy="25" r="7" fill="#262626"/><path d="M130.5 25h5M133 22.5v5" stroke="${Y}" stroke-width="1.4" stroke-linecap="round"/>`;
  // полоса недели дневника: настроение по дням
  s += `<rect x="10" y="40" width="130" height="38" rx="9" fill="#0A0A0A" stroke="#1F1F1F"/>`;
  s += `<text class="m" x="17" y="52" font-size="6.5" font-weight="600" letter-spacing=".6" fill="#8A8A8A">${T(diary.toUpperCase())}</text>`;
  const mood = [0.35, 0.6, 1, 0.8, 1, 0.5, 0];
  mood.forEach((m, i) => { s += `<rect x="${17 + i * 17.5}" y="57" width="13" height="13" rx="4" fill="${m ? Y : '#262626'}" fill-opacity="${m || 1}"/>`; });
  // заметки
  const rows = [[86, 42, true], [134, 28, false], [168, 28, false]];
  rows.forEach(([ry, h, cur], i) => {
    s += `<rect x="10" y="${ry}" width="130" height="${h}" rx="8" fill="#1D1D1D"/>`;
    s += `<rect x="18" y="${ry + 8}" width="${[64, 52, 70][i]}" height="5" rx="2" fill="#EDEDED"/><circle cx="131" cy="${ry + 10.5}" r="2.6" fill="${[Y, '#5C5C5C', Y][i]}"/>`;
    s += `<rect x="18" y="${ry + 18}" width="${[100, 84, 76][i]}" height="3.5" rx="1.7" fill="#6B6B6B"/>`;
    if (cur) s += `<rect x="18" y="${ry + 27}" width="58" height="3.5" rx="1.7" fill="#6B6B6B"/><rect class="blink" x="79" y="${ry + 25}" width="1.6" height="8" fill="${Y}"/>`;
  });
  s += `<rect x="98" y="201" width="42" height="18" rx="7" fill="${Y}"/><rect class="pulse" x="98" y="201" width="42" height="18" rx="7" fill="#fff"/><path d="M114.5 210h9M119 205.5v9" stroke="#1A1400" stroke-width="1.8" stroke-linecap="round"/>`;
  return `<g transform="translate(${x} ${y}) scale(${k})">${s}</g>`;
}

// ---------- телефоны тем (значения палитр из ui/Theme.kt) ----------
const TONAL = { bg: '#F7F2FA', surfaceHi: '#ECE6F0', ink: '#1D1B20', primary: '#4A2FC0', container: '#E8DEFF', pink: '#FFD8E4', green: '#D7F5E1', stroke: '#E4DAEE' };
function phoneTonal(x, y) {
  const p = TONAL;
  let s = `<rect width="150" height="228" rx="20" fill="${p.bg}" stroke="${p.stroke}"/>`;
  s += `<rect x="10" y="14" width="100" height="16" rx="8" fill="${p.surfaceHi}"/><circle cx="130" cy="22" r="8" fill="${p.primary}"/>`;
  s += `<rect x="10" y="40" width="74" height="9" rx="3" fill="${p.ink}"/>`;
  s += `<rect x="10" y="58" width="130" height="40" rx="12" fill="${p.container}"/>`;
  for (let i = 0; i < 7; i++) s += `<rect x="${16 + i * 17.5}" y="76" width="14" height="14" rx="5" fill="${i < 2 ? p.primary : i === 2 ? p.pink : p.bg}"/>`;
  s += `<rect x="10" y="106" width="62" height="40" rx="10" fill="#fff"/><rect x="78" y="106" width="62" height="28" rx="10" fill="${p.pink}"/>`;
  s += `<rect x="10" y="152" width="62" height="28" rx="10" fill="${p.green}"/><rect x="78" y="140" width="62" height="34" rx="10" fill="#fff"/>`;
  s += `<rect x="98" y="180" width="42" height="18" rx="7" fill="${p.primary}"/>`;
  s += `<path d="M0 204h150v4a20 20 0 0 1-20 20H20A20 20 0 0 1 0 208z" fill="#EFE8F7"/><rect x="28" y="210" width="24" height="10" rx="5" fill="${p.container}"/>`;
  return `<g transform="translate(${x} ${y})">${s}</g>`;
}
function phoneOkto(x, y, p) {
  let s = `<rect width="150" height="228" rx="20" fill="${p.bg}" stroke="${p.edge}"/>`;
  s += `<circle cx="16" cy="18" r="3" fill="${p.key}"/><rect x="24" y="15" width="34" height="6" rx="2" fill="${p.ink}" fill-opacity=".8"/><rect x="100" y="15" width="38" height="6" rx="2" fill="${p.muted}" fill-opacity=".6"/>`;
  s += `<rect x="10" y="32" width="70" height="11" rx="2" fill="${p.ink}"/>`;
  for (let i = 0; i < 2; i++) {
    const wx = 10 + i * 67;
    s += `<rect x="${wx}" y="52" width="63" height="44" rx="6" fill="${p.well}"/>`;
    for (let d = 0; d < 2; d++) s += `<rect x="${wx + 8 + d * 14}" y="66" width="11" height="20" rx="2" fill="${p.wellInk}"/>`;
  }
  for (let i = 0; i < 4; i++) {
    s += `<rect x="10" y="${104 + i * 22}" width="130" height="18" rx="5" fill="${i === 0 ? p.tint : p.row}"/>`;
    s += `<rect x="16" y="${110 + i * 22}" width="${60 - i * 8}" height="5" rx="2" fill="${p.ink}" fill-opacity=".85"/><rect x="118" y="${110 + i * 22}" width="16" height="5" rx="2" fill="${p.muted}" fill-opacity=".7"/>`;
  }
  s += `<rect x="8" y="202" width="32" height="18" rx="4" fill="${p.row}"/><rect x="44" y="202" width="32" height="18" rx="4" fill="${p.key}"/><rect x="80" y="202" width="62" height="18" rx="4" fill="${p.primary}"/>`;
  return `<g transform="translate(${x} ${y})">${s}</g>`;
}
const OKTO = { bg: '#141414', edge: '#2e2e2e', key: '#262626', ink: '#EDEDED', muted: '#8E8E8E', well: '#0A0A0A', wellInk: '#F2F2F2', row: '#1D1D1D', tint: '#2A1E1F', primary: '#EDEDED' };
const LIGHT = { bg: '#F6F6F6', edge: '#DADADA', key: '#DADADA', ink: '#141414', muted: '#6B6B6B', well: '#EAEAEA', wellInk: '#141414', row: '#FFFFFF', tint: '#F6E7E7', primary: '#141414' };
const OWN = { bg: '#120f0a', edge: '#2a2418', key: '#231d12', ink: '#f3e7cf', muted: '#9a8a6a', well: '#050402', wellInk: Y, row: '#1a160e', tint: '#2a2010', primary: Y };

// ---------- HERO ----------
function hero(t) {
  const W = 880, H = 480;
  let b = `<g class="r">${logo(40, 36, 36)}<text x="88" y="62" font-size="22" font-weight="800" letter-spacing="-.3">${T('Okto Notes')}</text></g>`;
  b += `<g class="r" ${delay(1)}>${chip(238, 39, 'v' + VERSION).s}</g>`;
  const ts = fitSize(t.hero.title, 530, 38, 800);
  b += `<g class="r" ${delay(2)}>${lines(40, 146, t.hero.title.map(one), { size: ts, weight: 800, lh: 46, spacing: '-1' })}</g>`;
  const ss = fitSize(t.hero.sub, 530, 16, 500);
  b += `<g class="r" ${delay(3)}>${lines(40, 232, t.hero.sub.map(one), { size: ss, weight: 500, lh: 24, fill: P.muted })}</g>`;
  t.hero.cards.forEach(([a, d], i) => {
    const x = 40 + (i % 2) * 262, y = 290 + Math.floor(i / 2) * 86;
    b += `<g class="r" ${delay(4 + i)}>${card(x, y, 250, 74)}
<circle cx="${x + 22}" cy="${y + 27}" r="4" fill="${Y}"/>
${fitText(x + 34, y + 32, a, 200, { size: 16, weight: 700 })}
${fitText(x + 20, y + 54, d, 214, { size: 13, weight: 500, fill: P.muted })}</g>`;
  });
  b += `<g class="r" ${delay(3)}><ellipse cx="722" cy="244" rx="150" ry="190" fill="url(#g1)"/>${phoneNotes(594, 46, t, 1.7)}</g>`;
  return svg(W, H, t.hero.alt, b, LOGO_DEFS);
}

// ---------- ВОЗМОЖНОСТИ ----------
function features(t) {
  const W = 880;
  const head = sectionHead(t.features.chip, t.features.title);
  let b = head.s;
  const items = t.features.items.map(([title, ls]) => [title, ls.flatMap((l) => wrap(l, 344, 15, 500))]);
  const ch = 92 + Math.max(...items.map(([, ls]) => ls.length)) * 22 - 4;
  items.forEach(([title, ls], i) => {
    const x = 40 + (i % 2) * 408, y = head.y + Math.floor(i / 2) * (ch + 16);
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, 392, ch, 20)}${badge(x + 40, y + 42, i + 1)}
${fitText(x + 68, y + 48, title, 300, { size: 20, weight: 700 })}
${lines(x + 24, y + 92, ls, { size: 15, weight: 500, lh: 22, fill: P.muted })}</g>`;
  });
  return svg(W, head.y + 2 * ch + 16 + 36, t.features.alt, b);
}

// ---------- ТЕМЫ ----------
function themes(t) {
  const W = 880;
  const head = sectionHead(t.themes.chip, t.themes.title);
  let b = head.s;
  const phones = [(x, y) => phoneTonal(x, y), (x, y) => phoneOkto(x, y, OKTO), (x, y) => phoneOkto(x, y, LIGHT), (x, y) => phoneOkto(x, y, OWN)];
  t.themes.cols.forEach(([name, d], i) => {
    const x = 40 + i * 204, y = head.y;
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, 188, 326, 20)}${phones[i](x + 19, y + 20)}
<text class="m" x="${x + 20}" y="${y + 272}" font-size="11" font-weight="600" letter-spacing="1" fill="${P.dim}">${T(String(i + 1).padStart(2, '0'))}</text>
${fitText(x + 44, y + 273, name, 130, { size: 19, weight: 800 })}
${fitText(x + 20, y + 300, d, 150, { size: 14, weight: 500, fill: P.muted })}</g>`;
  });
  const y2 = head.y + 326 + 34;
  const sw = ['#8a8a8a', '#7c5cff', '#3d7bff', '#2ec4d6', '#2fbf8a', '#5cc85a', '#a6d93a', Y, '#ff7a2e', '#ef5a5f', '#ff5c9a', '#b05cff'];
  sw.forEach((c, i) => { b += `<circle class="r" ${delay(6 + i * 0.3)} cx="${52 + i * 26}" cy="${y2}" r="9" fill="${c}"${c === Y ? ` stroke="#fff" stroke-width="2"` : ''}/>`; });
  b += fitText(52 + 12 * 26 + 2, y2 + 5, t.themes.accents, 480, { size: 14, weight: 500, fill: P.muted });
  return svg(W, y2 + 38, t.themes.alt, b);
}

// ---------- В ЦИФРАХ ----------
function numbers(t) {
  const W = 880;
  const head = sectionHead(t.numbers.chip, t.numbers.title);
  let b = head.s;
  const w = (800 - 4 * 12) / 5;
  t.numbers.items.forEach(([n, unit, ls], i) => {
    const x = 40 + i * (w + 12), y = head.y;
    b += `<g class="r" ${delay(2 + i)}>${card(x, y, w, 124, 18)}
<text class="m" x="${x + 20}" y="${y + 56}" font-size="38" font-weight="700" letter-spacing="-1" fill="url(#tg)">${T(n)}<tspan font-size="16" fill="${P.muted}">${T(unit)}</tspan></text>
${ls.map((l, k) => fitText(x + 20, y + 86 + k * 18, l, w - 30, { size: 13, weight: 500, fill: P.muted })).join('\n')}</g>`;
  });
  return svg(W, head.y + 124 + 34, t.numbers.alt, b);
}

// ---------- сборка ----------
const langs = {};
for (const [lang, t] of Object.entries(STR)) {
  langs[lang] = () => ({
    [`hero-${lang}.svg`]: hero(t),
    [`btn-android-${lang}.svg`]: K.button(t.buttons.android, ICONS.android, true),
    [`btn-new-${lang}.svg`]: K.button(t.buttons.whatsNew, ICONS.spark, false),
    [`features-${lang}.svg`]: features(t),
    [`themes-${lang}.svg`]: themes(t),
    [`numbers-${lang}.svg`]: numbers(t),
  });
}
await K.build(OUT, langs, 'vOktoNotes.');
