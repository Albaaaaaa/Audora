# Audora

[![Android](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/github/license/Albaaaaaa/Audora?color=6C63FF)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)](https://developer.android.com/jetpack/compose)

**Audora** is an experimental, privacy-friendly Android music player with a deeply personal listening experience. It combines library playback, queue tools, lyrics, and creative listening controls in one modern Compose interface.

> **Project status:** active development / beta. Interfaces and integrations may change while the app evolves.

## Highlights

- **Smart Mix** — arrange the upcoming queue around a Chill, Balanced, Build, or Peak energy curve.
- **Edit Transition** — preview and customize crossfades between two tracks with tempo, musical-key, volume, EQ, effect, and preset controls.
- **Up Next** — add tracks to a clean queue from search, then keep the listening flow organized.
- **Sleep Mix** — set a sleep timer, fade the volume gently, and optionally continue with generated rain ambience.
- **Lyrics Moments** — long-press a lyric to save its timestamp as a shareable card.
- **Sound Capsule & Aura** — view weekly listening highlights, favourite tracks, listening mood, and a personal listener aura.
- **Dynamic player theme** — player colours adapt subtly to the current artwork.
- **Library tools** — playlists, downloads / smart cache, search, favourites, listening history, and imports from supported services.
- **Privacy-first by design** — listening data and preferences stay on the device unless a selected provider needs a network request.

## Screenshots

Screenshots and a product walkthrough will be added as the visual system is finalised.

## Getting started

### Requirements

- Android Studio (latest stable recommended)
- JDK 21
- Android SDK with API 36 installed
- An Android emulator or a USB-debugging-enabled Android device

### Run from Android Studio

1. Clone the repository:

   ```bash
   git clone https://github.com/Albaaaaaa/Audora.git
   cd Audora
   ```

2. Open the folder in Android Studio and let Gradle sync finish.
3. Select the **universalFossDebug** build variant.
4. Select an emulator or connected phone, then press **Run**.

### Run from the terminal

On Windows PowerShell:

```powershell
.\gradlew.bat :app:installUniversalFossDebug
```

This builds, installs, and updates the debug app on the connected device or emulator.

To create an APK without installing it:

```powershell
.\gradlew.bat :app:assembleUniversalFossDebug
```

The project also includes `build.sh`; run `./build.sh --help` on a Bash-compatible terminal to see its packaging options.

## How to use the main features

| Feature | Where to find it | How it works |
| --- | --- | --- |
| Smart Mix | Bottom navigation | Add at least two songs to **Up Next**, choose an energy arc, then tap **Build Smart Mix**. |
| Edit Transition | Player / right-side feature menu | Select the current and next tracks, adjust the transition controls, preview, then save. |
| Sleep Mix | Player menu | Choose a timer and optional rain ambience; Audora fades playback before sleep. |
| Lyrics Moments | Lyrics screen | Long-press a lyric line while music is playing, then open **Lyrics Moments** to share or remove it. |
| Sound Capsule | Profile | Listen through Audora during the week, then open the Capsule to see your highlights and aura. |
| Smart Cache | Playlist / download controls | Enable downloads for favourite content to keep selected music available offline. |

## Technology

- Kotlin and Jetpack Compose
- AndroidX Media3 / ExoPlayer playback
- Room local database and DataStore preferences
- Hilt dependency injection
- Coil image loading
- Gradle Kotlin DSL

## Data, services, and attribution

Audora is an independent project and is not affiliated with YouTube, YouTube Music, Google, Spotify, Apple, Last.fm, Shazam, Discord, or other connected services. Names and trademarks belong to their respective owners.

Audora does not host or distribute music. Where a feature retrieves metadata, lyrics, or streams, it uses services selected by the user and those services may change or become unavailable. Use the app responsibly and according to the terms and copyright rules that apply to you.

This project builds on and integrates ideas or components from these open-source projects and services:

- [ViviMusic](https://github.com/vivizzz007/vivi-music) — original application base
- [Metrolist](https://github.com/mostafaalagamy/Metrolist) — framework and implementation inspiration
- [SimpMusic](https://github.com/maxrave-dev/SimpMusic) — lyrics provider integration
- [RiMusic / InnerTube](https://github.com/fast4x/RiMusic) — YouTube Music InnerTube wrapper inspiration
- [LRCLIB](https://github.com/tranxuanthang/lrclib) — synchronised lyrics integration
- [Monochrome Music](https://github.com/monochrome-music/monochrome) — FLAC stream support inspiration
- [AutoEq](https://autoeq.app/) — equalizer data
- [LyricsPlus](https://github.com/ibratabian17/LyricsPlus) and [Better Lyrics API](https://github.com/boidushya/better-lyrics) — lyrics data providers

Please retain attribution and follow the licences and terms of every dependency, upstream project, and service.

## Contributing

Issues and pull requests are welcome.

1. Create a branch: `git switch -c feature/your-feature`
2. Make one focused change.
3. Build the app with `:app:compileUniversalFossDebugKotlin`.
4. Commit with a clear message, for example: `feat: improve sleep mix controls`.
5. Push the branch and open a pull request against `main`.

## License

Audora is distributed under the [GNU General Public License v3.0](LICENSE). Any redistributed or modified version must comply with GPL-3.0.
