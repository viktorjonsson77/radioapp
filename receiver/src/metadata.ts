import { Channel, contentType } from "./channel";

export interface NextProgramMetadata {
  programId: number | null;
  name: string;
  startsAt: Date | null;
  endsAt: Date | null;
}

export interface NowPlayingMetadata {
  channelId: string;
  programId: number | null;
  programName: string;
  programDescription: string | null;
  imageUrl: string | null;
  startsAt: Date | null;
  endsAt: Date | null;
  nextProgram: NextProgramMetadata | null;
  updatedAt: Date;
}

export interface ReceiverMediaInformation {
  contentId: string;
  contentUrl: string;
  contentType: string;
  streamType: "LIVE";
  entity: string;
  metadata: {
    metadataType: number;
    title: string;
    artist: string;
    subtitle: string;
    images: Array<{ url: string }>;
  };
}

type FetchLike = (input: string, init?: RequestInit) => Promise<Response>;
interface CacheEntry { value: NowPlayingMetadata | null; refreshAt: number; staleUntil: number; }

export class SrMetadataProvider {
  private readonly cache = new Map<string, CacheEntry>();

  constructor(
    private readonly fetcher: FetchLike = fetch,
    private readonly now: () => Date = () => new Date(),
    private readonly timeoutMs = 4_000,
  ) {}

  async nowPlaying(channel: Channel): Promise<NowPlayingMetadata | null> {
    const now = this.now();
    const cached = this.cache.get(channel.id);
    if (cached && now.getTime() < cached.refreshAt) return cached.value;
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), this.timeoutMs);
      let response: Response;
      try {
        response = await this.fetcher(
          `https://api.sr.se/api/v2/scheduledepisodes/rightnow?channelid=${channel.srChannelId}&format=json`,
          { headers: { Accept: "application/json" }, signal: controller.signal },
        );
      } finally {
        clearTimeout(timeout);
      }
      if (!response.ok) throw new Error(`SR API returned HTTP ${response.status}`);
      const value = parseSrRightNow(await response.json(), channel.id, now);
      const delay = metadataRefreshDelayMs(value, now);
      const staleUntil = value?.endsAt
        ? Math.min(value.endsAt.getTime() + 120_000, now.getTime() + 1_800_000)
        : now.getTime() + 120_000;
      this.cache.set(channel.id, { value, refreshAt: now.getTime() + delay, staleUntil });
      return value;
    } catch (error) {
      if (cached && now.getTime() <= cached.staleUntil) return cached.value;
      throw error;
    }
  }
}

export function parseSrRightNow(value: unknown, channelId: string, updatedAt = new Date()): NowPlayingMetadata | null {
  if (!isRecord(value) || !isRecord(value.channel) || !isRecord(value.channel.currentscheduledepisode)) return null;
  const current = value.channel.currentscheduledepisode;
  const program = isRecord(current.program) ? current.program : null;
  const programName = nonEmpty(current.title) ?? nonEmpty(program?.name);
  if (!programName) return null;
  const next = isRecord(value.channel.nextscheduledepisode) ? parseNext(value.channel.nextscheduledepisode) : null;
  return {
    channelId,
    programId: integer(program?.id),
    programName,
    programDescription: nonEmpty(current.description),
    imageUrl: httpsUrl(current.socialimage) ?? httpsUrl(current.imageurl),
    startsAt: parseSrDate(current.starttimeutc),
    endsAt: parseSrDate(current.endtimeutc),
    nextProgram: next,
    updatedAt,
  };
}

export function metadataRefreshDelayMs(value: NowPlayingMetadata | null, now = new Date()): number {
  const candidate = value?.endsAt ? value.endsAt.getTime() - now.getTime() - 15_000 : 120_000;
  return Math.max(30_000, Math.min(300_000, candidate));
}

function parseNext(value: Record<string, unknown>): NextProgramMetadata | null {
  const program = isRecord(value.program) ? value.program : null;
  const name = nonEmpty(value.title) ?? nonEmpty(program?.name);
  return name ? {
    programId: integer(program?.id),
    name,
    startsAt: parseSrDate(value.starttimeutc),
    endsAt: parseSrDate(value.endtimeutc),
  } : null;
}

function parseSrDate(value: unknown): Date | null {
  if (typeof value !== "string") return null;
  const match = /^\/Date\((-?\d+)(?:[+-]\d{4})?\)\/$/.exec(value);
  const date = match ? new Date(Number(match[1])) : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function nonEmpty(value: unknown): string | null {
  return typeof value === "string" && value.trim() ? value : null;
}
function httpsUrl(value: unknown): string | null {
  return typeof value === "string" && value.startsWith("https://") ? value : null;
}
function integer(value: unknown): number | null { return Number.isInteger(value) ? Number(value) : null; }
function isRecord(value: unknown): value is Record<string, unknown> { return typeof value === "object" && value !== null; }

export function channelEntity(channelId: string): string { return `radioapp://channel/${channelId}`; }

export function channelIdFromEntity(entity: string): string | null {
  const prefix = "radioapp://channel/";
  return entity.startsWith(prefix) ? entity.slice(prefix.length) : null;
}

export function mapChannelToMedia(
  channel: Channel,
  artworkUrl = "./assets/radioapp-channel.svg",
  program: NowPlayingMetadata | null = null,
): ReceiverMediaInformation {
  return {
    contentId: channel.streamUrl,
    contentUrl: channel.streamUrl,
    contentType: contentType(channel),
    streamType: "LIVE",
    entity: channelEntity(channel.id),
    metadata: {
      metadataType: 3,
      title: program?.programName ?? channel.name,
      artist: program ? channel.name : "Sveriges Radio",
      subtitle: "LIVE · Sveriges Radio",
      images: [{ url: program?.imageUrl ?? channel.imageUrl ?? artworkUrl }],
    },
  };
}
