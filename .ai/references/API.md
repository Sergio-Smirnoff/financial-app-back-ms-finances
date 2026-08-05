# ms-finances — API

Endpoints and error codes. Envelope shape, exception hierarchy and the DomainError → HTTP
mapping: parent `.ai/references/APP_STRUCTURE.md` — not repeated here.

## Endpoints

| Method | Path | Purpose | Error codes |
|---|---|---|---|
| POST | `/api/v1/finances/transactions` | Record a new account-to-account transaction | `invalid_cbu`, `same_account_transfer`, `invalid_money`, `unsupported_currency`, `category_not_found` |
| PUT | `/api/v1/finances/transactions/{id}` | Update category, description or date of a transaction | `transaction_not_found`, `category_not_found` |
| DELETE | `/api/v1/finances/transactions/{id}` | Delete (reverse) a transaction and emit reversal event | `transaction_not_found` |
| GET | `/api/v1/finances/transactions` | List transactions (user-scoped or internal `?accountCbu=`) | `invalid_cbu` |
| GET | `/api/v1/finances/transactions/summary` | Ranged spending summary by currency (`?from=&to=`) | `invalid_date_range` |
| GET | `/api/v1/finances/categories` | List user categories with nested active subcategories | — |
| GET | `/api/v1/finances/categories/{id}` | Get one category by ID | `category_not_found` |
| POST | `/api/v1/finances/categories` | Create a new category | `category_already_exists`, `invalid_category_name` |
| PUT | `/api/v1/finances/categories/{id}` | Rename an existing category | `category_not_found`, `category_already_exists`, `invalid_category_name` |
| DELETE | `/api/v1/finances/categories/{id}` | Soft-delete (archive) a category | `category_not_found` |
| POST | `/api/v1/finances/categories/{id}/restore` | Restore an archived category | `category_not_found` |
| GET | `/api/v1/finances/categories/{id}/subcategories` | List subcategories for a category | `category_not_found` |
| POST | `/api/v1/finances/categories/{id}/subcategories` | Add a subcategory under a category | `category_not_found`, `subcategory_already_exists`, `invalid_category_name` |
| PUT | `/api/v1/finances/categories/{id}/subcategories/{subId}` | Rename a subcategory | `subcategory_not_found`, `invalid_category_name` |
| DELETE | `/api/v1/finances/categories/{id}/subcategories/{subId}` | Soft-delete a subcategory | `subcategory_not_found` |
| POST | `/api/v1/finances/categories/{id}/subcategories/{subId}/restore` | Restore an archived subcategory | `subcategory_not_found` |

## DomainError catalog

| Slug | HTTP status | When it is thrown |
|---|---|---|
| `transaction_not_found` | 404 | Transaction lookup returned no match for user/id |
| `category_not_found` | 404 | Category lookup returned no match for user/id |
| `subcategory_not_found` | 404 | Subcategory lookup returned no match |
| `category_already_exists` | 409 | Unique category name constraint violated |
| `subcategory_already_exists` | 409 | Unique subcategory name constraint violated under parent |
| `same_account_transfer` | 422 | `fromCbu` and `toCbu` are identical |
| `invalid_money` | 400 | Transaction magnitude is zero or negative |
| `unsupported_currency` | 400 | Currency code not in `SupportedCurrencies` whitelist |
| `invalid_cbu` | 400 | CBU is not 22 digits or fails checksum |
| `invalid_category_name` | 400 | Category name is blank or > 100 characters |
| `invalid_date_range` | 400 | `from` date is after `to` date or partial range supplied |
| `internal_error` | 500 | Unmapped failure |
