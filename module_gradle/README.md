# module_gradle

Gradle-плагин для задач синхронизации исходников NSMP.
При подключении объявляет source sets:
- `src/main/scripts`
- `src/main/modules`
- `src/main/resources`

## Maven coordinates

```text
ru.kazantsev.nsmp.sdk.sources_sync:gradle:1.0.0
```

## Plugin id

```text
nsmp_sdk_sources_sync
```

## Подключение плагина

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        //именно в этом репозитории лежит плагин, все остальные чисто что бы были не терялись
        maven {
            name = "exekiGithubRepo"
            url = uri("https://maven.pkg.github.com/exeki/*")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

`build.gradle.kts`:

```kotlin
plugins {
    id("nsmp_sdk_sources_sync") version "1.0.0"
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

- `pull` - получить исходники из инсталляции
- `push` - загрузить исходники в инсталляцию 
- `syncCheck` - проверить, были ли модифицированы исходники в инсталляции с момента последнего pull

### Общие CLI-опции задач:

Все опции строковые, флагов нет.

#### Опции инсталляции:

Система указания инсталляции наследуется от библиотеки [nsmp.basic_api_connector](https://github.com/exeki/nsmp.basic_api_connector), так что порядок передачи параметров инсталляции можно понять оттуда.
Кратко по опциям:
1. самое простое - создайте конфигурационный файл по стандартному пути, описанный в `nsmp.basic_api_connector`, при вызове команды укажите только ID инсталляции.
2. не хочется помещать по стандартному пути? Создайте где угодно, при вызове команды передавайте ID инсталляции и путь до конфигурационного файла.
3. не хочется делать конфигурационный файл вообще? При вызове команды передайте все параметры инсталляции вручную.

Все опции инсталляции могут быть заданы через gradle build extension.

- `--installationId` 
- `--configPath` 
- `--scheme` 
- `--host` 
- `--accessKey` 
- `--ignoreSsl`

#### Опции исходников:

- `--scripts` - коды модулей через запятую
- `--modules` - коды модулей через запятую
- `--advImports` - коды advImports через запятую
- `--allScripts` - взять все scripts (`true|false`)
- `--allModules` - взять все modules (`true|false`)
- `--allAdvImports` - взять все advImports (`true|false`)
- `--scriptsExcluded` - коды scripts для исключения
- `--modulesExcluded` - коды modules для исключения
- `--advImportsExcluded` - коды advImports для исключения

#### Дополнительно для `push`:

- `--force` - пропустить syncCheck при загрузке исходников на инсталляцию (`true|false`)

### Передача аргументов 

Для list-аргументов (`scripts`, `modules`, `advImports`, `scriptsExcluded`, `modulesExcluded`, `advImportsExcluded`) в задачу передается строка единая строка, значения поделены запятыми.

Boolean параметры передаются только строками, пример: `--ignoreSsl=true|false`
Передача флагом без значения не используется.

## Примеры запуска задач

В указанных примерах опции инсталляции заданы через extension.

```bash
./gradlew pull --scripts=testScript1,testScript2 --modules=testModule1
./gradlew pull --allScripts=true --scriptsExcluded=testScript2
./gradlew syncCheck --scripts=testScript1 --modules=testModule1
./gradlew push --scripts=testScript1 --modules=testModule1 --force=true
./gradlew pull --allScripts=true
./gradlew push --allModules=true --force=true
./gradlew syncCheck --allAdvImports=true
```
