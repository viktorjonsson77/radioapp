import { BrowseSectionModel } from "./catalog";

export function toCastBrowseContent(section: BrowseSectionModel): object {
  const items = section.items.slice(0, 30).map((model) => {
    const item = new cast.framework.ui.BrowseItem();
    item.entity = model.entity;
    item.title = model.title;
    item.subtitle = model.subtitle;
    item.imageType = cast.framework.ui.BrowseImageType.MUSIC_TRACK;
    item.mediaBadge = cast.framework.ui.BrowseMediaBadge.LIVE;
    item.image = new cast.framework.messages.Image(model.imageUrl);
    return item;
  });
  const content = new cast.framework.ui.BrowseContent();
  content.title = section.title;
  content.items = items;
  content.targetAspectRatio = cast.framework.ui.BrowseImageAspectRatio.SQUARE_1_TO_1;
  return content;
}
