export type StreamQuality = "AAC_32" | "AAC_128" | "AAC_320" | "MP3_96";
export type StreamFormat = "AAC" | "MP3";
export type ChannelCategory = "NATIONAL" | "LOCAL_P4";

export interface Channel {
  id: string;
  name: string;
  shortName: string;
  description: string;
  streamUrl: string;
  streamQuality: StreamQuality;
  streamFormat: StreamFormat;
  imageUrl?: string | null;
  category: ChannelCategory;
  region?: string | null;
  isLocal: boolean;
  isFavoriteCapable: boolean;
}

export interface ChannelCatalog { schemaVersion: number; channels: Channel[]; }

const qualities = new Set(["AAC_32", "AAC_128", "AAC_320", "MP3_96"]);
const formats = new Set(["AAC", "MP3"]);
const categories = new Set(["NATIONAL", "LOCAL_P4"]);

export function parseChannelCatalog(value: unknown): ChannelCatalog {
  if (!isRecord(value) || value.schemaVersion !== 1 || !Array.isArray(value.channels)) {
    throw new Error("Unsupported or invalid channel catalog");
  }
  const channels = value.channels.map(parseChannel);
  if (new Set(channels.map((channel) => channel.id)).size !== channels.length) {
    throw new Error("Channel IDs must be unique");
  }
  return { schemaVersion: 1, channels };
}

export async function loadChannels(url = "./generated/channels.json"): Promise<Channel[]> {
  const response = await fetch(url);
  if (!response.ok) throw new Error(`Channel catalog request failed (${response.status})`);
  return parseChannelCatalog(await response.json()).channels;
}

export function contentType(channel: Channel): "audio/aac" | "audio/mpeg" {
  return channel.streamFormat === "AAC" ? "audio/aac" : "audio/mpeg";
}

function parseChannel(value: unknown): Channel {
  if (!isRecord(value)) throw new Error("Channel must be an object");
  const requiredStrings = ["id", "name", "shortName", "description", "streamUrl"] as const;
  for (const key of requiredStrings) {
    if (typeof value[key] !== "string" || value[key].length === 0) throw new Error(`Invalid ${key}`);
  }
  const url = new URL(value.streamUrl as string);
  if (url.protocol !== "https:" || url.hostname !== "live1.sr.se") {
    throw new Error(`Channel ${value.id as string} must use an official HTTPS SR stream`);
  }
  if (!qualities.has(String(value.streamQuality)) || !formats.has(String(value.streamFormat)) ||
      !categories.has(String(value.category))) throw new Error(`Invalid enum in ${value.id as string}`);
  if (typeof value.isLocal !== "boolean" || typeof value.isFavoriteCapable !== "boolean") {
    throw new Error(`Invalid flags in ${value.id as string}`);
  }
  return value as unknown as Channel;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
