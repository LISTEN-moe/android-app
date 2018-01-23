<div align="center">
	<img src="https://lolisafe.moe/DJwzPbWD.png" />
</div>
<h1 align="center">Official Android app</h1>

## Download

<a href="https://play.google.com/store/apps/details?id=me.echeung.moemoekyun">
  <img height="50" alt="Get it on Google Play"
       src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" />
</a>

<!--
<a href="https://f-droid.org/app/me.echeung.moemoekyun.fdroid">
  <img height="50" alt="Get it on F-Droid"
       src="https://f-droid.org/badge/get-it-on.png">
</a>
-->


## About

A native Android app using things like [OkHttp](http://square.github.io/okhttp/), [RetroFit](http://square.github.io/retrofit/), and [data binding](https://developer.android.com/topic/libraries/data-binding/index.html). Features things like [Android Auto](https://www.android.com/auto/) and [autofill](https://android-developers.googleblog.com/2017/11/getting-your-android-app-ready-for.html) support.

This is a fork/rewrite of J-Cotter's [original app](https://play.google.com/store/apps/details?id=jcotter.listenmoe) and serves as the official Android app.


## Developing

### Prerequisites

- [Android Studio](https://developer.android.com/studio/index.html)
- [Lombok Plugin](https://projectlombok.org/setup/android#android-studio) for Android Studio

### Project structure

The project contains 2 submodules:
- `app`: The Android app itself.
- `listenmoe-api`: An Android library that wraps the [LISTEN.moe API](https://listen-moe.github.io/documentation/), including the websocket and stream.

### Translations

Translations are crowdsourced through [OneSky](https://osfmofb.oneskyapp.com/collaboration/project?id=271507) and managed by [@arkon](https://github.com/arkon/).

### Release

The release builds are signed by and uploaded to the Google Play Store and Amazon App Store by [@arkon](https://github.com/arkon/).


## License

[MIT](https://github.com/LISTEN-moe/android-app/blob/master/LICENSE)
