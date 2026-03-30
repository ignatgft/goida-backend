# Dokploy Deployment Guide

## Overview
This guide explains how to deploy the backend and PostgreSQL database to Dokploy.

## Environment Variables for Dokploy

Configure the following environment variables in your Dokploy service:

### Required Variables

```bash
# Server
SERVER_PORT=8080

# CORS - Add your frontend URL
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080,http://57.151.105.173:3000,http://57.151.105.173

# Authentication
AUTH_SECRET=<generate-strong-random-secret>
AUTH_TOKEN_TTL_SECONDS=86400

# Database (internal Docker network)
DB_URL=jdbc:postgresql://postgres:5432/goida
DB_USERNAME=postgres
DB_PASSWORD=<your-db-password>
POSTGRES_DB=goida
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your-db-password>

# JPA
JPA_DDL_AUTO=update
FLYWAY_ENABLED=false
JPA_SHOW_SQL=false

# Storage
STORAGE_UPLOAD_DIR=/app/storage
STORAGE_PUBLIC_BASE_URL=http://57.151.105.173:3000

# File Upload
MULTIPART_MAX_FILE_SIZE=10MB
MULTIPART_MAX_REQUEST_SIZE=20MB

# Google OAuth (optional)
GOOGLE_WEB_CLIENT_ID=1021431144189-7tqhb0pare644pauck40j728vdos7dup.apps.googleusercontent.com
GOOGLE_JWK_SET_URI=https://www.googleapis.com/oauth2/v3/certs

# AI Provider (optional)
AI_PROVIDER=mock
# or for Groq:
# AI_PROVIDER=groq
# GROQ_API_KEY=<your-groq-api-key>
# GROQ_BASE_URL=https://api.groq.com/openai/v1
# GROQ_MODEL=llama-3.3-70b-versatile

# Rates Provider (optional)
RATES_PROVIDER=mock
# or for real rates:
# RATES_PROVIDER=exchange-api
# EXCHANGE_API_KEY=<your-exchange-api-key>

# Receipt Provider (optional)
RECEIPT_PROVIDER=mock
```

## Dokploy Setup Steps

### 1. Create PostgreSQL Service

1. In Dokploy dashboard, create a new **PostgreSQL** service
2. Configure:
   - Database name: `goida`
   - Username: `postgres`
   - Password: `<your-secure-password>`
3. Note the internal connection URL (should be `postgresql://postgres:5432/goida`)

### 2. Create Backend Service

1. In Dokploy dashboard, create a new **Docker Compose** service
2. Upload or link to this repository
3. Set the docker-compose.yml path to the root of the backend
4. Add all environment variables from above
5. Configure the service to use the same network as PostgreSQL

### 3. Network Configuration

The docker-compose.yml is configured to use the `dokploy-network` external network. Make sure:
- Both PostgreSQL and backend services are on the same network
- The backend can reach PostgreSQL at `postgres:5432`

### 4. Expose Backend

1. In Dokploy, configure the backend service to listen on port `3000`
2. Map external port `3000` to internal port `8080`
3. Ensure the domain/IP `http://57.151.105.173:3000` is accessible

### 5. Volumes

Dokploy will automatically manage volumes for:
- `postgres_data` - PostgreSQL data persistence
- `goida_storage` - File uploads storage

## Frontend Configuration

The frontend (Flutter app) is configured to connect to:
- **API URL**: `http://57.151.105.173:3000/api`

To change the API URL, update `lib/core/api/endpoints.dart` in the frontend repository.

## Health Check

After deployment, verify the backend is running:
```bash
curl http://57.151.105.173:3000/api/health
```

## Troubleshooting

### CORS Issues
If you get CORS errors, make sure your frontend URL is in `CORS_ALLOWED_ORIGINS`.

### Database Connection Errors
- Verify PostgreSQL service is healthy
- Check the internal network connectivity
- Ensure `DB_URL` uses the correct internal hostname (`postgres`)

### Storage Issues
- Verify `STORAGE_UPLOAD_DIR` is writable
- Check `STORAGE_PUBLIC_BASE_URL` matches your public IP/port
