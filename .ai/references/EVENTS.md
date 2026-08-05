# ms-finances — messaging and jobs

CloudEvents 1.0, Kafka binary mode, via `commons-messaging`. Topic name = `ce_type`. Outbox,
`OutboxRelay` and DLT conventions: parent `.ai/references/ARCHITECTURE.md` — not repeated here.

## Published

| ce_type / topic | when emitted | payload fields |
|---|---|---|
| `finances.transaction.created` | New transaction recorded or transaction deleted (reversal) — records cash leg adjustments | transactionId (outbox row id), accountCbu, amount (signed), currency |
| `finances.budget.threshold_reached` | Daily `BudgetAlertScheduler` detects spend >= `alertThresholdPct` | budgetId, userId, categoryId, pctUsed, alertThresholdPct, year, month |

Both creation and reversal share `TransactionCreatedEvent` wire payload; reversals carry negated amount.

## Consumed

| ce_type | handler | idempotency key | DLT behaviour |
|---|---|---|---|
| `payment-events` | `PaymentEventListener.handlePaymentEvent` — records ledger transaction for bank installment payment | SHA-256 hash of event fields, via `processed_inbound_event` (`ProcessedInboundEventJpaEntity`) | retries, then lands on `payment-events.DLT` |

## Scheduled jobs

| Job | Cron | What it does |
|---|---|---|
| `OutboxRelay.publishOutboxEvents` | `*/2 * * * * *` (every 2s) | Polls `outbox_event` for `sent=false` rows and publishes to Kafka |
| `BudgetAlertScheduler.evaluateBudgetAlerts` | `0 0 8 * * *` (daily 08:00 AM) | Evaluates active budget spend against threshold and emits `finances.budget.threshold_reached` |

## Outbound calls

| Target service | Endpoint | Why |
|---|---|---|
| ms-banks | `GET /api/v1/banks/accounts` (`BankAccountOwnershipGateway`) | Retrieves user's owned account CBUs to classify transaction kind |
| ms-investments | `GET /api/v1/investments/fx/rates/at` (`FxRateGateway`) | Captures immutable historical FX rate snapshot on transaction creation |
