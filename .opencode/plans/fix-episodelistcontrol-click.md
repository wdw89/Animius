# Fix: EpisodeListControl click not responding

## Problem
EpisodeListControl "reverse list" and "more episodes" buttons stopped responding to clicks after adding background + focus inverse color effect.

## Root cause
The modifier chain used `.let { mod -> if (isAndroidTV) mod.onFocusChanged{}.focusable() else mod }` before `.clickable()`. This appears to interfere with `clickable`'s internal focus/click handling — `clickable` already adds `focusable()` internally, and the explicit one before it may conflict.

## Fix
Replace the conditional `.let{}` with direct `.onFocusChanged{}`:
- Remove the `.let{}` block
- Remove the explicit `.focusable()` (redundant — `clickable` handles it)
- Use `.onFocusChanged {}` directly at the same position (safe observer, no side effects on phone/TV)
- Keep the `isAndroidTV` guard inside the lambda to only update state on TV

## Modified modifier chain (phone and TV same):
`clip -> background -> onFocusChanged -> clickable -> padding`

## Build & Install
- `.\gradlew assembleDebug`
- `adb install -r app/build/outputs/apk/debug/Animius-v1.3.5-debug.apk`
