# E-Commerce Java

Full-stack e-commerce platform — Spring Boot 4 + Next.js.

## Features

- **Auth** — Register, login, JWT tokens, refresh
- **Products** — CRUD, variants (color/storage), filters, search, categories, colors
- **Cart** — Add/remove/update variants, guest → user merge
- **Orders** — Create from cart or direct, status workflow, shipping cost strategies
- **Payments** — Stripe checkout, webhook, refund, admin transaction stats
- **Users** — Profile, avatar upload, addresses, password change
- **Admin** — Dashboard, sales/customer analytics, store settings, Stripe payout management
- **Reviews** — Ratings, comments, product review stats
- **Wishlist** — Add/remove products

## Tech Stack

| Layer | Stack |
|---|---|
| Backend | Java 17, Spring Boot 4, Spring Security, Spring Data JPA, Flyway |
| Database | MySQL 8, Redis (cache) |
| Message Queue | RabbitMQ (async event processing) |
| Payments | Stripe SDK |
| Frontend | Next.js, Tailwind CSS, TypeScript |
| Infra | Docker Compose (nginx, backend, frontend, MySQL, Redis, RabbitMQ) |

## Architecture

Domain-module structure with **Command/Query Separation**:

```
common/         → Shared: BaseCrudService, events, shipping strategies
modules/
  users/        → Auth + Account services
  products/     → CRUD, variants, categories, colors
  orders/       → Command + Query services
  payments/     → PaymentCommand + StripeSession + StripeWebhook + PaymentQuery
  cart/         → Cart service
  reviews/      → Review service
  wishlist/     → Wishlist service
  adminpanel/   → Analytics + Settings + Payment stats
```

## Event-Driven Messaging

```
OrderCommandService ──→ EventPublisher ──→ RabbitMQ order.exchange
                                             ├── order.created.queue        → LoggingEventHandler
                                             └── order.status.changed.queue → LoggingEventHandler

PaymentCommandService ──→ EventPublisher ──→ RabbitMQ payment.exchange
                                              └── payment.succeeded.queue   → LoggingEventHandler
```

All domain events are published asynchronously via RabbitMQ Topic Exchanges, decoupling core business logic from side-effects (logging, notifications, analytics).

### Design Patterns Applied

| Pattern | Implementation |
|---|---|
| **Strategy** | `ShippingCostStrategy` ← `StandardShippingStrategy`, `ExpressShippingStrategy`, `OvernightShippingStrategy` + `ShippingCostCalculator` context |
| **Observer (Async)** | `OrderCreatedEvent` / `PaymentSucceededEvent` / `OrderStatusChangedEvent` published via `EventPublisher` → RabbitMQ Topic Exchange → async consumers (`LoggingEventHandler`) |
| **Template Method** | `BaseCrudService<T>` defines CRUD skeleton → `CategoryCrudService`, `ColorCrudService` override specifics |
| **Command/Query Separation** | Split mutations / reads: `PaymentCommandService` + `PaymentQueryService`, `OrderCommandService` + `OrderQueryService`, `ProductCommandService` + `ProductService` |
| **Auth/Account Separation** | `UserAuthService` (register/login/refresh/token) separate from `UserAccountService` (profile/address/password) — isolates security-sensitive domain |
| **Facade** | `PaymentCommandService` orchestrates Stripe + Order + PaymentTransaction; `AdminPanelService` provides simplified interface for login + payment stats |
| **Adapter** | `StripeSessionService` wraps Stripe SDK into domain methods; `StripeWebhookHandler` adapts raw JSON into domain updates |
| **Chain of Responsibility** | `TokenAuthenticationFilter` in Spring Security filter chain — authenticates Bearer token before reaching controllers |
| **DTO / Record** | All request/response use Java `record` — immutable, auto getter/constructor |
| **Repository (DAO)** | Spring Data JPA `JpaRepository` — each entity has its own repository |
| **Layer Isolation** | Each module: Controller → Service → Repository, no cross-module dependencies except through `common/` |

## Quick Start

```bash
cp .env.example .env        # edit credentials
docker compose up -d        # starts all services
```

Backend: `http://localhost:8080`  
Frontend: `http://localhost:3000`

### Without Docker

```bash
# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev
```
