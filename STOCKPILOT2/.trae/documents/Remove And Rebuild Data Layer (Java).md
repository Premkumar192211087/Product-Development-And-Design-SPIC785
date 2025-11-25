## What Will Be Removed
- No direct database connections exist in Java; all data access goes through HTTP to a PHP/MySQL backend.
- Targets to delete and recreate from scratch:
  - `ApiUrls.java` (Retrofit builder, base URL, endpoint constants) — c:\Users\user\AndroidStudioProjects\STOCKPILOT2\app\src\main\java\com\example\stockpilot\ApiUrls.java:179–199, 214–309
  - `ApiService.java` (Retrofit interface for all endpoints) — c:\Users\user\AndroidStudioProjects\STOCKPILOT2\app\src\main\java\com\example\stockpilot\ApiService.java:30–392
  - Direct OkHttp login flow in `LoginActivity.java` — c:\Users\user\AndroidStudioProjects\STOCKPILOT2\app\src\main\java\com\example\stockpilot\LoginActivity.java:111–117, 162–210
  - Trust-all SSL client — c:\Users\user\AndroidStudioProjects\STOCKPILOT2\app\src\main\java\com\example\stockpilot\SSLTrustAll.java:17–55
  - All usages of `ApiUrls.getApiService()` across Activities/Workers/Repositories (examples):
    - `CustomersActivity.java:91, 471, 622`
    - `AdminHomeActivity.java:70`, `PackagesActivity.java:63`, `SalesOrdersActivity.java:56`, `ShipmentDetailActivity.java:59`, `ThresholdCheckWorker.java:37`, `PaymentService.java:36`, `VendorRepository.java:29`, `ItemRepository.java:32`, `MarkDeliveredHelper.java:37`, and others

## Rebuild Plan (Clean Data Layer)
- Create a consistent network layer in Java with Retrofit + OkHttp:
  - `data/network/ApiClient` provides a single `Retrofit` configured with timeouts, logging, JSON converter, and secure TLS (remove trust-all).
  - Move base URL to `BuildConfig` via `build.gradle` `buildConfigField` and read from `BuildConfig.API_BASE_URL`.
- Split service interfaces by domain in `data/network/service/`:
  - `AuthService`, `CustomerService`, `VendorService`, `InventoryService`, `ReportsService`, `ShipmentService`, `PaymentService`.
- Define typed models in `data/model/` to replace `Map<String,Object>` usage.
- Add `Result<T>` wrapper and central error handling (HTTP errors, JSON parsing) in `data/network/ResultAdapter`.
- Implement repositories in `data/repository/` used by Activities/Workers:
  - Example: `CustomerRepository` encapsulates `CustomerService` and returns `Result<List<Customer>>`.
- Replace all usages in Activities/Workers:
  - Remove `ApiUrls.getApiService()` calls; inject/get repositories and call methods (e.g., `customerRepository.getCustomers(storeId)`).
  - Migrate `LoginActivity` to use `AuthService.login(username, password)` via Retrofit (no raw OkHttp).
- Optional: add local caching with Room later (not included unless requested).

## Implementation Steps
1. Remove old network layer files: `ApiUrls.java`, `ApiService.java`, `SSLTrustAll.java`.
2. Add `ApiClient` with secure OkHttp and Retrofit configuration; base URL from `BuildConfig`.
3. Create domain service interfaces (`AuthService`, `CustomerService`, etc.) mapping current backend endpoints.
4. Introduce typed request/response models and replace `Map<String,Object>`/`JSONObject` usage.
5. Implement repositories that expose simple methods returning `Result<T>`.
6. Update all Activities/Workers/Repositories to use new repositories; eliminate direct Retrofit/OkHttp calls.
7. Add lightweight unit tests for services/repositories using mock responses.

## Notes and Impacts
- App functionality will remain the same; only the data layer and all call sites are rewritten for clarity, safety, and maintainability.
- Login flow becomes Retrofit-based and reuses common error handling.
- Network discovery/`BASE_URL` handling moves from shared prefs into `BuildConfig` + a small runtime setter if dynamic discovery is still required.

## Confirmation
- Confirm that you want the above deletions and a complete rebuild of the Java data layer as outlined. On approval, I will perform the removals, implement the new structure, and update all usages across the app, verifying with builds and basic tests.