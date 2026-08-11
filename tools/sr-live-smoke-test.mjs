import { spawn } from "node:child_process";
import { readFile } from "node:fs/promises";
import { resolve } from "node:path";

console.log("LIVE_EXTERNAL_TEST START");
process.once("exit", (code) => console.log(`LIVE_EXTERNAL_TEST ${code === 0 ? "PASS" : "FAIL"}`));

const root = process.cwd();
const catalog = JSON.parse(await readFile(resolve(root, "shared/channels.json"), "utf8"));

const streamExitCode = await new Promise((resolvePromise, reject) => {
  const child = spawn(process.execPath, [resolve(root, "tools/validate-streams.mjs")], {
    cwd: root,
    stdio: "inherit",
    env: { ...process.env, RADIOAPP_STREAM_CONCURRENCY: "3" },
  });
  child.once("error", reject);
  child.once("exit", (code) => resolvePromise(code ?? 1));
});

const p3 = catalog.channels.find(({ id }) => id === "p3");
if (!p3) throw new Error("P3 is missing from the catalog");
const endpoint = `https://api.sr.se/api/v2/scheduledepisodes/rightnow?channelid=${p3.srChannelId}&format=json`;
const controller = new AbortController();
const timeout = setTimeout(() => controller.abort(), 10_000);
try {
  const response = await fetch(endpoint, {
    headers: { Accept: "application/json", Origin: "https://viktorjonsson77.github.io" },
    signal: controller.signal,
  });
  if (!response.ok) throw new Error(`Metadata HTTP ${response.status}`);
  const body = await response.json();
  const current = body?.channel?.currentscheduledepisode;
  if (!current || typeof current.title !== "string" || typeof current.starttimeutc !== "string" ||
      typeof current.endtimeutc !== "string") throw new Error("Unexpected rightnow response shape");
  const cors = response.headers.get("access-control-allow-origin");
  console.log(`METADATA OK channel=${body.channel.name} program=${current.title} start=${current.starttimeutc} end=${current.endtimeutc}`);
  console.log(`CORS access-control-allow-origin=${cors ?? "MISSING"}`);
  if (cors !== "*") throw new Error("SR API did not return wildcard CORS for the receiver origin request");
} finally {
  clearTimeout(timeout);
}
if (streamExitCode !== 0) process.exitCode = 1;
