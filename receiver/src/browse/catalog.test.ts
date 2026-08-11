import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { parseChannelCatalog } from "../channel";
import { createBrowsePlan } from "./catalog";

const channels = parseChannelCatalog(JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"))).channels;

describe("Media Browse plan", () => {
  it("provides favorites and a deterministic under-30 in-player subset", () => {
    const plan = createBrowsePlan(channels);
    expect(plan.landing.title).toBe("Favoriter");
    expect(plan.landing.items.map((item) => item.title)).toEqual(["P1", "P3", "P4 Malmöhus"]);
    expect(plan.inPlayer.title).toBe("Utvalda kanaler");
    expect(plan.inPlayer.items).toHaveLength(12);
    expect(plan.inPlayer.items.map((item) => item.title)).toContain("P4 Malmöhus");
    expect(plan.inPlayer.items.map((item) => item.title)).not.toContain("P4 Blekinge");
    expect(plan.inPlayer.items.length).toBeLessThanOrEqual(30);
    expect(plan.inPlayer.items.every((item) => item.live)).toBe(true);
  });

  it("always supplies a fallback image", () => {
    const withoutImage = { ...channels[0]!, imageUrl: null };
    expect(createBrowsePlan([withoutImage]).inPlayer.items[0]?.imageUrl).toBe("./assets/radioapp-channel.svg");
  });
});
