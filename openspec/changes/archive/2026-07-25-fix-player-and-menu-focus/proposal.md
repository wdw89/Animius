## Why

Multiple TV/handheld interaction bugs degrade the experience. The Back key requires two presses on tablet devices. Long-press context menus (downloads, favorites) lack focus highlighting and don't work reliably with D-pad. The D-pad OK button can't resume playback hidden UI. Context menu actions (delete) have no accessible D-pad alternative. The player control overlay appears on every launch, adding friction.

## What Changes

- **Context menus via Key.Menu**: Keep touch long-press, but use `Key.Menu` (remote) instead of unreliable D-pad long-press for opening popup menus on TV/handheld.
- **PopupMenu inverse color**: `DropdownMenuItem` in download/favorites menus display `primary`/`onPrimary` focus highlighting.
- **Player Back key**: Intercept `Key.Back` in root `onKeyEvent` to immediately hide UI on all devices.
- **D-pad OK toggle**: Hidden UI + OK → playing → pause + show UI; paused → play only.
- **Delete mode**: Favorites and download detail screens get a "delete" button (top-right). Activating it enters delete mode — clicking a card/row deletes it. Touch long-press + Menu key still available as shortcuts.
- **Download detail "anime detail" button**: Navigate from download detail back to the anime detail page.
- **Player starts without control UI**: Remove the `showControlUi()` call on initial load — controls appear only on user interaction (D-pad, touch).

## Capabilities

### New Capabilities

- `player-back-key-intercept`: Intercept Back key at root `onKeyEvent` to dismiss player UI immediately on all device types.
- `delete-mode`: Favorites and download detail screens support a toggleable delete mode accessible via a top-right button.

### Modified Capabilities

- `tv-focus-inverse-color`: `DropdownMenuItem` in download and favorites menus SHALL display inverse-color focus.
- `player-focus-navigation`: D-pad OK/Center when UI hidden SHALL toggle play/pause. Player starts playback without showing the control overlay.

## Impact

- `app/.../component/PopupMenuListItem.kt` — remove D-pad long-press, keep Menu key + inverse color
- `app/.../favourite/FavouriteScreen.kt` — remove D-pad long-press; add delete mode button + logic
- `app/.../downloaddetail/DownloadDetailScreen.kt` — add delete mode button + anime detail nav button
- `app/.../videoplayer/VideoPlayerScreen.kt` — Back key intercept, OK toggle
- `video-player/.../VideoPlayer.kt` — remove `showControlUi()` on initial load; remove debug logging
