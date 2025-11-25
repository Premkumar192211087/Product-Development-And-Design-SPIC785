package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

 

public class ProductSelectionActivity extends AppCompatActivity implements ProductSelectionAdapter.OnProductClickListener {

    private ProductSelectionAdapter adapter;

    private final List<Product> productList = new ArrayList<>();

    
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_selection);

        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView rvProducts = findViewById(R.id.rv_products);
        Button btnClose = findViewById(R.id.btn_close);

        adapter = new ProductSelectionAdapter(productList, this, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);

        btnClose.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filterProducts(s.toString());
            }
        });

        fetchProducts();
    }

    private void fetchProducts() {
        String storeId = String.valueOf(userSession.getStoreId());
        productList.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void filterProducts(String query) {
        String lowerQuery = query.toLowerCase();
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList) {
            if (p.getProductName().toLowerCase().contains(lowerQuery) || p.getSku().toLowerCase().contains(lowerQuery)) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }

    @Override
    public void onProductClick(Product product) {
        Intent result = new Intent();
        result.putExtra("selected_product", product);
        setResult(RESULT_OK, result);
        finish();
    }
}
