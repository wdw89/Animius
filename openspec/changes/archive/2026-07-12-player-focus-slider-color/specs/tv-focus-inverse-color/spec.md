## ADDED Requirements

### Requirement: Slider focus indicated by thumb animation
The `Slider` component SHALL indicate D-pad focus through thumb animations (size increase + white border). The track colors (play progress, buffered, unplayed) SHALL remain unchanged when focus changes.

#### Scenario: Slider receives D-pad focus
- **WHEN** D-pad navigation focuses the slider
- **THEN** the thumb grows from 15dp to 20dp
- **AND** a 3dp white border appears around the thumb
- **AND** the track colors (play progress, buffered, unplayed) remain unchanged

#### Scenario: Slider loses D-pad focus
- **WHEN** D-pad focus moves away from the slider
- **THEN** the thumb shrinks from 20dp to 15dp
- **AND** the white border disappears
- **AND** the track colors remain unchanged

#### Scenario: Touch interaction on slider
- **WHEN** user touches or drags the slider on a touch-only device
- **THEN** the slider does NOT show the focus thumb animation
- **AND** the standard touch interaction (drag/tap) works normally

### Requirement: Side sheet selected item is visually prominent
The currently selected item in the speed and resize side sheets SHALL display in `primary` color AND bold font weight to clearly distinguish it from unselected items.

#### Scenario: Selected speed option is bold
- **WHEN** the speed side sheet is open
- **AND** a speed option (e.g., "1.0X") is the currently active value
- **THEN** that item's text is displayed in `primary` color AND `FontWeight.Bold`

#### Scenario: Selected resize option is bold
- **WHEN** the resize side sheet is open
- **AND** a resize option (e.g., "适应") is the currently active value
- **THEN** that item's text is displayed in `primary` color AND `FontWeight.Bold`
