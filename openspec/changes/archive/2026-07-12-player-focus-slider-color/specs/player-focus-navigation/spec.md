## ADDED Requirements

### Requirement: Vertical focus flows between player control rows
Interactive elements in the video player control overlay SHALL transfer focus in visual row order (header → slider → playback controls) when the user presses up/down on D-pad, regardless of horizontal position differences between elements in adjacent rows.

#### Scenario: Down from header row to slider
- **WHEN** D-pad focus is on any focusable element in the header row (back button, forward button, or more options button)
- **THEN** pressing DOWN moves focus to the slider

#### Scenario: Down from slider to playback controls
- **WHEN** D-pad focus is on the slider
- **THEN** pressing DOWN moves focus to the play/pause button in the playback control row

#### Scenario: Up from slider to header (TV)
- **WHEN** D-pad focus is on the slider on a TV device (fullscreen button hidden)
- **THEN** pressing UP moves focus to the back button in the header row

#### Scenario: Up from slider to fullscreen button (non-TV)
- **WHEN** D-pad focus is on the slider on a non-TV device (fullscreen button visible)
- **THEN** pressing UP moves focus to the fullscreen button in the timeline row
- **AND** default spatial algorithm handles this naturally (fullscreen button sits directly above slider's right side)

#### Scenario: Up from playback controls to slider
- **WHEN** D-pad focus is on any button in the playback control row
- **THEN** pressing UP moves focus to the slider

#### Scenario: Horizontal navigation within playback control row
- **WHEN** D-pad focus is on a button in the playback control row
- **THEN** pressing LEFT or RIGHT moves focus between buttons within the same row (standard spatial navigation)

### Requirement: Slider D-pad seeking at 10-second step
When the full control UI is visible and the slider has D-pad focus, LEFT/RIGHT SHALL seek by 10 seconds. The slider's `onKeyEvent` uses `onClick` (not `onValueChange` + `onValueChangeFinished`) so that `isSeeking` is not toggled and the compact overlay stays compact during continuous seek.

#### Scenario: Left on focused slider seeks 10s back
- **WHEN** D-pad focus is on the slider
- **AND** the full control UI is visible
- **THEN** pressing LEFT seeks backward by 10 seconds

#### Scenario: Right on focused slider seeks 10s forward
- **WHEN** D-pad focus is on the slider
- **AND** the full control UI is visible
- **THEN** pressing RIGHT seeks forward by 10 seconds

#### Scenario: Continuous seek via repeated presses
- **WHEN** D-pad focus is on the slider
- **AND** the user presses LEFT or RIGHT multiple times in succession
- **THEN** each press seeks by 10 seconds
- **AND** the compact overlay remains visible (does not flash to full UI)

#### Scenario: Slider thumb animates on focus
- **WHEN** D-pad focus enters the slider
- **THEN** the thumb grows from 15dp to 20dp with animation
- **AND** a 3dp white border appears around the thumb
- **AND** the `onFocusChanged` modifier is placed before `.focusable()` so the focus callback fires

### Requirement: Initial focus on control show
When the player control overlay becomes visible via D-pad input or initial load, focus SHALL be placed on the appropriate control.

#### Scenario: Default focus on initial player load
- **WHEN** the player first opens
- **AND** the control UI is visible
- **THEN** focus is on the play/pause button

#### Scenario: Left key with hidden UI
- **WHEN** user presses LEFT on D-pad while controls are hidden
- **THEN** playback seeks backward 10 seconds
- **AND** a compact overlay appears showing the progress slider and timestamp
- **AND** the full control overlay (header, playback buttons) does NOT appear
- **AND** focus is placed on the slider so subsequent LEFT presses continuously seek

#### Scenario: Right key with hidden UI
- **WHEN** user presses RIGHT on D-pad while controls are hidden
- **THEN** playback seeks forward 10 seconds
- **AND** a compact overlay appears showing the progress slider and timestamp
- **AND** the full control overlay (header, playback buttons) does NOT appear
- **AND** focus is placed on the slider so subsequent RIGHT presses continuously seek

#### Scenario: Continuous seek from hidden UI
- **WHEN** user presses LEFT or RIGHT while controls are hidden
- **AND** then continues pressing LEFT or RIGHT
- **THEN** each subsequent press seeks by 10 seconds
- **AND** the compact overlay remains visible
- **AND** the slider thumb shows focus animation (grow + white border)

#### Scenario: Controls shown via up key
- **WHEN** user presses UP on D-pad while controls are hidden
- **THEN** the control overlay becomes visible
- **AND** focus is placed on the Forward 85s button in the header row

#### Scenario: Controls shown via down key
- **WHEN** user presses DOWN on D-pad while controls are hidden
- **THEN** the control overlay becomes visible
- **AND** focus is placed on the "选集" (episode) text button in the playback control row

#### Scenario: Controls shown via OK/Center key
- **WHEN** user presses OK/Center on D-pad while controls are hidden
- **AND** the video is currently playing
- **THEN** playback pauses
- **AND** the control overlay becomes visible
- **AND** focus is placed on the play/pause button

### Requirement: Back key hides player control UI
When the player control overlay is visible, pressing the Back key SHALL hide the overlay instead of exiting the player.

#### Scenario: Back key with UI visible
- **WHEN** the player control overlay is visible
- **AND** user presses Back on D-pad or remote
- **THEN** the control overlay hides
- **AND** the player does NOT exit

#### Scenario: Back key with UI hidden
- **WHEN** the player control overlay is hidden
- **AND** user presses Back on D-pad or remote
- **THEN** the player exits (standard back navigation)

### Requirement: Fullscreen button hidden on TV
The fullscreen toggle button in the `TimelineControl` row SHALL NOT be rendered on TV devices (Android TV / Leanback).

#### Scenario: TimelineControl on TV
- **WHEN** the player control overlay is rendered on a TV device (`FEATURE_LEANBACK`)
- **THEN** the fullscreen toggle button is not visible
- **AND** the timestamp text occupies its natural position

### Requirement: Side sheets receive initial focus
When the speed or resize side sheet opens, focus SHALL be automatically placed on the first selectable item so D-pad users can navigate immediately.

#### Scenario: Speed side sheet opens
- **WHEN** user activates the speed button ("倍速") with OK key
- **THEN** the speed side sheet appears
- **AND** focus is on the first speed option (e.g., "2.0X")

#### Scenario: Resize side sheet opens
- **WHEN** user activates the resize button ("适应") with OK key
- **THEN** the resize side sheet appears
- **AND** focus is on the first resize option (e.g., "适应")

#### Scenario: Back key closes side sheet
- **WHEN** a side sheet (speed or resize) is visible
- **AND** user presses Back
- **THEN** the side sheet closes
- **AND** focus returns to the main control UI

### Requirement: Auto-hide timer resets on D-pad input
Any D-pad key event (up, down, left, right, center, back) SHALL reset the player UI auto-hide timer so the UI remains visible while the user is actively navigating with a remote.

#### Scenario: D-pad navigation keeps UI alive
- **WHEN** the player control UI is visible
- **AND** user presses any D-pad direction key
- **THEN** the auto-hide timer resets
- **AND** the UI does NOT disappear during active navigation

#### Scenario: Browsing dropdown menu keeps UI alive
- **WHEN** the MoreVert dropdown menu is open
- **AND** user presses UP or DOWN to browse menu items
- **THEN** the auto-hide timer resets
- **AND** the UI and dropdown menu remain visible
