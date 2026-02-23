# financial-app-finances

Finances microservice — income, expenses, loans, card expenses, and categories.

## Port: 8082

## Database Schema: `finances`

## Endpoints
```
GET    /api/v1/finances/summary
GET    /api/v1/finances/transactions
POST   /api/v1/finances/transactions
PUT    /api/v1/finances/transactions/{id}
DELETE /api/v1/finances/transactions/{id}
GET    /api/v1/finances/loans
POST   /api/v1/finances/loans
PUT    /api/v1/finances/loans/{id}
GET    /api/v1/finances/card-expenses
POST   /api/v1/finances/card-expenses
GET    /api/v1/finances/categories
POST   /api/v1/finances/categories
```

## Kafka — Publishes
- `payment.due`
- `loan.reminder`
- `installment.reminder`

## Environment Variables
See `.env.example`.

## Local Development

```bash
cd ../financial-app-parent && mvn install -N
cd ../financial-app-finances
cp .env.example .env
mvn spring-boot:run
```

## Swagger
`http://localhost:8082/swagger-ui.html`
