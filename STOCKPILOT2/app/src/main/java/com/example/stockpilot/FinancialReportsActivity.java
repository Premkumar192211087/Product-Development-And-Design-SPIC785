package com.example.stockpilot;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stockpilot.UserSession;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.*;

public class FinancialReportsActivity extends AppCompatActivity {

    private TextView tvTotalSales, tvTotalRevenue, tvTotalReturns, tvNetProfit;
    private TextView tvPaidInvoices, tvUnpaidInvoices, tvPartialInvoices;
    private TextView tvCashAmount;
    private LineChart chartSalesTrend;
    private Spinner spinnerChartPeriod;
    private TextView tvFromDate, tvToDate;
    private ImageView btnBack;
    private Calendar fromCalendar, toCalendar;
    private SimpleDateFormat dateFormat;

    
     

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_reports); // Adjust if the layout filename differs

        // Initialize views
        tvTotalSales = findViewById(R.id.tv_total_sales);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvTotalReturns = findViewById(R.id.tv_total_returns);
        tvNetProfit = findViewById(R.id.tv_net_profit);

        tvPaidInvoices = findViewById(R.id.tv_paid_invoices);
        tvUnpaidInvoices = findViewById(R.id.tv_unpaid_invoices);
        tvPartialInvoices = findViewById(R.id.tv_partial_invoices);

        tvCashAmount = findViewById(R.id.tv_cash_amount);
        chartSalesTrend = findViewById(R.id.chartSalesTrend);
        spinnerChartPeriod = findViewById(R.id.spinner_chart_period);
        
        // Initialize date views
        tvFromDate = findViewById(R.id.tv_from_date);
        tvToDate = findViewById(R.id.tv_to_date);
        btnBack = findViewById(R.id.btn_back);
        
        // Initialize date format and calendars
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fromCalendar = Calendar.getInstance();
        toCalendar = Calendar.getInstance();
        
        // Set default date range (current year)
        fromCalendar.set(Calendar.DAY_OF_YEAR, 1);
        tvFromDate.setText(dateFormat.format(fromCalendar.getTime()));
        tvToDate.setText(dateFormat.format(toCalendar.getTime()));
        
        // Setup click listeners
        setupClickListeners();

        loadFinancialData(dateFormat.format(fromCalendar.getTime()), 
                         dateFormat.format(toCalendar.getTime()), 
                         "monthly");
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        tvFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvToDate.setOnClickListener(v -> showDatePickerDialog(false));
        
        spinnerChartPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String period = parent.getItemAtPosition(position).toString().toLowerCase();
                loadFinancialData(dateFormat.format(fromCalendar.getTime()),
                                 dateFormat.format(toCalendar.getTime()),
                                 period);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void showDatePickerDialog(final boolean isFromDate) {
        Calendar calendar = isFromDate ? fromCalendar : toCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    String formattedDate = dateFormat.format(calendar.getTime());
                    if (isFromDate) {
                        tvFromDate.setText(formattedDate);
                    } else {
                        tvToDate.setText(formattedDate);
                    }
                    
                    // Reload data with new date range
                    String period = spinnerChartPeriod.getSelectedItem().toString().toLowerCase();
                    loadFinancialData(dateFormat.format(fromCalendar.getTime()),
                                     dateFormat.format(toCalendar.getTime()),
                                     period);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadFinancialData(String fromDate, String toDate, String period) {
        // Data layer removed: set default values and clear chart
        tvTotalSales.setText("₹0");
        tvTotalRevenue.setText("₹0.00");
        tvTotalReturns.setText("₹0.00");
        tvNetProfit.setText("₹0.00");
        tvPaidInvoices.setText("0");
        tvUnpaidInvoices.setText("0");
        tvPartialInvoices.setText("0");
        tvCashAmount.setText("₹0.00");
        chartSalesTrend.setData(new LineData());
        chartSalesTrend.invalidate();
    }
}
