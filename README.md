# Payment Microservice (RetailFlow)

Runs on port **8093**. Calls Invoice and AuditLog via Feign.

## Setup
```sql
CREATE DATABASE paymentdb;
```
Edit `application.properties`: set DB password, `invoice.service.url`, `auditlog.service.url`.

## Start order
Eureka (8761) -> AuditLog (8082) -> Invoice (8092) -> Payment (8093)

## Endpoints
| Method | Path | Purpose |
|--------|------|---------|
| POST   | /api/payments | Process payment (updates invoice status) |
| PUT    | /api/payments/{id} | Update method |
| PATCH  | /api/payments/{id}/refund | Refund (recalculates invoice status) |
| GET    | /api/payments/{id} | Get one |
| GET    | /api/payments | Get all |
| GET    | /api/payments/invoice/{invoiceId} | By invoice |
| GET    | /api/payments/paginated?page=0&size=5 | Paginated |

## Test flow
1. Create an invoice in Invoice service (or via a Sale).
2. POST http://localhost:8093/api/payments
   { "invoiceId": 1, "amount": 175.0, "method": "CARD" }
3. Check invoice status changed to PAID/PARTIALLY_PAID.
4. PATCH /api/payments/1/refund -> invoice goes back to PENDING/PARTIALLY_PAID.
