package com.example.stockpilot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.stockpilot.UserSession;
import com.example.stockpilot.ErrorLogger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
 
import androidx.annotation.NonNull;

public class AddCustomerActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etAddress, etDOB, etNotes;
    private Spinner spinnerStatus;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        
        UserSession session = UserSession.getInstance(this);

        String storeIdStr = session.getStoreId();
        int storeId = storeIdStr.isEmpty() ? 0 : Integer.parseInt(storeIdStr);

        // Bind Views
        TextView tvAvatar = findViewById(R.id.tv_avatar);
        TextView tvSave = findViewById(R.id.tv_save);
        ImageView ivBack = findViewById(R.id.iv_back); // Local variable
        etName = findViewById(R.id.et_customer_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etDOB = findViewById(R.id.et_date_of_birth);
        etNotes = findViewById(R.id.et_notes);
        spinnerStatus = findViewById(R.id.spinner_customer_status);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"active", "inactive"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Avatar update on name input
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                CharSequence nameSeq = etName.getText();
                String name = nameSeq != null ? nameSeq.toString().trim() : "";
                if (!name.isEmpty()) {
                    tvAvatar.setText(name.substring(0, 1).toUpperCase());
                }
            }
        });

        // Date picker
        etDOB.setOnClickListener(view -> showDatePicker());

        // Save click
        tvSave.setOnClickListener(view -> {
            try {
                submitCustomer(storeId);
            } catch (Exception e) {
                ErrorLogger.logError("AddCustomer", "Failed to prepare customer data", e);
                // Removed printStackTrace, already logged
                Toast.makeText(this, "Failed to prepare data", Toast.LENGTH_SHORT).show();
            }
        });

        // Back click
        ivBack.setOnClickListener(view -> finish());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        etDOB.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void submitCustomer(int storeId) {
        CharSequence nameSeq = etName.getText();
        String name = nameSeq != null ? nameSeq.toString().trim() : "";
        CharSequence emailSeq = etEmail.getText();
        String email = emailSeq != null ? emailSeq.toString().trim() : "";
        CharSequence phoneSeq = etPhone.getText();
        String phone = phoneSeq != null ? phoneSeq.toString().trim() : "";
        CharSequence addressSeq = etAddress.getText();
        String address = addressSeq != null ? addressSeq.toString().trim() : "";
        CharSequence dobSeq = etDOB.getText();
        String dob = dobSeq != null ? dobSeq.toString().trim() : "";
        String status = spinnerStatus.getSelectedItem().toString();
        CharSequence notesSeq = etNotes.getText();
        String notes = notesSeq != null ? notesSeq.toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> customerData = new HashMap<>();
        customerData.put("store_id", String.valueOf(storeId));
        customerData.put("customer_name", name);
        customerData.put("email", email);
        customerData.put("phone", phone);
        customerData.put("address", address);
        customerData.put("date_of_birth", dob);
        customerData.put("status", status);
        customerData.put("loyalty_points", "0"); // Default to 0 since there's no input
        customerData.put("notes", notes);

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
}
