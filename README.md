# ms-finances

Finances microservice — account-to-account transactions, hierarchical categories, and legacy installment tracking (loans, card expenses). Port 8082, PostgreSQL schema `finances`.

**Swagger UI:** `http://localhost:8082/swagger-ui.html`

---

## Tech Stack

Java 21, Spring Boot 3.4.2, Spring Data JPA, Flyway, MapStruct, Lombok, Apache Kafka (Outbox pattern), OpenFeign.

---

## Account-to-account transaction model

Every transaction is a money movement from one CBU to another. `TransactionKind` (EXPENSE / INCOME / TRANSFER) is **never stored** — it is derived at read time by checking which CBUs the user owns via ms-banks.

```
expense  = owns(fromCbu) && !owns(toCbu)
income   = !owns(fromCbu) && owns(toCbu)
transfer = owns(fromCbu) && owns(toCbu)
```

`Money` is always a positive magnitude (amount > 0, scale 2 HALF_EVEN). Direction is derived via `Transaction.signedFor(Cbu)`, never encoded in the amount.

`Cbu` is a 22-digit value object. `Cbu.EXTERNAL_INSTALLMENT_CBU` (`"0000000000000000000000"`) is the sentinel for bank-originated installment payments.

Balance adjustments are written to the `outbox_event` table in the same DB transaction as the `Transaction` row. `OutboxRelay` polls every 2 s and publishes to Kafka. The outbox row id is the idempotency key — a transfer produces two distinct keys, one per direction.

`categoryName` is resolved server-side in the application layer and injected into `UserTransactionView` before mapping to `TransactionResponse`.

---

## Folder tree

```
src/main/java/com/financialapp/finances/
│
├── FinancesApplication.java
│
├── web/                                   HTTP boundary
│   ├── controller/
│   │   ├── TransactionController.java
│   │   └── CategoryController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── RecordTransactionRequest.java
│   │   │   ├── UpdateTransactionRequest.java
│   │   │   ├── CreateCategoryRequest.java
│   │   │   ├── CreateSubcategoryRequest.java
│   │   │   ├── UpdateCategoryRequest.java
│   │   │   └── RenameSubcategoryRequest.java
│   │   └── response/
│   │       ├── TransactionResponse.java
│   │       ├── AccountTransactionResponse.java
│   │       ├── CategoryResponse.java
│   │       ├── SubcategoryResponse.java
│   │       └── CurrencySummaryResponse.java
│   ├── mapper/
│   │   ├── TransactionWebMapper.java
│   │   └── CategoryWebMapper.java
│   └── error/
│       └── GlobalExceptionHandler.java
│
├── application/                           Use-case implementations
│   ├── transaction/impl/
│   │   ├── RecordTransactionUseCaseImpl.java
│   │   ├── UpdateTransactionUseCaseImpl.java
│   │   ├── DeleteTransactionUseCaseImpl.java
│   │   ├── ListUserTransactionsUseCaseImpl.java
│   │   ├── ListAccountTransactionsUseCaseImpl.java
│   │   └── GetTransactionSummaryUseCaseImpl.java
│   └── category/impl/
│       ├── CreateCategoryUseCaseImpl.java
│       ├── CreateSubcategoryUseCaseImpl.java
│       ├── UpdateCategoryUseCaseImpl.java
│       ├── ArchiveCategoryUseCaseImpl.java
│       ├── ArchiveSubcategoryUseCaseImpl.java
│       ├── RestoreCategoryUseCaseImpl.java
│       ├── RestoreSubcategoryUseCaseImpl.java
│       ├── RenameSubcategoryUseCaseImpl.java
│       ├── GetCategoryUseCaseImpl.java
│       ├── ListCategoriesUseCaseImpl.java
│       └── ListSubcategoriesUseCaseImpl.java
│
├── domain/                                Pure domain — zero framework imports
│   ├── common/model/
│   │   ├── Money.java                     VO: positive magnitude + Currency
│   │   ├── Cbu.java                       VO: 22-digit Argentine CBU
│   │   ├── DateRange.java                 VO: [from, to] with from<=to invariant
│   │   ├── TransactionId.java
│   │   ├── CategoryId.java
│   │   ├── UserId.java
│   │   └── CategoryStatus.java
│   ├── model/
│   │   ├── transaction/
│   │   │   ├── Transaction.java           Aggregate root (fromCbu/toCbu/money)
│   │   │   ├── TransactionKind.java       EXPENSE | INCOME | TRANSFER (derived)
│   │   │   ├── ClassifiedTransaction.java (Transaction, TransactionKind)
│   │   │   ├── BalanceMovement.java       Signed adjustment per owned account
│   │   │   ├── TransactionSummary.java
│   │   │   └── UserTransactionView.java   Resolved categoryName carried through use-case
│   │   └── category/
│   │       ├── Category.java              Aggregate root; owns subcategories list
│   │       ├── Subcategory.java
│   │       └── CategoryName.java          VO: trimmed, non-blank, ≤ 100 chars
│   ├── event/
│   │   ├── DomainEvent.java
│   │   ├── TransactionCreated.java
│   │   └── TransactionReversed.java
│   ├── service/
│   │   ├── TransactionClassifier.java     Derives EXPENSE/INCOME/TRANSFER from owned set
│   │   ├── TransactionPosting.java        Computes BalanceMovements for outbox
│   │   └── TransactionCurrencyValidator.java
│   ├── gateway/
│   │   ├── AccountOwnershipGateway.java   Port: owned CBUs for a user
│   │   ├── DomainEventPublisher.java      Port: write domain events to outbox
│   │   └── SupportedCurrencies.java       Port: currency whitelist
│   ├── repository/
│   │   ├── TransactionRepository.java
│   │   └── CategoryRepository.java
│   ├── usecase/
│   │   ├── transaction/                   Interfaces + command records
│   │   └── category/                      Interfaces + command records
│   └── exception/
│       ├── DomainException.java
│       ├── DomainErrorCode.java
│       ├── ErrorCategory.java
│       ├── InvalidCbuException.java
│       ├── InvalidMoneyException.java
│       ├── CurrencyMismatchException.java
│       ├── UnsupportedCurrencyException.java
│       └── transaction/ + category/       Domain-specific exceptions
│
└── infrastructure/                        Spring + JPA + Kafka adapters
    ├── config/
    │   ├── InternalAuthFilter.java        Validates X-Internal-Token header
    │   ├── FeignConfig.java
    │   ├── MessagingConfig.java
    │   ├── SupportedCurrenciesImpl.java
    │   └── DomainServiceConfig.java
    ├── persistence/
    │   ├── entity/
    │   │   ├── TransactionJpaEntity.java
    │   │   ├── CategoryJpaEntity.java
    │   │   ├── OutboxEventJpaEntity.java
    │   │   └── ProcessedInboundEventJpaEntity.java
    │   ├── jpa/                           Spring Data JPA repositories
    │   ├── mapper/
    │   │   ├── TransactionPersistenceMapper.java
    │   │   └── CategoryPersistenceMapper.java
    │   └── repository/
    │       ├── TransactionRepositoryImpl.java
    │       ├── CategoryRepositoryImpl.java
    │       └── SystemCategoryResolver.java
    ├── messaging/
    │   ├── OutboxDomainEventPublisher.java  Implements DomainEventPublisher port
    │   ├── mapper/TransactionEventMapper.java
    │   ├── payload/
    │   │   ├── TransactionCreatedEvent.java  Wire record for ms-banks
    │   │   └── PaymentEvent.java             Inbound from ms-banks
    │   └── listener/
    │       └── PaymentEventListener.java     Consumes payment-events topic
    ├── gateway/
    │   └── Impl/BankAccountOwnershipGateway.java  Feign → ms-banks /accounts
    └── scheduler/
        └── OutboxRelay.java               Polls outbox; publishes to Kafka
```

---

## Endpoints

All responses use the shared envelope `{ status, title, code, message, data }` from `commons-core`
(`com.financialapp.commons.core.response.ApiResponse`). `status`/`title` mirror the HTTP status;
`code` appears only on errors with the `DomainErrorCode` slug; error details travel in `data`.
Errors are rendered by `GlobalExceptionHandler extends ApiExceptionHandler` (commons-web) and every
endpoint declares its throwable codes with `@ApiErrorCodes` (generated Swagger examples).
User identity arrives via the `X-User-Id` header injected by the gateway.

### TransactionController — `/api/v1/finances/transactions`

| Method | Path | Request | Response |
|---|---|---|---|
| POST | `/` | `X-User-Id` + `RecordTransactionRequest` | `ApiResponse<TransactionResponse>` 201 |
| PUT | `/{id}` | `X-User-Id` + `UpdateTransactionRequest` | `ApiResponse<TransactionResponse>` |
| DELETE | `/{id}` | `X-User-Id` | `ApiResponse<Void>` 200 |
| GET | `/` | `X-User-Id` **or** `?accountCbu=` | with cursor/paging/filters -> `ApiResponse<PageResultResponse<TransactionResponse>>`; with `accountCbu` -> `ApiResponse<List<AccountTransactionResponse>>` |
| GET | `/{id}` | `X-User-Id` | `ApiResponse<TransactionResponse>` (includes paymentMethod, note) |
| GET | `/uncategorised/count` | `X-User-Id` | `ApiResponse<UncategorisedCountResponse>` |
| GET | `/summary` | `X-User-Id` + optional `?from=&to=` (ISO date) | `ApiResponse<Map<String, CurrencySummaryResponse>>` |
| GET | `/summary/monthly` | `X-User-Id` + `?from=&to=` (ISO date) | `ApiResponse<List<MonthlyFlowResponse>>` |
| GET | `/search` | `X-User-Id` + `?q=&limit=` | `ApiResponse<List<TransactionSearchResponse>>` |

### BudgetController — `/api/v1/finances/budgets`

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/` | `X-User-Id` + `?year=&month=` | `ApiResponse<List<BudgetResponse>>` |
| PUT | `/{categoryId}` | `X-User-Id` + `UpsertBudgetRequest` | `ApiResponse<BudgetResponse>` |
| GET | `/pace` | `X-User-Id` + `?year=&month=` | `ApiResponse<List<BudgetPaceResponse>>` |

### CategorizationRuleController — `/api/v1/finances/categorization-rules`

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/` | `X-User-Id` | `ApiResponse<List<CategorizationRuleResponse>>` |
| POST | `/` | `X-User-Id` + `CreateCategorizationRuleRequest` | `ApiResponse<CategorizationRuleResponse>` 201 |
| POST | `/{id}/preview` | `X-User-Id` | `ApiResponse<RulePreviewResponse>` |
| DELETE | `/{id}` | `X-User-Id` | `ApiResponse<Void>` |
| POST | `/suggest` | `SuggestCategoriesRequest` | `ApiResponse<List<CategorySuggestionResponse>>` |

### CategoryController — `/api/v1/finances/categories`

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/` | `X-User-Id` | `ApiResponse<List<CategoryResponse>>` (nested active subcategories) |
| GET | `/spend` | `X-User-Id` + optional `?from=&to=&kind=` | `ApiResponse<List<CategorySpendResponse>>` |
| GET | `/{id}` | `X-User-Id` | `ApiResponse<CategoryResponse>` |
| POST | `/` | `X-User-Id` + `CreateCategoryRequest` | `ApiResponse<CategoryResponse>` 201 |
| PUT | `/{id}` | `X-User-Id` + `UpdateCategoryRequest` | `ApiResponse<CategoryResponse>` |
| DELETE | `/{id}` | `X-User-Id` | `ApiResponse<Void>` — soft-delete (archive) |
| POST | `/{id}/restore` | `X-User-Id` | `ApiResponse<CategoryResponse>` |
| GET | `/{id}/subcategories` | `X-User-Id` | `ApiResponse<List<SubcategoryResponse>>` |
| POST | `/{id}/subcategories` | `X-User-Id` + `CreateSubcategoryRequest` | `ApiResponse<SubcategoryResponse>` 201 |
| DELETE | `/{id}/subcategories/{subId}` | `X-User-Id` | `ApiResponse<Void>` — soft-delete |
| PUT | `/{id}/subcategories/{subId}` | `X-User-Id` + `RenameSubcategoryRequest` | `ApiResponse<SubcategoryResponse>` |
| POST | `/{id}/subcategories/{subId}/restore` | `X-User-Id` | `ApiResponse<SubcategoryResponse>` |

---

## Kafka events

### Published (finances → ms-banks)

| Event | Topic | Trigger | Payload |
|---|---|---|---|
| `TransactionCreated` | `finances.transaction.created` | New transaction recorded | outbox row id, accountCbu, signed amount, currency |
| `TransactionReversed` | `finances.transaction.created` | Transaction deleted | same shape; negated amount |

### Consumed (ms-banks → finances)

| Topic | Listener | Action |
|---|---|---|
| `payment-events` | `PaymentEventListener` | Records a ledger `Transaction` for bank-originated installment payments; dedup via SHA-256; external side uses `Cbu.EXTERNAL_INSTALLMENT_CBU` |

---

## Run

```bash
# Recommended: infra + service via dev script (from workspace root)
./scripts/dev.sh local service-finances

# Maven directly (from this directory)
mvn spring-boot:run

# Docker (all services)
./scripts/dev.sh up
```

Swagger UI: `http://localhost:8082/swagger-ui.html`

---

## Required environment variables

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL — e.g. `jdbc:postgresql://postgres:5432/financialapp?currentSchema=finances` |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker — e.g. `kafka:9092` |
| `INTERNAL_AUTH_TOKEN` | Shared secret for `X-Internal-Token` header (service-to-service calls) |

Copy `.env.example` (workspace root) to `.env` in this directory and fill in the values.

---

## Flyway migrations

Current head: **V25**. Never modify existing migration files; always add a new versioned file.

| Version | Description |
|---|---|
| V1 | create categories |
| V2 | create transactions |
| V3 | create loans |
| V4 | create loan_installments |
| V5 | create card_expenses |
| V6 | insert default categories |
| V7 | create card_expense_installments |
| V8 | add performance indexes |
| V9 | drop card_expenses |
| V10 | drop loans |
| V11 | add account and transfer to transactions |
| V12 | wipe transactions data |
| V13 | add unassigned categories |
| V14 | add unassigned subcategories |
| V15 | restructure transactions account-to-account |
| V16 | create outbox_event |
| V17 | create processed_inbound_event |
| V18 | collapse unassigned and drop category presentation columns |
| V19 | drop transaction category FK |
| V20 | insert investments system category |
| V21 | align outbox event payload |
| V22 | create budgets |
| V23 | create categorization rules |
| V24 | add payment method and note to transactions |
| V25 | add transaction cursor index |

---

> Full design: `docs/specs/services/ms-finances.md` (parent workspace).

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PRs; push to develop/master | tests + docker build via shared `backend-ci.yml` |
| `docker-publish.yml` | push to master; `v*` tags | GHCR publish: `latest`, `sha-*`, semver on tags |
| `release.yml` | manual (bump dropdown) | next `vX.Y.Z` tag + Release + versioned publish |

Reusable workflows live in the root repo `Sergio-Smirnoff/financial-app`.
