# Player Focus Navigation (Delta)

## MODIFIED Requirements

### Requirement: Initial focus on control show

When the player control overlay becomes visible via D-pad input or initial load, focus SHALL be placed on the appropriate control. When any side sheet (episode, speed, or resize) is visible, the hidden-UI shortcut handler SHALL NOT intercept D-pad keys — the keys SHALL pass through to the side sheet.

#### Scenario: Default focus on initial player load
- **WHEN** the player first opens
- **AND** the control UI is visible
- **THEN** focus is on the play/pause button

#### Scenario: Left key with hidden UI
- **WHEN** user presses LEFT on D-pad while controls are hidden
- **AND** no side sheet is visible
- **THEN** playback seeks backward 10 seconds
- **AND** a compact overlay appears showing the progress slider and timestamp
- **AND** the full control overlay (header, playback buttons) does NOT appear
- **AND** focus is placed on the slider so subsequent LEFT presses continuously seek

#### Scenario: Right key with hidden UI
- **WHEN** user presses RIGHT on D-pad while controls are hidden
- **AND** no side sheet is visible
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
- **AND** no side sheet is visible
- **THEN** the control overlay becomes visible
- **AND** focus is placed on the Forward 85s button in the header row

#### Scenario: Controls shown via down key
- **WHEN** user presses DOWN on D-pad while controls are hidden
- **AND** no side sheet is visible
- **THEN** the control overlay becomes visible
- **AND** focus is placed on the "选集" (episode) text button in the playback control row

#### Scenario: Controls shown via OK/Center key
- **WHEN** user presses OK/Center on D-pad while controls are hidden
- **AND** the video is currently playing
- **AND** no side sheet is visible
- **THEN** playback pauses
- **AND** the control overlay becomes visible
- **AND** focus is placed on the play/pause button

#### Scenario: UP/DOWN key during compact seek mode exits to full UI
- **WHEN** the slider has focus in compact seek mode (after LEFT/RIGHT with hidden UI)
- **AND** the compact overlay is showing (slider + timestamp only)
- **AND** user presses UP or DOWN on D-pad
- **THEN** the Slider SHALL consume the key event and exit compact mode
- **AND** the full control UI SHALL become visible
- **AND** the application SHALL NOT crash from dangling focus targets

#### Scenario: Hidden-UI shortcuts suppressed when side sheet is open
- **WHEN** any side sheet (episode, speed, resize) is visible
- **AND** the control overlay is hidden
- **AND** user presses any D-pad direction key or CENTER/Spacebar
- **THEN** the hidden-UI shortcut handler SHALL NOT intercept the key
- **AND** the key event SHALL pass through to the side sheet for its own navigation
