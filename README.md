# tgbot-by

Телеграм-бот на **Java 25 / Spring Boot 4 / Spring Modulith**.

## Возможности

| Команда | Что делает |
|---|---|
| `/kb` | Конвертирует текст, набранный не в той раскладке (ЙЦУКЕН ↔ QWERTY) |
| `/w [город]` | Погода на сейчас и на 3 дня (Open-Meteo) |
| `/rate`, `/conv` | Официальные курсы НБРБ и конвертер валют |
| `/chart` | График курса за период картинкой |
| `/img` | Поиск картинок через Google Custom Search |
| `/subscribe` | Рассылка погоды, курсов и сводного дайджеста в группу по расписанию |
| `/alert` | Уведомление при достижении курсом заданного порога |

## Архитектура

Модульный монолит на Spring Modulith. Доменные модули (`weather`, `currency`, `image`, `layout`)
не знают о Telegram — адаптер `telegram` парсит команды и рендерит результат. Благодаря этому
рассылка по расписанию переиспользует те же сервисы, что и команды, без дублирования логики.

Цикл `telegram ↔ subscription` разорван событиями Modulith: планировщик публикует `SubscriptionDue`,
адаптер его слушает. Event Publication Registry хранит событие в БД до успешной обработки, поэтому
рассылка доезжает даже если приложение упало между срабатыванием расписания и отправкой.

## Стек

Java 25 (virtual threads, scoped values, sealed interfaces) · Spring Boot 4.1 / Framework 7 ·
Spring Modulith 2.1 · PostgreSQL + Spring Data JDBC + Flyway · Caffeine · TelegramBots (long polling) ·
Testcontainers · GitHub Actions → GHCR → VPS

## Запуск

```bash
docker compose up -d postgres
BOT_TELEGRAM_TOKEN=<token> ./gradlew bootRun
```

Без `BOT_TELEGRAM_TOKEN` приложение стартует, но к Telegram не подключается — это нужно,
чтобы сборка и тесты не требовали секрета.

Секреты передаются только через переменные окружения и в репозиторий не попадают.

## Сборка

```bash
./gradlew build
```
