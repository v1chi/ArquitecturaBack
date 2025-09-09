# Social Network Backend

Minimal Spring Boot backend with JWT authentication and PostgreSQL.

## Stack

- Spring Boot 3 (Java 17)
- Spring Security + JWT
- Spring Data JPA + PostgreSQL

## Quick Start (Docker)

```bash
docker compose up --build -d
```

- App: http://localhost:8080

## Stop (Docker)

```bash
docker compose down
```

Tip: use `docker compose stop` to stop without removing networks/containers.

## Run Locally

- Requirements: Java 17, Maven, PostgreSQL
- Ensure a DB exists that matches `.env`
```bash
mvn spring-boot:run
```

## Environment

Set in `.env`:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET` (64+ chars)
- `API_BASE_URL` (used for email links; default http://localhost:8080)

## Auth
- POST `/auth/register` → sends confirmation email (no token)
- GET `/auth/confirm-email?token=...` → activate account
- POST `/auth/login` → returns `{access_token}` (28 days)
- POST `/auth/request-password-reset` → sends reset email
- POST `/auth/reset-password` → set a new password with token
- Use header: `Authorization: Bearer <access_token>`

## Reset Database (Docker)

With containers stopped, remove the PostgreSQL data volume:

```bash
docker volume rm backend_db_data
```

Then start the stack again using the Start/Stop commands above.
