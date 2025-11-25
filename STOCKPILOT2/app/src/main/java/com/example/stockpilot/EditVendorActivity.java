package com.example.stockpilot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.stockpilot.R;
 
import com.example.stockpilot.UserSession;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;

public class EditVendorActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvSave, tvAvatar, tvRegistrationInfo;
    private TextInputEditText etVendorName, etContactPerson, etEmail, etPhone, etAddress;
    private TextInputEditText etCity, etState, etZipCode, etCountry, etNotes;
    private Spinner spinnerVendorStatus;

    private String vendorId;
    private String storeId;
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vendor);

        vendorId = getIntent().getStringExtra("vendorId");
        if (vendorId == null) {
            Toast.makeText(this, "Vendor ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivBack = findViewById(R.id.iv_back);
        tvSave = findViewById(R.id.tv_save);
        tvAvatar = findViewById(R.id.tv_avatar);
        tvRegistrationInfo = findViewById(R.id.tv_registration_info);

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

        
        storeId = UserSession.getInstance(this).getStoreId();

        setupListeners();
        loadVendorData();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        tvSave.setOnClickListener(v -> {
            if (validateForm()) updateVendor();
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

    private void loadVendorData() {
        Map<String, String> params = new HashMap<>();
        params.put("vendor_id", vendorId);
        params.put("store_id", storeId);

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private boolean validateForm() {
        String name = etVendorName.getText().toString().trim();
        if (name.isEmpty()) {
            etVendorName.setError("Vendor name is required");
            return false;
        }
        return true;
    }

    private void updateVendor() {
        tvSave.setEnabled(false);

        Map<String, String> vendorData = new HashMap<>();
        vendorData.put("action", "update");
        vendorData.put("vendor_id", vendorId);
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

        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(vendorData)).toString());

        tvSave.setEnabled(true);
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
}
