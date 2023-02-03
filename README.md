# Chimber bot

## Prerequisites

* Install Java 17 and make sure with `java --version`.
* Create the `local.properties` file in `src/main/resources`.
* Fill in a valid discord application token.
* To use Yandex TTS fill in a valid yandex OAuth token as well as your FOLDER_ID in Yandex Cloud.

```
DISCORD_TOKEN=xxx
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
