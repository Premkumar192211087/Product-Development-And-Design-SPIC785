## Affected Files (by reference)
- Imports of `ApiService`:
  - `app/src/main/java/com/example/stockpilot/AddBatchActivity.java`
  - `app/src/main/java/com/example/stockpilot/AddVendorActivity.java`
  - `app/src/main/java/com/example/stockpilot/ChangePasswordActivity.java`
  - `app/src/main/java/com/example/stockpilot/DeleteStaffActivity.java`
  - `app/src/main/java/com/example/stockpilot/DialogSelectLayout.java`
  - `app/src/main/java/com/example/stockpilot/EditVendorActivity.java`
  - `app/src/main/java/com/example/stockpilot/EditProfileActivity.java`
  - `app/src/main/java/com/example/stockpilot/AddStaffActivity.java`
  - `app/src/main/java/com/example/stockpilot/PurchaseReportsActivity.java`
  - `app/src/main/java/com/example/stockpilot/ReportsActivity.java`
  - `app/src/main/java/com/example/stockpilot/EditPurchaseOrderActivity.java`
  - `app/src/main/java/com/example/stockpilot/PurchaseOrdersActivity.java`
- Imports of `ApiUrls`:
  - `app/src/main/java/com/example/stockpilot/AddVendorActivity.java`
  - `app/src/main/java/com/example/stockpilot/ChangePasswordActivity.java`
  - `app/src/main/java/com/example/stockpilot/DeleteStaffActivity.java`
  - `app/src/main/java/com/example/stockpilot/DialogSelectLayout.java`
  - `app/src/main/java/com/example/stockpilot/EditVendorActivity.java`
  - `app/src/main/java/com/example/stockpilot/EditProfileActivity.java`
  - `app/src/main/java/com/example/stockpilot/AddStaffActivity.java`
  - `app/src/main/java/com/example/stockpilot/PurchaseReportsActivity.java`
  - `app/src/main/java/com/example/stockpilot/ReportsActivity.java`
  - `app/src/main/java/com/example/stockpilot/EditPurchaseOrderActivity.java`
  - `app/src/main/java/com/example/stockpilot/PurchaseOrdersActivity.java`
  - `app/src/main/java/com/example/stockpilot/MyApplication.java:13`
  - `app/src/main/java/com/example/stockpilot/FinancialReportsActivity.java:41, 141`
- Calls to `ApiUrls.getApiService()`:
  - `app/src/main/java/com/example/stockpilot/CustomersActivity.java:91, 471, 622`
  - `app/src/main/java/com/example/stockpilot/AddBatchActivity.java:139`
  - `app/src/main/java/com/example/stockpilot/AdminHomeActivity.java:70`
  - `app/src/main/java/com/example/stockpilot/BillsActivity.java:89`
  - `app/src/main/java/com/example/stockpilot/BillDetailsActivity.java:40`
  - `app/src/main/java/com/example/stockpilot/AddCustomerActivity.java:32`
  - `app/src/main/java/com/example/stockpilot/AdditemActivity.java:47`
  - `app/src/main/java/com/example/stockpilot/AddVendorActivity.java:69`
  - `app/src/main/java/com/example/stockpilot/ChangePasswordActivity.java:51`
  - `app/src/main/java/com/example/stockpilot/DeleteStaffActivity.java:86`
  - `app/src/main/java/com/example/stockpilot/DialogSelectLayout.java:54`
  - `app/src/main/java/com/example/stockpilot/NotificationsActivity.java:51`
  - `app/src/main/java/com/example/stockpilot/NotificationWorker.java:44`
  - `app/src/main/java/com/example/stockpilot/PaymentService.java:36`
  - `app/src/main/java/com/example/stockpilot/StockPilotFirebaseMessagingService.java:227`
  - `app/src/main/java/com/example/stockpilot/AddPurchaseOrderActivity.java:103`
  - `app/src/main/java/com/example/stockpilot/EditVendorActivity.java:71`
  - `app/src/main/java/com/example/stockpilot/EditProfileActivity.java:61`
  - `app/src/main/java/com/example/stockpilot/AddStaffActivity.java:64`
  - `app/src/main/java/com/example/stockpilot/PurchaseReportsActivity.java:92`
  - `app/src/main/java/com/example/stockpilot/ReportsActivity.java:190`
  - `app/src/main/java/com/example/stockpilot/EditPurchaseOrderActivity.java:108`
  - `app/src/main/java/com/example/stockpilot/MarkDeliveredHelper.java:37`
  - `app/src/main/java/com/example/stockpilot/PurchaseOrdersActivity.java:87`
- Additional references to `ApiUrls`:
  - `app/src/main/java/com/example/stockpilot/MyApplication.java:13` (initialization)
  - `app/src/main/java/com/example/stockpilot/FinancialReportsActivity.java:41` (`getHttpClient()`), `:141` (`GET_FINANCIAL_REPORTS`)

## What I Will Remove Per File
- Remove imports: `import com.example.stockpilot.ApiService;` and `import com.example.stockpilot.ApiUrls;`
- Remove fields: `private ApiService apiService;` and any `ApiUrls`-related fields/constants.
- Remove initializations: `apiService = ApiUrls.getApiService();` and any calls to `ApiUrls.initialize(...)`, `ApiUrls.getHttpClient()`, or `ApiUrls.*` endpoint constants.
- Remove network calls using `apiService` (enqueue/execute blocks) and replace with safe UI no-ops:
  - Clear adapters/lists (`list.clear(); adapter.notifyDataSetChanged();`)
  - Set counters/totals to defaults (`"0"`, `"₹0.00"`)
  - Show lightweight messages like `"Data layer removed"` where appropriate.
- Decouple services/workers:
  - `NotificationWorker`, `PaymentService`, `StockPilotFirebaseMessagingService`, `ThresholdCheckWorker`: remove `ApiService` usage and return early or log.
  - `NetworkDiscoveryService`: keep broadcasting results but remove any `ApiUrls` interaction.
  - `MyApplication`: remove `ApiUrls.initialize(this);`.
- Remove `ApiUrls` usage in `FinancialReportsActivity` (OkHttp client and URL constants) and stub out data load.

## Batch Order Of Changes
1. Core app bootstrap and services
   - `MyApplication.java:13` — remove `ApiUrls.initialize(this)`
   - `services/NetworkDiscoveryService.java` — ensure no `ApiUrls` calls
   - `NotificationWorker.java`, `PaymentService.java`, `StockPilotFirebaseMessagingService.java`, `ThresholdCheckWorker.java` — strip `ApiService` usage
2. Screen activities using `ApiService`/`ApiUrls`
   - Customers, Vendors, Sales Orders, Shipments, Batches, Invoices, Bills, Reports, Profiles, Staff, Purchases
   - Remove imports/fields/initializations, stub network handlers
3. Utilities and helpers
   - `MarkDeliveredHelper.java`, `DialogSelectLayout.java` — remove `ApiService` usage and network blocks
4. Remove `FinancialReportsActivity` dependencies on `ApiUrls` and stub data load

## Deliverable
- A compiling project with all data-layer references removed across the listed files. All screens will render with empty lists and default values, without attempting any HTTP/database calls.

## Notes
- I will preserve UI structures and user flows; only network/data calls and their related fields/imports are removed.
- If you prefer certain screens to show a specific placeholder message instead of empty lists, I can add those during the sweep.

## Confirmation
- Confirm to proceed. I will perform these removals in one pass, across all files above, and verify the project compiles successfully.