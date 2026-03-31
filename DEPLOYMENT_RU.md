# Полное руководство по деплою Goida AI Backend в Dokploy

## Содержание

1. [Требования](#требования)
2. [Подготовка сервера](#подготовка-сервера)
3. [Установка Dokploy](#установка-dokploy)
4. [Настройка базы данных](#настройка-базы-данных)
5. [Деплой приложения](#деплой-приложения)
6. [Настройка домена и SSL](#настройка-домена-и-ssl)
7. [Мониторинг и логи](#мониторинг-и-логи)
8. [Резервное копирование](#резервное-копирование)
9. [Troubleshooting](#troubleshooting)

---

## Требования

### Минимальные
- **CPU**: 2 ядра
- **RAM**: 4 GB
- **Disk**: 40 GB SSD
- **OS**: Ubuntu 22.04 LTS / Debian 11+

### Рекомендуемые
- **CPU**: 4 ядра
- **RAM**: 8 GB
- **Disk**: 80 GB SSD
- **OS**: Ubuntu 22.04 LTS

---

## Подготовка сервера

### 1. Обновление системы

```bash
sudo apt update && sudo apt upgrade -y
sudo reboot
```

### 2. Установка Docker

```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker
```

### 3. Проверка Docker

```bash
docker --version
docker compose version
```

---

## Установка Dokploy

### 1. Запуск Dokploy

```bash
docker run -d \
  --name dokploy \
  -p 3000:3000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v dokploy-data:/app/data \
  --restart unless-stopped \
  dokploy/dokploy:latest
```

### 2. Открытие панели управления

Откройте в браузере: `http://<ваш-IP>:3000`

### 3. Первоначальная настройка

1. Создайте администратора (email + пароль)
2. Сохраните credentials в менеджере паролей
3. Войдите в панель

---

## Настройка базы данных

### Вариант 1: PostgreSQL через Dokploy (рекомендуется)

1. В панели Dokploy нажмите **"Create Service"**
2. Выберите **"Database"** → **"PostgreSQL"**
3. Настройте параметры:
   - **Name**: `goida-db`
   - **Database**: `goida`
   - **User**: `postgres`
   - **Password**: `<сложный-пароль-минимум-16-символов>`
4. Нажмите **"Create"**
5. Скопируйте **Internal Connection String** (вида `postgresql://user:pass@host:5432/db`)

### Вариант 2: Внешняя PostgreSQL

1. Создайте БД на сервисе (Supabase, Neon, Railway, Timeweb Cloud)
2. Получите credentials
3. Скопируйте connection string

---

## Деплой приложения

### 1. Подготовка репозитория

Убедитесь что код в репозитории:

```bash
cd /path/to/test_backend
git status
git push origin main
```

### 2. Создание сервиса в Dokploy

1. В панели Dokploy нажмите **"Create Service"**
2. Выберите **"GitHub Repository"**
3. Подключите GitHub аккаунт (если еще не подключен)
4. Выберите репозиторий: `ignatgft/goida-backend`
5. Branch: `main`
6. Build Path: `/test_backend` (оставьте пустым если pom.xml в корне)

### 3. Настройка сборки

**Build Command:**
```
./mvnw clean package -DskipTests -B
```

**Dockerfile Path:**
```
Dockerfile
```

**Port:**
```
8080
```

### 4. Переменные окружения

Добавьте следующие переменные в разделе **Environment Variables**:

```bash
# ===== ОБЯЗАТЕЛЬНЫЕ =====

# Профиль
SPRING_PROFILES_ACTIVE=prod

# Порт
SERVER_PORT=8080

# База данных (обязательно)
DB_URL=jdbc:postgresql://goida-db:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=<ваш-пароль-от-БД>
JPA_DDL_AUTO=update
FLYWAY_ENABLED=false

# Безопасность (обязательно)
AUTH_SECRET=$(openssl rand -base64 32)
# Пример: AUTH_SECRET=xY7kL9mN2pQ5rS8tU1vW4xZ6aB3cD0eF

# ===== ОПЦИОНАЛЬНЫЕ =====

# CORS (обязательно для фронтенда)
CORS_ALLOWED_ORIGINS=http://your-domain.com,https://your-domain.com,http://localhost:3000

# Google OAuth (если используется)
GOOGLE_WEB_CLIENT_ID=<your-google-client-id>

# AI Provider (Groq для ИИ)
AI_PROVIDER=groq
GROQ_API_KEY=<your-groq-api-key>
GROQ_MODEL=llama-3.3-70b-versatile

# Курсы валют
RATES_PROVIDER=mock

# Storage
STORAGE_UPLOAD_DIR=/app/storage
STORAGE_PUBLIC_BASE_URL=https://your-domain.com

# Redis (кэширование, отключено по умолчанию)
REDIS_ENABLED=false

# Kafka (уведомления, отключено по умолчанию)
KAFKA_ENABLED=false
```

### 5. Запуск деплоя

1. Нажмите **"Deploy"**
2. Дождитесь завершения сборки (3-7 минут)
3. Проверьте логи на наличие ошибок
4. Проверьте health check: `http://<IP>:8080/actuator/health`

---

## Настройка домена и SSL

### 1. Добавление домена в Dokploy

1. В настройках сервиса перейдите в **"Domains"**
2. Нажмите **"Add Domain"**
3. Введите домен: `api.your-domain.com`
4. Dokploy автоматически настроит SSL через Let's Encrypt

### 2. DNS записи

Добавьте у вашего регистратора доменов:

```
A     api.your-domain.com    <IP-адрес-сервера>
CNAME www.api.your-domain.com api.your-domain.com
```

### 3. Проверка SSL

```bash
curl -I https://api.your-domain.com/actuator/health
```

Должен вернуться статус 200.

---

## Мониторинг и логи

### Просмотр логов в Dokploy

1. Откройте сервис в панели
2. Вкладка **"Logs"**
3. Real-time streaming логов
4. Фильтрация по уровню (INFO, WARN, ERROR)

### Health Check

```bash
# Проверка здоровья
curl https://api.your-domain.com/actuator/health

# Проверка метрик
curl https://api.your-domain.com/actuator/metrics

# Проверка памяти
curl https://api.your-domain.com/actuator/metrics/jvm.memory.used
```

### Уведомления

Настройте уведомления в Dokploy:
1. Settings → Notifications
2. Добавьте Email для алертов
3. Опционально: Webhook в Telegram/Slack

---

## Резервное копирование

### База данных

#### Автоматический backup скрипт

Создайте файл `/usr/local/bin/backup-goida.sh`:

```bash
#!/bin/bash

BACKUP_DIR="/backups/goida"
DATE=$(date +%Y%m%d_%H%M%S)

# Создаем директорию
mkdir -p $BACKUP_DIR

# Получаем имя контейнера с БД
DB_CONTAINER=$(docker ps --filter "name=goida-db" --format "{{.ID}}")

if [ -z "$DB_CONTAINER" ]; then
    echo "Контейнер с БД не найден"
    exit 1
fi

# Делаем backup
docker exec $DB_CONTAINER pg_dump -U postgres goida > $BACKUP_DIR/backup_$DATE.sql

# Сжимаем
gzip $BACKUP_DIR/backup_$DATE.sql

# Удаляем старые backup (хранить 7 дней)
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "Backup создан: backup_$DATE.sql.gz"
```

#### Сделайте скрипт исполняемым

```bash
sudo chmod +x /usr/local/bin/backup-goida.sh
```

#### Cron job для автоматического запуска

```bash
sudo crontab -e
```

Добавьте строку (запуск каждый день в 3:00):

```
0 3 * * * /usr/local/bin/backup-goida.sh
```

### Конфигурация

Сохраните переменные окружения:

```bash
docker inspect <container-name> | grep -A 20 "Env" > /backups/goida/env_backup.txt
```

---

## Troubleshooting

### Приложение не запускается

**Проверка логов:**

```bash
docker logs goida-backend --tail 100
```

**Проверка подключения к БД:**

```bash
docker exec goida-backend env | grep DB_URL
```

**Перезапуск:**

```bash
docker restart goida-backend
```

### Ошибки CORS

Добавьте домен фронтенда в `CORS_ALLOWED_ORIGINS`:

```bash
CORS_ALLOWED_ORIGINS=https://frontend.your-domain.com
```

### Проблемы с памятью

**Увеличение лимитов в Dokploy:**

1. Settings → Resources
2. Memory Limit: 2GB
3. CPU Limit: 2 cores

**Оптимизация JVM:**

Добавьте в Environment Variables:

```bash
JAVA_OPTS=-Xmx1g -Xms512m
```

### Медленные запросы

**Включите логирование SQL:**

```bash
JPA_SHOW_SQL=true
logging.level.org.hibernate.SQL=DEBUG
```

### Ошибки аутентификации

**Проверка AUTH_SECRET:**

```bash
# Должен быть минимум 32 символа
echo $AUTH_SECRET | wc -c
```

**Генерация нового:**

```bash
openssl rand -base64 32
```

### Ошибки анализа документов

**Проверка логов:**

```bash
docker logs goida-backend 2>&1 | grep -i "анализ\|document\|PDF"
```

**Проверка размера файла:**

Максимальный размер: 10MB

---

## Production Checklist

Перед запуском в production убедитесь:

- [ ] Все переменные окружения установлены
- [ ] AUTH_SECRET минимум 32 символа (сгенерирован через openssl)
- [ ] Пароль БД сложный (16+ символов, буквы+цифры+спецсимволы)
- [ ] HTTPS настроен через Dokploy
- [ ] Health check работает (`/actuator/health`)
- [ ] Логи доступны и мониторятся
- [ ] Backup настроен (ежедневный)
- [ ] CORS настроен для фронтенда
- [ ] Мониторинг памяти/CPU настроен
- [ ] Документация обновлена
- [ ] Тесты проходят (`mvn test`)

---

## Команды для диагностики

```bash
# Статус контейнеров
docker ps -a | grep goida

# Логи приложения
docker logs goida-backend --tail 100 -f

# Использование ресурсов
docker stats goida-backend

# Подключение к БД
docker exec -it goida-db psql -U postgres -d goida

# Проверка переменных окружения
docker exec goida-backend env

# Перезапуск сервиса
docker restart goida-backend

# Остановка
docker stop goida-backend

# Удаление (с данными!)
docker rm -f goida-backend
```

---

## Безопасность

### Рекомендации

1. **Брандмауэр**: Откройте только порты 80, 443, 22

   ```bash
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   sudo ufw allow 22/tcp
   sudo ufw enable
   sudo ufw status
   ```

2. **Fail2Ban**: Защита от brute-force

   ```bash
   sudo apt install fail2ban -y
   sudo systemctl enable fail2ban
   sudo systemctl start fail2ban
   ```

3. **Regular Updates**: Обновляйте зависимости

   ```bash
   # Backend
   cd test_backend
   ./mvnw versions:display-dependency-updates
   
   # Frontend
   cd demo2
   flutter pub outdated
   ```

4. **Monitoring**: Настройте алерты
   - Падение сервиса
   - Высокое использование CPU (>80%)
   - Высокое использование RAM (>80%)
   - Ошибки 5xx

---

## Контакты поддержки

- **GitHub Issues**: https://github.com/ignatgft/goida-backend/issues
- **Email**: support@goida.ai
- **Telegram**: @goida_support

---

## Приложения

### A. Полный docker-compose.yml для локальной разработки

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
      - CORS_ALLOWED_ORIGINS=http://localhost:3000
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

### B. Пример .env файла

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=secure-password-here

# Security
AUTH_SECRET=your-random-32-char-string-here
AUTH_TOKEN_TTL_SECONDS=86400

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# AI
AI_PROVIDER=groq
GROQ_API_KEY=your-api-key-here

# Storage
STORAGE_UPLOAD_DIR=./storage
STORAGE_PUBLIC_BASE_URL=http://localhost:8080
```

### C. Чеклист перед деплоем

```markdown
## Pre-Deployment Checklist

### Код
- [ ] Все тесты проходят (`mvn test`)
- [ ] Нет warning'ов в логах
- [ ] Code review выполнен
- [ ] Документация обновлена

### Безопасность
- [ ] AUTH_SECRET установлен (32+ символа)
- [ ] Пароли сложные (16+ символов)
- [ ] HTTPS настроен
- [ ] CORS настроен

### База данных
- [ ] Backup настроен
- [ ] Миграции применены
- [ ] Индексы созданы

### Мониторинг
- [ ] Health check работает
- [ ] Логи доступны
- [ ] Алерты настроены

### Документация
- [ ] README обновлен
- [ ] API документация актуальна
- [ ] Переменные окружения задокументированы
```
