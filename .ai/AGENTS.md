# ms-finances

Records every money movement in the user's ledger, transaction categories, and budgets.
Port **8082**, schema **`finances`**. Own git repo — commit finances work here, never from the
parent workspace.

## Package tree

com.financialapp.finances
├── domain            pure — aggregates, VOs, ports, domain services. No Spring.
├── application       use-case implementations, @Transactional lives here
├── web               controllers, DTOs, MapStruct mappers
└── infrastructure    JPA adapters, Kafka, Feign, scheduler

Layer boundaries are enforced by `LayeredArchitectureTest` (ArchUnit) — a violation fails
`mvn verify`, it is not a review opinion.

## Load-bearing facts

- Every transaction is an account-to-account money movement carrying `fromCbu` and `toCbu`.
- `TransactionKind` (`EXPENSE`, `INCOME`, `TRANSFER`) is **never stored** — it is derived at read
  time by checking which CBUs the user owns via ms-banks.
- `Money` is always positive-magnitude (amount > 0). Per-account sign is derived from the CBU pair.
- Cursor paging and automatic transaction classifier live on the current branch. `CursorPage.ofPage`
  adds an opt-in row-offset mode (`page * size`), honoured only when `cursor` is absent — the two
  paging modes are mutually exclusive, never combined.
- Outbound events go through the transactional outbox (`outbox_event`), published by `OutboxRelay`.
  Each movement gets an outbox row ID used as Kafka idempotency key.
- Inbound dedup for bank payment events uses `finances.processed_inbound_event` (V17), mapped
  by `ProcessedInboundEventJpaEntity`.

## Read when

| File | Read when |
|---|---|
| `.ai/references/DOMAIN.md` | changing an aggregate, a value object, an enum or a migration |
| `.ai/references/API.md` | adding or changing an endpoint, a DTO or an error code |
| `.ai/references/EVENTS.md` | touching Kafka, the outbox, a scheduled job or a Feign call |

## Global rules

R1–R18, the four workflow modes, the tech stack, the response envelope and the exception
hierarchy live in the **parent workspace** at `.ai/references/`. They are not duplicated here.
Working in this repo without the parent workspace present means working without the rules —
open `financial-app/` as the project root.

Human onboarding and how to run this service: `README.md`.
