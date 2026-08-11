import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { contentType, parseChannelCatalog } from "./channel";

const fixture = JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"));

describe("channel catalog", () => {
  it("parses the complete canonical channel catalog", () => {
    const catalog = parseChannelCatalog(fixture);
    expect(catalog.channels).toHaveLength(36);
    expect(catalog.channels.filter((channel) => channel.category === "LOCAL_P4")).toHaveLength(25);
    expect(new Set(catalog.channels.map((channel) => channel.category))).toEqual(
      new Set(["NATIONAL", "LOCAL_P4", "NEWS", "MUSIC", "DIGITAL", "LANGUAGE", "CHILDREN"]),
    );
    expect(catalog.channels.every((channel) => channel.streamQuality === "AAC_128")).toBe(true);
    expect(catalog.channels.every((channel) => channel.streamUrl.startsWith("https://live1.sr.se/"))).toBe(true);
  });

  it("maps AAC to its content type", () => {
    expect(contentType(parseChannelCatalog(fixture).channels[0]!)).toBe("audio/aac");
  });

  it("keeps the registry model ready for official alternate stream qualities", () => {
    const alternate = structuredClone(fixture);
    alternate.channels[0].streamUrl = "https://live1.sr.se/p1-mp3-96";
    alternate.channels[0].streamQuality = "MP3_96";
    alternate.channels[0].streamFormat = "MP3";

    expect(contentType(parseChannelCatalog(alternate).channels[0]!)).toBe("audio/mpeg");
  });

  it("rejects non-SR and non-HTTPS stream URLs", () => {
    const invalid = structuredClone(fixture);
    invalid.channels[0].streamUrl = "http://example.com/radio";
    expect(() => parseChannelCatalog(invalid)).toThrow(/official HTTPS SR stream/);
  });

  it("rejects duplicate SR IDs and missing P4 region data", () => {
    const duplicate = structuredClone(fixture);
    duplicate.channels[1].srChannelId = duplicate.channels[0].srChannelId;
    expect(() => parseChannelCatalog(duplicate)).toThrow(/SR channel IDs/);
    const regionless = structuredClone(fixture);
    regionless.channels.find((channel: any) => channel.category === "LOCAL_P4").region = null;
    expect(() => parseChannelCatalog(regionless)).toThrow(/P4 region/);
  });

  it("rejects mismatched stream quality and format", () => {
    const mismatch = structuredClone(fixture);
    mismatch.channels[0].streamQuality = "MP3_96";

    expect(() => parseChannelCatalog(mismatch)).toThrow(/quality\/format mismatch/);
  });
});
