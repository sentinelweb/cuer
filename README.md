# Cuer

[![Actions Status](https://github.com/sentinelweb/cuer/workflows/Android%20CI/badge.svg)](https://github.com/sentinelweb/cuer/actions)

## Youtube Media queue manager for Android

This project is a playlist manager for Youtube content. (In development)

Youtube has very bad playlist & chromecast queue useability.

It's a learning project to hone my Material / Jetpack / Kotlin / Coroutines / Chromecast skills

See the app website here: https://cuer.app

Using

- Kotlin Multiplatform
  - shared module: domain model and common app components
  - remote module: web / react interface
  - website module: kotlin JS module
- Material Design Libraries & Themes (Day/Night)
- Jetpack Libraries
  - Room
  - Lifecycle
  - Androidx / KTX
  - Navigation
  - Jetpack Compose
    - https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/search
- Architecture
  - MVP
    - https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui
  - Some MVVM for practice -
    e.g. https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/playlist_item_edit
  - MVI (using https://arkivanov.github.io/MVIKotlin/) -
    e.g.https://github.com/sentinelweb/cuer/tree/develop/shared/src/commonMain/kotlin/uk/co/sentinelweb/cuer/app/ui/player
- Kotlin (1.4)
  - Coroutines
  - Serialisation
- Firebase
  - Storage
  - Crashlytics
- Chromecast (using https://github.com/PierfrancescoSoffritti/Android-YouTube-Player)
- Koin
- Glide
- Test
  - JUnit 4
  - Mockk
  - Hamcrest
  - JFixture
  - Android X Test - enables the same code to be used for robolectric and espresso tests
  - Robolectric
  - Espresso
  - Room test
  - Koin test
  - Coroutines test
- Github Actions (Build / Test)

## screenshots

### Playlist

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/playlist-20210708-231348.png" width="400">

### Playlists

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/playlists-20210708-231504.png" width="400">

### Video info

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/info-20210708-231529.png" width="400">

### Portrait Player

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/player-port_20210708-231425.png" width="400">

### Landscape Player

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/player-land_20210708-231437.png" height="400">

### Add via Share

<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/share-20210708-235851.png" width="400">


