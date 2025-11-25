package com.example.stockpilot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.ShipmentProductAdapter;
 
import com.example.stockpilot.ShipmentProduct;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipmentDetailActivity extends AppCompatActivity {

    private static final String TAG = "ShipmentDetailActivity";

    // UI
    private TextView tvShipmentNumber, tvOrderInfo, tvStatus, tvCarrierName, tvTrackingNumber,
            tvShippingMethod, tvShippingCost, tvShipDate, tvEstDelivery, tvActualDelivery,
            tvRecipientName, tvRecipientAddress, tvRecipientPhone, tvNotes,
            tvTotalItems, tvTotalValue;

    private LinearLayout layoutActualDelivery;
    private MaterialCardView layoutNotes;

    private RecyclerView recyclerViewShipmentItems;
    private ShipmentProductAdapter productAdapter;
    private List<ShipmentProduct> productList;

    private int shipmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_shipment);

        Log.d(TAG, "onCreate: Activity started");

        
        initializeViews();

        shipmentId = getIntent().getIntExtra("shipment_id", 0);
        Log.d(TAG, "onCreate: Shipment ID received: " + shipmentId);

        if (shipmentId == 0) {
            Log.e(TAG, "onCreate: Invalid shipment ID (0)");
            Toast.makeText(this, "Invalid shipment ID", Toast.LENGTH_SHORT).show();
            return;
        }

        tvShipmentNumber.setText(getIntent().getStringExtra("shipment_number"));

        String orderType = getIntent().getStringExtra("order_type");
        int orderId = getIntent().getIntExtra("order_id", 0);
        tvOrderInfo.setText(orderType + " #" + orderId);

        tvStatus.setText(getIntent().getStringExtra("status"));
        tvCarrierName.setText(getIntent().getStringExtra("carrier_name"));
        tvTrackingNumber.setText(getIntent().getStringExtra("tracking_number"));
        tvShippingMethod.setText(getIntent().getStringExtra("shipping_method"));
        tvShippingCost.setText("₹" + getIntent().getDoubleExtra("shipping_cost", 0));
        tvShipDate.setText(getIntent().getStringExtra("ship_date"));
        tvEstDelivery.setText(getIntent().getStringExtra("estimated_delivery_date"));

        String actualDelivery = getIntent().getStringExtra("actual_delivery_date");
        if (actualDelivery != null && !actualDelivery.isEmpty()) {
            layoutActualDelivery.setVisibility(View.VISIBLE);
            tvActualDelivery.setText(actualDelivery);
        }

        String notes = getIntent().getStringExtra("notes");
        if (notes != null && !notes.isEmpty()) {
            layoutNotes.setVisibility(View.VISIBLE);
            tvNotes.setText(notes);
        }

        tvRecipientName.setText(getIntent().getStringExtra("recipient_name"));
        tvRecipientPhone.setText(getIntent().getStringExtra("recipient_phone"));
        tvRecipientAddress.setText(getIntent().getStringExtra("recipient_address"));

        // Initialize RecyclerView
        Log.d(TAG, "onCreate: Initializing RecyclerView");
        productList = new ArrayList<>();
        productAdapter = new ShipmentProductAdapter(productList);

        if (recyclerViewShipmentItems != null) {
            recyclerViewShipmentItems.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewShipmentItems.setAdapter(productAdapter);
            Log.d(TAG, "onCreate: RecyclerView setup completed");
        } else {
            Log.e(TAG, "onCreate: RecyclerView is null! Check XML layout and findViewById");
        }

        loadShipmentItems();
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews: Starting view initialization");

        try {
            tvShipmentNumber = findViewById(R.id.tvDetailShipmentNumber);
            tvOrderInfo = findViewById(R.id.tvDetailOrderInfo);
            tvStatus = findViewById(R.id.tvDetailStatus);
            tvCarrierName = findViewById(R.id.tvDetailCarrierName);
            tvTrackingNumber = findViewById(R.id.tvDetailTrackingNumber);
            tvShippingMethod = findViewById(R.id.tvDetailShippingMethod);
            tvShippingCost = findViewById(R.id.tvDetailShippingCost);
            tvShipDate = findViewById(R.id.tvDetailShipDate);
            tvEstDelivery = findViewById(R.id.tvDetailEstDeliveryDate);
            tvActualDelivery = findViewById(R.id.tvDetailActualDeliveryDate);
            tvRecipientName = findViewById(R.id.tvDetailRecipientName);
            tvRecipientPhone = findViewById(R.id.tvDetailRecipientPhone);
            tvRecipientAddress = findViewById(R.id.tvDetailRecipientAddress);
            tvNotes = findViewById(R.id.tvDetailNotes);
            tvTotalItems = findViewById(R.id.tvTotalItems);
            tvTotalValue = findViewById(R.id.tvTotalValue);

            layoutActualDelivery = findViewById(R.id.layoutActualDelivery);
            layoutNotes = findViewById(R.id.cardNotes);

            recyclerViewShipmentItems = findViewById(R.id.recyclerViewShipmentItems);

            // Log which views are null
            if (recyclerViewShipmentItems == null) {
                Log.e(TAG, "initializeViews: recyclerViewShipmentItems is NULL");
            } else {
                Log.d(TAG, "initializeViews: recyclerViewShipmentItems found successfully");
            }

            if (tvTotalItems == null) {
                Log.e(TAG, "initializeViews: tvTotalItems is NULL");
            }

            if (tvTotalValue == null) {
                Log.e(TAG, "initializeViews: tvTotalValue is NULL");
            }

            Log.d(TAG, "initializeViews: View initialization completed");

        } catch (Exception e) {
            Log.e(TAG, "initializeViews: Error during view initialization", e);
        }
    }

    private void loadShipmentItems() {
        productList.clear();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }
        if (tvTotalItems != null) {
            tvTotalItems.setText("0");
        }
        if (tvTotalValue != null) {
            tvTotalValue.setText("₹0.00");
        }
    }
}
