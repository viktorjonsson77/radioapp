import { Channel } from "../channel";
import { channelEntity } from "../metadata";

export const DEFAULT_FAVORITES = new Set(["p1", "p3", "p4-malmo"]);
export const DEFAULT_P4 = "p4-malmo";

export interface BrowseItemModel {
  entity: string;
  title: string;
  subtitle: string;
  imageUrl: string;
  live: true;
}

export interface BrowseSectionModel { title: string; items: BrowseItemModel[]; }
export interface BrowsePlan { landing: BrowseSectionModel; inPlayer: BrowseSectionModel; }

export function createBrowsePlan(
  channels: Channel[],
  favoriteIds: ReadonlySet<string> = DEFAULT_FAVORITES,
  fallbackImage = "./assets/radioapp-channel.svg",
): BrowsePlan {
  const asItem = (channel: Channel): BrowseItemModel => ({
    entity: channelEntity(channel.id),
    title: channel.name,
    subtitle: channel.isLocal ? `LIVE · ${channel.region?.name ?? channel.name}` : "LIVE · Sveriges Radio",
    imageUrl: channel.imageUrl ?? fallbackImage,
    live: true,
  });
  return {
    landing: { title: "Favoriter", items: channels.filter((channel) => favoriteIds.has(channel.id)).map(asItem) },
    inPlayer: {
      title: "Utvalda kanaler",
      items: channels.filter((channel) => channel.category !== "LOCAL_P4" || channel.id === DEFAULT_P4).map(asItem),
    },
  };
}
