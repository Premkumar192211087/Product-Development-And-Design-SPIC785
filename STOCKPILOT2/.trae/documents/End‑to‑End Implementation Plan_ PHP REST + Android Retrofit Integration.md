## What I Found

* PHP controllers present under:

  * `app/src/main/php/` (e.g., `db_connection.php`)

  * `app/backeup_setup/phpfiles/` (e.g., `vendors.php`, `inventory_adjustments.php`, `products_get.php`, many more for shipments, customers, items, notifications, reports)

  * SQL dumps under `app/backeup_setup/database/` (e.g., `inventory_management (3).sql`, `user_preferences_table.sql`).

* Schema includes `customers`, `categories`, `batch_details`, `inventory_alerts`, `invoices`, etc., matching the app’s field expectations.

## Approach

1. Normalize endpoints: reuse existing PHP scripts where signatures match; create thin wrappers where names differ to align with REST paths (e.g., `/stores/{storeId}/products`, `/inventory/movements`).
2. Android: extend `ApiService` to call these existing controllers and map payloads/results to the app models.
3. Repositories: for Vendors, Items, Customers, Inventory, Notifications, Reports, wire methods to the corresponding controllers.
4. Activities: replace "Data layer removed" spots with Retrofit calls and UX handling.

## Endpoint Mapping (Initial)

* Products list: `phpfiles/products_get.php?store_id={id}` → `GET /stores/{storeId}/products` (map `price→selling_price`, `quantity→stock_quantity`).

* Inventory movements list: `phpfiles/inventory_adjustments.php?store_id={id}&movement_type=...` → `GET /stores/{storeId}/inventory/movements`.

* Vendors list/distribution: `phpfiles/vendors.php?action=suppliers&store_id={id}&search=&status=&sort=` → `GET /stores/{storeId}/vendors`.

* Notifications: `phpfiles/notification_get.php?store_id={id}` and `markasread.php`, `notificaction_delete.php` → `GET`, `PUT mark-read`, `PUT delete`.

* Customers: `phpfiles/customers.php` and `add_customer.php` → list/create/update/delete/status toggle.

* Shipments: `phpfiles/shipments.php`, `shipment_items.php`, `get_shipment_details.php` → list/details/create.

* Purchase Orders: `phpfiles/get_purchase_orders.php`, `create_purchase_order.php`, `edit_purchase_order.php`, `get_purchase_order_details.php`, `delete_purchase_order.php` → list/details/create/update/delete.

* Bills: `phpfiles/get_bill_details.php`, `update_bill.php` and payments scripts → list/details/create.

* Sales: `phpfiles/add_sale.php`, `API/add_sale.php`, `get_invoices.php` → create/list.

* Reports: `phpfiles/get_sales_reports.php`, `get_inventory_reports.php`, `get_financial_reports.php` → compose charts.

## Android Changes (Per Domain)

* Products: implement repository list fetch, map fields; wire item creation via multipart (`add_item.php`) and update/delete.

* Inventory Movements: AddAdjustmentActivity submit to `inventory_adjustments_create.php` (new) or extend existing script to accept `POST` with `store_id, product_id, movement_type, quantity, unit_price, total_value, notes, performed_by`.

* Vendors: repository list, add/update/delete via `add_vendor.php`, `update_vendor.php`, `delete_vendor.php`.

* Customers: list with search/status filter; toggle status; CRUD via existing scripts.

* Shipments: build header + items payload and post; list by status/date.

* Notifications: fetch, swipe-delete, mark-read.

* Reports: call three controllers in parallel and update UI.

## PHP Adjustments (Minimal)

* Add `POST` handlers or wrappers for REST-style paths if needed; reuse existing SQL.

* Return `{ success, data, message }` consistently; ensure CORS headers; use prepared statements;

* Validate `store_id` and `token`.

## Validation

* Postman: create a collection pointing to your local server (dev: `http://10.0.2.2/stockpilot/api/` from emulator, device via LAN IP).

* Android: build, run flows for Items, Adjustments, Shipments, Vendors, Customers, Notifications, Reports.

## Milestones (One Pass Within Client; server wrappers in parallel)

1. Wire Products, Categories.
2. Wire Vendors, Customers.
3. Wire Purchase Orders, Bills.
4. Wire Sales, Shipments.
5. Wire Inventory Movements, Reports.
6. Wire Notifications.

## Confirmation

* Confirm this plan to proceed with implementation that ties Android directly to your existing PHP controllers, adding small wrappers only where unavoidable.

