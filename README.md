# Chimber bot

https://chimchima.ru/bot

## Prerequisites

* Install Java 21 and make sure with `java -version`.
* Create the `local.properties` file in `src/main/resources`.
    * Provide a valid discord application token.
    * To use YouTube provide a valid `YOUTUBE_REFRESH_TOKEN` as
      described [here](https://github.com/lavalink-devs/youtube-source?tab=readme-ov-file#using-oauth-tokens).
    * To load playlists from YouTube provide valid `YOUTUBE_PO_TOKEN` and `YOUTUBE_VISITOR_DATA` as
      described [here](https://github.com/lavalink-devs/youtube-source?tab=readme-ov-file#using-a-potoken).
    * To use Yandex TTS fill in a valid yandex OAuth token as well as your FOLDER_ID in Yandex Cloud.
    * To use a remote YT Cipher service (like [this one](https://github.com/kikkia/yt-cipher)), set
      `USE_REMOTE_YT_CIPHER`
      to true.

```
DISCORD_TOKEN=xxx
YOUTUBE_REFRESH_TOKEN=xxx
YOUTUBE_PO_TOKEN=xxx
YOUTUBE_VISITOR_DATA=xxx
YANDEX_OAUTH_TOKEN=xxx
YANDEX_FOLDER_ID=xxx
USE_REMOTE_YT_CIPHER=xxx
```

## Run

To build a fat jar and run it use:

```bash
./gradlew build
java -jar build/libs/chimber-bot-1.0-SNAPSHOT.jar
```
