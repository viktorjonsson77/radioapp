import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { contentType, parseChannelCatalog } from "./channel";

const fixture = JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"));

describe("channel catalog", () => {
  it("parses the canonical five-channel catalog", () => {
    const catalog = parseChannelCatalog(fixture);
    expect(catalog.channels).toHaveLength(5);
    expect(catalog.channels.every((channel) => channel.streamQuality === "AAC_128")).toBe(true);
    expect(catalog.channels.every((channel) => channel.streamUrl.startsWith("https://live1.sr.se/"))).toBe(true);
  });

  it("maps AAC to its content type", () => {
    expect(contentType(parseChannelCatalog(fixture).channels[0]!)).toBe("audio/aac");
  });

  it("rejects non-SR and non-HTTPS stream URLs", () => {
    const invalid = structuredClone(fixture);
    invalid.channels[0].streamUrl = "http://example.com/radio";
    expect(() => parseChannelCatalog(invalid)).toThrow(/official HTTPS SR stream/);
  });
});
