package com.example.stockpilot;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
 

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

 

public abstract class DialogSelectLayout extends AppCompatActivity implements ItemProductSelection.OnProductSelectedListener {

    private static final String TAG = "DialogSelectLayout";

    private EditText etSearchProduct;
    private RecyclerView rvProducts;
    private LinearLayout layoutEmptyProducts;
    private ImageButton btnCloseDialog;

    private List<Product> productList = new ArrayList<>();
    private ItemProductSelection adapter;
    
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_select_layout);

        userSession = UserSession.getInstance(this);
        
        initViews();
        setupRecyclerView();
        setupSearchFilter();
        fetchProducts();
    }

    private void initViews() {
        etSearchProduct = findViewById(R.id.et_search_product);
        rvProducts = findViewById(R.id.rv_products);
        layoutEmptyProducts = findViewById(R.id.layout_empty_products);
        btnCloseDialog = findViewById(R.id.btn_close_dialog);

        btnCloseDialog.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ItemProductSelection(this, productList, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        int itemCount = adapter.getItemCount();
        Log.d(TAG, "Updating empty state. Item count: " + itemCount);
        
        if (itemCount == 0) {
            Log.d(TAG, "Showing empty state view");
            rvProducts.setVisibility(View.GONE);
            layoutEmptyProducts.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "Showing recycler view with " + itemCount + " items");
            rvProducts.setVisibility(View.VISIBLE);
            layoutEmptyProducts.setVisibility(View.GONE);
        }
    }

    private void fetchProducts() {
        String storeId = userSession.getStoreId();
        Log.d(TAG, "Fetching products for store ID: " + storeId);

        productList.clear();
        adapter.filter("");
        updateEmptyState();
        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void parseProductsResponse(JSONArray productsArray) throws JSONException {
        productList.clear();
        Log.d(TAG, "Parsing products array of length: " + productsArray.length());

        for (int i = 0; i < productsArray.length(); i++) {
            try {
                JSONObject productObj = productsArray.getJSONObject(i);
                Log.d(TAG, "Product JSON at index " + i + ": " + productObj.toString());
                
                Product product = new Product(
                        productObj.getInt("id"),
                        productObj.getString("product_name"),
                        productObj.getString("sku"),
                        productObj.getString("category"),
                        productObj.getInt("stock_quantity"),
                        productObj.getDouble("selling_price")
                );
                productList.add(product);
                Log.d(TAG, "Added product: " + product.getProductName());
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing product at index " + i + ": " + e.getMessage());
            }
        }

        Log.d(TAG, "Total products added to list: " + productList.size());
        runOnUiThread(() -> {
            adapter.filter(""); // Reset filter to show all products
            updateEmptyState();
            Log.d(TAG, "UI updated with " + adapter.getItemCount() + " products");
        });
    }

    @Override
    public void onProductSelected(Product product) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_product", product);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
