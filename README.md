# Cuer
[![Actions Status](https://github.com/sentinelweb/cuer/workflows/Android%20CI/badge.svg)](https://github.com/sentinelweb/cuer/actions)

## Youtube Media queue manager for Android

This project is a playlist manager for Youtube content. (In development)

Youtube has very bad playlist & chromecast queue useability.

It's a learning project to hone my Material / Jetpack / Kotlin / Coroutines / Chromecast skillz

Using
- Material Design Libraries & Themes (Day/Night)
- Jetpack Libraries
   - Room
   - Lifecycle
   - Androidx / KTX
   - Navigation
   - Jetpack Compose
     - https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/search
- Architecture 
   - MVP - https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui
   - Some MVVM for practice - e.g. https://github.com/sentinelweb/cuer/tree/develop/app/src/main/java/uk/co/sentinelweb/cuer/app/ui/playlist_item_edit
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
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/playlist-2021-04-13-23.31.47.jpeg" width="400">

### Playlists
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/playlists-2021-04-13-23.41.42.jpeg" width="400">

### Video info
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/info-2021-04-13-23.33.19.jpeg" width="400">

### Add via Share
<img src="https://raw.githubusercontent.com/sentinelweb/cuer/develop/media/screenshots/share-2021-04-13-23.35.07.jpeg"  width="400">


