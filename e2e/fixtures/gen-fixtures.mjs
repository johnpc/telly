#!/usr/bin/env node
// Deterministic IPTV fixture generator for telly e2e / reference capture.
// Usage: node gen-fixtures.mjs [startEpochMillis]
//   startEpochMillis: "now" anchor. EPG covers now-6h .. now+24h. Defaults to Date.now().
// Writes playlist.m3u and epg.xml next to this script.

import { readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const BASE = "http://10.0.2.2:8090";
const DIR = dirname(fileURLToPath(import.meta.url));
const anchor = Number(process.argv[2]) || Date.now();

// ---- deterministic PRNG (mulberry32) seeded per channel ------------------
function rng(seed) {
  let a = seed >>> 0;
  return () => {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

// ---- channel plan: 30 channels over 5 groups, reusing the 5 streams ------
const GROUPS = {
  News: { stream: "news-one", titles: ["Morning Report", "World News Now", "The Daily Brief", "Politics Tonight", "Business Hour", "Weather Watch", "Global Update", "Newsroom Live"], cat: "News" },
  Sports: { stream: "sports-arena", titles: ["Match of the Day", "Championship Live", "Sports Center", "The Halftime Show", "Boxing Classics", "Motorsport Weekly", "Tennis Masters", "Extreme Games"], cat: "Sports" },
  Movies: { stream: "movie-house", titles: ["The Long Road Home", "Midnight in Berlin", "Chasing Shadows", "The Last Stand", "Summer of '89", "Iron Harvest", "The Quiet Hour", "City of Glass"], cat: "Movie / Drama" },
  Kids: { stream: "kids-zone", titles: ["Puppy Patrol", "Space Cadets", "The Magic Treehouse", "Dino Explorers", "Robot Friends", "Fairy Tale Theater", "Junior Chefs", "Ocean Adventures"], cat: "Children's / Youth" },
  Music: { stream: "music-box", titles: ["Top 40 Countdown", "Classic Rock Block", "Jazz After Dark", "Electronic Sessions", "Country Roads", "Hip Hop Nation", "Symphony Hall", "Indie Discoveries"], cat: "Music / Ballet / Dance" },
};
const DESCS = [
  "An in-depth look at the stories shaping our world today, with expert analysis and live reports from the field.",
  "Join our hosts for the latest updates, interviews and highlights you won't want to miss.",
  "A fan-favorite returns with more surprises, bigger moments and unforgettable characters.",
  "Critics call it 'a triumph' - experience the acclaimed hit everyone is talking about.",
  "All-new episode featuring special guests and exclusive behind-the-scenes footage.",
  "The definitive guide to what's happening now, presented by our award-winning team.",
];

const channels = [];
let num = 1;
for (const [group, def] of Object.entries(GROUPS)) {
  for (let i = 1; i <= 6; i++) {
    const nameBase = def.stream.split("-").map((w) => w[0].toUpperCase() + w.slice(1)).join(" ");
    const name = i === 1 ? nameBase : `${nameBase} ${["", "HD", "+1", "Extra", "2", "24"][i - 1]}`;
    channels.push({
      num: num++,
      id: `${def.stream}-${i}.fixture`,
      name,
      group,
      logo: `${BASE}/logos/${def.stream}.png`,
      url: `${BASE}/streams/${def.stream}.ts`,
      titles: def.titles,
      cat: def.cat,
    });
  }
}

// HLS recording e2e: ONE channel appended after the 30-channel plan (the
// VOD/catch-up append precedent keeps existing numbering intact) whose stream
// is a live-style HLS master playlist over three TS segments sliced out of
// news-one.ts below. No EPG (see NO_EPG_IDS); mirrored 1:1 by FixturePlan.
const HLS_ID = "hls-live-1.fixture";
channels.push({
  num: num++,
  id: HLS_ID,
  name: "HLS Live",
  group: "HLS",
  logo: `${BASE}/logos/news-one.png`,
  url: `${BASE}/streams/hls-live.m3u8`,
  titles: GROUPS.News.titles,
  cat: "News",
});

// ---- HLS stream fixture: master + live media playlist + TS segment slices --
// Slices are cut at 188-byte TS packet boundaries, so concatenating them (what
// the recorder does) reproduces news-one.ts byte for byte. The media playlist
// has no #EXT-X-ENDLIST: it is a live playlist that never advances, like a
// stalled provider — recording stops on user stop / planned end.
{
  const TS_PACKET = 188;
  const SEGMENTS = 3;
  const source = readFileSync(join(DIR, "streams", "news-one.ts"));
  const per = Math.ceil(Math.floor(source.length / TS_PACKET) / SEGMENTS) * TS_PACKET;
  const names = [];
  for (let i = 0; i < SEGMENTS; i++) {
    const name = `hls-live-${i}.ts`;
    names.push(name);
    writeFileSync(join(DIR, "streams", name), source.subarray(i * per, Math.min((i + 1) * per, source.length)));
  }
  writeFileSync(
    join(DIR, "streams", "hls-live.m3u8"),
    `#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1280000,RESOLUTION=1280x720\nhls-live-media.m3u8\n`,
  );
  writeFileSync(
    join(DIR, "streams", "hls-live-media.m3u8"),
    `#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-TARGETDURATION:2\n#EXT-X-MEDIA-SEQUENCE:0\n` +
      names.map((n) => `#EXTINF:2.0,\n${n}`).join("\n") + "\n",
  );
}

// ---- playlist.m3u ---------------------------------------------------------
// VOD entries: video-file (.mp4) stream URLs that telly must classify as
// "Movies", never as guide channels. Mirrored 1:1 by FixturePlan.vodItems.
const VOD_ITEMS = [
  { id: "vod-big-buck.fixture", name: "Big Buck Bunny", group: "Cinema" },
  { id: "vod-sintel.fixture", name: "Sintel", group: "Cinema" },
];
// Catch-up e2e (mirrored 1:1 by FixturePlan): News One is the only
// catch-up-enabled channel; the template resolves to the same fixture .ts
// (query params are ignored by the servers).
const CATCHUP_ID = "news-one-1.fixture";
const CATCHUP_DAYS = 2;
const catchupAttrs = (c) =>
  c.id === CATCHUP_ID
    ? ` catchup="default" catchup-source="${c.url}?utc={utc}&lutc={lutc}&d={duration}" catchup-days="${CATCHUP_DAYS}"`
    : "";
let m3u = `#EXTM3U url-tvg="${BASE}/epg.xml"\n`;
for (const c of channels) {
  m3u += `#EXTINF:-1 tvg-id="${c.id}" tvg-name="${c.name}" tvg-logo="${c.logo}"${catchupAttrs(c)} group-title="${c.group}",${c.name}\n${c.url}\n`;
}
for (const v of VOD_ITEMS) {
  m3u += `#EXTINF:-1 tvg-id="${v.id}" tvg-name="${v.name}" tvg-logo="${BASE}/logos/movie-house.png" group-title="${v.group}",${v.name}\n${BASE}/streams/vod-sample.mp4\n`;
}
writeFileSync(join(DIR, "playlist.m3u"), m3u);

// ---- epg.xml (XMLTV) ------------------------------------------------------
const fmt = (ms) => {
  const d = new Date(ms);
  const p = (n, l = 2) => String(n).padStart(l, "0");
  return `${d.getUTCFullYear()}${p(d.getUTCMonth() + 1)}${p(d.getUTCDate())}${p(d.getUTCHours())}${p(d.getUTCMinutes())}${p(d.getUTCSeconds())} +0000`;
};
const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");

const startWindow = anchor - 6 * 3600e3;
const endWindow = anchor + 24 * 3600e3;
// snap window start to a 30-min boundary so guide cells align nicely
const snapped = Math.floor(startWindow / 1800e3) * 1800e3;

let xml = `<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE tv SYSTEM "xmltv.dtd">\n<tv generator-info-name="telly-fixtures">\n`;
for (const c of channels) {
  xml += `  <channel id="${esc(c.id)}">\n    <display-name>${esc(c.name)}</display-name>\n    <icon src="${esc(c.logo)}" />\n  </channel>\n`;
}
// sports-arena-1 ships WITHOUT EPG so "No information" guide cells are real;
// hls-live-1 records on the no-EPG 3 h fallback.
const NO_EPG_IDS = new Set(["sports-arena-1.fixture", HLS_ID]);
for (const c of channels) {
  if (NO_EPG_IDS.has(c.id)) continue;
  const rand = rng(c.num * 7919);
  let t = snapped;
  let ep = 1;
  while (t < endWindow) {
    // 30-90 min durations in 15-min steps: 30,45,60,75,90
    const dur = (2 + Math.floor(rand() * 5)) * 15 * 60e3;
    const stop = t + dur;
    const title = c.titles[Math.floor(rand() * c.titles.length)];
    const desc = DESCS[Math.floor(rand() * DESCS.length)];
    const withEp = rand() < 0.4;
    xml += `  <programme start="${fmt(t)}" stop="${fmt(stop)}" channel="${esc(c.id)}">\n`;
    xml += `    <title lang="en">${esc(title)}</title>\n`;
    xml += `    <sub-title lang="en">${esc(withEp ? `Episode ${ep}` : title + " Special")}</sub-title>\n`;
    xml += `    <desc lang="en">${esc(desc)}</desc>\n`;
    xml += `    <category lang="en">${esc(c.cat)}</category>\n`;
    if (withEp) xml += `    <episode-num system="xmltv_ns">0.${ep - 1}.</episode-num>\n`;
    xml += `  </programme>\n`;
    t = stop;
    ep++;
  }
}
// News One + News One HD carry EPG a full extra day into the past ("Past …"
// titles, generated BACKWARDS from the main window so now/next phases never
// shift) — a −24 h guide day jump lands on real programmes for the catch-up
// scenarios. Deterministic; mirrored 1:1 by FixturePlan.deepPastSchedule.
const DEEP_PAST_IDS = new Set(["news-one-1.fixture", "news-one-2.fixture"]);
const DEEP_PAST_HOURS = 30;
const PAST_DURATIONS = [30, 45, 60, 75, 90];
for (const c of channels) {
  if (!DEEP_PAST_IDS.has(c.id)) continue;
  const floor = snapped - (DEEP_PAST_HOURS - 6) * 3600e3;
  let end = snapped;
  let index = 1;
  while (end > floor) {
    const start = end - PAST_DURATIONS[(c.num + index) % PAST_DURATIONS.length] * 60e3;
    const title = `Past ${c.titles[(c.num * 3 + index) % c.titles.length]}`;
    xml += `  <programme start="${fmt(start)}" stop="${fmt(end)}" channel="${esc(c.id)}">\n`;
    xml += `    <title lang="en">${esc(title)}</title>\n`;
    xml += `    <sub-title lang="en">${esc(title)} Special</sub-title>\n`;
    xml += `    <desc lang="en">${esc(DESCS[(c.num + index) % DESCS.length])}</desc>\n`;
    xml += `  </programme>\n`;
    end = start;
    index += 1;
  }
}
xml += `</tv>\n`;
writeFileSync(join(DIR, "epg.xml"), xml);

// ---- epg-alt.xml: a second, custom-source EPG --------------------------------
// Covers ONE channel epg.xml misses (sports-arena-1) plus ONE it also covers
// (news-one-1) with distinct "Alt "-prefixed titles, so custom-source merge
// and per-channel precedence are both observable. Fully deterministic (no
// RNG) and mirrored 1:1 by the instrumentation FixturePlan.altSchedule.
const ALT_IDS = new Set(["news-one-1.fixture", "sports-arena-1.fixture"]);
let altXml = `<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE tv SYSTEM "xmltv.dtd">\n<tv generator-info-name="telly-fixtures-alt">\n`;
for (const c of channels) {
  if (!ALT_IDS.has(c.id)) continue;
  altXml += `  <channel id="${esc(c.id)}">\n    <display-name>${esc(c.name)}</display-name>\n    <icon src="${esc(c.logo)}" />\n  </channel>\n`;
}
const ALT_DURATIONS = [30, 45, 60, 75, 90];
for (const c of channels) {
  if (!ALT_IDS.has(c.id)) continue;
  let t = snapped;
  let index = 0;
  while (t < endWindow) {
    const stop = t + ALT_DURATIONS[(c.num + index) % ALT_DURATIONS.length] * 60e3;
    const title = `Alt ${c.titles[(c.num * 3 + index) % c.titles.length]}`;
    altXml += `  <programme start="${fmt(t)}" stop="${fmt(stop)}" channel="${esc(c.id)}">\n`;
    altXml += `    <title lang="en">${esc(title)}</title>\n`;
    altXml += `    <sub-title lang="en">${esc(title)} Special</sub-title>\n`;
    altXml += `    <desc lang="en">${esc(DESCS[(c.num + index) % DESCS.length])}</desc>\n`;
    altXml += `  </programme>\n`;
    t = stop;
    index += 1;
  }
}
altXml += `</tv>\n`;
writeFileSync(join(DIR, "epg-alt.xml"), altXml);

console.log(`Wrote playlist.m3u (${channels.length} channels), epg.xml and epg-alt.xml (anchor=${new Date(anchor).toISOString()})`);
