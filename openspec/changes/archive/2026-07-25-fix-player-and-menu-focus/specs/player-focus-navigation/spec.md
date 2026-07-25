## MODIFIED Requirements

### Requirement: Controls shown via OK/Center key
When the control UI is hidden and the user presses OK/Center on D-pad, the system SHALL toggle play/pause: pause if currently playing, play if currently paused. After toggling, the control overlay SHALL become visible with focus placed on the play/pause button. This mirrors the touch double-tap behavior.

#### Scenario: OK/Center pauses while playing with hidden UI
- **WHEN** user presses OK/Center on D-pad while controls are hidden
- **AND** the video is currently playing
- **AND** no side sheet is visible
- **THEN** playback pauses
- **AND** the control overlay becomes visible
- **AND** focus is placed on the play/pause button

#### Scenario: OK/Center resumes while paused with hidden UI
- **WHEN** user presses OK/Center on D-pad while controls are hidden
- **AND** the video is currently paused
- **AND** no side sheet is visible
- **THEN** playback resumes
- **AND** the control overlay becomes visible
- **AND** focus is placed on the play/pause button
