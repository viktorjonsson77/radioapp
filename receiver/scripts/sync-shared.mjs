import { copyFile, mkdir } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const receiverRoot = resolve(scriptDir, "..");
const source = resolve(receiverRoot, "../shared/channels.json");
const targetDir = resolve(receiverRoot, "public/generated");

await mkdir(targetDir, { recursive: true });
await copyFile(source, resolve(targetDir, "channels.json"));
console.log("Synced shared/channels.json");
