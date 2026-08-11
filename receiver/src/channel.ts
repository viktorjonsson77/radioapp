export type StreamQuality = "AAC_32" | "AAC_128" | "AAC_320" | "MP3_96";
export type StreamFormat = "AAC" | "MP3";
export type ChannelCategory = "NATIONAL" | "LOCAL_P4" | "NEWS" | "MUSIC" | "DIGITAL" | "LANGUAGE" | "CHILDREN";
export interface ChannelRegion { name: string; slug: string; }

export interface Channel {
  id: string;
  srChannelId: number;
  name: string;
  shortName: string;
  description: string;
  streamUrl: string;
  streamQuality: StreamQuality;
  streamFormat: StreamFormat;
  imageUrl?: string | null;
  category: ChannelCategory;
  region?: ChannelRegion | null;
  isLocal: boolean;
  isFavoriteCapable: boolean;
}

export interface ChannelCatalog { schemaVersion: number; channels: Channel[]; }

const qualities = new Set(["AAC_32", "AAC_128", "AAC_320", "MP3_96"]);
const formats = new Set(["AAC", "MP3"]);
const categories = new Set(["NATIONAL", "LOCAL_P4", "NEWS", "MUSIC", "DIGITAL", "LANGUAGE", "CHILDREN"]);

export function parseChannelCatalog(value: unknown): ChannelCatalog {
  if (!isRecord(value) || value.schemaVersion !== 2 || !Array.isArray(value.channels)) {
    throw new Error("Unsupported or invalid channel catalog");
  }
  const channels = value.channels.map(parseChannel);
  if (new Set(channels.map((channel) => channel.id)).size !== channels.length) {
    throw new Error("Channel IDs must be unique");
  }
  if (new Set(channels.map((channel) => channel.srChannelId)).size !== channels.length) {
    throw new Error("SR channel IDs must be unique");
  }
  if (new Set(channels.map((channel) => channel.streamUrl)).size !== channels.length) {
    throw new Error("Stream URLs must be unique");
  }
  return { schemaVersion: 2, channels };
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
  if (!Number.isInteger(value.srChannelId) || Number(value.srChannelId) <= 0) throw new Error(`Invalid srChannelId in ${value.id as string}`);
  if (typeof value.isLocal !== "boolean" || typeof value.isFavoriteCapable !== "boolean") {
    throw new Error(`Invalid flags in ${value.id as string}`);
  }
  if (value.category === "LOCAL_P4") {
    if (!isRecord(value.region) || typeof value.region.name !== "string" || typeof value.region.slug !== "string" || !value.isLocal) {
      throw new Error(`Invalid P4 region in ${value.id as string}`);
    }
  } else if (value.region !== null || value.isLocal) throw new Error(`Unexpected region in ${value.id as string}`);
  return value as unknown as Channel;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
