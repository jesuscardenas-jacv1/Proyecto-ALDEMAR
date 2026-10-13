---
name: Patagonian Aquaculture Field System
colors:
  surface: '#f6fafe'
  surface-dim: '#d6dade'
  surface-bright: '#f6fafe'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f0f4f8'
  surface-container: '#eaeef2'
  surface-container-high: '#e4e9ed'
  surface-container-highest: '#dfe3e7'
  on-surface: '#171c1f'
  on-surface-variant: '#45464d'
  inverse-surface: '#2c3134'
  inverse-on-surface: '#edf1f5'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#515f74'
  on-secondary: '#ffffff'
  secondary-container: '#d5e3fd'
  on-secondary-container: '#57657b'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#00174b'
  on-tertiary-container: '#497cff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#d5e3fd'
  secondary-fixed-dim: '#b9c7e0'
  on-secondary-fixed: '#0d1c2f'
  on-secondary-fixed-variant: '#3a485c'
  tertiary-fixed: '#dbe1ff'
  tertiary-fixed-dim: '#b4c5ff'
  on-tertiary-fixed: '#00174b'
  on-tertiary-fixed-variant: '#003ea8'
  background: '#f6fafe'
  on-background: '#171c1f'
  surface-variant: '#dfe3e7'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 44px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.02em
  display-md:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  title-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-tablet: 1.5rem
  gutter-desktop: 2rem
  margin: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

The design system serves frontline operators, field biologists, and quality control supervisors operating in the demanding marine environment of southern Chile (Chiloé, Reloncaví, and Aysén fjords). The physical conditions are severe: direct marine sunlight, saltwater spray, overcast glare, and operations performed while wearing industrial waterproof gloves. 

The emotional tone balances absolute operational reliability, environmental sturdiness, and analytical rigor. It communicates utilitarian confidence rather than decorative consumer polish. Every pixel prioritizes speed of capture, zero input ambiguity, and immediate visual verification of mussel (*Mytilus chilensis*) sample traceability.

The visual direction follows **Industrial Material 3**:
- High-contrast structural boundaries designed to cut through water droplets and glare.
- Tactile, oversized touch zones (minimum 48dp, scaling to 56dp for high-frequency actions).
- Strict information hierarchy where critical biometric metrics (calibers, biomass, water salinity, harvest batch codes) take immediate precedence over ambient decoration.

## Colors

The color palette is calibrated for high-luminosity marine environments and outdoor tablet/handheld rugged devices. It rejects subtle pastels in favor of deep, definitive slate tones and high-chroma semantic indicators.

### Roles & Semantic Assignments
- **Primary (`#0F172A`)**: Slate deep navy. Used for top application bars, high-prominence primary actions, prominent Floating Action Buttons, and terminal action surfaces. Provides maximum outdoor contrast against the sky and ocean backdrops.
- **Secondary (`#334155`)**: Slate-700. Structural supporting elements, table header backgrounds, secondary control pills, and active navigation indicator capsules.
- **Tertiary (`#2563EB`)**: Technical blue. Informational callouts, active batch synchronizations, and the "Corregido" state indicator.
- **Background (`#F1F5F9`)**: Matte slate base. Eliminates the blinding glare of raw `#FFFFFF` under direct open-water sun while maintaining extreme contrast against foreground text.
- **Surface (`#FFFFFF`)**: Card surfaces, modal sheets, bottom sheets, and inspection overlays.
- **Text & Core Data (`#000000`)**: Key metrics, calibration weights, shell measurements, QR/RFID readouts, and primary titles. Strictly pure black for absolute optical density.
- **Border & Subtle Metadata (`#64748B`)**: Structural card outlines (1.5dp to 2dp), table grids, and input container borders.

### Field Operational Status Tokens
Operational states must be instantly distinguishable at arm's length without color-alone dependency:
- **Validado (`#16A34A`)**: Certified samples meeting sanitary and size thresholds.
- **Pendiente (`#D97706`)**: Awaiting laboratory or depuration verification.
- **Observado (`#DC2626`)**: Contamination, parasite presence, or broken shell alerts.
- **Corregido (`#2563EB`)**: Rectified batch log with supervisory override.

## Typography

Typography relies on **Inter** configured with tall x-heights, robust geometric counters, and tabular lining figures enabled by default (`tnum`) to ensure numeric telemetry (gram weight, length in mm, salinity, coordinates) does not cause layout jitter when reading live sensors or digital calipers.

### Hierarchy & Readability Rules
- **Numerical Dominance**: Primary biometric data values (e.g., sample weight `34.2g`) utilize `display-md` or `headline-lg` in bold weights (`700`) to remain readable when the device is placed on an inspection table or raft gunwale.
- **Elevated Body Weight**: Standard body text is elevated to weight `500` (Medium) instead of `400` (Regular) to compensate for bright maritime daylight washing out thin typographic stems.
- **Label Capitalization**: Technical status badges and operational prompts employ uppercase tracking on `label-sm` to maintain high legibility under dirty or scratched screen protectors.

## Layout & Spacing

The layout is built around a field-ready **8dp grid**, modified with a base spacing module that enforces physical finger clearance on handheld Android devices.

### Form Factor Behavior
- **Mobile (Compact, < 600dp)**: 4-column layout with 16dp margins. Bottom-anchored interactions via `NavigationBar` and bottom app sheets. All primary workflow CTAs stay within thumb reach.
- **Tablet (Medium, 600dp - 839dp)**: 8-column layout with 24dp margins. Split-screen master-detail mode: Mussel long-line harvesting grid on the left, sample entry form on the right.
- **Rugged Handheld / Desktop (Expanded, ≥ 840dp)**: 12-column fixed-gutter layout. Dedicated navigation rail, persistent data tables, and batch verification panes.

### Ergonomic Clearance
Gaps between interactive list items and cards adhere strictly to `space-md` (16dp). This spacing prevents accidental double-tap triggers when users handle devices with wet hands or heavy neoprene diving/harvesting gloves.

## Elevation & Depth

Marine and aquaculture software must avoid subtle, soft ambient shadows, which vanish entirely under midday maritime sunlight. Depth is established through **structural border definition combined with tactile tonal stepped elevation**.

### Tiers of Depth
- **Level 0 (Flat Surface / Canvas)**: `#F1F5F9`. The base ground for content scroll and container boundaries.
- **Level 1 (Card & Row Panels)**: `#FFFFFF` bounded by a structural 1.5dp border (`#64748B` at 30% opacity). Flat elevation; depth is defined by edge separation, not blur.
- **Level 2 (Active/Pressed Cards, Bottom Sheets)**: `#FFFFFF` resting on a 1dp top highlight and a crisp drop offset (`0px 4px 0px rgba(15, 23, 42, 0.08)` paired with `0px 2px 4px rgba(15, 23, 42, 0.12)`).
- **Level 3 (FAB, Menus, Modals)**: `#0F172A` (or `#FFFFFF` for dialogs) with a punchy, short-radius shadow (`0px 6px 12px rgba(15, 23, 42, 0.24)`). This creates distinct isolation from the background grid.

### Glare Defense Protocol
Never use layered transparency (blur effects or glassmorphism) over operational data. Surfaces must remain 100% opaque to preserve chromatic contrast ratios under direct sun exposure.

## Shapes

The design system adopts a **Rounded (Level 2)** geometry, balancing contemporary Material 3 contours with an intentional industrial feel.

- **Containers & Data Cards**: 8dp (`0.5rem`) corner radius. Keeps card corners distinct and prevents visual overlap in dense sampling matrices.
- **Inputs & Outlined Elements**: 8dp (`0.5rem`) for text boxes, segmented button containers, and list row wrappers.
- **State Badges & Chips**: 8dp (`0.5rem`) to 12dp rounded corners, retaining crisp corner recognition without softening into organic consumer pills.
- **Floating Action Buttons (FAB)**: 16dp (`rounded-lg`) corner radius, maintaining the hallmark Material 3 squircle silhouette for the primary sampling trigger.
- **Touch Target Boundary**: Internal icon or text elements may have smaller visual geometry, but their interactive bounding box enforces a strict minimum 48dp × 48dp boundary.

## Components

### TopAppBar (Material 3)
- **Background**: `#0F172A` (Primary Navy) with title text in `#FFFFFF`.
- **Height**: 64dp standard; collapses on scroll when inspecting lengthy batch records.
- **Metadata**: Center title contains module state (e.g., "Muestreo: Línea 04 - Sector Castro"); subtitle shows connection status ("Modo Offline: Sincronización Pendiente") in `#64748B` lightened to `#94A3B8`.
- **Navigation Icon**: 48dp hit box with high-contrast `#FFFFFF` icon.

### NavigationBar (Bottom Bar)
- **Surface**: `#FFFFFF` with a 1dp top divider line (`#64748B` at 25% opacity).
- **Active Pill**: `#334155` background capsule, displaying a pure white `#FFFFFF` icon.
- **Labels**: `label-md` weight `600`; active in `#0F172A`, inactive in `#64748B`.
- **Height**: 80dp to ensure comfortable bottom thumb access.

### FloatingActionButton (FAB)
- **Style**: Primary M3 Large FAB for rapid sample capture ("+ Muestra").
- **Container Color**: `#0F172A` with `#FFFFFF` label and vector glyph.
- **Dimensions**: 56dp × 56dp (standard) or Extended (56dp height, variable width with `label-lg`).
- **Elevation**: Level 3 with tactile active depression state.

### Outlined Cards & Data Tiles
- **Surface**: Pure `#FFFFFF`.
- **Border**: 1.5dp solid `#64748B` (40% opacity in normal state; turns to `#0F172A` 2dp when selected).
- **Internal Padding**: 16dp (`space-md`).
- **Data Display**: Metrics arranged in key-value format. Label in `body-sm` (`#64748B`), Value in `headline-sm` (`#000000`).

### OutlinedTextField
- **Target Height**: 56dp minimum container height.
- **Outlines**: 1.5dp stroke `#64748B`; active focus scales to 2dp `#0F172A`.
- **Typography**: Input text in `body-lg` (`#000000`, 600 weight); floating label in `label-md` (`#334155`).
- **Supporting / Error Text**: Persistent (never hidden), minimum `body-sm` (`#DC2626` for input errors).
- **Action Affordance**: Suffix icon buttons (barcode/RFID scanner trigger, clear field) must be 48dp touch targets.

### Assist & Filter Chips
- **Height**: 36dp container with 48dp touch target envelope.
- **Inactive**: `#FFFFFF` fill with 1.5dp `#64748B` border, text `#334155`.
- **Active/Selected**: `#334155` fill, text `#FFFFFF`, leading checkmark in `#FFFFFF`.
- **Corner Radius**: 8dp.

### Status Badges (Field Validation)
Industrial indicators for sample status verification:
- **Validado**: Container `#DCFCE7`, Border `#16A34A` (1.5dp), Text `#14532D`, Icon `check_circle`.
- **Pendiente**: Container `#FEF3C7`, Border `#D97706` (1.5dp), Text `#78350F`, Icon `schedule`.
- **Observado**: Container `#FEE2E2`, Border `#DC2626` (1.5dp), Text `#7F1D1D`, Icon `warning`.
- **Corregido**: Container `#DBEAFE`, Border `#2563EB` (1.5dp), Text `#1E3A8A`, Icon `published_with_changes`.
- **Typography**: `label-sm` uppercase, bold (`700`), 6dp horizontal internal padding.

### Checkboxes & Radio Controls
- **Hit Target**: Enclosed in a full 48dp × 48dp frame.
- **Border Width**: 2dp for clear visibility through dirty or wet screen covers.
- **Active State**: `#0F172A` check box fill with pure white tick mark.