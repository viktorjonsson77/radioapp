import { readFile } from "node:fs/promises";
import { resolve } from "node:path";

const catalog = JSON.parse(await readFile(resolve(process.cwd(), "shared/channels.json"), "utf8"));
const concurrency = Number(process.env.RADIOAPP_STREAM_CONCURRENCY ?? 3);
let cursor = 0;
const results = [];

async function validateNext() {
  const channel = catalog.channels[cursor++];
  if (!channel) return;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 12_000);
  try {
    const response = await fetch(channel.streamUrl, {
      method: "GET",
      headers: { Range: "bytes=0-0" },
      signal: controller.signal,
    });
    const contentType = response.headers.get("content-type")?.split(";", 1)[0] ?? "";
    const finalUrl = new URL(response.url);
    const expectedPath = new URL(channel.streamUrl).pathname;
    const ok = (response.ok || response.status === 206) &&
      ["audio/aac", "audio/aacp", "application/octet-stream"].includes(contentType) &&
      finalUrl.protocol === "https:" && finalUrl.hostname.endsWith("sr.se") &&
      finalUrl.pathname === expectedPath;
    results.push({ ok, status: response.status, channel: channel.name, contentType, finalUrl: response.url });
    await response.body?.cancel();
  } catch (error) {
    results.push({ ok: false, status: "ERROR", channel: channel.name, contentType: String(error), finalUrl: channel.streamUrl });
  } finally {
    clearTimeout(timeout);
  }
  await validateNext();
}

await Promise.all(Array.from({ length: Math.min(concurrency, catalog.channels.length) }, validateNext));
results.sort((a, b) => a.channel.localeCompare(b.channel, "sv"));
for (const result of results) {
  console.log(`${result.ok ? "OK" : "FAIL"} ${result.status} ${result.contentType} ${result.channel} ${result.finalUrl}`);
}

const failed = results.filter(({ ok }) => !ok).length;
console.log(`SUMMARY total=${results.length} passed=${results.length - failed} failed=${failed}`);
process.exitCode = failed ? 1 : 0;
