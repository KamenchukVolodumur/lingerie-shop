# Lingerie Shop — Distributed E-Commerce Platform

A modular microservices-based e-commerce platform built with **Java 21**, **Spring Boot**, **PostgreSQL**, **Redis**, and **React**. Designed with a focus on transactional integrity, order idempotency, distributed concurrency control, and real-world payment integrations.

---

## Architecture Overview

The system is decomposed into four domain services and a single-page application frontend. Services communicate synchronously via declarative Feign clients and asynchronously through webhooks.

```mermaid
flowchart TD
    subgraph Client ["Client Layer"]
        UI["React SPA (Vite + Tailwind)<br/>Port: 5173"]
    end

    subgraph Microservices ["Backend Microservices (Spring Boot)"]
        PROD["Product Service<br/>Port: 8080"]
        CART["Cart Service<br/>Port: 8082"]
        ORD["Order Service<br/>Port: 8083"]
        PAY["Payment Service<br/>Port: 8084"]
    end

    subgraph Data ["Databases & Caches"]
        DB_PROD[("PostgreSQL<br/>lingerie_products")]
        DB_ORD[("PostgreSQL<br/>lingerie_orders")]
        DB_PAY[("PostgreSQL<br/>lingerie_payments")]
        REDIS[("Redis 7<br/>Cart Data & Locks")]
    end

    subgraph External ["External Services & APIs"]
        CLOUDINARY["Cloudinary<br/>(Media CDN)"]
        NP["Nova Poshta API<br/>(Warehouses & Shipping)"]
        MONO["Monobank API<br/>(2-Stage Payments & Webhooks)"]
    end

    UI -->|REST /api/products| PROD
    UI -->|REST /api/carts| CART
    UI -->|REST /api/v1/orders| ORD
    UI -->|Admin & Status| PAY

    CART -->|OpenFeign + CircuitBreaker| PROD
    ORD -->|OpenFeign| PROD
    ORD -->|OpenFeign| CART
    ORD -->|OpenFeign| PAY
    PAY -->|OpenFeign (status callbacks)| ORD

    PROD --> DB_PROD
    ORD --> DB_ORD
    PAY --> DB_PAY
    CART --> REDIS

    PROD -.-> CLOUDINARY
    ORD -.-> NP
    PAY -.-> MONO
```

---

## Services & Tech Stack

| Service | Port | Storage | Key Responsibilities |
| :--- | :---: | :--- | :--- |
| **`product-service`** | `8080` | PostgreSQL (`lingerie_products`) | Catalog management, multi-variant products, inventory deduction/release, Cloudinary image upload. |
| **`cart-service`** | `8082` | Redis 7 | User carts, transient storage with TTL, distributed locking via Redisson, Resilience4j Circuit Breaker for product metadata lookups. |
| **`order-service`** | `8083` | PostgreSQL (`lingerie_orders`) | Order state machine, checkout flow, `Idempotency-Key` deduplication, Nova Poshta shipping autocomplete and address validation. |
| **`payment-service`** | `8084` | PostgreSQL (`lingerie_payments`) | Payment transaction lifecycle, Monobank hold/capture/cancel flow, ECDSA webhook signature verification. |
| **`frontend`** | `5173` | — | Customer storefront & Admin Panel (React 19, Vite, Tailwind CSS, Lucide icons, Axios). |

---

## Key Engineering Decisions

### 1. Order Idempotency (Preventing Duplicate Charges)
In e-commerce and financial systems, network timeouts and client retries often result in duplicate order creations and multiple customer charges.
* The client passes an `Idempotency-Key` header with each checkout request.
* `OrderIdempotencyService` registers an atomic reservation in PostgreSQL before processing:
  * If a request with the same key arrives while another is in progress, the service returns `409 Conflict`.
  * If the key was already used with different payload data (calculated via request SHA-256 hash), it rejects the request as a conflict.
  * If the key was already successfully processed, the existing `orderId` and payment link are returned immediately without reprocessing.

### 2. Two-Stage Payment Flow (Hold & Capture)
Rather than executing single-stage immediate capture, the payment flow uses Monobank's authorization hold:
1. **Hold Creation:** `PaymentService` creates a hold invoice via Monobank API and stores a transaction with status `PENDING`.
2. **Customer Authorization:** Customer completes payment on Monobank checkout.
3. **Webhook Verification:** Monobank notifies `PaymentService` via webhook. The payload's cryptographic signature is verified using Monobank's public key (ECDSA). Status updates to `HOLD`.
4. **Order Confirmation:** When the store administrator confirms the order, `PaymentService.capturePayment()` finalizes the hold. If the order is rejected or cancelled, the hold is released.

### 3. Distributed Cart Locking (Redisson)
To prevent race conditions when a user performs rapid concurrent updates to their cart (e.g., across multiple tabs or automated retries), `CartService` applies distributed locks (`RLock`) via Redisson backed by Redis:
```java
RLock lock = redissonClient.getLock("cart:lock:" + cartId);
if (lock.tryLock(2, 5, TimeUnit.SECONDS)) {
    try {
        // Safe cart calculation & persistence
    } finally {
        lock.unlock();
    }
}
```

### 4. Fault Tolerance (Resilience4j)
Calls from `cart-service` to `product-service` are wrapped in a **Resilience4j Circuit Breaker** and **Retry** configuration:
* If `product-service` experiences degradation, the circuit opens after 50% failures, failing fast rather than exhausting thread pools.

### 5. Database Schema Evolution (Flyway)
Databases start from JPA domain models with incremental schema changes and table constraints managed version-by-version using **Flyway**:
* `V1__extend_order_payment_status.sql`
* `V2__create_order_idempotency.sql`
* `V1__extend_payment_transaction_status.sql`

---

## Local Development & Setup

### Prerequisites
* **Java 21**
* **Node.js 18+** & npm
* **PostgreSQL 15+** (running on port `5433` or configure via `DB_URL`)
* **Redis 6+** (running on port `6379`)

### 1. Configure Local Secrets
Local credentials (passwords, API tokens) are decoupled into `application-local.yml` files (which are git-ignored).

Templates are provided in each service:
```bash
# Product Service
cp product-service/src/main/resources/application-local.yml.example \
   product-service/src/main/resources/application-local.yml

# Order Service
cp order-service/src/main/resources/application-local.yml.example \
   order-service/src/main/resources/application-local.yml

# Payment Service
cp payment-service/src/main/resources/application-local.yml.example \
   payment-service/src/main/resources/application-local.yml
```
Fill in your database password and optional API keys (Monobank token, Nova Poshta key, Cloudinary keys).

### 2. Build & Run Tests
Run the entire test suite across all microservices:
```bash
./gradlew test
```

### 3. Run Microservices
Each service can be started via Gradle:
```bash
# Terminal 1: Product Service (port 8080)
./gradlew :product-service:bootRun

# Terminal 2: Cart Service (port 8082)
./gradlew :cart-service:bootRun

# Terminal 3: Order Service (port 8083)
./gradlew :order-service:bootRun

# Terminal 4: Payment Service (port 8084)
./gradlew :payment-service:bootRun
```

### 4. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` to access the storefront and `/admin` for the management dashboard.

---

## Testing Strategy

The repository includes unit test suites targeting critical business logic:
* **`CartServiceTest`**: Tests cart lifecycle, pricing calculations, item mutations, and Redisson lock acquisition.
* **`ProductServiceTest`**: Validates inventory deduction, out-of-stock boundary conditions, and inventory release.
* **`OrderIdempotencyServiceTest`**: Validates deduplication, hash conflict detection, concurrent lock states, and reservation release.
* **`PaymentServiceTest`**: Validates Monobank invoice dispatch, webhook status transitions (hold, failure, cancel), and capture conflicts.
* **Integration Tests**: Spring Boot context tests with H2 in-memory test profiles for isolated pipeline validation.
