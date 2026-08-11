import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { parseChannelCatalog } from "../channel";
import { createBrowsePlan } from "./catalog";

const channels = parseChannelCatalog(JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"))).channels;

describe("Media Browse plan", () => {
  it("provides favorites for landing and all channels in-player", () => {
    const plan = createBrowsePlan(channels);
    expect(plan.landing.title).toBe("Favoriter");
    expect(plan.landing.items.map((item) => item.title)).toEqual(["P1", "P3", "P4 Malmöhus"]);
    expect(plan.inPlayer.title).toBe("Kanaler");
    expect(plan.inPlayer.items).toHaveLength(5);
    expect(plan.inPlayer.items.every((item) => item.live)).toBe(true);
  });
});
