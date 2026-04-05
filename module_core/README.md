# module_core

Библиотека с основной логикой синхронизации исходников NSMP.

## Maven coordinates

```text
ru.kazantsev.nsmp.sdk.sources_sync:core:1.0.0
```

## Подключение

Gradle Kotlin DSL:

```kotlin
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

`SrcService` работает с директориями проекта:
- `src/main/scripts`
- `src/main/modules`

и файлом локальной информации:
- `.smp_sdk/src_info.json`

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
    Paths.get(".")
)

service.pull(
    scripts = listOf("testScript1"),
    modules = listOf("testModule1")
)
```
