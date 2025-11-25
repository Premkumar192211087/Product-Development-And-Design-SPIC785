## Overview

* Use PHP (XAMPP) + MySQL for REST endpoints returning JSON matching your app’s existing field names.

* Android client: Retrofit + OkHttp + JSON (Gson/Moshi), optional Room for caching.

* Authentication via token; session values already present in `UserSession` (userId, storeId, storeName, role, token).

## Connectivity (Dev/Prod)

* Emulator → `http://10.0.2.2/stockpilot/api/`

* Device → `http://<PC-LAN-IP>/stockpilot/api/`

* Production → HTTPS on domain; same JSON contracts.

## Schema Map (Based on Project Fields)

* **users**: `id, username, password_hash, full_name, email, phone, role, store_id, created_at`

* **stores**: `id, store_name`

* **products**: `id, product_name, sku, category_id, selling_price, stock_quantity, barcode, created_at, updated_at`

* **categories**: `id, category_name, store_id`

* **vendors**: `id, vendor_name, contact_person, email, phone, address, city, state, zip_code, country, status, created_at, updated_at`

* **customers**: `customer_id, customer_name, email, phone, address, date_of_birth, status, loyalty_points, store_id`

* **purchase\_orders**: `id, po_number, store_id, vendor_id, vendor_name, po_date, expected_date, status, subtotal, tax, discount, total, notes, created_at, updated_at`

* **po\_items**: `id, po_id, product_id, item_name, item_description, quantity, unit_price, total`

* **bills**: `id, bill_number, purchase_order_id, vendor_id, vendor_name, store_id, bill_date, due_date, status, amount, amount_paid, balance, notes, created_at, updated_at`

* **bill\_items**: `id, bill_id, product_id, quantity, unit_price, total`

* **sales**: `id, store_id, invoice_number, customer_id, payment_method, payment_status, discount, tax, total, notes, created_at`

* **sale\_items**: `id, sale_id, product_id, quantity, unit_price`

* **shipments**: `id, store_id, shipment_number, order_type, order_id, carrier_name, tracking_number, shipping_method, shipping_cost, ship_date, estimated_delivery_date, actual_delivery_date, status, recipient_name, recipient_phone, recipient_address, created_at`

* **shipment\_items**: `id, shipment_id, product_id, quantity_shipped, unit_price, batch_id`

* **inventory\_movements**: `movement_id, store_id, product_id, product_name, category, movement_type, quantity, unit_price, total_value, reference_type, performed_by, timestamp`

* **notifications**: `id, store_id, title, message, type, status, timestamp`

* **user\_settings**: `user_id, notifications_enabled, dark_mode_enabled, low_stock_threshold, expiry_days_threshold, damaged_items_notifications_enabled, expiry_notifications_enabled, low_stock_notifications_enabled`

* Indices on `sku`, foreign keys, and date/status fields to back list filters and reports.

## Endpoint Contracts (Match App Usage)

* **Auth**

  * `POST /auth/login` → `{ username, password }` → `{ success, data: { token, user:{id,role}, store:{id,store_name} }, message }`

* **Products**

  * `GET /stores/{storeId}/products?search=&category=&page=&limit=` → array of `{ id, product_name, sku, category, stock_quantity, selling_price }`

  * `POST /products` → create (uses `product_name, sku, category, price, quantity, barcode`)

  * `PUT /products/{id}` / `DELETE /products/{id}`

* **Categories (ItemGroups)**

  * `GET /stores/{storeId}/categories` → `{ id, category_name }[]`

  * `POST /categories` / `PUT /categories/{id}` / `DELETE /categories/{id}`

* **Vendors**

  * `GET /stores/{storeId}/vendors` / `GET /vendors/{id}` → includes `contact_person, address, zip_code, country, status, created_at`

  * `PUT /vendors/{id}` (EditVendorActivity fields) / `POST /vendors` / `DELETE /vendors/{id}`

* **Customers**

  * `GET /stores/{storeId}/customers?search=&status=` → fields: `customer_id, customer_name, email, phone, address, date_of_birth, status, loyalty_points`

  * `POST /customers` / `PUT /customers/{id}` / `DELETE /customers/{id}`

  * `PUT /customers/{id}/status` → toggle (CustomersActivity)

* **Purchase Orders**

  * `GET /stores/{storeId}/purchase-orders?status=&from=&to=` → `id, po_number, vendor_id, vendor_name, po_date, expected_date, status, subtotal, tax, discount, total, created_at, updated_at`

  * `GET /purchase-orders/{id}` → header + `items: [{ id, item_name, item_description, quantity, unit_price, total }]`

  * `POST /purchase-orders` → from AddPurchaseOrderActivity

  * `PUT /purchase-orders/{id}` → from EditPurchaseOrderActivity

* **Bills**

  * `GET /stores/{storeId}/bills` and `GET /bills/{id}` → `bill_number, bill_date, due_date, amount, amount_paid, balance, status`

  * `POST /bills` (build from PO)

* **Sales**

  * `GET /stores/{storeId}/sales?from=&to=&method=&status=` → summary lists

  * `POST /sales` → AddSaleActivity fields

* **Shipments**

  * `GET /stores/{storeId}/shipments?status=&from=&to=` → `shipment_number, carrier_name, tracking_number, shipping_method, shipping_cost, recipient_*`

  * `POST /shipments` → AddShipmentActivity

* **Inventory Movements/Adjustments**

  * `GET /stores/{storeId}/inventory/movements?type=&from=&to=` → `movement_type, quantity, performed_by, timestamp`

  * `POST /inventory/movements` → AddAdjustmentActivity fields

* **Notifications**

  * `GET /stores/{storeId}/notifications` → `[ { id, store_id, title, message, type, status, timestamp } ]`

  * `PUT /notifications/{id}` (mark read)

* **Reports**

  * `GET /stores/{storeId}/reports/sales?range=&from=&to=` → `{ summary:{ total_sales, total_orders }, payment_distribution, recent_transactions, revenue_trend, revenue_by_category }`

  * `GET /stores/{storeId}/reports/inventory?range=&from=&to=` → `{ summary:{ total_products, low_stock, expired }, stock_movements, low_stock_alerts, categories }`

  * `GET /stores/{storeId}/reports/financial?range=&from=&to=` → `{ financial:{ revenue, cost, profit } }`

* **Response wrapper**: `{ success: boolean, data: <payload>, message?: string }`

## PHP Implementation (XAMPP)

* Use **PDO** with prepared statements; central DB connector; error handler returns `{ success:false, message }`.

* Controllers: one per domain (products.php, vendors.php, customers.php, purchase\_orders.php, bills.php, sales.php, shipments.php, inventory.php, notifications.php, reports.php).

* Map JSON keys exactly as used in app to minimize client changes.

* Add pagination (`page`, `limit`), sorting (`sort_by`, `sort_dir`), and filtering query params in list endpoints.

## Android Client (Refit to Current Code)

* **ApiService**: restore interface signatures reflecting above endpoints and field names.

* **ApiUrls**: base URL configurable (dev/prod), fallback to stored in `Constants`/`SharedPreferences`.

* **AuthInterceptor**: adds `Authorization: Bearer <token>` from `UserSession`.

* **Repositories**: Items/Vendors/Customers/PO/Bills/Sales/Shipments/Inventory/Reports; each maps raw JSON to models already used (e.g., `VendorModel`, `Product`, etc.).

* **Offline Cache (optional)**: Room entities for products/vendors/customers; write-through on fetch; UI observes Room.

* **Workers**: refresh notifications, thresholds (low stock/expiry) using WorkManager.

## Security & Permissions

* No direct DB access from Android; all via REST.

* Token-based auth; validate `store_id` per request to restrict data scope.

* Input validation server-side; all DB access via prepared statements.

* HTTPS in production; CORS for dev if needed.

## Migration Phases

1. Stand up `auth`, `products`, and `categories` endpoints; wire Items/ItemGroups.
2. Vendors/Customers endpoints → wire screens.
3. Purchase Orders and Bills → add header/items flows.
4. Sales and Shipments endpoints → wire AddSale/AddShipment.
5. Inventory Movements (adjustments) + aggregate Reports.
6. Notifications + mark-read.
7. Optional: Room caching and background sync.

## Best-Fit Recommendation

* Given XAMPP, implement **PHP + PDO** REST with the schema above and reintroduce **Retrofit** on Android. This matches your existing field naming and screen behaviors, is fast to ship, and keeps the door open to a Laravel migration later for migrations, auth, and ORM.

## Deliverables

* SQL scripts for tables + indexes + foreign keys.

* PHP endpoints with request/response JSON matching existing keys.

* Android client: `ApiService`, `ApiUrls`, interceptors, repositories, and screen integrations for each domain.

* Postman collection covering all endpoints and sample payloads.

