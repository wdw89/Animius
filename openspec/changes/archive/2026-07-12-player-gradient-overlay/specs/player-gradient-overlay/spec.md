## ADDED Requirements

### Requirement: Player overlay uses vertical gradient backgrounds

The video player control overlay SHALL render vertical gradient backgrounds at the top and bottom edges instead of a uniform semi-transparent background, providing stronger contrast for white icons and text against bright video content while keeping the center of the video fully visible.

#### Scenario: Top gradient provides contrast for header controls

- **WHEN** the player control overlay is visible
- **THEN** the top edge of the overlay SHALL display a vertical gradient from `Color.Black.copy(0.55f)` at the top to `Color.Transparent` at approximately 35% from the top
- **AND** the back button, title text, and subtitle text in the header SHALL be clearly visible against any underlying video content

#### Scenario: Bottom gradient provides contrast for playback controls

- **WHEN** the player control overlay is visible
- **THEN** the bottom edge of the overlay SHALL display a vertical gradient from `Color.Transparent` at approximately 65% from the top to `Color.Black.copy(0.55f)` at the bottom
- **AND** the playback buttons, timestamp text, slider, and text buttons SHALL be clearly visible against any underlying video content

#### Scenario: Center of video remains unobstructed

- **WHEN** the player control overlay is visible
- **THEN** the middle portion of the overlay (approximately 35% to 65% from the top) SHALL be fully transparent
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

### Requirement: Existing background parameter remains functional

The `background` parameter on `VideoPlayerControl` SHALL remain in the function signature without breaking changes, ensuring backward compatibility for callers that pass a custom background color.

#### Scenario: Caller passes custom background color

- **WHEN** a caller passes a custom `background` parameter to `VideoPlayerControl`
- **THEN** the composable SHALL honor the custom background and not render the default gradient overlays

#### Scenario: Caller does not pass background parameter

- **WHEN** a caller invokes `VideoPlayerControl` without overriding the `background` parameter
- **THEN** the composable SHALL render the default vertical gradient overlays (top and bottom)
