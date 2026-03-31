# Backend Documentation - Goida AI

## Обзор

Backend приложения Goida AI реализован на Spring Boot 4.0.4 с использованием Java 17. Предоставляет REST API для мобильного приложения на Flutter.

## Технологии

- **Spring Boot** 4.0.4
- **Spring Security** с OAuth2 Resource Server
- **Spring Data JPA** для работы с БД
- **PostgreSQL** / **H2** (для разработки)
- **Flyway** для миграций БД
- **Redis** для кэширования (опционально)
- **Kafka** для асинхронных уведомлений (опционально)
- **Lettuce** Redis клиент
- **Jackson** для JSON сериализации

## Структура проекта

```
src/main/java/ru/goidaai/test_backend/
├── config/                 # Конфигурация приложения
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   ├── RedisConfig.java
│   ├── KafkaConfig.java
│   └── AppProperties.java
├── controller/             # REST контроллеры
│   ├── AssetsController.java
│   ├── AuthController.java
│   ├── TransactionsController.java
│   ├── DashboardController.java
│   ├── ProfileController.java
│   ├── RatesController.java
│   └── ReceiptController.java
├── dto/                    # Data Transfer Objects
│   ├── AssetDTO.java
│   ├── AssetBalanceSummaryDTO.java
│   ├── TransactionDTO.java
│   ├── UserDTO.java
│   └── analytics/
├── model/                  # JPA сущности
│   ├── User.java
│   ├── Asset.java
│   ├── Transaction.java
│   ├── Receipt.java
│   └── enums/
├── repository/             # Репозитории
│   ├── UserRepository.java
│   ├── AssetRepository.java
│   └── TransactionRepository.java
├── security/               # Компоненты безопасности
│   ├── JwtService.java
│   ├── GoogleTokenVerifier.java
│   └── GoogleUser.java
└── service/                # Бизнес-логика
    ├── AssetsService.java
    ├── TransactionsService.java
    ├── AuthService.java
    ├── RatesService.java
    ├── NotificationService.java
    ├── CacheService.java
    └── analytics/
```

## Конфигурация

### application.properties

```properties
# Основные настройки
spring.application.name=goida-ai-backend
server.port=8080

# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/goida
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

# Redis (опционально)
redis.enabled=false
redis.host=localhost
redis.port=6379
redis.ttl.minutes=10

# Kafka (опционально)
kafka.enabled=false
kafka.bootstrap-servers=localhost:9092
kafka.consumer.group-id=test-backend-group

# Безопасность
app.auth.secret=your-secret-key
app.auth.token-ttl-seconds=86400
```

## REST API

### Аутентификация

#### POST /api/auth/google

Вход через Google OAuth.

**Request:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-123",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

#### POST /api/auth/dev

Dev-вход для тестирования (только profile=local).

**Request:**
```json
{
  "userId": "test-user"
}
```

### Активы

#### GET /api/assets

Получить список активов пользователя.

**Response:**
```json
{
  "assets": [
    {
      "id": "asset-123",
      "name": "Основная карта",
      "type": "bank_account",
      "currency": "USD",
      "amount": 1000.00,
      "currentValue": 1000.00,
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /api/assets

Создать новый актив.

**Request:**
```json
{
  "name": "Сберегательный счет",
  "type": "savings",
  "symbol": "USD",
  "balance": 5000.00,
  "note": "На отпуск"
}
```

#### PUT /api/assets/{id}

Обновить актив.

**Request:**
```json
{
  "name": "Обновленное имя",
  "type": "savings",
  "symbol": "USD",
  "balance": 6000.00
}
```

#### DELETE /api/assets/{id}

Удалить актив.

**Response:** 204 No Content

#### GET /api/assets/balance-summary

Получить сводку по балансу активов.

**Query Parameters:**
- `period` (optional): week, month, year (default: month)

**Response:**
```json
{
  "totalAssets": 15000.00,
  "spentBalance": 2500.00,
  "baseCurrency": "USD",
  "periodLabel": "This month"
}
```

### Транзакции

#### GET /api/transactions

Получить список транзакций с пагинацией.

**Query Parameters:**
- `cursor`: курсор для пагинации
- `limit`: количество записей (default: 20)
- `category`: фильтр по категории
- `period`: период (week, month, year)

**Response:**
```json
{
  "items": [
    {
      "id": "txn-123",
      "title": "Покупка продуктов",
      "amount": 1500.00,
      "currency": "RUB",
      "category": "groceries",
      "type": "expense",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "nextCursor": "eyJpZCI6InR4bi0xMjMiLCJjcmVhdGVkQXQiOiIyMDI0LTAxLTE1VDEwOjMwOjAwWiJ9"
}
```

#### POST /api/transactions

Создать транзакцию.

**Request:**
```json
{
  "title": "Покупка продуктов",
  "amount": 1500.00,
  "currency": "RUB",
  "category": "groceries",
  "type": "expense",
  "createdAt": "2024-01-15T10:30:00Z",
  "note": "Магнит",
  "sourceAssetId": "asset-123"
}
```

#### PUT /api/transactions/{id}

Обновить транзакцию.

#### DELETE /api/transactions/{id}

Удалить транзакцию.

### Дашборд

#### GET /api/dashboard/overview

Получить общую сводку дашборда.

**Query Parameters:**
- `period`: week, month, year (default: month)

**Response:**
```json
{
  "userId": "user-123",
  "baseCurrency": "USD",
  "periodLabel": "This month",
  "assets": [...],
  "spending": {
    "spent": 2500.00,
    "budget": 5000.00
  },
  "budgetStatus": {
    "remaining": 2500.00,
    "percentageSpent": 50.0
  },
  "categoryBreakdown": [...],
  "spendingTrend": {...},
  "spendingPercentages": {...},
  "totalNetWorth": 15000.00
}
```

### Курсы валют

#### GET /api/rates/fiat

Получить курсы фиатных валют.

**Response:**
```json
{
  "baseCurrency": "USD",
  "rates": {
    "EUR": 0.92,
    "RUB": 92.4,
    "KZT": 503.7
  }
}
```

#### GET /api/rates/crypto

Получить курсы криптовалют.

**Query Parameters:**
- `fiat`: базовая валюта (default: USD)
- `symbols`: список криптовалют через запятую

**Response:**
```json
{
  "quoteCurrency": "USD",
  "prices": {
    "BTC": 67250.0,
    "ETH": 3480.0,
    "BNB": 605.0
  }
}
```

### Профиль

#### GET /api/profile

Получить профиль пользователя.

**Response:**
```json
{
  "id": "user-123",
  "email": "user@example.com",
  "name": "John Doe",
  "baseCurrency": "USD",
  "monthlyBudget": 5000.00,
  "avatarUrl": "https://..."
}
```

#### PUT /api/profile

Обновить профиль.

**Request:**
```json
{
  "name": "New Name",
  "baseCurrency": "EUR",
  "monthlyBudget": 6000.00
}
```

#### POST /api/profile/avatar

Загрузить аватар.

**Request:** multipart/form-data с файлом изображения

**Response:**
```json
{
  "avatarUrl": "https://..."
}
```

### Чеки

#### POST /api/receipt/process

Обработать чек через OCR.

**Request:** multipart/form-data с файлом изображения

**Response:**
```json
{
  "merchantName": "Магнит",
  "merchantAddress": "ул. Ленина 1",
  "total": 1500.00,
  "currency": "RUB",
  "date": "2024-01-15T10:30:00Z",
  "items": [
    {
      "name": "Молоко",
      "quantity": 2,
      "price": 100.00,
      "total": 200.00
    }
  ],
  "confidence": 0.95
}
```

## Кэширование (Redis)

### Включение

```properties
redis.enabled=true
```

### Аннотации

```java
@Cacheable(value = "assets", key = "#user.id")
public List<AssetDTO> listForUser(User user) { ... }

@CacheEvict(value = {"assets", "dashboard"}, key = "#userId")
public void delete(String userId, String assetId) { ... }
```

### Кэш регионы

| Регион | TTL | Описание |
|--------|-----|----------|
| assets | 5 мин | Списки активов |
| rates | 1 мин | Курсы валют |
| dashboard | 2 мин | Данные дашборда |
| transactions | 1 мин | Списки транзакций |

## Уведомления (Kafka)

### Включение

```properties
kafka.enabled=true
```

### Топики

| Топик | Описание |
|-------|----------|
| transaction.created | Создание транзакции |
| transaction.updated | Обновление транзакции |
| asset.updated | Обновление актива |
| user.notification | Пользовательские уведомления |

### Формат сообщений

```json
{
  "userId": "user-123",
  "data": {
    "type": "transaction_created",
    "transactionId": "txn-123",
    "amount": 1500.00,
    "currency": "RUB"
  },
  "timestamp": 1704067200000
}
```

## Безопасность

### JWT Токены

- **Алгоритм**: HS256
- **Время жизни**: 24 часа (настраивается)
- **Claims**: sub (userId), email, name, iat, exp

### OAuth2 Google

- **JWK Set URI**: https://www.googleapis.com/oauth2/v3/certs
- **Required scopes**: email, profile

### CORS

Настройка разрешенных origin через `app.security.allowed-origins`.

## Обработка ошибок

### Формат ответа

```json
{
  "type": "https://api.goida.ai/errors/resource-not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Asset with id 'asset-123' not found",
  "instance": "/api/assets/asset-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Типы ошибок

| Тип | HTTP Status | Описание |
|-----|-------------|----------|
| BadRequestException | 400 | Некорректный запрос |
| ResourceNotFoundException | 404 | Ресурс не найден |
| UnauthorizedException | 401 | Неавторизован |
| InternalServerErrorException | 500 | Внутренняя ошибка |

## Тестирование

### Запуск тестов

```bash
./mvnw test
```

### Интеграционные тесты

```bash
./mvnw test -Dtest=*IntegrationTest
```

### Покрытие

Требуемое минимальное покрытие: 80%

## Развертывание

### Docker

```bash
docker build -t goida-backend .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://db:5432/goida \
  -e AUTH_SECRET=secret \
  goida-backend
```

### Docker Compose

```bash
docker-compose up -d
```

### Переменные окружения

| Переменная | Описание | Default |
|------------|----------|---------|
| DB_URL | JDBC URL БД | jdbc:postgresql://localhost:5432/goida |
| DB_USERNAME | Пользователь БД | postgres |
| DB_PASSWORD | Пароль БД | postgres |
| AUTH_SECRET | Секрет JWT | local-development-secret... |
| AUTH_TOKEN_TTL_SECONDS | Время жизни токена | 86400 |
| REDIS_ENABLED | Включить Redis | false |
| KAFKA_ENABLED | Включить Kafka | false |

## Мониторинг

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### Логи

Логи выводятся в stdout в формате JSON.

## Производительность

### Рекомендации

1. Включить Redis кэширование для production
2. Настроить connection pool для БД
3. Использовать курсорную пагинацию
4. Оптимизировать SQL запросы с помощью индексов

### Индексы БД

- `idx_assets_user_id` - assets(user_id)
- `idx_transactions_user_id` - transactions(user_id)
- `idx_transactions_occurred_at` - transactions(occurredAt)
- `idx_transactions_category` - transactions(category)

## Лицензия

MIT
