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

- `--installationId` - идентификатор инсталляции
- `--configPath` - путь до конфигурационного файла
- `--scheme` - схема подключания 
- `--host` - хостнейм инсталляции
- `--accessKey` - ключ доступа
- `--ignoreSsl` (boolean-флаг) - игнорировать ssl
- `--projectPath` - путь до целевого проекта
- `--log-level` (`trace|debug|info|warn|error`)
- `--scripts` (коды скриптов через запятую)
- `--modules` (коды модулей через запятую)

## Опции команды `push`

- `--force` (boolean-флаг, отключает sync check перед upload)

## Примеры

Через файл конфигурации по умолчанию:

```bash
java -jar module_cli/build/libs/cli-1.0.0.jar pull \
  --installationId EXEKI1 \
  --scripts testScript1,testScript2 \
  --modules testModule1
```

Через `configPath`:

```bash
java -jar module_cli/build/libs/cli-1.0.0.jar syncCheck \
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

## Логи

По умолчанию используется `slf4j-simple`.
Чтобы писать логи в `stdout`, добавьте в `simplelogger.properties`:

```properties
org.slf4j.simpleLogger.logFile=System.out
```
