package com.example.stockpilot;

import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.R;
 
import com.example.stockpilot.UserSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVendorActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvSave, tvAvatar, tvRegistrationInfo;
    private EditText etVendorName, etContactPerson, etEmail, etPhone, etAddress, etCity, etState, etZipCode, etCountry, etNotes;
    private Spinner spinnerVendorStatus;

    
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vendor);

        ivBack = findViewById(R.id.iv_back);
        tvSave = findViewById(R.id.tv_save);
        tvAvatar = findViewById(R.id.tv_avatar);

        etVendorName = findViewById(R.id.et_vendor_name);
        etContactPerson = findViewById(R.id.et_contact_person);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etZipCode = findViewById(R.id.et_zip_code);
        etCountry = findViewById(R.id.et_country);
        etNotes = findViewById(R.id.et_notes);

        spinnerVendorStatus = findViewById(R.id.spinner_vendor_status);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this, R.array.vendor_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVendorStatus.setAdapter(statusAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        tvRegistrationInfo.setText("This vendor will be registered on: " + sdf.format(new Date()));

        storeId = String.valueOf(UserSession.getInstance(this).getStoreId());

        setupListeners();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        tvSave.setOnClickListener(v -> {
            if (validateForm()) saveVendor();
        });

        etVendorName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                updateAvatar(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
        });
    }

    private void updateAvatar(String name) {
        tvAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
    }

    private boolean validateForm() {
        boolean valid = true;
        String name = etVendorName.getText().toString().trim();
        if (name.isEmpty()) {
            etVendorName.setError("Vendor name required");
            valid = false;
        }
        String email = etEmail.getText().toString().trim();
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            valid = false;
        }
        return valid;
    }

    private void saveVendor() {
        tvSave.setEnabled(false);

        Map<String, String> vendorData = new HashMap<>();
        vendorData.put("store_id", storeId);
        vendorData.put("vendor_name", etVendorName.getText().toString().trim());
        vendorData.put("contact_person", etContactPerson.getText().toString().trim());
        vendorData.put("email", etEmail.getText().toString().trim());
        vendorData.put("phone", etPhone.getText().toString().trim());
        vendorData.put("address", etAddress.getText().toString().trim());
        vendorData.put("city", etCity.getText().toString().trim());
        vendorData.put("state", etState.getText().toString().trim());
        vendorData.put("zip_code", etZipCode.getText().toString().trim());
        vendorData.put("country", etCountry.getText().toString().trim());
        vendorData.put("status", spinnerVendorStatus.getSelectedItem().toString().toLowerCase());
        vendorData.put("notes", etNotes.getText().toString().trim());

        tvSave.setEnabled(true);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
}
