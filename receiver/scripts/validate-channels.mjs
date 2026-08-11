import { readFile } from "node:fs/promises";
import { resolve } from "node:path";
import Ajv2020 from "ajv/dist/2020.js";

const root = resolve(process.cwd(), "..");
const [catalog, schema] = await Promise.all([
  readFile(resolve(root, "shared/channels.json"), "utf8").then(JSON.parse),
  readFile(resolve(root, "shared/channels.schema.json"), "utf8").then(JSON.parse),
]);
const validate = new Ajv2020({ allErrors: true }).compile(schema);
if (!validate(catalog)) {
  console.error(validate.errors);
  process.exit(1);
}

const ids = catalog.channels.map(({ id }) => id);
const srIds = catalog.channels.map(({ srChannelId }) => srChannelId);
const streams = catalog.channels.map(({ streamUrl }) => streamUrl);
const regions = catalog.channels.filter(({ category }) => category === "LOCAL_P4");
const unique = (values) => new Set(values).size === values.length;
const checks = [
  [unique(ids), "duplicate internal channel ID"],
  [unique(srIds), "duplicate SR channel ID"],
  [unique(streams), "duplicate stream URL"],
  [catalog.channels.every(({ name }) => name.trim()), "empty channel name"],
  [catalog.channels.every(({ streamUrl }) => streamUrl.startsWith("https://")), "non-HTTPS stream"],
  [regions.every(({ region, isLocal }) => region?.name && region?.slug && isLocal), "invalid P4 region mapping"],
  [catalog.channels.filter(({ category }) => category !== "LOCAL_P4").every(({ region, isLocal }) => region === null && !isLocal), "region/local mismatch"],
];
for (const [ok, message] of checks) if (!ok) throw new Error(message);
console.log(`VALID catalog=${catalog.channels.length} p4Regions=${regions.length} schemaVersion=${catalog.schemaVersion}`);
