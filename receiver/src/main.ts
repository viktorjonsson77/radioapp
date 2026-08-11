import "./style.css";
import { Channel, loadChannels } from "./channel";
import { createBrowsePlan } from "./browse/catalog";
import { toCastBrowseContent } from "./browse/castBrowse";
import { channelIdFromEntity, mapChannelToMedia } from "./metadata";

const status = document.querySelector<HTMLElement>("#receiver-status");
const preview = document.querySelector<HTMLElement>("#channel-preview");
const errorOverlay = document.querySelector<HTMLElement>("#error-overlay");

function log(event: string, data?: unknown): void {
  data === undefined ? console.info(`[RadioApp Receiver] ${event}`) : console.info(`[RadioApp Receiver] ${event}`, data);
}

function showError(message: string): void {
  console.error(`[RadioApp Receiver] ${message}`);
  if (errorOverlay) { errorOverlay.textContent = message; errorOverlay.hidden = false; }
}

function showBrowserPreview(channels: Channel[]): void {
  if (status) status.textContent = "Browserläge – inte en Cast-emulator";
  if (preview) {
    preview.replaceChildren(...channels.map((channel) => {
      const card = document.createElement("div");
      card.className = "preview-card";
      card.textContent = channel.name;
      return card;
    }));
  }
}

async function bootstrap(): Promise<void> {
  log("startup");
  const receiverBase = import.meta.env.BASE_URL;
  let channels: Channel[];
  try {
    channels = await loadChannels(`${receiverBase}generated/channels.json`);
  } catch (error) {
    showError(`Kanallistan kunde inte läsas: ${String(error)}`);
    return;
  }

  if (typeof cast === "undefined" || !cast.framework) {
    showBrowserPreview(channels);
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

  controls.setBrowseContent(toCastBrowseContent(plan.landing));

  playerDataBinder.addEventListener(cast.framework.ui.PlayerDataEventType.STATE_CHANGED, (event: any) => {
    log(`player state: ${String(event.value)}`);
    if (event.value === cast.framework.ui.State.PLAYING) log("play");
    if (event.value === cast.framework.ui.State.PAUSED) log("pause");
    if (event.value === cast.framework.ui.State.IDLE) log("stop");
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
