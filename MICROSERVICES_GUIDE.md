# RetailFlow Microservices — Sale, Invoice, Payment

## Port map
| Service | Port | DB |
|---------|------|-----|
| Eureka Server | 8761 | - |
| User | 8081 | userdb |
| AuditLog | 8082 | auditlogdb |
| Product | (your port) | productdb |
| Catalog | (your port) | catalogdb |
| **Sale** | 8091 | saledb |
| **Invoice** | 8092 | invoicedb |
| **Payment** | 8093 | paymentdb |

## One-time setup
```sql
CREATE DATABASE saledb;
CREATE DATABASE invoicedb;
CREATE DATABASE paymentdb;
```
In each service's application.properties set the DB password and fix the
service URLs (product/catalog/auditlog/invoice) to match real ports.

## Start order (important!)
1. Eureka Server
2. AuditLog, Product, Catalog (teammates')
3. Invoice (8092)   <- start before Sale, since Sale calls it for COMPLETED sales
4. Sale (8091)
5. Payment (8093)

## End-to-end happy path
1. Make sure a Product (id 1) is ACTIVE and has an ACTIVE Catalog covering today.
2. Sale: POST /api/sales { productId:1, customerId:101, quantity:2, status:"COMPLETED" }
   -> creates sale + auto-creates invoice. Note the invoiceId in response.
3. Invoice: GET /api/invoices/{invoiceId} -> status PENDING.
4. Payment: POST /api/payments { invoiceId:X, amount:<full amount>, method:"CARD" }
   -> payment SUCCESS, invoice becomes PAID.
5. Payment: PATCH /api/payments/{id}/refund -> invoice back to PENDING.

## How they call each other (Feign)
- Sale -> Product   GET /api/products/{id}
- Sale -> Catalog   GET /api/catalogs/product/{productId}
- Sale -> Invoice   POST /api/invoices, GET+DELETE /api/invoices/sale/{saleId}
- Sale -> AuditLog  POST /api/audit-logs
- Payment -> Invoice GET /api/invoices/{id}, PATCH /api/invoices/{id}/status
- Payment -> AuditLog POST /api/audit-logs
- Invoice -> AuditLog POST /api/audit-logs

## Notes
- Boot 3.4.5 + Spring Cloud 2024.0.1. If teammates use Boot 4.0.6, tell me to switch.
- Audit logging is best-effort: if AuditLog is down, the main operation still succeeds.
