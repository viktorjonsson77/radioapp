import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it, vi } from "vitest";
import { parseChannelCatalog } from "./channel";
import {
  channelIdFromEntity,
  mapChannelToMedia,
  metadataRefreshDelayMs,
  parseSrRightNow,
  SrMetadataProvider,
} from "./metadata";

const channels = parseChannelCatalog(JSON.parse(readFileSync(resolve(process.cwd(), "../shared/channels.json"), "utf8"))).channels;
const p3 = channels.find((channel) => channel.id === "p3")!;
const fixture = {
  channel: {
    id: 164,
    name: "P3",
    currentscheduledepisode: {
      title: "Morgonpasset i P3",
      description: "Aktuellt morgonprogram.",
      starttimeutc: "/Date(1786428000000)/",
      endtimeutc: "/Date(1786435200000)/",
      program: { id: 2024, name: "Morgonpasset i P3" },
      socialimage: "https://static-cdn.sr.se/images/2024/program.jpg",
    },
    nextscheduledepisode: {
      title: "P3 Nyheter",
      starttimeutc: "/Date(1786435200000)/",
      endtimeutc: "/Date(1786435500000)/",
      program: { id: 1646, name: "P3 Nyheter" },
    },
  },
};

describe("receiver metadata", () => {
  it("parses current and next program from the documented rightnow shape", () => {
    const result = parseSrRightNow(fixture, "p3", new Date("2026-08-11T06:00:00Z"));
    expect(result?.programName).toBe("Morgonpasset i P3");
    expect(result?.programDescription).toBe("Aktuellt morgonprogram.");
    expect(result?.programId).toBe(2024);
    expect(result?.nextProgram?.name).toBe("P3 Nyheter");
    expect(result?.startsAt?.toISOString()).toBe("2026-08-11T06:00:00.000Z");
  });

  it("accepts missing optional image and description", () => {
    const missing = structuredClone(fixture);
    delete (missing.channel.currentscheduledepisode as any).socialimage;
    delete (missing.channel.currentscheduledepisode as any).description;
    const result = parseSrRightNow(missing, "p3");
    expect(result?.imageUrl).toBeNull();
    expect(result?.programDescription).toBeNull();
  });

  it("returns no metadata for malformed or empty responses", () => {
    expect(parseSrRightNow({ channel: { currentscheduledepisode: { title: "" } } }, "p3")).toBeNull();
    expect(parseSrRightNow("not-json-shape", "p3")).toBeNull();
  });

  it("maps program-first live media and channel-image fallback", () => {
    const program = parseSrRightNow(fixture, "p3")!;
    const media = mapChannelToMedia(p3, "fallback.svg", program);
    expect(media.streamType).toBe("LIVE");
    expect(media.contentUrl).toBe("https://live1.sr.se/p3-aac-128");
    expect(media.metadata.title).toBe("Morgonpasset i P3");
    expect(media.metadata.artist).toBe("P3");
    expect(channelIdFromEntity(media.entity)).toBe("p3");
    expect(mapChannelToMedia(p3).metadata.artist).toBe("Sveriges Radio");
    expect(mapChannelToMedia({ ...p3, imageUrl: null }, "fallback.svg").metadata.images[0]?.url).toBe("fallback.svg");
  });

  it("caches fresh data and uses stale data for a short network failure", async () => {
    let now = new Date("2026-08-11T06:00:00Z");
    const fetcher = vi.fn()
      .mockResolvedValueOnce(new Response(JSON.stringify(fixture), { status: 200 }))
      .mockRejectedValueOnce(new Error("network"));
    const provider = new SrMetadataProvider(fetcher, () => now);
    expect((await provider.nowPlaying(p3))?.programName).toBe("Morgonpasset i P3");
    expect((await provider.nowPlaying(p3))?.programName).toBe("Morgonpasset i P3");
    expect(fetcher).toHaveBeenCalledTimes(1);
    now = new Date("2026-08-11T06:06:00Z");
    expect((await provider.nowPlaying(p3))?.programName).toBe("Morgonpasset i P3");
    expect(fetcher).toHaveBeenCalledTimes(2);
  });

  it("surfaces network failure and timeout without manufacturing metadata", async () => {
    await expect(new SrMetadataProvider(async () => { throw new Error("offline"); }).nowPlaying(p3)).rejects.toThrow("offline");
    const hanging = (_url: string, init?: RequestInit) => new Promise<Response>((_resolve, reject) => {
      init?.signal?.addEventListener("abort", () => reject(new DOMException("aborted", "AbortError")));
    });
    await expect(new SrMetadataProvider(hanging, () => new Date(), 1).nowPlaying(p3)).rejects.toThrow();
  });

  it("refreshes near program end within bounded polling limits", () => {
    const now = new Date("2026-08-11T06:00:00Z");
    const program = parseSrRightNow(fixture, "p3", now)!;
    expect(metadataRefreshDelayMs(program, now)).toBe(300_000);
    expect(metadataRefreshDelayMs(null, now)).toBe(120_000);
  });
});
