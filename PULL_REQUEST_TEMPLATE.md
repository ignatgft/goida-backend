# Pull Request: Март 2026 - Анализ документов, уведомления, напоминания, ИИ чат

## Описание

Этот PR добавляет новые функции для анализа документов, систему уведомлений и напоминаний, историю чатов с ИИ, а также настройки пользователя.

## Тип изменений

✨ New Feature

## Изменения

### Новые функции

#### 1. Анализ документов (PDF/скриншоты)
- **DocumentAnalysisService** - сервис для распознавания чеков и квитанций
- Авто-определение валюты (10+ валют: USD, EUR, RUB, KZT, GBP, JPY, CNY, CHF, BTC, ETH, USDT)
- Распознавание сумм, дат, мерчантов
- Поддержка PDF через Apache PDFBox
- **API**: `POST /api/documents/analyze`

#### 2. Система уведомлений
- **Notification** модель и сервис
- Типы: TRANSACTION, ASSET, REMINDER, SYSTEM, MESSAGE
- Отметка прочитанными
- Подсчет непрочитанных
- **API**: `GET/POST /api/notifications/*`

#### 3. Система напоминаний
- **Reminder** модель и сервис
- Повторяющиеся напоминания (DAILY, WEEKLY, MONTHLY)
- Проверка и отправка уведомлений
- **API**: `GET/POST /api/reminders/*`

#### 4. История чатов с ИИ
- **AiChatHistory** с контекстом для каждого сообщения
- Загрузка файлов в чат с ИИ
- Управление историей чатов
- **API**: `/api/ai-chat/*`

#### 5. Настройки пользователя
- Язык (RU/EN), тема (light/dark)
- Базовая валюта, месячный бюджет
- Загрузка/удаление аватара
- **API**: `/api/settings/*`

#### 6. Чистая архитектура
- **Domain слой**: модели (AssetPool, PoolItem, Notification, Reminder, AiChatHistory, WalletConnect)
- **Application слой**: use case сервисы (AssetPoolService, ChatService, NotificationService, ReminderService, AiChatHistoryService, WalletConnectService)
- **Adapter слой**: REST контроллеры, JPA репозитории
- **Port интерфейсы**: для инверсии зависимостей

### Технические изменения

#### Новые зависимости
- Apache PDFBox 2.0.30 - обработка PDF
- Lombok - уменьшение boilerplate кода
- MapStruct - маппинг DTO

#### Оптимизации
- Redis кэширование (отключено по умолчанию через `redis.enabled=false`)
- Kafka уведомления (отключено по умолчанию через `kafka.enabled=false`)
- WebSocket для real-time сообщений

#### Документация
- `DEPLOYMENT_RU.md` - полное руководство по деплою в Dokploy
- `AI_CONTEXT.md` - контекст проекта для ИИ ассистентов

## Тестирование

### Unit тесты
```
✅ AssetPoolServiceTest: 5 тестов пройдено
✅ ChatServiceTest: 7 тестов пройдено
```

### Компиляция
```
✅ BUILD SUCCESS - все компилируется без ошибок
```

### Integration тесты
⚠️ Требуют настройки тестового контекста (не критично для продакшена)

## Переменные окружения

### Обязательные
```properties
# База данных
DB_URL=jdbc:postgresql://host:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=<password>

# Безопасность
AUTH_SECRET=<32+ символа>
```

### Опциональные
```properties
# Redis кэширование
REDIS_ENABLED=false

# Kafka уведомления
KAFKA_ENABLED=false

# AI провайдер
AI_PROVIDER=groq
GROQ_API_KEY=<key>
```

## Чек-лист

- [x] Код компилируется без ошибок
- [x] Unit тесты проходят
- [x] Документация обновлена
- [x] Переменные окружения задокументированы
- [ ] Integration тесты (требуют настройки)

## Скриншоты

Не применимо (backend изменения)

## Совместимость

- **Java**: 17+
- **Spring Boot**: 4.0.4
- **PostgreSQL**: 14+

## Миграции

Миграции не требуются (JPA DDL_AUTO=update)

## Риски

- Минимальные - новые функции изолированы
- Redis и Kafka отключены по умолчанию

## Инструкция по деплою

См. `DEPLOYMENT_RU.md` для подробной инструкции по деплою в Dokploy.

## Ссылки

- Репозиторий: https://github.com/ignatgft/goida-backend
- Документация: https://github.com/ignatgft/goida-backend/blob/main/DEPLOYMENT_RU.md
