import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { parseChannelCatalog } from "./channel";
import { channelIdFromEntity, mapChannelToMedia } from "./metadata";

const channels = parseChannelCatalog(JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"))).channels;

describe("receiver metadata", () => {
  it("maps a channel to live media fetched directly from SR", () => {
    const media = mapChannelToMedia(channels[2]!);
    expect(media.streamType).toBe("LIVE");
    expect(media.contentUrl).toBe("https://live1.sr.se/p3-aac-128");
    expect(media.metadata.artist).toBe("Sveriges Radio");
    expect(channelIdFromEntity(media.entity)).toBe("p3");
  });
});
