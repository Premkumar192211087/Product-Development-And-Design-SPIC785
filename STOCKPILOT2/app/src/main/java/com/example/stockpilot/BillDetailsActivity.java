package com.example.stockpilot;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

 

public class BillDetailsActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        textView = new TextView(this);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        layout.addView(textView);
        scrollView.addView(layout);
        setContentView(scrollView);

        String billId = getIntent().getStringExtra("bill_id");
        if (billId == null) {
            textView.setText("No bill id provided");
            return;
        }

        textView.setText("Data layer removed\nBill ID: " + billId);
    }
}


