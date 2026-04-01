# Goida AI Backend - Complete Deployment Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Local Development Setup](#local-development-setup)
5. [Production Deployment](#production-deployment)
6. [Docker Deployment](#docker-deployment)
7. [Dokploy/Kubernetes Deployment](#dokploykubernetes-deployment)
8. [Configuration Reference](#configuration-reference)
9. [Monitoring & Troubleshooting](#monitoring--troubleshooting)
10. [Security Best Practices](#security-best-practices)

---

## Overview

Goida AI Backend is a Spring Boot 4.x based financial management application with the following features:

- **User Management**: Google OAuth2 authentication, JWT tokens
- **Financial Tracking**: Assets, transactions, budgets, receipts
- **AI Assistant**: Integration with Groq/OpenAI for financial advice
- **Real-time Messaging**: WebSocket-based user-to-user chat
- **File Management**: Avatar upload, receipt scanning, document analysis
- **Multi-language**: Russian and English localization
- **Notifications**: Email and push notifications (optional)

### Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.0.4 |
| Language | Java 17 |
| Database | PostgreSQL 16 / H2 (local) |
| Security | Spring Security 6 + OAuth2 + JWT |
| ORM | Spring Data JPA + Hibernate |
| Cache | Redis (optional) |
| Messaging | Kafka (optional) |
| WebSocket | Spring WebSocket + STOMP |
| AI | Groq API / OpenAI |
| Build | Maven |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Goida AI Backend                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   REST API   │  │  WebSocket   │  │   Security   │      │
│  │  Controllers │  │   Messages   │  │   JWT/OAuth2 │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Application Services                     │  │
│  │  Avatar │ Chat │ AI │ Transactions │ Assets │ OCR   │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Repository  │  │   Entities   │  │     DTOs     │      │
│  │    Layer     │  │    (JPA)     │  │  (Mapping)   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│  External Services: PostgreSQL │ Redis │ Kafka │ Groq     │
└─────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

### Required Software

- **JDK 17+**: Eclipse Temurin or OpenJDK
- **Maven 3.8+**: Build tool
- **PostgreSQL 16+**: Production database
- **Git**: Version control

### Optional Software

- **Docker & Docker Compose**: Containerization
- **Redis 7+**: Caching layer
- **Kafka 3+**: Event streaming
- **Dokploy**: Kubernetes deployment platform

### Environment Requirements

- **Memory**: Minimum 1GB RAM (2GB recommended)
- **Storage**: 500MB for application + database space
- **CPU**: 1 core minimum (2+ cores recommended)
- **Network**: Port 8080 (configurable)

---

## Local Development Setup

### Step 1: Clone Repository

```bash
git clone https://github.com/ignatgft/goida-backend.git
cd test_backend
```

### Step 2: Configure Environment

Create `.env` file in project root:

```bash
# Local Development Configuration
SPRING_PROFILES_ACTIVE=local

# H2 Database (auto-configured for local)
# No additional setup needed

# Security (development only)
AUTH_SECRET=local-development-secret-key-for-testing-only-must-be-long-enough
AUTH_TOKEN_TTL_SECONDS=86400

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080

# AI (optional - mock responses if not set)
AI_PROVIDER=mock
GROQ_API_KEY=

# Storage
STORAGE_UPLOAD_DIR=./storage
STORAGE_PUBLIC_BASE_URL=http://localhost:8080
```

### Step 3: Run Application

```bash
# Using Maven wrapper (recommended)
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run

# Or build and run JAR
./mvnw clean package -DskipTests
java -jar target/test_backend-0.0.1-SNAPSHOT.jar
```

### Step 4: Verify Setup

Open browser and navigate to:

- **Application**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:goida`
  - Username: `sa`
  - Password: (empty)

### Step 5: Run Tests

```bash
./mvnw test
```

---

## Production Deployment

### Database Setup

#### Option A: Managed PostgreSQL (Recommended)

Services like Supabase, Neon, Railway, AWS RDS:

1. Create PostgreSQL database
2. Get connection credentials
3. Set environment variables:
   ```bash
   DB_URL=jdbc:postgresql://<host>:5432/goida
   DB_USERNAME=<username>
   DB_PASSWORD=<secure-password>
   ```

#### Option B: Self-hosted PostgreSQL

```bash
# Install PostgreSQL 16
sudo apt install postgresql-16

# Create database and user
sudo -u postgres psql
CREATE DATABASE goida;
CREATE USER goida_user WITH PASSWORD 'secure-password';
GRANT ALL PRIVILEGES ON DATABASE goida TO goida_user;
\q
```

### Application Build

```bash
# Build production JAR
./mvnw clean package -DskipTests

# Verify JAR
java -jar target/test_backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Systemd Service (Linux)

Create `/etc/systemd/system/goida-backend.service`:

```ini
[Unit]
Description=Goida AI Backend Service
After=network.target postgresql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/goida-backend
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="AUTH_SECRET=your-secure-secret-key-min-32-chars"
Environment="DB_URL=jdbc:postgresql://localhost:5432/goida"
Environment="DB_USERNAME=goida_user"
Environment="DB_PASSWORD=secure-password"
ExecStart=/usr/bin/java -jar target/test_backend-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable goida-backend
sudo systemctl start goida-backend
sudo systemctl status goida-backend
```

---

## Docker Deployment

### Dockerfile

The project includes a multi-stage Dockerfile:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build Docker Image

```bash
docker build -t goida-backend:latest .
```

### Docker Compose (Full Stack)

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  app:
    image: goida-backend:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://db:5432/goida
      - DB_USERNAME=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - AUTH_SECRET=${AUTH_SECRET}
      - CORS_ALLOWED_ORIGINS=https://your-domain.com
      - STORAGE_UPLOAD_DIR=/app/storage
      - STORAGE_PUBLIC_BASE_URL=https://your-domain.com
    depends_on:
      db:
        condition: service_healthy
    volumes:
      - app-storage:/app/storage
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=goida
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped
    environment:
      - REDIS_ENABLED=true
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}

volumes:
  app-storage:
  postgres-data:
  redis-data:
```

### Run with Docker Compose

```bash
# Create .env file
cat > .env << EOF
DB_PASSWORD=your-secure-db-password
AUTH_SECRET=your-secure-auth-secret-min-32-chars
REDIS_PASSWORD=your-secure-redis-password
EOF

# Start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f app

# Stop services
docker-compose -f docker-compose.prod.yml down
```

---

## Dokploy/Kubernetes Deployment

### Step 1: Create Service in Dokploy

1. Open Dokploy dashboard
2. Click **"Create Service"** → **"Docker Image"** or **"GitHub Repository"**
3. Repository: `https://github.com/ignatgft/goida-backend.git`
4. Branch: `main`
5. Build Path: `/test_backend`

### Step 2: Configure Build Settings

```yaml
Build Command: ./mvnw clean package -DskipTests -B
Output Directory: target/
Container Port: 8080
```

### Step 3: Environment Variables

```bash
# Required
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<db-host>:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=<secure-password>
AUTH_SECRET=<generate-32-char-random-string>
CORS_ALLOWED_ORIGINS=https://your-domain.com

# Optional
AI_PROVIDER=groq
GROQ_API_KEY=<your-groq-key>
REDIS_ENABLED=true
REDIS_HOST=redis-service
REDIS_PORT=6379
KAFKA_ENABLED=false
STORAGE_PUBLIC_BASE_URL=https://api.your-domain.com
```

### Step 4: Configure Domain & SSL

1. Add domain: `api.your-domain.com`
2. Dokploy auto-provisions SSL via Let's Encrypt
3. Update DNS records:
   ```
   A    api.your-domain.com    <server-ip>
   ```

### Step 5: Health Check

```yaml
Health Check:
  Endpoint: /actuator/health
  Interval: 30s
  Timeout: 10s
  Retries: 3
  Start Period: 60s
```

---

## Configuration Reference

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | No | `prod` | Active Spring profile |
| `SERVER_PORT` | No | `8080` | HTTP server port |
| `DB_URL` | **Yes** | - | PostgreSQL JDBC URL |
| `DB_USERNAME` | **Yes** | - | Database username |
| `DB_PASSWORD` | **Yes** | - | Database password |
| `JPA_DDL_AUTO` | No | `update` | Hibernate DDL mode |
| `FLYWAY_ENABLED` | No | `false` | Enable Flyway migrations |
| `AUTH_SECRET` | **Yes** | - | JWT signing secret (min 32 chars) |
| `AUTH_TOKEN_TTL_SECONDS` | No | `86400` | JWT token TTL (24h) |
| `CORS_ALLOWED_ORIGINS` | No | `localhost` | Allowed CORS origins |
| `AI_PROVIDER` | No | `mock` | AI provider (`groq`, `openai`, `mock`) |
| `GROQ_API_KEY` | No | - | Groq API key |
| `GROQ_MODEL` | No | `llama-3.3-70b-versatile` | Groq model |
| `OPENAI_API_KEY` | No | - | OpenAI API key |
| `RATES_PROVIDER` | No | `mock` | Exchange rates provider |
| `EXCHANGE_API_KEY` | No | - | Exchange rate API key |
| `STORAGE_UPLOAD_DIR` | No | `./storage` | File upload directory |
| `STORAGE_PUBLIC_BASE_URL` | No | - | Public URL for uploaded files |
| `REDIS_ENABLED` | No | `false` | Enable Redis caching |
| `REDIS_HOST` | No | `localhost` | Redis host |
| `REDIS_PORT` | No | `6379` | Redis port |
| `REDIS_PASSWORD` | No | - | Redis password |
| `KAFKA_ENABLED` | No | `false` | Enable Kafka messaging |
| `KAFKA_BOOTSTRAP_SERVERS` | No | `localhost:9092` | Kafka brokers |
| `KAFKA_CONSUMER_GROUP_ID` | No | `test-backend-group` | Kafka consumer group |
| `RECEIPT_PROVIDER` | No | `mock` | Receipt OCR provider |

### Generate Secure Secrets

```bash
# AUTH_SECRET (32+ characters)
openssl rand -base64 32

# Database password
openssl rand -base64 24

# Or use pwgen
pwgen -s 32 1
```

---

## Monitoring & Troubleshooting

### Health Checks

```bash
# Basic health check
curl https://api.your-domain.com/actuator/health

# Detailed health
curl https://api.your-domain.com/actuator/health/readiness

# Metrics
curl https://api.your-domain.com/actuator/metrics

# Prometheus format
curl https://api.your-domain.com/actuator/prometheus
```

### Application Logs

```bash
# Docker logs
docker logs -f <container-id>

# Systemd logs
journalctl -u goida-backend -f

# Dokploy logs
dokploy logs <service-name>
```

### Common Issues

#### Application Won't Start

1. Check logs for errors
2. Verify database connectivity
3. Ensure all required env vars are set
4. Check port availability

```bash
# Test database connection
psql $DB_URL -c "SELECT 1"

# Check port
netstat -tlnp | grep 8080
```

#### CORS Errors

Add frontend domain to `CORS_ALLOWED_ORIGINS`:

```bash
CORS_ALLOWED_ORIGINS=https://frontend.your-domain.com,https://app.your-domain.com
```

#### Memory Issues

Increase JVM heap size:

```bash
JAVA_OPTS="-Xmx2g -Xms512m"
```

Add to Docker Compose:

```yaml
environment:
  - JAVA_OPTS=-Xmx2g -Xms512m
```

#### Slow Database Queries

Enable SQL logging:

```bash
JPA_SHOW_SQL=true
spring.jpa.properties.hibernate.format_sql=true
```

### Performance Tuning

#### Database Connection Pool (HikariCP)

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

#### JPA/Hibernate

```properties
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

---

## Security Best Practices

### 1. Secure Authentication

- Use strong `AUTH_SECRET` (32+ random characters)
- Rotate secrets regularly
- Use HTTPS in production
- Enable token expiration

### 2. Database Security

- Use strong passwords (16+ characters)
- Limit database user privileges
- Enable SSL for database connections
- Regular backups

### 3. Network Security

- Use firewall (ufw, iptables)
- Only expose necessary ports
- Use reverse proxy (Nginx, Traefik)
- Enable rate limiting

### 4. Application Security

```bash
# Production profile
SPRING_PROFILES_ACTIVE=prod

# Disable H2 console in prod
spring.h2.console.enabled=false

# Enable CSRF for sensitive endpoints
spring.security.csrf.enabled=true
```

### 5. Secrets Management

- Never commit secrets to Git
- Use environment variables or secrets manager
- Rotate credentials regularly
- Use different secrets for dev/staging/prod

### 6. File Upload Security

```properties
# Limit file sizes
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB

# Validate file types
# (Implemented in StorageService)
```

---

## Backup & Recovery

### Database Backup

```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump $DB_URL > backup_$DATE.sql

# Upload to S3
aws s3 cp backup_$DATE.sql s3://your-bucket/backups/

# Keep only last 7 days
find /backups -name "backup_*.sql" -mtime +7 -delete
```

### Restore Database

```bash
psql $DB_URL < backup_20240101_120000.sql
```

### Configuration Backup

Backup all environment variables and configuration files:

```bash
tar -czf goida-config-backup.tar.gz .env docker-compose.yml
```

---

## Production Checklist

- [ ] All required environment variables set
- [ ] Database configured and accessible
- [ ] HTTPS/SSL configured
- [ ] Health checks passing
- [ ] Logging and monitoring enabled
- [ ] Backups configured and tested
- [ ] CORS configured for frontend domains
- [ ] AUTH_SECRET set to secure random value
- [ ] Database credentials secured
- [ ] Firewall rules configured
- [ ] Resource limits set (memory, CPU)
- [ ] Documentation updated
- [ ] Rollback plan prepared

---

## Support & Resources

- **GitHub Issues**: https://github.com/ignatgft/goida-backend/issues
- **Documentation**: `/BACKEND_DOCUMENTATION.md`
- **API Documentation**: `/API_DOCUMENTATION.md`
- **Email**: support@goida.ai
- **Telegram**: @goida_support

---

## Appendix

### A. Quick Start Commands

```bash
# Local development
./mvnw spring-boot:run

# Build production JAR
./mvnw clean package -DskipTests

# Run with production profile
java -jar target/*.jar --spring.profiles.active=prod

# Docker build
docker build -t goida-backend:latest .

# Docker run
docker run -p 8080:8080 -e AUTH_SECRET=secret -e DB_URL=... goida-backend:latest

# Docker Compose
docker-compose -f docker-compose.prod.yml up -d
```

### B. API Endpoints Summary

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/auth/*` | POST | No | Authentication |
| `/api/profile` | GET | Yes | User profile |
| `/api/settings/*` | GET/PUT | Yes | User settings |
| `/api/avatars/*` | GET/POST/DELETE | Yes | Avatar management |
| `/api/messages/*` | GET/POST | Yes | User messaging |
| `/api/chat` | POST | Yes | AI chat |
| `/api/assets/*` | GET/POST/PUT/DELETE | Yes | Asset management |
| `/api/transactions/*` | GET/POST/PUT/DELETE | Yes | Transaction management |
| `/api/rates/*` | GET | No | Currency rates |
| `/api/receipt/process` | POST | Yes | Receipt OCR |
| `/api/dashboard/overview` | GET | Yes | Dashboard data |

### C. Default Ports

| Service | Port |
|---------|------|
| Application | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |
| H2 Console (local) | 8080/h2-console |
