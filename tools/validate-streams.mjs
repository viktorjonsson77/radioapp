import { readFile } from "node:fs/promises";
import { resolve } from "node:path";

const catalog = JSON.parse(await readFile(resolve(process.cwd(), "shared/channels.json"), "utf8"));
let failed = false;

for (const channel of catalog.channels) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 10_000);
  try {
    const response = await fetch(channel.streamUrl, {
      method: "GET",
      headers: { Range: "bytes=0-0" },
      signal: controller.signal,
    });
    const ok = response.ok || response.status === 206;
    console.log(`${ok ? "OK" : "FAIL"} ${response.status} ${channel.name} ${channel.streamUrl}`);
    failed ||= !ok;
    await response.body?.cancel();
  } catch (error) {
    failed = true;
    console.error(`FAIL ${channel.name} ${String(error)}`);
  } finally {
    clearTimeout(timeout);
  }
}

process.exitCode = failed ? 1 : 0;
