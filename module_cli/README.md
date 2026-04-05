# module_cli

CLI-утилита для синхронизации исходников NSMP.

## Maven coordinates

```text
ru.kazantsev.nsmp.sdk.sources_sync:cli:{версия}
```

Публикуется fat jar (shadow).

## Сборка и запуск

Команды:
- `pull` - затянуть в проект исходники
- `push` - отправить исходники из проекта в инсталляцию
- `syncCheck` - проверить, была ли модификация исходника на стороне инсталляции с момента последнего poll

## Общие опции

Все опции строковые, флагов нет.

Система указания инсталляции наследуется от библиотеки [nsmp.basic_api_connector](https://github.com/exeki/nsmp.basic_api_connector), так что порядок передачи параметров инсталляции можно понять оттуда. 
Кратко по опциям:
1. самое простое - создайте конфигурационный файл по стандартному пути, описанный в `nsmp.basic_api_connector`, при вызове команды укажите только ID инсталляции.
2. не хочется помещать по стандартному пути? Создайте где угодно, при вызове команды передавайте ID инсталляции и путь до конфигурационного файла.
3. не хочется делать конфигурационный файл вообще? При вызове команды передайте все параметры инсталляции вручную. 


### Опции авторизации

- `--installationId` - идентификатор инсталляции (варианты авторизации 1, 2, 3)
- `--configPath` - путь до конфигурационного файла (варианты авторизации 1, 2)
- `--scheme` - схема подключания (варианты авторизации 1, 3)
- `--host` - хостнейм инсталляции (варианты авторизации 1, 3)
- `--accessKey` - ключ доступа (варианты авторизации 1, 3)
- `--ignoreSsl` - игнорировать ssl (варианты авторизации 1, 3)

### Опции команд

- `--projectPath` - путь до целевого проекта
- `--log-level` (`trace|debug|info|warn|error`) - уровень логирования. Старт и окончание команды пишутся просто в stdout.
- `--scripts` - коды скриптов через запятую
- `--modules` - коды модулей через запятую
- `--force` - отключает sync check перед upload, применяется только в команде `push`

## Примеры

Через файл конфигурации по умолчанию:

```bash
java -jar cli-1.0.0.jar pull \
  --installationId EXEKI1 \
  --scripts testScript1,testScript2 \
  --modules testModule1
```

Через `configPath`:

```bash
java -jar cli-1.0.0.jar syncCheck \
  --installationId EXEKI1 \
  --configPath C:\Users\user\.nsmp_sdk\conf\connector_params.json \
  --scripts testScript1 \
  --modules testModule1
```

Прямые параметры подключения + push:

```bash
java -jar module_cli/build/libs/cli-1.0.0.jar push \
  --installationId EXEKI1 \
  --scheme https \
  --host nsd1.exeki.local \
  --accessKey your-access-key \
  --ignoreSsl \
  --scripts testScript1 \
  --modules testModule1 \
  --force
```
