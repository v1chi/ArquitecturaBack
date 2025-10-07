# Social Network Backend

Minimal Spring Boot backend with JWT authentication, PostgreSQL, and comprehensive CI/CD pipeline.

## Stack

- Spring Boot 3 (Java 17)
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- JaCoCo for code coverage (65% minimum)
- GitHub Actions CI/CD
- Docker containerization

## CI/CD Pipeline

This project includes a comprehensive CI/CD pipeline with:

 Automated Testing
- Unit tests with Mockito
- Code coverage analysis with JaCoCo (65% minimum required)

Pipeline Demo
Last pipeline demonstration: 7 de octubre de 2025 - Pipeline funcionando correctamente 
- Coverage reports on pull requests

Automated Deployment
- **Railway**: Automatic deployment to Railway (primary)
- **Render**: Alternative deployment option
- Docker-based deployments

Setup CI/CD

1. **For Railway Deployment:**
   ```
   # Add these secrets to your GitHub repository:
   RAILWAY_TOKEN=your_railway_token
   RAILWAY_SERVICE_NAME=your_service_name
   ```

2. **For Render Deployment (alternative):**
   ```
   # Add these secrets to your GitHub repository:
   RENDER_SERVICE_ID=your_service_id  
   RENDER_API_KEY=your_api_key
   ```

3. **Coverage Reports:**
   - Codecov integration (optional)
   - Coverage comments on PRs automatically

## Testing & Coverage

### Run tests locally:
```bash
mvn clean test
```

### Generate coverage report:
```bash
mvn jacoco:report
```

### Verify coverage meets minimum threshold (65%):
```bash
mvn jacoco:check
```

### View coverage report:
Open `target/site/jacoco/index.html` in your browser

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



