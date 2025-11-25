package com.example.stockpilot;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stockpilot.PaymentMade;


import okhttp3.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

public class PaymentsMadeActivity extends AppCompatActivity {
    private static final String TAG = "PaymentsMadeActivity";

    private RecyclerView recyclerView;
    private PaymentsMadeAdapter adapter;
    private List<PaymentMade> paymentsList;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private TextView emptyStateText;
    private EditText searchEditText;
    private Spinner filterSpinner, sortSpinner;
    private Button searchButton;

    private String storeId;
    private String storeName;
    private OkHttpClient client;
    private UserSession userSession;
    private PaymentService paymentService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_made);

        // Initialize components
        initializeViews();
        setupListeners();

        // Initialize user session to get store information
        userSession = new UserSession(this);
        storeId = String.valueOf(userSession.getStoreId());
        storeName = userSession.getStoreName();

        // Initialize OkHttp client
        client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // Initialize PaymentService with store ID from UserSession
        paymentService = new PaymentService(this, userSession.getStoreId());


        // Initialize payments list and adapter
        paymentsList = new ArrayList<>();
        adapter = new PaymentsMadeAdapter(this, paymentsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up filter spinner
        setupFilterSpinner();

        // Set up sort spinner
        setupSortSpinner();

        // Load payments
        loadPayments();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.rv_payments);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        emptyStateText = findViewById(R.id.tv_empty_state_message);
        searchEditText = findViewById(R.id.et_search);
        filterSpinner = findViewById(R.id.spinner_filter);
        sortSpinner = findViewById(R.id.spinner_sort);
        searchButton = findViewById(R.id.btn_search);
    }

    private void setupListeners() {
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            filterPayments(query);
        });

        // Set adapter click listener
        adapter.setOnItemClickListener(new PaymentsMadeAdapter.OnItemClickListener() {
            @Override
            public void onViewDetailsClick(PaymentMade payment) {
                // Navigate to payment details
                Toast.makeText(PaymentsMadeActivity.this,
                        "View details for Payment #" + payment.getPaymentNumber(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPayments(String query) {
        if (adapter != null) {
            String selectedFilter = filterSpinner.getSelectedItem().toString();
            adapter.filter(query, selectedFilter);
            checkEmptyState();
        }
    }

    private void setupFilterSpinner() {
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Completed", "Pending", "Cancelled"}
        );
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                String searchQuery = searchEditText.getText().toString().trim();
                if (adapter != null) {
                    adapter.filter(searchQuery, selectedFilter);
                    checkEmptyState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSortSpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Date (Newest)", "Date (Oldest)", "Amount (Highest)", "Amount (Lowest)", "Bill Number"}
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = parent.getItemAtPosition(position).toString();
                if (adapter != null) {
                    adapter.sort(selectedSort);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            emptyStateText.setText("No matching payments found");
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void loadPayments() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);

        // Use PaymentService to get all payments
        paymentService.getAllPayments(new PaymentService.PaymentCallback() {
            @Override
            public void onSuccess(List<PaymentMade> payments) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    paymentsList.clear();
                    if (payments != null) {
                        paymentsList.addAll(payments);
                    }
                    adapter.updateList(paymentsList);

                    // Show empty state if no payments
                    if (paymentsList.isEmpty()) {
                        emptyStateLayout.setVisibility(View.VISIBLE);
                        emptyStateText.setText("No payments found");
                    } else {
                        emptyStateLayout.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PaymentsMadeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    emptyStateText.setText("Failed to load payments: " + errorMessage);
                    ErrorLogger.logError(TAG, "Failed to load payments: " + errorMessage, null);
                });
            }
        });
    }


    private void filterPaymentsByStatus(String filter) {
        if (adapter != null) {
            String searchQuery = searchEditText.getText().toString().trim();
            adapter.filter(searchQuery, filter);
            checkEmptyState();
        }
    }

    private void sortPayments(String sortOption) {
        if (adapter != null) {
            adapter.sort(sortOption);
            checkEmptyState();
        }
    }
}


