# API Документация Goida Backend

## Обзор

Бекенд предоставляет REST API для мобильного приложения учета финансов.

**Base URL:** `http://localhost:8080/api`

## Аутентификация

Все запросы (кроме `/auth/*` и `/rates/*`) требуют JWT токен в заголовке:

```
Authorization: Bearer <your-token>
```

---

## Endpoints

### 🔐 Аутентификация

#### Google Login
```http
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "string",
  "accessToken": "string",
  "googleId": "string",
  "email": "string",
  "displayName": "string",
  "photoUrl": "string"
}
```

**Ответ:**
```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbG...",
  "sessionToken": "eyJhbG...",
  "expiresIn": 86400,
  "user": { ... }
}
```

#### Dev Login (только для разработки)
```http
POST /api/auth/dev
Content-Type: application/json

{
  "email": "test@example.com",
  "fullName": "Test User"
}
```

---

### 👤 Профиль

#### Получить профиль
```http
GET /api/profile
Authorization: Bearer <token>
```

**Ответ:**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "fullName": "User Name",
  "avatarUrl": "https://...",
  "baseCurrency": "USD",
  "monthlyBudget": 1000.00,
  "authProvider": "GOOGLE",
  "emailVerified": true,
  "lastLoginAt": "2024-01-01T00:00:00Z",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### Загрузить аватар
```http
POST /api/profile/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <image-file>
```

**Ответ:**
```json
{
  "avatarUrl": "http://localhost:8080/uploads/avatars/uuid.jpg"
}
```

---

### 📊 Дашборд

#### Получить сводку дашборда
```http
GET /api/dashboard/overview?period=month
Authorization: Bearer <token>
```

**Параметры:**
- `period` (опционально): `day`, `week`, `month`, `year`, `all` (по умолчанию: `month`)

**Ответ:**
```json
{
  "userId": "uuid",
  "baseCurrency": "USD",
  "periodLabel": "Last 30 days",
  "assets": [ ... ],
  "spending": {
    "spent": 500.00,
    "budget": 1000.00
  },
  "budgetStatus": {
    "budget": 1000.00,
    "spent": 500.00,
    "remaining": 500.00,
    "percentageUsed": 50.00,
    "dailyAverage": 16.67,
    "projectedEndOfMonth": 1000.00
  },
  "categoryBreakdown": [
    {
      "category": "food",
      "categoryLabel": "Еда",
      "amount": 200.00,
      "percentage": 40.00,
      "percentageOfBudget": 20.00,
      "transactionCount": 15
    }
  ],
  "trend": {
    "period": "Last 30 days",
    "totalSpent": 500.00,
    "averageDaily": 16.67,
    "peakDayAmount": 50.00,
    "dailyBreakdown": [ ... ]
  },
  "spendingPercentages": {
    "food": 40.00,
    "transport": 20.00,
    "shopping": 30.00,
    "other": 10.00
  },
  "totalNetWorth": 10000.00
}
```

---

### 💰 Активы

#### Список активов
```http
GET /api/assets
Authorization: Bearer <token>
```

#### Создать актив
```http
POST /api/assets
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Cash Wallet",
  "type": "cash",
  "currency": "USD",
  "amount": 1000.00,
  "note": "My cash"
}
```

**Типы активов:** `cash`, `bank_account`, `savings`, `investments`, `crypto`

**Ответ:**
```json
{
  "id": "uuid",
  "name": "Cash Wallet",
  "type": "cash",
  "currency": "USD",
  "amount": 1000.00,
  "symbol": "USD",
  "balance": 1000.00,
  "currentValue": 1000.00,
  "baseCurrency": "USD",
  "note": "My cash",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### Обновить актив
```http
PUT /api/assets/{assetId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Name",
  "type": "cash",
  "currency": "USD",
  "amount": 1500.00
}
```

#### Удалить актив
```http
DELETE /api/assets/{assetId}
Authorization: Bearer <token>
```

**Ответ:** `204 No Content`

---

### 💸 Транзакции

#### Список транзакций
```http
GET /api/transactions?category=food&period=month&limit=20&cursor=abc123
Authorization: Bearer <token>
```

**Параметры:**
- `category` (опционально): категория для фильтрации
- `period` (опционально): `day`, `week`, `month`, `year`, `all`
- `limit` (опционально): количество записей (по умолчанию: 20, макс: 100)
- `cursor` (опционально): курсор для пагинации

**Ответ:**
```json
{
  "items": [
    {
      "id": "uuid",
      "title": "Coffee",
      "category": "food",
      "type": "expense",
      "kind": "EXPENSE",
      "sourceType": "MANUAL",
      "amount": 5.50,
      "currency": "USD",
      "occurredAt": "2024-01-01T10:00:00Z",
      "createdAt": "2024-01-01T10:00:00Z",
      "sourceAssetId": "uuid",
      "sourceAssetName": "Cash Wallet",
      "note": "Morning coffee",
      "receipt": null
    }
  ],
  "nextCursor": "xyz789",
  "hasMore": true
}
```

#### Создать транзакцию
```http
POST /api/transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Coffee",
  "amount": 5.50,
  "currency": "USD",
  "category": "food",
  "kind": "EXPENSE",
  "sourceAssetId": "uuid",
  "occurredAt": "2024-01-01T10:00:00Z",
  "note": "Morning coffee",
  "receipt": {
    "merchant": "Starbucks",
    "total": 5.50,
    "currency": "USD",
    "purchasedAt": "2024-01-01T10:00:00Z",
    "items": [
      {
        "name": "Latte",
        "quantity": 1,
        "unitPrice": 5.50,
        "totalPrice": 5.50
      }
    ]
  }
}
```

**Типы транзакций:**
- `kind`: `EXPENSE`, `INCOME`, `TRANSFER`
- `sourceType`: `MANUAL`, `OCR` (автоматически при наличии чека)

#### Обновить транзакцию
```http
PUT /api/transactions/{transactionId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "amount": 10.00,
  "category": "shopping",
  "kind": "EXPENSE"
}
```

#### Удалить транзакцию
```http
DELETE /api/transactions/{transactionId}
Authorization: Bearer <token>
```

**Ответ:** `204 No Content`

---

### 📈 Курсы валют

#### Фиатные курсы
```http
GET /api/rates/fiat?base=USD
```

**Ответ:**
```json
{
  "base": "USD",
  "retrievedAt": "2024-01-01T00:00:00Z",
  "rates": {
    "EUR": 0.92,
    "RUB": 92.50,
    "KZT": 450.00
  }
}
```

#### Крипто курсы
```http
GET /api/rates/crypto?fiat=USD
```

**Ответ:**
```json
{
  "quoteCurrency": "USD",
  "retrievedAt": "2024-01-01T00:00:00Z",
  "prices": {
    "BTC": 45000.00,
    "ETH": 3000.00,
    "USDT": 1.00
  }
}
```

---

### 🧾 Чеки

#### Обработать чек
```http
POST /api/receipt/process
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <image-file>
```

**Ответ:**
```json
{
  "id": "uuid",
  "merchant": "Store Name",
  "total": 100.00,
  "currency": "USD",
  "purchasedAt": "2024-01-01T00:00:00Z",
  "items": [
    {
      "name": "Product",
      "quantity": 2,
      "unitPrice": 50.00,
      "totalPrice": 100.00
    }
  ],
  "imageUrl": "http://localhost:8080/uploads/receipts/uuid.jpg",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

## Категории транзакций

**Расходы:**
- `food` - Еда
- `transport` - Транспорт
- `shopping` - Покупки
- `bills` - Счета
- `subscriptions` - Подписки
- `entertainment` - Развлечения
- `health` - Здоровье
- `education` - Обучение
- `travel` - Путешествия
- `crypto` - Криптовалюта
- `other` - Другое

**Доходы:**
- `salary` - Зарплата
- `other` - Другое

---

## Обработка ошибок

**Формат ошибок:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be positive",
  "path": "/api/transactions"
}
```

**Коды статусов:**
- `200 OK` - Успешный запрос
- `201 Created` - Ресурс создан
- `204 No Content` - Ресурс удалён
- `400 Bad Request` - Ошибка валидации
- `401 Unauthorized` - Требуется аутентификация
- `403 Forbidden` - Нет доступа
- `404 Not Found` - Ресурс не найден
- `500 Internal Server Error` - Ошибка сервера

---

## Пагинация

Для списков с большим количеством записей используется курсорная пагинация:

1. Получите первые N записей: `GET /api/transactions?limit=20`
2. Если в ответе `hasMore: true`, используйте `nextCursor` для следующей страницы
3. Запрос: `GET /api/transactions?limit=20&cursor=<nextCursor>`

---

## Безопасность

- Все пароли и токены хранятся в зашифрованном виде
- JWT токены истекают через 24 часа
- CORS настроен для разрешённых origin
- SQL инъекции предотвращаются через JPA/Hibernate
- Валидация входных данных на уровне DTO
