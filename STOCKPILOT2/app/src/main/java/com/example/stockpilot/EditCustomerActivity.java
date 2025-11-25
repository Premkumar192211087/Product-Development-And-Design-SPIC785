package com.example.stockpilot;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.stockpilot.R;
import com.example.stockpilot.UserSession;

public class EditCustomerActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvSave;
    private UserSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Initialize session
        session = UserSession.getInstance(this);
        
        // Setup UI components
        setupViews();
        setupClickListeners();
    }
    
    private void setupViews() {
        // Find views by ID - assuming these exist in your layout
        // If they don't exist yet, you'll need to add them to your layout file
        ivBack = findViewById(R.id.iv_back);
        tvSave = findViewById(R.id.tv_save);
    }
    
    private void setupClickListeners() {
        // Back button click listener
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        // Save button click listener
        if (tvSave != null) {
            tvSave.setOnClickListener(v -> {
                // Implement save functionality here
                // After saving, navigate back
                finish();
            });
        }
    }
}
