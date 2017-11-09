<h1 align="center">LISTEN.moe Android app</h1>

## Download

<a href="https://play.google.com/store/apps/details?id=me.echeung.moemoekyun">
  <img height="50" alt="Get it on Google Play"
       src="https://play.google.com/intl/en_us/badges/images/apps/en-play-badge.png" />
</a>

<a href="https://www.amazon.com/gp/product/B075VJFSTL/ref=mas_pm_listen_moe">
  <img height="50" alt="Get it on the Amazon app store"
       src="https://images-na.ssl-images-amazon.com/images/G/01/mobile-apps/devportal2/res/images/amazon-underground-app-us-white.png" />
</a>


## About

A native Android app using things like [OkHttp](http://square.github.io/okhttp/), [RetroFit](http://square.github.io/retrofit/), and [data binding](https://developer.android.com/topic/libraries/data-binding/index.html). Implements all functionality exposed through the official [LISTEN.moe API](https://listen-moe.github.io/documentation/).

This is a fork/rewrite of J-Cotter's [original app](https://play.google.com/store/apps/details?id=jcotter.listenmoe) and serves as the official Android app.


## Developing

### Prerequisites

- [Android Studio](https://developer.android.com/studio/index.html)

### Project structure

The project contains 2 submodules:
- `app`: The Android app itself.
- `listenmoe-api`: An Android library that contains abstracts the API, including the websocket and stream.

### Translations

Translations are crowdsourced through [OneSky](https://osfmofb.oneskyapp.com/collaboration/project?id=271507) and managed by [@arkon](https://github.com/arkon/).

### Release

The release builds are signed by and uploaded to Google Play and Amazon App Store by [@arkon](https://github.com/arkon/).


## License

[MIT](https://github.com/LISTEN-moe/android-app/blob/master/LICENSE)
