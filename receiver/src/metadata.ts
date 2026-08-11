import { Channel, contentType } from "./channel";

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

export function channelEntity(channelId: string): string { return `radioapp://channel/${channelId}`; }

export function channelIdFromEntity(entity: string): string | null {
  const prefix = "radioapp://channel/";
  return entity.startsWith(prefix) ? entity.slice(prefix.length) : null;
}

export function mapChannelToMedia(channel: Channel, artworkUrl = "./assets/radioapp-channel.svg"): ReceiverMediaInformation {
  return {
    contentId: channel.streamUrl,
    contentUrl: channel.streamUrl,
    contentType: contentType(channel),
    streamType: "LIVE",
    entity: channelEntity(channel.id),
    metadata: {
      metadataType: 3,
      title: channel.name,
      artist: "Sveriges Radio",
      subtitle: "Direktsänd radio",
      images: [{ url: channel.imageUrl ?? artworkUrl }],
    },
  };
}
