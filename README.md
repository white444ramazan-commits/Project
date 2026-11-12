# PUBG KR Companion
Kotlin / Jetpack Compose Android companion app. Includes an aim practice mini-game, standalone generated marker simulator, local sensitivity profiles, opt-in root diagnostics and device diagnostics.

## Build
Push to main/master or run Actions → Android APK → Run workflow. Download the APK from the completed run's Artifacts section. The CI output is a debug-signed APK.

## Scope
The app does not inspect or modify PUBG memory, files, processes, network traffic or configuration. Sensitivity values are stored locally only. Root check calls `su -c id` only after the user taps the check button. Thermal sensor availability depends on the device vendor.
