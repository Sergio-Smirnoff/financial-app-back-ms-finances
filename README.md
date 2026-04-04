# ms-finances

Finances microservice — transactions (income/expense), hierarchical categories, loans with installment tracking, and card installment expenses.

- **Internal port:** 8082
- **Database schema:** `finances`
- **Swagger UI:** `http://localhost:8082/swagger-ui.html`

---

## Required Environment Variables

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL (e.g. `jdbc:postgresql://postgres:5432/financialapp?currentSchema=finances`) |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address (e.g. `kafka:9092`) |
| `DAYS_BEFORE_PAYMENT_ALERT` | Days ahead to alert on card expenses (default: 3) |
| `DAYS_BEFORE_LOAN_ALERT` | Days ahead to alert on loans (default: 3) |
| `DAYS_BEFORE_INSTALLMENT_ALERT` | Days ahead to alert on loan installments (default: 3) |

Copy `.env.example` to `.env` and fill in the values.

---

## API Endpoints

All responses are wrapped in `ApiResponse<T>`. User identity is taken from the `X-User-Id` header (injected by the API Gateway).

### Categories

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/categories` | Category tree (parents + nested subcategories). Filters: `type`, `isSystem` |
| GET | `/api/v1/categories/flat` | Flat category list with `parentId`. Filters: `type`, `isSystem` |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| GET | `/api/v1/categories/{id}/subcategories` | Subcategories of a parent |
| POST | `/api/v1/categories` | Create user parent category |
| POST | `/api/v1/categories/{id}/subcategories` | Create subcategory under parent |
| PUT | `/api/v1/categories/{id}` | Update user parent category |
| PUT | `/api/v1/subcategories/{id}` | Update subcategory (system or user) |
| DELETE | `/api/v1/categories/{id}` | Soft-delete user parent category |
| DELETE | `/api/v1/subcategories/{id}` | Soft-delete user subcategory |

### Transactions

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/transactions` | Paginated list. Filters: `type`, `categoryId`, `currency`, `dateFrom`, `dateTo` |
| POST | `/api/v1/transactions` | Create transaction (must use a subcategory) |
| GET | `/api/v1/transactions/{id}` | Get transaction by ID |
| PUT | `/api/v1/transactions/{id}` | Update transaction |
| DELETE | `/api/v1/transactions/{id}` | Physical delete |
| GET | `/api/v1/transactions/summary` | Financial summary per currency. Filters: `currency`, `dateFrom`, `dateTo` |

### Loans

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/loans` | List loans. Filters: `active`, `currency` |
| POST | `/api/v1/loans` | Create loan + auto-generate all installments |
| GET | `/api/v1/loans/{id}` | Get loan by ID |
| PUT | `/api/v1/loans/{id}` | Update description and entity only |
| DELETE | `/api/v1/loans/{id}` | Delete loan |
| GET | `/api/v1/loans/{id}/installments` | List all installments |
| PUT | `/api/v1/loans/{id}/installments/{installmentId}/pay` | Mark installment as paid |

### Card Expenses

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/card-expenses` | List card expenses. Filters: `active`, `cardId`, `currency` |
| POST | `/api/v1/card-expenses` | Register card expense |
| GET | `/api/v1/card-expenses/{id}` | Get by ID |
| PUT | `/api/v1/card-expenses/{id}` | Update description and cardId |
| DELETE | `/api/v1/card-expenses/{id}` | Delete card expense |
| POST | `/api/v1/card-expenses/{id}/pay-installment` | Register current installment payment |

---

## Kafka Topics Published

| Topic | Trigger |
|---|---|
| `payment.due` | Daily — card expenses with `next_due_date` within N days |
| `loan.reminder` | Daily — loans with `next_payment_date` within N days |
| `installment.reminder` | Daily — unpaid loan installments with `due_date` within N days |

Schedulers run at 08:00, 08:05, and 08:10 respectively.

---

## Local Development

```bash
# From back/ directory — install parent POM
cd financial-app-parent && mvn install -N -q && cd ..

# Run the service
cd ms-finances
cp .env.example .env
# Edit .env with your local values
mvn spring-boot:run
```

Make sure PostgreSQL is running and the `finances` schema exists (created by `infra/postgres/init/01-create-schemas.sql`).

## Database Migrations

Flyway runs automatically on startup. Migration files are in `src/main/resources/db/migration/`:

- `V1` — categories table
- `V2` — transactions table
- `V3` — loans table
- `V4` — loan_installments table
- `V5` — card_expenses table
- `V6` — default system categories seed data

**Never modify existing migration files.** Always add new versioned migrations.
