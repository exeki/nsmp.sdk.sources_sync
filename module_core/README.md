# module_core

Библиотека с основной логикой синхронизации исходников NSMP.

## Подключение

Gradle Kotlin DSL:

`build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/exeki/*")
        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("ru.kazantsev.nsmp.sdk.sources_sync:core:1.0.0")
}
```

## Основной API

Класс: `ru.kazantsev.nsmp.sdk.sources_sync.SrcService`

Методы:

- `pull()`
- `push()`
- `syncCheck()`

## Минимальный пример

```kotlin
val params = ConnectorParams.byConfigFile("EXEKI1")
val service = SrcService(
    SrcConnector(params),
    ObjectMapper(),
    Paths.get("/my_project")
)

//Скачает и сохранит исходники
service.pull(
    SrcRequset(
        modules = listOf("test2"),
        allModules = false,
        scripts = [],
        allScripts = true,
        advImports = listOf("test2"),
        allAdvImports = false
    )
)
```
