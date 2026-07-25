## ADDED Requirements

### Requirement: Back key immediately hides player UI on all device types
When the player control overlay or any side sheet (speed, resize, episode) is visible, pressing the Back key SHALL immediately hide the visible UI component in a single press, regardless of device type (TV, tablet, phone). The Back key SHALL be intercepted at the root `onKeyEvent` level to bypass platform-specific `OnBackPressedDispatcher` behavior differences between TV and tablet Compose implementations.

#### Scenario: Back key hides control UI on tablet
- **WHEN** the player control overlay is visible on a tablet device
- **AND** user presses Back on D-pad, remote, or keyboard
- **THEN** the control overlay hides immediately (single press)
- **AND** focus is not lost before the UI disappears

#### Scenario: Back key closes side sheet on tablet
- **WHEN** a side sheet (speed, resize, or episode) is visible on a tablet device
- **AND** user presses Back on D-pad, remote, or keyboard
- **THEN** the side sheet closes immediately (single press)
- **AND** focus is not lost before the UI disappears

#### Scenario: Back key hides control UI on TV
- **WHEN** the player control overlay is visible on a TV device
- **AND** user presses Back on D-pad or remote
- **THEN** the control overlay hides immediately (single press)
- **AND** existing TV behavior is preserved (no regression)

#### Scenario: Back key closes side sheet on TV
- **WHEN** a side sheet (speed, resize, or episode) is visible on a TV device
- **AND** user presses Back on D-pad or remote
- **THEN** the side sheet closes immediately (single press)
- **AND** existing TV behavior is preserved (no regression)

### Requirement: Back key interception coexists with BackHandler for exit
The root `onKeyEvent` intercept for Back key SHALL only consume the event when player UI or side sheets are visible. When all UI is hidden, the Back key SHALL pass through to the existing `BackHandler` which triggers player exit.

#### Scenario: Back key exits player when all UI is hidden
- **WHEN** all player UI (control overlay, side sheets) is hidden
- **AND** user presses Back
- **THEN** the root `onKeyEvent` does NOT consume the Back event
- **AND** the existing `BackHandler` triggers player exit

#### Scenario: Back key only hides UI when multiple layers are visible
- **WHEN** both control overlay and a side sheet are visible (side sheet was opened from control overlay)
- **AND** user presses Back
- **THEN** the side sheet closes (highest priority)
- **AND** the control overlay remains hidden (does not reappear)
