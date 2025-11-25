package com.example.stockpilot;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;

 
import com.example.stockpilot.R;
import com.example.stockpilot.UserSession;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBatchActivity extends AppCompatActivity {

    private EditText productCodeEditText;
    private Button scanBarcodeButton;
    private EditText manufacturingDateEditText;
    private EditText expiryDateEditText;
    private Button calculateExpiryButton;
    private EditText quantityEditText;
    private Button addBatchButton;
    private ImageButton backButton;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_batch);

        userSession = new UserSession(this);

        productCodeEditText = findViewById(R.id.product_code_edit_text);
        scanBarcodeButton = findViewById(R.id.scan_barcode_button);
        manufacturingDateEditText = findViewById(R.id.manufacturing_date_edit_text);
        expiryDateEditText = findViewById(R.id.expiry_date_edit_text);
        quantityEditText = findViewById(R.id.quantity_edit_text);
        addBatchButton = findViewById(R.id.add_batch_button);
        backButton = findViewById(R.id.back_button);

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddBatchActivity.this, Scanner_Helper.class);
                startActivityForResult(intent, 1001);
            }
        });

        calculateExpiryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateExpiryDate();
            }
        });

        addBatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBatch();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String barcode = data.getStringExtra("SCAN_RESULT");
            if (barcode != null) {
                productCodeEditText.setText(barcode);
            }
        }
    }

    private void calculateExpiryDate() {
        String mfgDateStr = manufacturingDateEditText.getText().toString().trim();
        if (mfgDateStr.isEmpty()) {
            Toast.makeText(this, "Please enter manufacturing date first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date mfgDate = sdf.parse(mfgDateStr);
            
            // Add 2 years as default shelf life
            Calendar cal = Calendar.getInstance();
            cal.setTime(mfgDate);
            cal.add(Calendar.YEAR, 2);
            
            String expiryDate = sdf.format(cal.getTime());
            expiryDateEditText.setText(expiryDate);
            
            Toast.makeText(this, "Expiry date calculated (2 years from MFG)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Invalid manufacturing date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
        }
    }

    private void addBatch() {
        String productCode = productCodeEditText.getText().toString().trim();
        String manufacturingDate = manufacturingDateEditText.getText().toString().trim();
        String expiryDate = expiryDateEditText.getText().toString().trim();
        String quantity = quantityEditText.getText().toString().trim();
        String storeId = String.valueOf(userSession.getStoreId());

        if (productCode.isEmpty() || manufacturingDate.isEmpty() || expiryDate.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> batchData = new HashMap<>();
        batchData.put("product_code", productCode);
        batchData.put("manufacturing_date", manufacturingDate);
        batchData.put("expiry_date", expiryDate);
        batchData.put("quantity", quantity);
        batchData.put("store_id", storeId);

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
}