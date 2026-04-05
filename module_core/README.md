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

- `pull(scripts, modules)`
- `push(scripts, modules, force)`
- `syncCheck(scripts, modules)`

## Минимальный пример

```kotlin
import com.fasterxml.jackson.databind.ObjectMapper
import ru.kazantsev.nsd.basic_api_connector.ConnectorParams
import ru.kazantsev.nsmp.sdk.sources_sync.SrcConnector
import ru.kazantsev.nsmp.sdk.sources_sync.SrcService
import java.nio.file.Paths

val params = ConnectorParams.byConfigFile("EXEKI1")
val service = SrcService(
    SrcConnector(params),
    ObjectMapper(),
    Paths.get("/my_project")
)

//Скачает и сохранит исходники
service.pull(
    scripts = listOf("testScript1"),
    modules = listOf("testModule1")
)
```
