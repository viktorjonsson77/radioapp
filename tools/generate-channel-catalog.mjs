import { writeFile } from "node:fs/promises";
import { resolve } from "node:path";

const CHANNELS_API = "https://api.sr.se/api/v2/channels?format=json&pagination=false";
const STREAMS_PAGE = "https://om.sverigesradio.se/lankar-till-ljudstrommar-for-alla-kanaler";

const definitions = [
  ["p1", 132, "P1", "p1", "NATIONAL"],
  ["p2", 163, "P2", "p2", "NATIONAL"],
  ["p3", 164, "P3", "p3", "NATIONAL"],
  ["p4-blekinge", 213, "P4 Blekinge", "p4blek", "LOCAL_P4", "Blekinge", "blekinge"],
  ["p4-dalarna", 223, "P4 Dalarna", "p4dala", "LOCAL_P4", "Dalarna", "dalarna"],
  ["p4-gotland", 205, "P4 Gotland", "p4gotl", "LOCAL_P4", "Gotland", "gotland"],
  ["p4-gavleborg", 210, "P4 Gävleborg", "p4gavl", "LOCAL_P4", "Gävleborg", "gavleborg"],
  ["p4-goteborg", 212, "P4 Göteborg", "p4gbg", "LOCAL_P4", "Göteborg", "goteborg"],
  ["p4-halland", 220, "P4 Halland", "p4hall", "LOCAL_P4", "Halland", "halland"],
  ["p4-jamtland", 200, "P4 Jämtland", "p4jmtl", "LOCAL_P4", "Jämtland", "jamtland"],
  ["p4-jonkoping", 203, "P4 Jönköping", "p4jkpg", "LOCAL_P4", "Jönköping", "jonkoping"],
  ["p4-kalmar", 201, "P4 Kalmar", "p4kalm", "LOCAL_P4", "Kalmar", "kalmar"],
  ["p4-kristianstad", 211, "P4 Kristianstad", "p4krist", "LOCAL_P4", "Kristianstad", "kristianstad"],
  ["p4-kronoberg", 214, "P4 Kronoberg", "p4kron", "LOCAL_P4", "Kronoberg", "kronoberg"],
  ["p4-malmo", 207, "P4 Malmöhus", "p4malm", "LOCAL_P4", "Malmöhus", "malmohus"],
  ["p4-norrbotten", 209, "P4 Norrbotten", "p4nbtn", "LOCAL_P4", "Norrbotten", "norrbotten"],
  ["p4-sjuharad", 206, "P4 Sjuhärad", "p4sju", "LOCAL_P4", "Sjuhärad", "sjuharad"],
  ["p4-skaraborg", 208, "P4 Skaraborg", "p4skbg", "LOCAL_P4", "Skaraborg", "skaraborg"],
  ["p4-stockholm", 701, "P4 Stockholm", "p4sth", "LOCAL_P4", "Stockholm", "stockholm"],
  ["p4-sormland", 202, "P4 Sörmland", "p4sorm", "LOCAL_P4", "Sörmland", "sormland"],
  ["p4-uppland", 218, "P4 Uppland", "p4uppl", "LOCAL_P4", "Uppland", "uppland"],
  ["p4-varmland", 204, "P4 Värmland", "p4vrml", "LOCAL_P4", "Värmland", "varmland"],
  ["p4-vast", 219, "P4 Väst", "p4vast", "LOCAL_P4", "Väst", "vast"],
  ["p4-vasterbotten", 215, "P4 Västerbotten", "p4vbtn", "LOCAL_P4", "Västerbotten", "vasterbotten"],
  ["p4-vasternorrland", 216, "P4 Västernorrland", "p4vnrl", "LOCAL_P4", "Västernorrland", "vasternorrland"],
  ["p4-vastmanland", 217, "P4 Västmanland", "p4vstm", "LOCAL_P4", "Västmanland", "vastmanland"],
  ["p4-orebro", 221, "P4 Örebro", "p4oreb", "LOCAL_P4", "Örebro", "orebro"],
  ["p4-ostergotland", 222, "P4 Östergötland", "p4ostg", "LOCAL_P4", "Östergötland", "ostergotland"],
  ["ekot-direkt", 4540, "Ekot sänder direkt", "ekotdirekt", "NEWS"],
  ["p3-din-gata", 2576, "P3 Din gata", "dingata", "MUSIC"],
  ["p4-digital", 5283, "P4 Digital", "p4digi", "DIGITAL"],
  ["p4-plus", 4951, "P4 Plus", "p4plus", "MUSIC"],
  ["p6", 166, "P6", "p6", "LANGUAGE"],
  ["radioapans-knattekanal", 2755, "Radioapans knattekanal", "knattekanalen", "CHILDREN"],
  ["sr-sapmi", 224, "SR Sápmi", "sameradion", "LANGUAGE"],
  ["sr-finska", 226, "Sveriges Radio Finska", "finska", "LANGUAGE"],
];

const [apiResponse, streamsResponse] = await Promise.all([fetch(CHANNELS_API), fetch(STREAMS_PAGE)]);
if (!apiResponse.ok || !streamsResponse.ok) throw new Error("Official SR sources could not be loaded");
const api = await apiResponse.json();
const streamPage = await streamsResponse.text();
const byId = new Map(api.channels.map((channel) => [channel.id, channel]));

const channels = definitions.map(([id, srChannelId, name, streamSlug, category, regionName, regionSlug]) => {
  const source = byId.get(srChannelId);
  if (!source) throw new Error(`Missing SR API channel ${srChannelId} for ${id}`);
  const streamUrl = `https://live1.sr.se/${streamSlug}-aac-128`;
  if (!streamPage.includes(streamUrl)) throw new Error(`Missing official stream ${streamUrl}`);
  return {
    id,
    srChannelId,
    name,
    shortName: name,
    description: source.tagline,
    streamUrl,
    streamQuality: "AAC_128",
    streamFormat: "AAC",
    imageUrl: source.image,
    category,
    region: regionName ? { name: regionName, slug: regionSlug } : null,
    isLocal: category === "LOCAL_P4",
    isFavoriteCapable: true,
  };
});

const output = `${JSON.stringify({ schemaVersion: 2, channels }, null, 2)}\n`;
if (process.argv.includes("--write")) {
  const target = resolve(process.cwd(), "shared/channels.json");
  await writeFile(target, output, "utf8");
  console.log(`Wrote ${channels.length} channels to ${target}`);
} else {
  process.stdout.write(output);
}
