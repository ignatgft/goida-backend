# Goida AI Backend

Финансовый бекенд для мобильного приложения учета финансов с AI-ассистентом.

## 🚀 Особенности

- **Учет финансов**: транзакции, активы, бюджеты
- **Мультивалютность**: 160+ фиатных валют, 30+ криптовалют
- **Аналитика**: статистика расходов, тренды, проценты трат
- **AI-ассистент**: интеграция с Groq API (Llama 3.3)
- **Безопасность**: JWT аутентификация, OAuth2 Google
- **Конвертация**: реальные курсы валют через API

## 📁 Структура проекта

```
src/main/java/ru/goidaai/test_backend/
├── config/              # Конфигурация безопасности и CORS
├── controller/          # REST API контроллеры
├── dto/                 # Data Transfer Objects
│   └── analytics/       # DTO для аналитики
├── exception/           # Обработка исключений
├── model/               # JPA сущности
├── repository/          # Spring Data репозитории
├── security/            # JWT и OAuth2
├── service/             # Бизнес-логика
│   ├── analytics/       # Аналитика и статистика
│   ├── rates/           # Провайдеры курсов валют
│   └── transaction/     # Сервисы транзакций
└── dto/                 # DTO запросов и ответов
```

## 🛠 Технологии

- Java 17
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 16
- Docker & Docker Compose
- ExchangeRate-API (фиат)
- CoinGecko (крипта)
- Groq API (AI)

## 🚀 Быстрый старт

### Через Docker Compose

```bash
# Клонировать репозиторий
git clone <repository-url>
cd test_backend

# Настроить окружение
cp .env.example .env
# Отредактировать .env (пароли, API ключи)

# Запустить
docker compose up -d --build

# Проверить
docker compose logs -f
```

API доступно на: `http://localhost:8080`

### Локальная разработка

```bash
# Запустить с профилем local
mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Или через IDE
# Запустить TestBackendApplication.java
```

## 📝 Конфигурация

### Переменные окружения (.env)

```env
# Профиль
SPRING_PROFILES_ACTIVE=prod

# Сервер
SERVER_PORT=8080

# База данных
DB_URL=jdbc:postgresql://postgres:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=your-password

# JWT
AUTH_SECRET=your-secret-key-min-32-chars
AUTH_TOKEN_TTL_SECONDS=86400

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# AI (Groq)
GROQ_API_KEY=your-groq-api-key
GROQ_MODEL=llama-3.3-70b-versatile

# Хранилище
STORAGE_UPLOAD_DIR=/app/storage
STORAGE_PUBLIC_BASE_URL=http://localhost:8080
```

## 📡 API Endpoints

### Аутентификация
- `POST /api/auth/google` - Google OAuth
- `POST /api/auth/dev` - Dev login (для тестов)

### Профиль
- `GET /api/profile` - Получить профиль
- `POST /api/profile/avatar` - Загрузить аватар

### Дашборд
- `GET /api/dashboard/overview?period=month` - Сводка с аналитикой

### Активы
- `GET /api/assets` - Список активов
- `POST /api/assets` - Создать актив
- `PUT /api/assets/{id}` - Обновить актив
- `DELETE /api/assets/{id}` - Удалить актив

### Транзакции
- `GET /api/transactions` - Список транзакций
- `POST /api/transactions` - Создать транзакцию
- `PUT /api/transactions/{id}` - Обновить транзакцию
- `DELETE /api/transactions/{id}` - Удалить транзакцию

### Курсы валют
- `GET /api/rates/fiat?base=USD` - Курсы фиата
- `GET /api/rates/crypto?fiat=USD` - Курсы крипты

### AI Чат
- `POST /api/chat` - Отправить сообщение AI

[Полная документация](API_DOCUMENTATION.md)

## 🧪 Тестирование

```bash
# Запустить тесты
mvnw test

# Интеграционные тесты
mvnw test -Dtest=ApiIntegrationTest
```

## 📦 Развёртывание

### Dokploy

1. Установить Dokploy на VPS
2. Создать проект и приложение
3. Подключить репозиторий
4. Настроить переменные окружения
5. Deploy

### Docker

```bash
# Собрать образ
docker build -t goida-backend .

# Запустить
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host:5432/goida \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=password \
  goida-backend
```

## 🔒 Безопасность

- JWT токены истекают через 24 часа
- Пароли хешируются
- CORS настроен для разрешённых origin
- Валидация всех входных данных
- SQL инъекции предотвращаются через JPA

## 📊 Мониторинг

```bash
# Логи
docker compose logs -f backend

# Метрики
GET /actuator/health
GET /actuator/metrics

# База данных
docker exec goida-postgres psql -U postgres -d goida
```

## 🤝 Вклад

1. Fork репозиторий
2. Создай ветку (`git checkout -b feature/amazing-feature`)
3. Commit изменений (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing-feature`)
5. Открой Pull Request

## 📄 Лицензия

MIT

## 📞 Контакты

- Telegram: @yourusername
- Email: your@email.com

---

**Goida AI** - Умный учет финансов 🚀
