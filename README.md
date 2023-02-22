# Cuer

[![Actions Status](https://github.com/sentinelweb/cuer/workflows/Android%20CI/badge.svg)](https://github.com/sentinelweb/cuer/actions)

## Youtube Media queue manager for Android/ios/desktop

This project is a playlist manager for Youtube content. (In development)

Youtube has very bad playlist & chromecast queue usability.

It's a learning project to hone my Material / Jetpack / Kotlin / Coroutines / Chromecast skills

See the app website here: https://cuer.app

Using

- Kotlin Multiplatform
  - Shared modules: (**shared**, **domain**, **database**, **netKmm**) shared domain model and common app components
  - Website module: **website** (kotlin JS module)
  - Android app module: **app**
  - iOS App: **ios_app**
  - Remote module: **remote** (web / react interface)
- Material Design Libraries & Themes (Day/Night)
- SwiftUI (iOS)
- Jetpack Libraries
  - Room
  - Lifecycle
  - Androidx / KTX
  - Navigation
  - Jetpack Compose
    - https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/search
- Architecture (I have used few different patterns for practice evaluation)
  - MVI: (using https://arkivanov.github.io/MVIKotlin/) -
    e.g. https://github.com/sentinelweb/cuer/tree/develop/shared/src/commonMain/kotlin/uk/co/sentinelweb/cuer/app/ui/player
  - MVVM:
    e.g. https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/playlist_item_edit
  - MVP
    e.g. https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/main
- Kotlin (1.4)
  - Coroutines
  - Serialisation
- Firebase
  - Storage
  - Crashlytics
- Chromecast (using: https://github.com/PierfrancescoSoffritti/Android-YouTube-Player)
- Koin
- Glide
- Test
  - JUnit 4
  - Mockk
  - Hamcrest
  - KotlinFixture
  - Android X Test - enables the same code to be used for robolectric and espresso tests
  - Robolectric
  - Espresso
  - Room test
  - Koin test
  - Coroutines test
- Github Actions (Build / Test)

## Screenshots

### Browse

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/browse_20221031_200950.png" width="400">
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/ios/browse-lg-2023-02-09.png" width="400">

### Playlist

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/playlist_20221031_200719.png" width="400">
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/ios/playlist-lg-2023-02-09.png" width="400">

### Playlists

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/playlists_20221031_200615.png" width="400">
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/ios/playlists-lg-2023-02-09.png" width="400">

### Video info

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/playlist_item_20221031_200847.png" width="400">

### Portrait Player

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/player_playlist_20221031_201518.png" width="400">

### Landscape Player

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/player-land-2021-07-19-204807.png" height="400">

### Add via Share

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/0.76/share_20221031_201927.png" width="400">

### Onboarding

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/onboard1_20230209.png" width="400">
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/onboard2_20230209.png" width="400">

