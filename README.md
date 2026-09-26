# Explore With Me

Сервис для поиска компании на мероприятия: пользователи создают события, подают заявки на участие,
оставляют комментарии и собирают подборки. Приложение разделено на микросервисы, которые находят
друг друга через Eureka, берут настройки из config-server и доступны снаружи через единственную точку
входа — gateway.

## Архитектура

```
                        ┌──────────────────┐
  клиент ──► :8080 ────►│  gateway-server  │
                        └─────────┬────────┘
                                  │ lb://<сервис>
   ┌──────────────┬───────────────┼───────────────┬──────────────────┐
   ▼              ▼               ▼               ▼                  ▼
user-service  event-service  request-service  comment-service  compilation-service
   │              │               │               │                  │
   ▼              ▼               ▼               ▼                  ▼
 user-db       event-db        request-db      comment-db       compilation-db

   ┌──────────────────┐   ┌──────────────────┐   ┌──────────────┐
   │ discovery-server │   │  config-server   │   │ stats-server │──► stats-db
   │      :8761       │◄──┤      :8888       │   │    :9090     │
   └──────────────────┘   └──────────────────┘   └──────────────┘
```

Сервисы вызывают друг друга напрямую по внутреннему API, минуя gateway: адрес соседа они берут
из Eureka по его имени.

### Инфраструктурные сервисы

| Сервис | Порт | Назначение |
|---|---|---|
| `infra/discovery-server` | 8761 | Eureka: сервисы регистрируются здесь и находят друг друга по имени |
| `infra/config-server` | 8888 | Отдаёт настройки из `infra/config-repo`, сам находится через Eureka |
| `infra/gateway-server` | 8080 | Единственная точка входа, маршрутизирует запросы по `lb://<сервис>` |

### Бизнес-сервисы

У каждого своя база данных, общих таблиц нет.

| Сервис | Порт | БД (порт снаружи) | За что отвечает |
|---|---|---|---|
| `core/user-service` | 8082 | `users` (5435) | Пользователи |
| `core/event-service` | 8083 | `events` (5436) | События и категории |
| `core/request-service` | 8084 | `requests` (5437) | Заявки на участие |
| `core/compilation-service` | 8085 | `compilations` (5438) | Подборки событий |
| `core/comment-service` | 8086 | `comments` (5439) | Комментарии к событиям |
| `stats-service/stats-server` | 9090 | `stats` (5433) | Статистика просмотров |

### Общие модули

| Модуль | Содержимое |
|---|---|
| `core/interaction-api` | Исключения, `ErrorHandler` с единым форматом ошибок, `FeignErrorDecoder`, настройка Feign |
| `stats-service/stats-dto` | DTO статистики: `EndpointHitDto`, `ViewStatsDto` |
| `stats-service/stats-client` | Клиент статистики: `hit(HttpServletRequest)` и `getStats(...)` |

Feign-клиенты и DTO для них **не** вынесены в общий модуль: каждый вызывающий сервис объявляет свои
в пакете `<сервис>.client` и описывает только те поля, которые ему нужны. Незнакомые поля в ответе
Jackson игнорирует, поэтому копии могут быть урезанными.

### Кто кого вызывает

| Вызывающий | Вызываемый | Зачем |
|---|---|---|
| event-service | user-service | Проверить инициатора, получить его имя для ответа |
| event-service | request-service | Количество подтверждённых заявок (`confirmedRequests`) |
| event-service | comment-service | Количество опубликованных комментариев (`comments`) |
| event-service | stats-server | Записать просмотр, получить `views` |
| request-service | user-service, event-service | Проверить пользователя, статус события, лимит участников |
| comment-service | user-service, event-service | Автор комментария, проверка что событие опубликовано |
| comment-service | stats-server | Записать просмотр страницы комментариев |
| compilation-service | event-service | Краткие данные событий для подборки |

Прямых обращений к чужой базе нет: в сущностях хранятся только идентификаторы
(`initiatorId`, `eventId`, `requesterId`, `authorId`), внешних ключей между сервисами тоже нет.

### Устойчивость к отказам

Вызовы обёрнуты в circuit breaker (Resilience4j). Важно различать два вида вызовов:

- **Необязательные данные** — есть fallback, ответ отдаётся без них:
  `comments` и `confirmedRequests` становятся нулями, если comment-service или request-service
  недоступны (`core/event-service/.../client/fallback`). Статистика ведёт себя так же:
  без stats-server `views` равны нулю.
- **Обязательные проверки** — fallback нет, клиент получает `503`: имя и существование пользователя,
  статус события, данные событий для подборки. Подменять их заглушками нельзя.

Коды ответов: `404` и `409`, полученные от соседнего сервиса, сохраняются как есть;
недоступность соседа или разомкнутый предохранитель — `503`.

## Где лежат настройки

| Что | Где |
|---|---|
| Общие настройки всех сервисов | `infra/config-repo/application.yml` |
| Настройки бизнес-сервиса | `infra/config-repo/core/<сервис>/<сервис>.yml` |
| Настройки gateway (маршруты) | `infra/config-repo/gateway-server/gateway-server.yml` |
| Настройки stats-server | `infra/config-repo/stats-server/stats-server.yml` |
| Адрес Eureka и имя сервиса | `<модуль>/src/main/resources/application.yml` |
| Схема БД сервиса | `<модуль>/src/main/resources/schema.sql` |

В `application.yml` из config-repo собрано то, что одинаково у всех: подключение к БД (кроме `url`),
`ddl-auto: validate`, таймауты и circuit breaker для Feign, actuator, уровни логирования.
В файле сервиса остаются только его порт и `url` базы.

Config-server ищет файлы по шаблону `{application}`, поэтому имя файла должно совпадать с
`spring.application.name` сервиса. Конфиги попадают внутрь образа config-server при сборке:
после их правки нужно пересобрать именно этот образ.

Все значения можно переопределить переменными окружения — так и сделано в `docker-compose.yml`:
`SPRING_DATASOURCE_URL`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` и другие.

## Внутренний API

Эндпоинты с префиксом `/internal` предназначены только для вызовов между сервисами: маршрутов на
них в gateway нет, снаружи они недоступны. Ошибки возвращаются в том же формате `ApiError`,
что и во внешнем API.

### user-service

| Метод | Ответ | Описание |
|---|---|---|
| `GET /internal/users/{userId}` | `UserShortDto` | Пользователь по id. `404`, если его нет. Используется как проверка существования |
| `GET /internal/users?userIds=1&userIds=2` | `List<UserShortDto>` | Пользователи по списку id. Отсутствующие просто не попадают в ответ |

```json
// UserShortDto
{ "id": 1, "name": "Иван" }
```

### event-service

| Метод | Ответ | Описание |
|---|---|---|
| `GET /internal/events/{eventId}` | `EventInternalDto` | Данные события для проверок. `404`, если события нет |
| `GET /internal/events?eventIds=1&eventIds=2` | `List<EventShortDto>` | Краткие данные событий: используется подборками |

```json
// EventInternalDto — только то, что нужно другим сервисам
{ "id": 1, "initiatorId": 5, "state": "PUBLISHED", "participantLimit": 10, "requestModeration": true }
```

`EventShortDto` совпадает с форматом внешнего API: `id`, `annotation`, `category`,
`confirmedRequests`, `eventDate`, `initiator`, `paid`, `title`, `views`, `comments`.

### request-service

| Метод | Ответ | Описание |
|---|---|---|
| `GET /internal/requests?eventIds=1&eventIds=2&status=CONFIRMED` | `Map<Long, Long>` | Количество заявок с указанным статусом по каждому событию |

```json
// события без заявок в ответе отсутствуют — читать через getOrDefault(id, 0)
{ "1": 3, "2": 1 }
```

### comment-service

| Метод | Ответ | Описание |
|---|---|---|
| `GET /internal/comments?eventIds=1&eventIds=2&status=PUBLISHED` | `Map<Long, Long>` | Количество комментариев с указанным статусом по каждому событию |

### stats-server

Вызывается не по HTTP напрямую, а через `stats-client`, который сам находит сервис в Eureka:

| Метод | Описание |
|---|---|
| `POST /hit` | Сохранить обращение к эндпоинту (`EndpointHitDto`) |
| `GET /stats?start=&end=&uris=&unique=` | Статистика просмотров (`List<ViewStatsDto>`) |

В отличие от остальных, эти два пути **доступны** снаружи через gateway: так требует внешняя
спецификация статистики.

## Внешний API

Спецификации OpenAPI лежат в корне репозитория:

- [ewm-main-service-spec.json](ewm-main-service-spec.json) — основной API: события, категории,
  пользователи, заявки, подборки, комментарии;
- [ewm-stats-service-spec.json](ewm-stats-service-spec.json) — API статистики.

Открыть их удобно в [Swagger Editor](https://editor-next.swagger.io/): «File» → «Import file».
Все пути из основной спецификации доступны через gateway на `http://localhost:8080`.

## Запуск

```bash
docker compose up -d
```

Поднимутся инфраструктура, шесть сервисов и шесть баз. Порядок запуска соблюдается через
healthcheck: сервис стартует после discovery-server, config-server и своей базы. Друг к другу
сервисы обращаются уже во время обработки запросов, поэтому порядок их старта не важен.

Полезные адреса:

- `http://localhost:8080` — API через gateway;
- `http://localhost:8761` — панель Eureka со списком зарегистрированных сервисов;
- `http://localhost:8888/<сервис>/default` — настройки, которые config-server отдаёт сервису.

Сборка без Docker:

```bash
mvn clean install
```

Для запуска сервиса из IDE ничего настраивать не нужно: адреса баз по умолчанию совпадают с портами,
которые docker-compose публикует наружу, — достаточно поднять стенд и остановить нужный контейнер.

Диагностика внутри контейнера (порты сервисов наружу не публикуются):

```bash
docker compose exec event-service curl -s localhost:8083/actuator/health
docker compose exec event-service curl -s localhost:8083/actuator/circuitbreakers
```

## Стек

Java 21, Spring Boot 3.3, Spring Cloud 2023.0 (Eureka, Config, Gateway, OpenFeign, Resilience4j),
Spring Data JPA, PostgreSQL 17, MapStruct, Lombok, Maven, Docker Compose.
