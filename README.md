# StaySphere — Smart Room Rental Platform

StaySphere is a full-stack rental booking platform for rooms, PGs, flats and hostels. Owners list properties, tenants search and book them, and payments flow through a separate payment microservice. Admins moderate users and listings from a dedicated dashboard.

## Architecture

The system is split into three deployable units:

- **StaySphere_Backend** — Spring Boot monolith. Owns users, properties, bookings, facilities, transactions, audit logs and the owner/admin dashboards.
- **payment-service** — a separate Spring Boot microservice that owns Razorpay integration and payment records. It is called internally by the backend over HTTP, authenticated with a shared internal API key, and is the only service that talks to Razorpay directly.
- **staysphere-frontend** — a React (Vite) single-page app consuming both services through the backend's REST API.

Payments were split out into their own service to isolate the third-party payment integration and its secrets from the rest of the domain logic, and so it can be scaled, redeployed or swapped independently of the core booking flow.

## Tech stack

- **Backend:** Java 17, Spring Boot, Spring Security (JWT), Spring Data JPA, MySQL, springdoc-openapi
- **Payment service:** Spring Boot, Razorpay Java SDK
- **Frontend:** React 18, React Router, Axios, Vite
- **Database:** MySQL (database-first — schema in `StaySphere_Backend/src/main/resources/db/schema-design.sql`)

## Roles

- **TENANT** — searches properties, requests bookings, tracks payment status
- **OWNER** — lists and manages properties, approves/rejects bookings, records offline payments, views a payout dashboard
- **ADMIN** — manages user accounts, moderates property listings, views audit logs

## Core features

- JWT-based auth with role-based access control (`@PreAuthorize`)
- Property search with filters (city, type, occupancy, rent range) and pagination
- Booking lifecycle: `REQUESTED → PAYMENT_PENDING → CONFIRMED`, with `REJECTED`/`CANCELLED` branches
- Overlap-aware room availability per property
- Online payments via Razorpay (through payment-service) and manual offline payment recording
- Owner payout account management
- Admin audit log of sensitive actions

## Prerequisites

- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8+

## Running locally

**1. Database**

MySQL is created automatically on first run (`createDatabaseIfNotExist=true`), but the schema itself is database-first — run `StaySphere_Backend/src/main/resources/db/schema-design.sql` against your MySQL instance before starting the backend.

**2. Environment variables**

Both backend services read secrets from environment variables, falling back to local-dev placeholders if unset. For anything beyond local development, set: