# StaySphere — Smart Room Rental Platform

StaySphere is a full-stack rental booking platform for rooms, PGs, flats and hostels. Owners list properties, tenants search and book them, and payments flow through a separate payment microservice. Admins moderate users and listings from a dedicated dashboard.

## Architecture

The system is split into three deployable units:

- **StaySphere_Backend** (`com.rms`) — Spring Boot monolith. Owns users, properties, bookings, facilities, transactions, audit logs and the owner/admin dashboards. Exposes the only API the frontend talks to.
- **payment-service** (`com.staysphere.payment`) — a separate Spring Boot microservice that owns Razorpay integration and payment records. It's called internally by the backend over HTTP (`PaymentServiceClient` → `PaymentServiceClientImpl`), authenticated with a shared `X-Internal-Api-Key` header, and is the only service that talks to Razorpay directly. It talks to Razorpay's REST API directly via Spring's `RestClient` and verifies signatures itself with HMAC-SHA256 — it does **not** use the Razorpay Java SDK.
- **staysphere-frontend** — a React (Vite) single-page app that only ever calls the backend (`StaySphere_Backend`); it never talks to payment-service directly.

Payments were split out into their own service to isolate the third-party payment integration and its secrets from the rest of the domain logic, and so it can be scaled, redeployed or swapped independently of the core booking flow.

## Tech stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT via `jjwt`), Spring Data JPA, MySQL, springdoc-openapi (Swagger UI)
- **Payment service:** Java 21, Spring Boot 3.3, Spring Data JPA, MySQL, Spring `RestClient` calling the Razorpay REST API directly (no Razorpay SDK), manual HMAC-SHA256 order/webhook signature verification
- **Frontend:** React 18, React Router 6, Axios, Vite 5
- **Database:** MySQL 8. The backend's schema is database-first (`StaySphere_Backend/src/main/resources/db/schema-design.sql`, loaded with `ddl-auto=validate`); payment-service instead lets Hibernate create/update its own schema automatically (`ddl-auto=update`) in a separate database.

## Roles

- **TENANT** — searches properties, requests bookings, pays and tracks payment status
- **OWNER** — lists and manages properties, approves/rejects bookings, records offline payments, manages a payout/bank account, views a dashboard
- **ADMIN** — manages user accounts, moderates property listings, views bookings and an audit log

## Core features

- JWT-based auth with role-based access control (`@PreAuthorize("hasRole(...)")` on nearly every write endpoint)
- Property search with filters and pagination (`GET /api/properties/search`), public browsing without login
- Facility and property-image management per listing
- Booking lifecycle: `REQUESTED → PAYMENT_PENDING → CONFIRMED`, with `REJECTED` / `CANCELLED` branches
- Online payments via Razorpay (through payment-service: create order → verify signature) and manual offline payment recording by the owner
- Owner payout account management and an owner earnings/bookings dashboard
- Admin audit log of sensitive actions
- Interactive API docs via Swagger UI (backend only)

## Prerequisites

- JDK 21+
- Maven 3.9+ (or use the bundled `./mvnw` / `mvnw.cmd` wrapper in each service)
- Node.js 18+
- MySQL 8+

## Project layout

```
StaySphere/
├── StaySphere_Backend/                    # Spring Boot monolith (port 8080)
├── payment-service/                       # Spring Boot payment microservice (port 8081)
└── staysphere-frontend/staysphere-frontend/  # React + Vite app (note the nested folder)
```

## Running locally

**1. Database**

Both services will auto-create their database on first connection (`createDatabaseIfNotExist=true`), but only payment-service lets Hibernate build its own tables. The backend expects its schema to already exist — run `StaySphere_Backend/src/main/resources/db/schema-design.sql` against MySQL before starting it. This creates and populates `staysphere_db`. `staysphere_payment_db` (used by payment-service) needs no manual setup.

**2. Environment variables**

Both services fall back to local-dev placeholder values if these are unset, so they will run out of the box against a local MySQL with the default `root`/`root` credentials. Override them for anything beyond local development:

*StaySphere_Backend*

| Variable | Default | Purpose |
|---|---|---|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `JWT_SECRET` | dev placeholder | HMAC signing key for JWTs |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | JWT lifetime |
| `PAYMENT_SERVICE_BASE_URL` | `http://localhost:8081` | Base URL used to call payment-service |
| `INTERNAL_API_KEY` | dev placeholder | Shared secret sent as `X-Internal-Api-Key` to payment-service — must match the same variable there |
| `PAYMENT_TOKEN_AMOUNT` | `2000` | Token/booking amount, in the smallest currency unit |

*payment-service*

| Variable | Default | Purpose |
|---|---|---|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `RAZORPAY_WEBHOOK_SECRET` | dev placeholder | Verifies incoming Razorpay webhook signatures |
| `RAZORPAY_BASE_URL` | `https://api.razorpay.com/v1` | Razorpay REST API base URL |
| `INTERNAL_API_KEY` | dev placeholder | Must match the backend's value |

> ⚠️ **`razorpay.key-id` and `razorpay.key-secret` are currently hardcoded (a Razorpay *test*-mode key pair) in `payment-service/src/main/resources/application.yml`** rather than read from environment variables. Before pushing this anywhere public or going beyond local testing, move these two to env vars the same way the other secrets are handled, and rotate the key pair.

**3. Start payment-service**

```bash
cd payment-service
./mvnw spring-boot:run
```

Runs on `http://localhost:8081`.

**4. Start the backend**

```bash
cd StaySphere_Backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html` (payment-service does not expose Swagger). CORS is pre-configured for `http://localhost:5173` only — update `SecurityConfig#corsConfigurationSource` if the frontend runs elsewhere.

**5. Start the frontend**

```bash
cd staysphere-frontend/staysphere-frontend
npm install
cp .env.example .env   # VITE_API_BASE_URL defaults to http://localhost:8080/api
npm run dev
```

Runs on `http://localhost:5173`.

## Frontend routes

| Path | Who | Purpose |
|---|---|---|
| `/` | anyone | Search + browse listings |
| `/properties/:propertyId` | anyone | Property details, request a booking |
| `/login`, `/register` | anyone | Auth |
| `/my-bookings` | TENANT | View/cancel bookings, pay |
| `/owner/dashboard` | OWNER | Totals + per-property earnings/bookings |
| `/owner/properties` | OWNER | Create/edit/delete listings |
| `/owner/properties/:propertyId/manage` | OWNER | Manage photos and facilities |
| `/owner/bookings` | OWNER | Confirm/reject booking requests |
| `/owner/payment-account` | OWNER | Manage payout/bank account |
| `/admin` | ADMIN | Manage users, properties, bookings, audit log |

## Payment flow

1. Frontend calls `POST /api/transactions/checkout` on the **backend** with `{ bookingId, paymentType, amount? }`.
2. The backend calls payment-service (internally, with the shared API key) to create a Razorpay order.
3. The frontend collects payment through Razorpay's checkout and gets back an order/payment/signature triple.
4. Frontend calls `POST /api/transactions/{transactionId}/verify`; the backend forwards this to payment-service, which verifies the HMAC-SHA256 signature against the Razorpay order and payment IDs and marks the payment accordingly.
5. Owners can instead record a payment made outside the app via `POST /api/transactions/offline`.

## Known gaps / things worth fixing

- Hardcoded Razorpay test-mode credentials in `payment-service/application.yml` (see warning above) — externalize and rotate before any public use.
- The nested `staysphere-frontend/staysphere-frontend/` directory name is easy to `cd` into wrong; consider flattening it.
- `payment-service`'s pom.xml has no dependency labeled Razorpay SDK — that's expected given the REST-based integration, but worth knowing if you're used to seeing `com.razorpay:razorpay-java` in similar projects.