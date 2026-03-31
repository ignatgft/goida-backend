# Dokploy Deployment Guide - Goida AI Backend

## Обзор

Данная инструкция описывает процесс развертывания backend приложения Goida AI на платформе Dokploy.

## Предварительные требования

- Сервер с установленным Dokploy
- Docker и Docker Compose
- Доменное имя (опционально)
- PostgreSQL база данных
- Redis (опционально, для кэширования)
- Kafka (опционально, для уведомлений)

## Структура проекта

```
test_backend/
├── src/main/java/ru/goidaai/test_backend/
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── .dockerignore
```

## Шаг 1: Подготовка Docker образа

### Dockerfile

Приложение уже содержит готовый Dockerfile:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Копируем исходный код и компилируем
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем скомпилированный JAR
COPY --from=build /app/target/*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### .dockerignore

```
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/java/**
!**/src/main/resources/**
mvnw
mvnw.cmd
.env
*.log
```

## Шаг 2: Настройка в Dokploy

### 2.1 Создание нового сервиса

1. Откройте панель управления Dokploy
2. Нажмите **"Create Service"** → **"Docker Image"** или **"GitHub Repository"**
3. Выберите репозиторий: `https://github.com/ignatgft/goida-backend.git`
4. Branch: `main`
5. Build Path: `/test_backend`

### 2.2 Настройки сборки

**Build Settings:**
```
Build Command: ./mvnw clean package -DskipTests -B
Output Directory: target/
Dockerfile Path: Dockerfile (по умолчанию)
```

**Port Mapping:**
```
Container Port: 8080
Host Port: 8080 (или любой другой свободный)
```

### 2.3 Переменные окружения

Добавьте следующие переменные в разделе **Environment Variables**:

```bash
# Основные настройки
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# PostgreSQL (обязательно)
DB_URL=jdbc:postgresql://<host>:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=<your-secure-password>
JPA_DDL_AUTO=update
FLYWAY_ENABLED=false

# Безопасность (обязательно)
AUTH_SECRET=<generate-secure-random-string-min-32-chars>
AUTH_TOKEN_TTL_SECONDS=86400

# CORS (обязательно для фронтенда)
CORS_ALLOWED_ORIGINS=http://your-frontend-domain.com,http://localhost:3000

# Google OAuth (опционально)
GOOGLE_WEB_CLIENT_ID=<your-google-client-id>

# AI Provider (опционально)
AI_PROVIDER=groq
GROQ_API_KEY=<your-groq-api-key>
GROQ_MODEL=llama-3.3-70b-versatile

# Exchange Rates (опционально)
RATES_PROVIDER=mock
EXCHANGE_API_KEY=

# Storage
STORAGE_UPLOAD_DIR=/app/storage
STORAGE_PUBLIC_BASE_URL=https://your-domain.com

# Receipt OCR (опционально)
RECEIPT_PROVIDER=mock

# Redis (опционально, для кэширования)
REDIS_ENABLED=false
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TTL_MINUTES=10

# Kafka (опционально, для уведомлений)
KAFKA_ENABLED=false
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_CONSUMER_GROUP_ID=test-backend-group
```

### 2.4 Health Check

Настройте health check для мониторинга состояния приложения:

```yaml
Health Check:
  Endpoint: /actuator/health
  Interval: 30s
  Timeout: 10s
  Retries: 3
  Start Period: 60s
```

## Шаг 3: Настройка базы данных

### Вариант A: Внешняя PostgreSQL (рекомендуется)

1. Создайте PostgreSQL базу данных на внешнем сервисе (Supabase, Neon, Railway, etc.)
2. Получите credentials
3. Добавьте в переменные окружения `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

### Вариант B: PostgreSQL через Dokploy

1. Создайте новый сервис **"Database"** → **"PostgreSQL"**
2. Настройте параметры:
   - Database Name: `goida`
   - User: `postgres`
   - Password: `<secure-password>`
3. После создания скопируйте connection string
4. Добавьте в переменные окружения backend сервиса

## Шаг 4: Настройка Redis (опционально)

Для включения кэширования:

1. Создайте сервис **"Database"** → **"Redis"**
2. Настройте пароль (рекомендуется)
3. Добавьте переменные окружения в backend:
   ```bash
   REDIS_ENABLED=true
   REDIS_HOST=<redis-service-host>
   REDIS_PORT=6379
   REDIS_PASSWORD=<redis-password>
   REDIS_TTL_MINUTES=10
   ```

## Шаг 5: Настройка Kafka (опционально)

Для включения системы уведомлений:

1. Создайте сервис **"Database"** → **"Kafka"**
2. Настройте топики:
   - `transaction.created`
   - `transaction.updated`
   - `asset.updated`
   - `user.notification`
3. Добавьте переменные окружения в backend:
   ```bash
   KAFKA_ENABLED=true
   KAFKA_BOOTSTRAP_SERVERS=<kafka-host>:9092
   KAFKA_CONSUMER_GROUP_ID=goida-backend-group
   ```

## Шаг 6: Настройка домена и SSL

### Domain Configuration

1. В панели Dokploy перейдите в настройки сервиса
2. Раздел **"Domains"**
3. Добавьте ваш домен: `api.your-domain.com`
4. Dokploy автоматически настроит SSL сертификат через Let's Encrypt

### DNS Records

Добавьте DNS записи у вашего регистратора доменов:

```
A     api.your-domain.com    <your-server-ip>
CNAME www.api.your-domain.com api.your-domain.com
```

## Шаг 7: Мониторинг и логи

### Логи

Просмотр логов в Dokploy:
- Вкладка **"Logs"** в интерфейсе сервиса
- Real-time streaming логов
- Фильтрация по уровню (INFO, WARN, ERROR)

### Метрики

Приложение предоставляет метрики через Spring Actuator:

```bash
# Health check
curl https://api.your-domain.com/actuator/health

# Metrics
curl https://api.your-domain.com/actuator/metrics

# Prometheus metrics (если включен)
curl https://api.your-domain.com/actuator/prometheus
```

## Шаг 8: Автоматический деплой

### GitHub Actions (CI/CD)

Создайте файл `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Dokploy

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: ./mvnw clean package -DskipTests -B
      
      - name: Deploy to Dokploy
        run: |
          # Dokploy автоматически деплоит при пуше в main
          # Если нужен manual trigger, используйте Dokploy API
          curl -X POST ${{ secrets.DOKPLOY_WEBHOOK_URL }}
```

### Dokploy Webhook

1. В настройках сервиса найдите **"Webhooks"**
2. Скопируйте webhook URL
3. Добавьте в GitHub Secrets как `DOKPLOY_WEBHOOK_URL`

## Шаг 9: Резервное копирование

### База данных

Настройте автоматическое резервное копирование PostgreSQL:

```bash
# Пример скрипта для backup
#!/bin/bash
pg_dump $DB_URL > backup_$(date +%Y%m%d_%H%M%S).sql
# Загрузите backup в S3 или другое хранилище
```

### Конфигурация

Сохраните все переменные окружения в безопасном месте (password manager, Vault, etc.)

## Шаг 10: Troubleshooting

### Приложение не запускается

1. Проверьте логи в Dokploy
2. Убедитесь что все обязательные переменные установлены
3. Проверьте подключение к базе данных

```bash
# Проверка подключения к БД
curl https://api.your-domain.com/actuator/health
```

### Ошибки CORS

Добавьте домен фронтенда в `CORS_ALLOWED_ORIGINS`:

```bash
CORS_ALLOWED_ORIGINS=https://frontend.your-domain.com,https://app.your-domain.com
```

### Проблемы с памятью

Увеличьте лимиты памяти в настройках сервиса Dokploy:

```yaml
Resources:
  Memory Limit: 2GB
  CPU Limit: 2 cores
```

### Медленные запросы

Включите логирование SQL:

```bash
JPA_SHOW_SQL=true
```

## Безопасность

### Рекомендации

1. **AUTH_SECRET**: Используйте случайную строку минимум 32 символа
   ```bash
   openssl rand -base64 32
   ```

2. **Пароль БД**: Минимум 16 символов, буквы + цифры + спецсимволы

3. **HTTPS**: Всегда используйте HTTPS в production

4. **Firewall**: Откройте только порт 8080 и 443

5. **Regular Updates**: Обновляйте зависимости регулярно

## Production Checklist

- [ ] Все обязательные переменные окружения установлены
- [ ] База данных настроена и подключена
- [ ] HTTPS настроен через Dokploy
- [ ] Health check работает
- [ ] Логи доступны и мониторятся
- [ ] Резервное копирование настроено
- [ ] CORS настроен для фронтенда
- [ ] AUTH_SECRET установлен в безопасное значение
- [ ] Мониторинг памяти и CPU настроен
- [ ] Документация обновлена

## Контакты поддержки

- GitHub Issues: https://github.com/ignatgft/goida-backend/issues
- Email: support@goida.ai
- Telegram: @goida_support

## Приложения

### A. Полный список переменных окружения

| Переменная | Обязательная | По умолчанию | Описание |
|------------|--------------|--------------|----------|
| SPRING_PROFILES_ACTIVE | Нет | prod | Профиль Spring |
| SERVER_PORT | Нет | 8080 | Порт сервера |
| DB_URL | Да | - | JDBC URL БД |
| DB_USERNAME | Да | - | Пользователь БД |
| DB_PASSWORD | Да | - | Пароль БД |
| JPA_DDL_AUTO | Нет | update | DDL режим |
| FLYWAY_ENABLED | Нет | false | Миграции Flyway |
| AUTH_SECRET | Да | - | JWT секрет |
| AUTH_TOKEN_TTL_SECONDS | Нет | 86400 | Время жизни токена |
| CORS_ALLOWED_ORIGINS | Нет | localhost | CORS origin |
| REDIS_ENABLED | Нет | false | Включить Redis |
| KAFKA_ENABLED | Нет | false | Включить Kafka |
| AI_PROVIDER | Нет | mock | AI провайдер |
| GROQ_API_KEY | Нет | - | Groq API ключ |

### B. Пример docker-compose.yml для локальной разработки

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=local
      - DB_URL=jdbc:postgresql://db:5432/goida
      - DB_USERNAME=postgres
      - DB_PASSWORD=postgres
      - AUTH_SECRET=local-dev-secret-change-in-production
    depends_on:
      - db
    volumes:
      - ./storage:/app/storage

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=goida
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### C. Команды для диагностики

```bash
# Проверка состояния приложения
curl https://api.your-domain.com/actuator/health

# Проверка метрик
curl https://api.your-domain.com/actuator/metrics

# Проверка подключения к БД
curl https://api.your-domain.com/api/profile

# Просмотр логов (Dokploy CLI)
dokploy logs <service-name>

# Перезапуск сервиса
dokploy restart <service-name>
```
