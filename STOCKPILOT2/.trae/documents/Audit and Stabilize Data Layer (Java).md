## Scope
- Remove all Java-side server/database connections: Retrofit, OkHttp, Volley, and any direct API calls.
- Keep local app state and preferences (`UserSession`, `Settings`) intact; only sever remote I/O.
- Already-stubbed screens (e.g., `AddAdjustmentActivity`) remain non-network.

## Targets (Java files)
- Retrofit usage (`import retrofit2`): 41 files including `PaymentService.java`, `ReportsActivity.java`, `AdditemActivity.java`, `BillsActivity.java`, `CustomersActivity.java`, `EditProfileActivity.java`, `EditVendorActivity.java`, `AddVendorActivity.java`, `AdminHomeActivity.java`, `AddBatchActivity.java`, `AddPurchaseOrderActivity.java`, `PurchaseOrdersActivity.java`, `PurchaseReportsActivity.java`, `ProductSelectionActivity.java`, `DialogSelectLayout.java`, `NotificationsActivity.java`, `ChangePasswordActivity.java`, `DeleteStaffActivity.java`, `ShipmentActivity.java`, `ShipmentDetailActivity.java`, `AddSaleActivity.java`, `InvoicesActivity.java`, `BatchListActivity.java`, `ThresholdCheckWorker.java`, `LoginActivity.java`, `BillDetailsActivity.java`, `NotificationWorker.java`, `StockPilotFirebaseMessagingService.java`, `PackagesActivity.java`, `ItemGroupsActivity.java`, `InventoryAdjustmentsActivity.java`, `ProfileActivity.java`, `SalesOrdersActivity.java`, `AddStaffActivity.java`, `EditPurchaseOrderActivity.java`, `VendorRepository.java`, `ItemRepository.java`, etc. (full list identified by search).
- OkHttp usage: `SalesReportsActivity.java` (83), `InventoryReportsActivity.java` (71), `PaymentsMadeActivity.java` (59), plus imports in other files.
- Volley usage: `AddPurchaseOrderActivity.java` imports `com.android.volley.*` (25–26).
- Central API access points using `ApiUrls.getApiService()`:
  - `PaymentService.java`: 36
  - `ReportsActivity.java`: 189
  - `MarkDeliveredHelper.java`: 37, 53
  - `NotificationWorker.java`: 44
  - `StockPilotFirebaseMessagingService.java`: 227
  - UI activities calling it: `AdditemActivity.java`: 47; `AddCustomerActivity.java`: 32; `BillsActivity.java`: 89; `DialogSelectLayout.java`: 53; `DeleteStaffActivity.java`: 85; `ChangePasswordActivity.java`: 50; `PurchaseReportsActivity.java`: 91; `EditProfileActivity.java`: 60; `EditVendorActivity.java`: 70; `CustomersActivity.java`: 412, 563; `BillDetailsActivity.java`: 40; `AddPurchaseOrderActivity.java`: 103; `AddStaffActivity.java`: 63; `EditPurchaseOrderActivity.java`: 107; `NotificationsActivity.java`: 51.

## Changes Per Category
- Remove API client initialization and calls
  - Delete `ApiUrls.getApiService()` and all `apiService.*` endpoint invocations in files listed above; replace with safe no-op and UI feedback like existing "Data layer removed" patterns.
  - Example edits:
    - `PaymentService.java`: remove calls in `getPayments` (39–85), `createPayment` (102–126), `updatePayment` (146–170), `deletePayment` (176–206); return success/failure via local stubs.
    - `ReportsActivity.java`: remove `getReports(params)` call (190–221); show empty dataset.
    - `MarkDeliveredHelper.java`: remove `markShipmentDelivered(...)` (53) and callback handling (41–77); return immediate success.
    - `NotificationWorker.java`: remove network check (53–66); short-circuit to `Result.success()`.
    - `StockPilotFirebaseMessagingService.java`: remove token registration (237–251).
- Remove OkHttp-based requests
  - `SalesReportsActivity.java`: delete client and `.post` request build (83, 158–168, 164 endpoint); show placeholder data or empty.
  - `InventoryReportsActivity.java`: delete client and `.post` request (71, 141–154, 150 endpoint); show placeholder/empty.
  - `PaymentsMadeActivity.java`: remove `OkHttpClient.Builder` (59–63) if no longer needed.
- Remove Volley
  - `AddPurchaseOrderActivity.java`: remove `JsonObjectRequest` and any Volley request usage; keep Retrofit removal same as above.
- Repositories
  - `VendorRepository.java`: keep stubbed methods; ensure no lingering imports; methods currently show "Data layer removed" (31–103).
  - `ItemRepository.java`: keep stubbed methods; ensure no lingering imports (38–70).

## Persistence
- Keep `UserSession.java` and `Settings.java` as local-only storage; they do not create remote connections.
- No Room/SQLite found in Java; nothing to delete for local DB.

## Build & Dependencies
- After code sweep, remove network libraries from `app/build.gradle`:
  - Retrofit: `com.squareup.retrofit2:*`
  - OkHttp: `com.squareup.okhttp3:*`
  - Volley: `com.android.volley:volley`
  - Gson converters if only used for network payloads.

## Verification
- Build the app; fix imports and unused code.
- Run critical screens to ensure no crashes when actions previously triggered network.
- Confirm no outbound network by searching for `retrofit2`, `okhttp3`, `ApiUrls.getApiService`, `JsonObjectRequest` all returning zero.

## Deliverables
- One change set touching all identified Java files in a single pass.
- A summary list of lines removed per file and brief note of stub behavior.
- Optional: second pass to remove corresponding Kotlin `ApiService`/`ApiUrls` if present.

## Notes
- `AddAdjustmentActivity.java` is already non-network and shows a "Data layer removed" toast upon save (save flow at 206–235). We will keep this behavior.
- We will not remove local SharedPreferences (`UserSession`, `Settings`) unless requested.