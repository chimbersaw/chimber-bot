# Chimber bot

https://chimchima.ru/bot

## Prerequisites

* Install Java 21 and make sure with `java --version`.
* Create the `local.properties` file in `src/main/resources`.
* Fill in a valid discord application token.
* To use YouTube fill in a valid YOUTUBE_REFRESH_TOKEN as
  described [here](https://github.com/lavalink-devs/youtube-source?tab=readme-ov-file#using-oauth-tokens).
* To use Yandex TTS fill in a valid yandex OAuth token as well as your FOLDER_ID in Yandex Cloud.

```
DISCORD_TOKEN=xxx
YOUTUBE_REFRESH_TOKEN=xxx
YANDEX_OAUTH_TOKEN=xxx
YANDEX_FOLDER_ID=xxx
```

## Run

To quickly run the application use your IDE or:

```bash
./gradlew run
```

To build a fat jar and run it use:

```bash
./gradlew build
java -jar build/libs/chimber-bot-1.0-SNAPSHOT.jar
```
