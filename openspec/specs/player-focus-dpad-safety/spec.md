# Player Focus D-pad Safety

## Purpose

Crash-free and correct D-pad focus navigation for all video player UI states, including the compact seek overlay and side-sheet-open states.

## Requirements

### Requirement: UP/DOWN during compact seek exits to full UI without crash

When the video player is in compact seek mode (overlay showing only slider + timestamp after LEFT/RIGHT with hidden UI), pressing UP or DOWN SHALL safely exit compact mode and show the full control UI, rather than crashing due to dangling focus targets.

#### Scenario: UP during compact seek mode
- **WHEN** user presses LEFT or RIGHT with hidden UI to enter compact seek mode
- **AND** the slider has focus showing the compact overlay (slider + timestamp only)
- **AND** user presses UP on D-pad
- **THEN** the Slider SHALL consume the UP key event
- **AND** the full control UI SHALL become visible (header + slider + playback controls)
- **AND** the application SHALL NOT crash

#### Scenario: DOWN during compact seek mode
- **WHEN** the slider has focus in compact seek mode
- **AND** user presses DOWN on D-pad
- **THEN** the Slider SHALL consume the DOWN key event
- **AND** the full control UI SHALL become visible
- **AND** the application SHALL NOT crash

#### Scenario: LEFT/RIGHT during compact seek mode continue seeking
- **WHEN** the slider has focus in compact seek mode
- **AND** user presses LEFT or RIGHT on D-pad
- **THEN** the Slider SHALL seek by 10 seconds per press (existing behavior unchanged)

### Requirement: D-pad keys pass through to side sheets without triggering control UI

When a side sheet (episode selector, playback speed, or video resize) is visible, D-pad direction keys and the center/OK key SHALL be routed to the side sheet, NOT intercepted by the hidden-UI shortcut handler.

#### Scenario: UP/DOWN with episode sheet open
- **WHEN** the episode selector side sheet is open (`isEpisodeUiVisible = true`)
- **AND** the control overlay is hidden (`isControlUiVisible = false`)
- **AND** user presses UP or DOWN on D-pad
- **THEN** the key event SHALL pass through to the episode side sheet
- **AND** the control overlay SHALL NOT appear
- **AND** the episode side sheet SHALL remain open

#### Scenario: UP/DOWN with speed sheet open
- **WHEN** the playback speed side sheet is open (`isSpeedUiVisible = true`)
- **AND** the control overlay is hidden
- **AND** user presses UP or DOWN on D-pad
- **THEN** the key event SHALL pass through to the speed side sheet
- **AND** the control overlay SHALL NOT appear

#### Scenario: UP/DOWN with resize sheet open
- **WHEN** the video resize side sheet is open (`isResizeUiVisible = true`)
- **AND** the control overlay is hidden
- **AND** user presses UP or DOWN on D-pad
- **THEN** the key event SHALL pass through to the resize side sheet
- **AND** the control overlay SHALL NOT appear

#### Scenario: CENTER/OK with side sheet open
- **WHEN** any side sheet is open
- **AND** the control overlay is hidden
- **AND** user presses CENTER or Spacebar on D-pad
- **THEN** the key event SHALL pass through to the side sheet
- **AND** playback SHALL NOT pause
- **AND** the control overlay SHALL NOT appear

#### Scenario: LEFT/RIGHT with side sheet open
- **WHEN** any side sheet is open
- **AND** the control overlay is hidden
- **AND** user presses LEFT or RIGHT on D-pad
- **THEN** the key event SHALL pass through
- **AND** no timed seek SHALL occur
- **AND** the compact seek overlay SHALL NOT appear
