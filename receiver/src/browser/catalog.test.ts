import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { parseChannelCatalog } from "../channel";
import { groupChannelsForBrowser } from "./catalog";

const channels = parseChannelCatalog(
  JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8")),
).channels;

describe("browser channel groups", () => {
  it("groups the complete registry into product sections", () => {
    const groups = groupChannelsForBrowser(channels);

    expect(groups.favorites.map((channel) => channel.id)).toEqual(["p1", "p3", "p4-malmo"]);
    expect(groups.national).toHaveLength(3);
    expect(groups.p4).toHaveLength(25);
    expect(groups.other).toHaveLength(8);
    expect(groups.defaultP4?.id).toBe("p4-malmo");
    expect(new Set([...groups.national, ...groups.p4, ...groups.other].map((channel) => channel.id)).size).toBe(36);
  });
});
