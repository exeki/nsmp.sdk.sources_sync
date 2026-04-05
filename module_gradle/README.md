# module_gradle

Gradle-плагин для задач синхронизации исходников NSMP.

## Maven coordinates

```text
ru.kazantsev.nsmp.sdk.sources_sync:gradle:1.0.0
```

## Plugin id

```text
gradle
```

## Подключение плагина

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}
```

`build.gradle.kts`:

```kotlin
plugins {
    id("gradle") version "1.0.0"
}
```

## Extension

Плагин добавляет extension `nsmpSdkSourcesSync`.

Пример:

```kotlin
nsmpSdkSourcesSync {
    setInstallation("EXEKI1")
    // или:
    // setInstallation("EXEKI1", "C:\\path\\connector_params.json")
    // или:
    // setInstallation("EXEKI1", "https", "nsd1.exeki.local", "access-key", true)
}
```

## Задачи

- `pull`
- `push`
- `syncCheck`

Общие CLI-опции задач:
- `--installationId`
- `--configPath`
- `--scheme`
- `--host`
- `--accessKey`
- `--ignoreSsl`
- `--scripts` (через запятую)
- `--modules` (через запятую)

Дополнительно для `push`:
- `--force`

## Примеры запуска задач

```bash
./gradlew pull --scripts=testScript1,testScript2 --modules=testModule1
./gradlew syncCheck --scripts=testScript1 --modules=testModule1
./gradlew push --scripts=testScript1 --modules=testModule1 --force
```
