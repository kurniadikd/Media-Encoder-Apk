# Material 3 Expressive (M3 Expressive) Design System Rules

Every user interface component created or modified in this repository MUST strictly follow the official Material 3 Expressive guidelines (https://m3.material.io).

## Core Principles

### 1. Flat Solid Style (No Thin Outlines)
- Never use thin border lines or outlines on Cards, Chips, Buttons, or Surfaces (`border = null`).
- Rely on rich, solid container fills (`primaryContainer`, `secondaryContainer`, `tertiaryContainer`, `surfaceVariant`).

### 2. Expressive Shapes & Extra-Rounded Radii
- **Hero / Card Containers**: `28.dp` or `24.dp` rounded corners.
- **Speed Dial FAB / Action Items**: `20.dp` rounded corners.
- **Chips & Dropdowns**: `16.dp` or `14.dp` rounded corners.
- **Floating Action Buttons**: `CircleShape` or `28.dp` extra-rounded shape.

### 3. Native M3 Motion Physics & Icon Morphing
- **Spring Animations**: Use Compose `spring(stiffness = Spring.StiffnessMediumLow)` for fluid, natural physics.
- **FAB Icon Morphing**: Rotate standard `+` icon 135 degrees smoothly via `graphicsLayer { rotationZ = fabRotation }` to transition to an `x` cancel state.
- **Staggered Entrance**: Use `slideInVertically` + `scaleIn(0.8f)` + `fadeIn` for Speed Dial items.
- **Backdrop Scrim**: Include semi-transparent backdrop overlay (`Color.Black.copy(alpha = 0.35f)`) when speed dial or modal states expand.

### 4. Expressive Progress Indicators
- Use **Circular Wavy Progress Indicators** (`M3ExpressiveCircularWavyProgressIndicator`) encircling media type icon badges with sinusoidal wave math (`sin()`).

### 5. Form Input Controls
- Use **ExposedDropdownMenuBox** with `OutlinedTextField` for clean parameter selections.
- Use **M3 Sliders** with live numerical value indicators for continuous parameters (e.g. Bitrate, Quality).
- Use **SegmentedButton** or **FilterChip** groups for discrete option toggles.

### 6. Edge-to-Edge Synergy
- Ensure `enableEdgeToEdge()` is active in Activity so system status bar and navigation bar seamlessly blend with the app background color.
