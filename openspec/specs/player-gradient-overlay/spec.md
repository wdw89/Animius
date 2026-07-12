## Requirements

### Requirement: Player overlay uses vertical gradient backgrounds

The video player control overlay SHALL render a vertical gradient background at the top and bottom edges instead of a uniform semi-transparent background, providing stronger contrast for white icons and text against bright video content while keeping the center of the video fully visible.

#### Scenario: Top gradient provides contrast for header controls

- **WHEN** the player control overlay is visible
- **THEN** the top edge of the overlay SHALL display a vertical gradient from `Color.Black.copy(0.55f)` at the top to `Color.Transparent` at approximately 25% from the top
- **AND** the back button, title text, and subtitle text in the header SHALL be clearly visible against any underlying video content

#### Scenario: Bottom gradient provides contrast for playback controls

- **WHEN** the player control overlay is visible
- **THEN** the bottom edge of the overlay SHALL display a vertical gradient from `Color.Transparent` at approximately 75% from the top to `Color.Black.copy(0.55f)` at the bottom
- **AND** the playback buttons, timestamp text, slider, and text buttons SHALL be clearly visible against any underlying video content

#### Scenario: Center of video remains unobstructed

- **WHEN** the player control overlay is visible
- **THEN** the middle portion of the overlay (approximately 25% to 75% from the top) SHALL be fully transparent
- **AND** the video content in this region SHALL not be dimmed or obscured by the overlay

#### Scenario: Gradient overlay on pure white video frame

- **WHEN** the underlying video content is pure white (#FFFFFF)
- **AND** the player control overlay is visible
- **THEN** all control icons and text SHALL be readable with sufficient contrast
- **AND** no control element SHALL blend into the white background

#### Scenario: Gradient overlay on very dark video frame

- **WHEN** the underlying video content is very dark or black
- **AND** the player control overlay is visible
- **THEN** the gradient edges SHALL blend seamlessly with the dark video content
- **AND** white icons and text SHALL remain clearly visible without appearing overly bright

### Requirement: Gradient hidden during seeking

During seeking (D-pad left/right or touch slider drag), the gradient background SHALL be hidden, displaying only the progress slider without any additional overlay.

#### Scenario: Gradient hidden during D-pad seeking

- **WHEN** the control overlay is visible
- **AND** the user presses D-pad LEFT or RIGHT to seek
- **THEN** the gradient background SHALL disappear
- **AND** only the slider SHALL remain visible (compact mode)

#### Scenario: Gradient hidden during touch slider seeking

- **WHEN** the control overlay is visible
- **AND** the user drags the slider thumb
- **THEN** the gradient background SHALL disappear
- **AND** only the slider SHALL remain visible

#### Scenario: Gradient reappears when full UI is shown

- **WHEN** the control overlay is in compact mode (seeking, gradient hidden)
- **AND** the user taps the screen to show the full control UI
- **THEN** the gradient background SHALL reappear along with the full control UI
