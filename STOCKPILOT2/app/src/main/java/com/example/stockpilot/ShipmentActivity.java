package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.ShipmentAdapter;
import com.example.stockpilot.Shipment;
 
import com.example.stockpilot.UserSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipmentActivity extends AppCompatActivity implements ShipmentAdapter.OnShipmentClickListener {

    // UI Components
    private ImageButton btnBack, btnAddShipment;
    private TextView tvStoreName, tvPendingCount, tvShippedCount, tvInTransitCount, tvDeliveredCount;
    private EditText etSearch;
    private Spinner spinnerStatus, spinnerOrderType;
    private RecyclerView recyclerViewShipments;
    private LinearLayout layoutEmptyState, layoutLoading;
    private Button btnCreateFirstShipment;

    // Data
    private List<Shipment> shipmentList;
    private List<Shipment> filteredShipmentList;
    private ShipmentAdapter shipmentAdapter;
    private UserSession userSession;

    // Filter variables
    private String currentStatusFilter = "";
    private String currentOrderTypeFilter = "";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipments);

        initializeViews();
        setupUserSession();
        setupRecyclerView();
        setupSpinners();
        setupSearchListener();
        setupClickListeners();

        

        loadShipments();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnAddShipment = findViewById(R.id.btnAddShipment);
        tvStoreName = findViewById(R.id.tvStoreName);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvShippedCount = findViewById(R.id.tvShippedCount);
        tvInTransitCount = findViewById(R.id.tvInTransitCount);
        tvDeliveredCount = findViewById(R.id.tvDeliveredCount);
        etSearch = findViewById(R.id.etSearch);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerOrderType = findViewById(R.id.spinnerOrderType);
        recyclerViewShipments = findViewById(R.id.recyclerViewShipments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutLoading = findViewById(R.id.layoutLoading);
        btnCreateFirstShipment = findViewById(R.id.btnCreateFirstShipment);
    }

    private void setupUserSession() {
        userSession = UserSession.getInstance(this);
        if (!userSession.isLoggedIn()) {
            finish();
            return;
        }
        tvStoreName.setText(userSession.getStoreName());
    }

    private void setupRecyclerView() {
        shipmentList = new ArrayList<>();
        filteredShipmentList = new ArrayList<>();
        shipmentAdapter = new ShipmentAdapter(this, filteredShipmentList, this);
        recyclerViewShipments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShipments.setAdapter(shipmentAdapter);
    }

    private void setupSpinners() {
        String[] statuses = {"All Status", "Pending", "Shipped", "In Transit", "Delivered"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatusFilter = position == 0 ? "" : statuses[position].toLowerCase().replace(" ", "_");
                applyFilters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] orderTypes = {"All Orders", "Purchase Order", "Sales Order"};
        ArrayAdapter<String> orderTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, orderTypes);
        orderTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderType.setAdapter(orderTypeAdapter);

        spinnerOrderType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentOrderTypeFilter = position == 0 ? "" : orderTypes[position].toLowerCase().replace(" ", "_");
                applyFilters();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddShipment.setOnClickListener(v -> {
            Intent intent = new Intent(ShipmentActivity.this, AddShipmentActivity.class);
            startActivity(intent);
        });

        btnCreateFirstShipment.setOnClickListener(v -> {
            Intent intent = new Intent(ShipmentActivity.this, AddShipmentActivity.class);
            startActivity(intent);
        });
    }

    private void loadShipments() {
        showLoading(false);
        shipmentList = new ArrayList<>();
        filteredShipmentList = new ArrayList<>();
        shipmentAdapter.notifyDataSetChanged();
        tvPendingCount.setText("0");
        tvShippedCount.setText("0");
        tvInTransitCount.setText("0");
        tvDeliveredCount.setText("0");
    }

    private void parseShipmentsData(JSONObject response) {
        try {
            shipmentList.clear();
            JSONArray dataArray = response.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject shipmentObj = dataArray.getJSONObject(i);
                Shipment shipment = new Shipment();

                shipment.setShipmentId(shipmentObj.getInt("shipment_id"));
                shipment.setShipmentNumber(shipmentObj.getString("shipment_number"));
                shipment.setOrderType(shipmentObj.getString("order_type"));
                shipment.setOrderId(shipmentObj.getInt("order_id"));
                shipment.setOrderNumber(shipmentObj.getString("order_number"));
                shipment.setCarrierName(shipmentObj.getString("carrier_name"));
                shipment.setTrackingNumber(shipmentObj.getString("tracking_number"));
                shipment.setShippingMethod(shipmentObj.getString("shipping_method"));
                shipment.setShippingCost(shipmentObj.getDouble("shipping_cost"));
                shipment.setShipDate(shipmentObj.optString("ship_date", null));
                shipment.setEstimatedDeliveryDate(shipmentObj.optString("estimated_delivery_date", null));
                shipment.setActualDeliveryDate(shipmentObj.optString("actual_delivery_date", null));
                shipment.setStatus(shipmentObj.getString("status"));
                shipment.setRecipientName(shipmentObj.getString("recipient_name"));
                shipment.setRecipientAddress(shipmentObj.getString("recipient_address"));
                shipment.setRecipientPhone(shipmentObj.getString("recipient_phone"));
                shipment.setNotes(shipmentObj.getString("notes"));
                shipment.setCreatedAt(shipmentObj.getString("created_at"));
                shipment.setCanTrack(shipmentObj.getBoolean("can_track"));
                shipment.setOverdue(shipmentObj.getBoolean("is_overdue"));

                shipmentList.add(shipment);
            }

            applyFilters();
        } catch (JSONException e) {
            showError("Error parsing shipments data: " + e.getMessage());
        }
    }

    private void updateStatistics(JSONObject statistics) throws JSONException {
        tvPendingCount.setText(String.valueOf(statistics.getInt("pending")));
        tvShippedCount.setText(String.valueOf(statistics.getInt("shipped")));
        tvInTransitCount.setText(String.valueOf(statistics.getInt("in_transit")));
        tvDeliveredCount.setText(String.valueOf(statistics.getInt("delivered")));
    }

    private void applyFilters() {
        filteredShipmentList.clear();

        for (Shipment shipment : shipmentList) {
            boolean matchesStatus = currentStatusFilter.isEmpty() || shipment.getStatus().equals(currentStatusFilter);
            boolean matchesOrderType = currentOrderTypeFilter.isEmpty() || shipment.getOrderType().equals(currentOrderTypeFilter);
            boolean matchesSearch = currentSearchQuery.isEmpty() || matchesSearchCriteria(shipment, currentSearchQuery);

            if (matchesStatus && matchesOrderType && matchesSearch) {
                filteredShipmentList.add(shipment);
            }
        }

        shipmentAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private boolean matchesSearchCriteria(Shipment shipment, String query) {
        String lowerQuery = query.toLowerCase();
        return shipment.getShipmentNumber().toLowerCase().contains(lowerQuery) ||
                shipment.getTrackingNumber().toLowerCase().contains(lowerQuery) ||
                shipment.getRecipientName().toLowerCase().contains(lowerQuery) ||
                shipment.getCarrierName().toLowerCase().contains(lowerQuery);
    }

    private void updateEmptyState() {
        if (filteredShipmentList.isEmpty()) {
            recyclerViewShipments.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewShipments.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            layoutLoading.setVisibility(View.VISIBLE);
            recyclerViewShipments.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        } else {
            layoutLoading.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadShipments();
    }

    @Override
    public void onViewDetailsClick(Shipment shipment) {
        Intent intent = new Intent(this, ShipmentDetailActivity.class);
        intent.putExtra("shipment_id", shipment.getShipmentId());
        intent.putExtra("shipment_number", shipment.getShipmentNumber());
        intent.putExtra("order_type", shipment.getOrderType());
        intent.putExtra("order_id", shipment.getOrderId());
        intent.putExtra("carrier_name", shipment.getCarrierName());
        intent.putExtra("tracking_number", shipment.getTrackingNumber());
        intent.putExtra("shipping_method", shipment.getShippingMethod());
        intent.putExtra("shipping_cost", shipment.getShippingCost());
        intent.putExtra("ship_date", shipment.getShipDate());
        intent.putExtra("estimated_delivery_date", shipment.getEstimatedDeliveryDate());
        intent.putExtra("actual_delivery_date", shipment.getActualDeliveryDate());
        intent.putExtra("status", shipment.getStatus());
        intent.putExtra("recipient_name", shipment.getRecipientName());
        intent.putExtra("recipient_address", shipment.getRecipientAddress());
        intent.putExtra("recipient_phone", shipment.getRecipientPhone());
        intent.putExtra("notes", shipment.getNotes());
        startActivity(intent);
    }

    @Override
    public void onTrackShipmentClick(Shipment shipment) {
        if (shipment.canTrack()) {
            Intent intent = new Intent(this, TrackShipment.class);
            intent.putExtra("tracking_number", shipment.getTrackingNumber());
            intent.putExtra("carrier_name", shipment.getCarrierName());
            startActivity(intent);
        } else {
            Toast.makeText(this, "No tracking number available", Toast.LENGTH_SHORT).show();
        }
    }
}

