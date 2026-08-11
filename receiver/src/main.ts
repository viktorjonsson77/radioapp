import "./style.css";
import { loadChannels } from "./channel";
import type { Channel } from "./channel";
import { groupChannelsForBrowser } from "./browser/catalog";
import { createBrowsePlan } from "./browse/catalog";
import { toCastBrowseContent } from "./browse/castBrowse";
import { channelIdFromEntity, mapChannelToMedia, metadataRefreshDelayMs, SrMetadataProvider } from "./metadata";
import type { NowPlayingMetadata } from "./metadata";

const status = document.querySelector<HTMLElement>("#receiver-status");
const errorOverlay = document.querySelector<HTMLElement>("#error-overlay");
const nowArtwork = document.querySelector<HTMLImageElement>("#now-artwork");
const nowChannel = document.querySelector<HTMLElement>("#now-channel");
const nowProgram = document.querySelector<HTMLElement>("#now-program");
const nowDescription = document.querySelector<HTMLElement>("#now-description");
const nowLiveRow = document.querySelector<HTMLElement>("#now-live-row");
const nowTime = document.querySelector<HTMLElement>("#now-time");
const nowNext = document.querySelector<HTMLElement>("#now-next");
const metadataProvider = new SrMetadataProvider();
let errorTimer: number | undefined;

function log(event: string, data?: unknown): void {
  data === undefined ? console.info(`[RadioApp Receiver] ${event}`) : console.info(`[RadioApp Receiver] ${event}`, data);
}

function showError(message: string, persistent = false): void {
  console.error(`[RadioApp Receiver] ${message}`);
  if (!errorOverlay) return;
  if (errorTimer !== undefined) window.clearTimeout(errorTimer);
  errorOverlay.textContent = message;
  errorOverlay.hidden = false;
  if (!persistent) {
    errorTimer = window.setTimeout(() => { errorOverlay.hidden = true; }, 6_000);
  }
}

function showBrowserPreview(channels: Channel[], fallbackImage: string): void {
  if (status) status.textContent = "Webbläge · inte Cast";
  const groups = groupChannelsForBrowser(channels);
  const audio = new Audio();

  const select = async (channel: Channel): Promise<void> => {
    document.querySelectorAll<HTMLElement>(".channel-card").forEach((card) => {
      card.classList.toggle("is-playing", card.dataset.channelId === channel.id);
      card.setAttribute("aria-pressed", String(card.dataset.channelId === channel.id));
      const subtitle = card.querySelector("small");
      if (subtitle) subtitle.textContent = card.dataset.channelId === channel.id ? "LIVE · Spelas nu" : card.dataset.subtitle ?? "Sveriges Radio";
    });
    updateBrowserNowPlaying(channel, null, fallbackImage);
    audio.src = channel.streamUrl;
    void audio.play().catch((error) => {
      log("browser playback failed", error);
      showError("Ljudet kunde inte startas i webbläsaren.");
    });
    try {
      const program = await metadataProvider.nowPlaying(channel);
      updateBrowserNowPlaying(channel, program, fallbackImage);
    } catch (error) {
      log("browser metadata fallback", error);
    }
  };

  const render = (targetId: string, items: Channel[], variant = ""): void => {
    const target = document.querySelector<HTMLElement>(`#${targetId}`);
    target?.replaceChildren(...items.map((channel) => createBrowserChannelCard(channel, fallbackImage, variant, () => void select(channel))));
  };

  render("favorite-preview", groups.favorites, "favorite-card");
  render("national-preview", groups.national);
  render("p4-preview", groups.p4);
  render("other-preview", groups.other);
  if (groups.defaultP4) render("p4-default", [groups.defaultP4], "default-card");

  const p4Toggle = document.querySelector<HTMLButtonElement>("#p4-toggle");
  const p4Preview = document.querySelector<HTMLElement>("#p4-preview");
  p4Toggle?.addEventListener("click", () => {
    const expanded = p4Toggle.getAttribute("aria-expanded") !== "true";
    p4Toggle.setAttribute("aria-expanded", String(expanded));
    p4Toggle.textContent = expanded ? "Dölj" : "Visa alla";
    if (p4Preview) p4Preview.hidden = !expanded;
  });
}

function createBrowserChannelCard(
  channel: Channel,
  fallbackImage: string,
  variant: string,
  onSelect: () => void,
): HTMLButtonElement {
  const card = document.createElement("button");
  card.type = "button";
  card.className = `channel-card ${variant}`.trim();
  card.dataset.channelId = channel.id;
  card.dataset.subtitle = channel.isLocal ? `Lokalt · ${channel.region?.name ?? channel.name}` : "Sveriges Radio";
  card.setAttribute("aria-label", `Spela ${channel.name}`);
  card.setAttribute("aria-pressed", "false");

  const image = document.createElement("img");
  image.src = channel.imageUrl ?? fallbackImage;
  image.alt = "";
  image.addEventListener("error", () => { image.src = fallbackImage; }, { once: true });
  const copy = document.createElement("span");
  copy.className = "channel-card-copy";
  const title = document.createElement("strong");
  title.textContent = channel.name;
  const subtitle = document.createElement("small");
  subtitle.textContent = card.dataset.subtitle;
  copy.append(title, subtitle);
  card.append(image, copy);
  card.addEventListener("click", onSelect);
  return card;
}

function updateBrowserNowPlaying(channel: Channel, program: NowPlayingMetadata | null, fallbackImage: string): void {
  if (nowArtwork) {
    nowArtwork.src = program?.imageUrl ?? channel.imageUrl ?? fallbackImage;
    nowArtwork.alt = program?.programName ?? channel.name;
    nowArtwork.onerror = () => {
      nowArtwork.onerror = null;
      nowArtwork.src = fallbackImage;
    };
  }
  if (nowChannel) nowChannel.textContent = channel.name;
  if (nowProgram) nowProgram.textContent = program?.programName ?? "Sveriges Radio";
  if (nowDescription) {
    nowDescription.textContent = program?.programDescription ?? "";
    nowDescription.hidden = !program?.programDescription;
  }
  if (nowLiveRow) nowLiveRow.hidden = false;
  if (nowTime) nowTime.textContent = formatProgramTime(program);
  if (nowNext) {
    const next = program?.nextProgram;
    if (next) {
      const label = document.createElement("strong");
      label.textContent = `Nästa${next.startsAt ? ` · ${formatTime(next.startsAt)}` : ""}`;
      const name = document.createElement("span");
      name.textContent = next.name;
      nowNext.replaceChildren(label, name);
      nowNext.hidden = false;
    } else {
      nowNext.replaceChildren();
      nowNext.hidden = true;
    }
  }
  document.title = `${channel.name} · RadioApp`;
}

function formatProgramTime(program: NowPlayingMetadata | null): string {
  return program?.startsAt && program.endsAt ? `${formatTime(program.startsAt)}–${formatTime(program.endsAt)}` : "";
}

function formatTime(date: Date): string {
  return new Intl.DateTimeFormat("sv-SE", { hour: "2-digit", minute: "2-digit" }).format(date);
}

async function bootstrap(): Promise<void> {
  log("startup");
  const receiverBase = import.meta.env.BASE_URL;
  let channels: Channel[];
  try {
    channels = await loadChannels(`${receiverBase}generated/channels.json`);
  } catch (error) {
    log("channel catalog unavailable", error);
    showError("Kanalerna kunde inte hämtas.", true);
    return;
  }

  if (typeof cast === "undefined" || !cast.framework) {
    showBrowserPreview(channels, `${receiverBase}assets/radioapp-channel.svg`);
    return;
  }

  document.body.classList.add("cast-runtime");
  const context = cast.framework.CastReceiverContext.getInstance();
  const playerManager = context.getPlayerManager();
  const controls = cast.framework.ui.Controls.getInstance();
  const artworkUrl = `${receiverBase}assets/radioapp-channel.svg`;
  const plan = createBrowsePlan(channels, undefined, artworkUrl);
  const playerData = new cast.framework.ui.PlayerData();
  const playerDataBinder = new cast.framework.ui.PlayerDataBinder(playerData);
  let metadataTimer: number | undefined;
  let activeMetadataChannelId: string | null = null;

  const scheduleMetadata = (channel: Channel): void => {
    if (metadataTimer !== undefined) window.clearTimeout(metadataTimer);
    activeMetadataChannelId = channel.id;
    const refresh = async (): Promise<void> => {
      let delay = 120_000;
      try {
        const program = await metadataProvider.nowPlaying(channel);
        delay = metadataRefreshDelayMs(program);
        const media = playerManager.getMediaInformation();
        if (media?.entity === `radioapp://channel/${channel.id}`) {
          media.metadata = mapChannelToMedia(channel, artworkUrl, program).metadata;
          playerManager.broadcastStatus(true);
          log("metadata updated", { channel: channel.id, program: program?.programName });
        } else delay = 1_000;
      } catch (error) {
        log("metadata unavailable; using channel fallback", error);
      }
      if (activeMetadataChannelId !== channel.id) return;
      metadataTimer = window.setTimeout(() => void refresh(), delay);
    };
    void refresh();
  };

  controls.setBrowseContent(toCastBrowseContent(plan.landing));

  playerDataBinder.addEventListener(cast.framework.ui.PlayerDataEventType.STATE_CHANGED, (event: any) => {
    log(`player state: ${String(event.value)}`);
    if (event.value === cast.framework.ui.State.PLAYING) log("play");
    if (event.value === cast.framework.ui.State.PAUSED) log("pause");
    if (event.value === cast.framework.ui.State.IDLE) {
      log("stop");
      activeMetadataChannelId = null;
      if (metadataTimer !== undefined) window.clearTimeout(metadataTimer);
    }
    const idle = event.value === cast.framework.ui.State.IDLE || event.value === cast.framework.ui.State.LAUNCHING;
    controls.setBrowseContent(toCastBrowseContent(idle ? plan.landing : plan.inPlayer));
  });

  playerManager.setMessageInterceptor(cast.framework.messages.MessageType.LOAD, (request: any) => {
    log("media load", request?.media?.entity ?? request?.media?.contentId);
    const entity = request?.media?.entity;
    if (typeof entity === "string") {
      const channelId = channelIdFromEntity(entity);
      const channel = channels.find((candidate) => candidate.id === channelId);
      if (!channel) throw new Error(`Unknown browse entity: ${entity}`);
      request.media = { ...request.media, ...mapChannelToMedia(channel, artworkUrl) };
      scheduleMetadata(channel);
    }
    return request;
  });

  context.addEventListener(cast.framework.system.EventType.READY, () => {
    const capabilities = context.getDeviceCapabilities() ?? {};
    const touchKey = cast.framework.system.DeviceCapabilities.TOUCH_INPUT_SUPPORTED;
    const touchSupported = capabilities[touchKey] === true;
    document.body.dataset.touch = String(touchSupported);
    log("ready", { touchSupported, capabilities });
  });
  context.addEventListener(cast.framework.system.EventType.SENDER_CONNECTED, (event: any) => log("sender connected", event.senderId));
  context.addEventListener(cast.framework.system.EventType.SENDER_DISCONNECTED, (event: any) => log("sender disconnect", event.reason));

  const mediaElement = document.querySelector("cast-media-player")?.shadowRoot?.querySelector("audio,video") ?? document.querySelector("audio,video");
  mediaElement?.addEventListener("play", () => log("play"));
  mediaElement?.addEventListener("pause", () => log("pause"));
  mediaElement?.addEventListener("ended", () => log("stop"));
  mediaElement?.addEventListener("error", () => showError("Stream error"));

  const options = new cast.framework.CastReceiverOptions();
  options.statusText = "Sveriges Radio";
  options.uiConfig = { touchScreenOptimizedApp: true };
  context.start(options);
}

void bootstrap().catch((error) => showError(`Receiver startup failed: ${String(error)}`));
