import { DEFAULT_FAVORITES, DEFAULT_P4 } from "../browse/catalog";
import type { Channel } from "../channel";

export interface BrowserChannelGroups {
  favorites: Channel[];
  national: Channel[];
  p4: Channel[];
  other: Channel[];
  defaultP4: Channel | null;
}

export function groupChannelsForBrowser(channels: Channel[]): BrowserChannelGroups {
  const p4 = channels.filter((channel) => channel.category === "LOCAL_P4");
  return {
    favorites: channels.filter((channel) => DEFAULT_FAVORITES.has(channel.id)),
    national: channels.filter((channel) => channel.category === "NATIONAL"),
    p4,
    other: channels.filter((channel) => channel.category !== "NATIONAL" && channel.category !== "LOCAL_P4"),
    defaultP4: p4.find((channel) => channel.id === DEFAULT_P4) ?? p4[0] ?? null,
  };
}
