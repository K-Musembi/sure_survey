# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sure Survey is a survey and rewards platform supporting multiple survey formats (SMS/Web/USSD/Mobile) with integrated payment processing, AI-powered analysis, and performance evaluation capabilities.

## Repository Structure

```
sure_survey/
├── backend/survey-engine/      # Java Spring Boot backend
└── user-interface/web/         # React/Vite frontend
```

## Commands

### Backend (from `backend/survey-engine/`)
```bash
mvn spring-boot:run             # Start dev server (port 8080)
mvn clean package               # Build JAR
mvn test                        # Run all tests
mvn test -Dtest=ClassName       # Run a single test class
```

### Frontend (from `user-interface/web/`)
```bash
npm run dev                     # Start dev server (port 5173)
npm run build                   # Production build
npm run lint                    # Run ESLint
```

## Architecture

### Backend: Spring Modulith (Modular Monolith)

The backend is a Spring Boot 3.5.6 / Java 17 modular monolith using Spring Modulith. Modules communicate through named interfaces (`@NamedInterface`) defined in `*Api.java` files — never call across module boundaries except through these interfaces.

**Modules** (`src/main/java/com/survey_engine/`):
- `user` — Authentication (JWT + OAuth2), user/tenant/participant management
- `survey` — Survey CRUD, question types, response collection
- `billing` — Subscription plans, wallet credit/debit, usage limits
- `payments` — Paystack payment processing, webhook handling
- `rewards` — Loyalty accounts, reward distribution, CredoFaster airtime
- `business_integration` — M-Pesa/Daraja webhooks, business API keys
- `performance_survey` — Performance evaluations, scoring, subject attribution
- `ai_analysis` — AI-powered survey generation/analysis via Groq LLM
- `common` — Shared utilities, audit logging, exception handling

**Infrastructure:**
- PostgreSQL + Flyway migrations (`src/main/resources/db/migration/`)
- Redis for caching
- RabbitMQ for async inter-module events
- HikariCP connection pooling (max 10 connections)

**API base path:** `http://localhost:8080/api/v1`

### Frontend: React + Vite

- **State management:** Zustand (`src/stores/`)
- **Server state:** TanStack React Query (`src/lib/queryClient.js`)
- **HTTP client:** Axios via `src/services/apiServices.js`
- **UI:** Tailwind CSS + Flowbite React components
- **Routing:** React Router v7
- Vite dev server proxies `/api` → `http://localhost:8080`

## Required Environment Variables

Set these before starting the backend:

| Variable | Purpose |
|---|---|
| `MAIN_SURVEY_ENGINE_PORT` | PostgreSQL port |
| `MAIN_SURVEY_ENGINE_DB` | Database name |
| `MAIN_SURVEY_ENGINE_ADMIN` | DB username |
| `MAIN_SURVEY_ENGINE_PASSWORD` | DB password |
| `JWT_STORE_PATH` | JKS keystore path |
| `JWT_STORE_PASSWORD` | Keystore password |
| `JWT_STORE_ALIAS` | Key alias |
| `APP_ENCRYPTION_PASSWORD` | Field-level encryption |
| `APP_ENCRYPTION_SALT` | Encryption salt |
| `GROQ_API_KEY` | Groq LLM (Spring AI) |
| `PAYSTACK_BASE_URL` | Paystack API base |
| `PAYSTACK_TEST_KEY` | Paystack secret key |
| `SAFARICOM_API_URL` | M-Pesa/Daraja API |
| `APP_BASE_URL` | Public app base URL |

## Key Conventions

- **Multi-tenancy:** All operations are scoped to a tenant; tenant context is propagated through services.
- **Module boundaries:** Cross-module calls go through `*Api` interfaces only. Implement these in `*ApiImpl` classes.
- **Database migrations:** Add Flyway migrations in `src/main/resources/db/migration/` as `V{N}__{description}.sql`. **Current latest is V34.**
- **Security:** JWT tokens expire in 60 minutes. Auth endpoints are under `/api/v1/auth/**`. Admin endpoints require `ROLE_SUPER_ADMIN`.
- **External service config:** Each module has its own YAML config file (e.g., `billing.yaml`, `payments.yaml`) in `src/main/resources/`.
- **Errors:** Throw `SurveyPlatformException` subclasses (`BusinessRuleException`, `ResourceNotFoundException`) — never plain `IllegalStateException` for domain errors.

## Modules (expanded)

- `referral` — Referral campaigns, invite dispatch, consent logging, data subject rights (ODPC)
- `intelligence` — Decision intelligence, AI-generated insight reports, action plans

## Billing Tiers (V29 + V31)

- **Basic** KES 2,999/mo: 10 surveys, 500 responses, Web + SMS
- **Pro** KES 7,999/mo: 50 surveys, 5,000 responses, all channels, AI, referral, performance surveys
- **Enterprise** (custom): unlimited, all features + webhooks, API access
- `GET /api/v1/billing/plans` is **public** (no auth). Enterprise shows "Contact Sales".

## Auth Architecture

- Access token: 15-min JWT (RSA-signed JKS), HttpOnly cookie `access_token`
- Refresh token: opaque UUID, 7-day TTL in Redis, HttpOnly cookie `refresh_token` (path `/api/v1/auth/refresh`)
- `POST /api/v1/auth/refresh` — public, rotates refresh token

## ODPC Compliance (Kenya Data Protection Act 2019)

### Migrations
- **V32** — `data_subject_requests` table (id UUID, request_type, phone_hash SHA-256, status, notes, tenant_id, timestamps)
- **V33** — `referral_campaigns.purpose_description`, `referral_campaigns.consent_version`; `referral_consent_log.purpose_snapshot`, `referral_consent_log.consent_version`
- **V34** — `system_settings` row: `PRIVACY_NOTICE_URL = 'https://suresurvey.co/privacy'`

### Data Subject Rights (`referral` module)
- `DataSubjectService` — phone pseudonymised as `SHA-256(phone + app-security.encryption.salt)`
- **SAR** `POST /api/v1/referrals/subjects/access` — returns invite + consent history for a phone (public, rate-limited 3/24h)
- **Erasure** `POST /api/v1/referrals/subjects/erasure` — pseudonymises phone in `referral_invites` + `referral_consent_log`, opts out active invites (public, rate-limited 3/24h)
- **Admin DSR list** `GET /api/v1/referrals/subjects/admin/requests` — requires `ROLE_SUPER_ADMIN`
- **Admin DSR update** `PATCH /api/v1/referrals/subjects/admin/requests/{id}` — requires `ROLE_SUPER_ADMIN`
- Rate limit enforced by counting `data_subject_requests` by `phone_hash` in the last 24 hours
- `ReferralConsentLog.phone` uses `@Modifying` JPQL query for erasure (bypasses `updatable=false` JPA constraint)

### Purpose Limitation
- `ReferralCampaign` has `purposeDescription` + `consentVersion` (incremented on purpose change)
- `ReferralConsentLog` snapshots `purposeSnapshot` + `consentVersion` at opt-in time (immutable)
- `onActionCompleted()` checks latest consent version against campaign version — throws `CONSENT_VERSION_STALE` if stale
- **Update purpose** `PATCH /api/v1/referrals/campaigns/{id}/purpose` — increments consent_version

### Privacy Notice in SMS
- `InviteDispatchService` reads `PRIVACY_NOTICE_URL` from `system_settings` and appends `" Privacy: {url}"` to all outbound referral SMS messages

### Security config
- `/api/v1/referrals/subjects/access` and `/api/v1/referrals/subjects/erasure` are in the **public** `securityMatcher` chain (no auth required)
