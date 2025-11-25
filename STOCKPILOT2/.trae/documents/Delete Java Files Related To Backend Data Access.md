## Files To Delete Now

* `app/src/main/java/com/example/stockpilot/ApiService.java` — Retrofit interface mapping all backend endpoints.

* `app/src/main/java/com/example/stockpilot/ApiUrls.java` — Central HTTP client/base URL/endpoint constants.

* `app/src/main/java/com/example/stockpilot/SSLTrustAll.java` — Insecure trust-all TLS helper (already removed in code; if it still exists in your workspace, it will be deleted).

## Impact

* All classes that call `ApiUrls.getApiService()` or depend on endpoint constants will not compile until the new data layer is recreated.

* Typical affected files include Activities/Workers/Repositories such as `CustomersActivity`, `AdminHomeActivity`, `PackagesActivity`, `SalesOrdersActivity`, `ShipmentDetailActivity`, `ThresholdCheckWorker`, `PaymentService`, `VendorRepository`, `ItemRepository`, `MarkDeliveredHelper`, and others.

## What Will NOT Be Deleted

* UI, session, and non-network logic remain intact (Activities, layouts, helpers not directly tied to the HTTP client).

* No local database code exists (Room/SQLite/JDBC/Firebase/Realm), so there are no Java-side DB helpers to remove beyond the network layer listed above.

## Next Steps After Deletion (For Your Later Plan)

* Provide your plan to recreate the data layer; I’ll implement a clean, secure Retrofit client and split service interfaces by domain with typed models and repository abstraction.

## Confirmation

* On approval, I will delete the listed files immediately and leave the project ready for your follow-up plan to rebuild the data layer from scratch.

things needs to be done:

everything related to the databse connetion in the files including the classes calliing apiservice\
i need you to delete the things related to the data layer

<br />

