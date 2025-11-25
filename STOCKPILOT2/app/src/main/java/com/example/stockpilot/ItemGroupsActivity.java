package com.example.stockpilot;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stockpilot.R;
import com.example.stockpilot.CategoryAdapter;
import com.example.stockpilot.Category;
import com.example.stockpilot.UserSession;
 
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 

public class ItemGroupsActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryActionListener {

    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private FloatingActionButton fabAddGroup;
    private ImageView backArrow, searchIcon;
    private TextView toolbarTitle;

    private UserSession userSession;
    private String storeId;
    private String storeName;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_group);

        initializeViews();
        setupUserSession();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        
        loadCategories();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        fabAddGroup = findViewById(R.id.fab_add_group);
        backArrow = findViewById(R.id.back_arrow);
        searchIcon = findViewById(R.id.search_icon);
        toolbarTitle = findViewById(R.id.toolbar_title);

        // Disable default title since we're using custom TextView
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupUserSession() {
        userSession = UserSession.getInstance(this);
        storeId = userSession.getStoreId();
        storeName = userSession.getStoreName();

        if (storeId.isEmpty() || storeName.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        if (toolbarTitle != null) {
            toolbarTitle.setText(storeName);
        } else {
            // Fallback to default title if custom TextView not found
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(storeName);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            }
        }
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        categoriesRecyclerView.setLayoutManager(gridLayoutManager);
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement search functionality
                Toast.makeText(ItemGroupsActivity.this, "Search functionality to be implemented", Toast.LENGTH_SHORT).show();
            }
        });

        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });
    }

    private void loadCategories() {
        categoryList.clear();
        categoryAdapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        final EditText input = new EditText(this);
        input.setHint("Enter category name");
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = input.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    addCategory(categoryName);
                } else {
                    Toast.makeText(ItemGroupsActivity.this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addCategory(String categoryName) {
        Toast.makeText(ItemGroupsActivity.this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditCategory(Category category) {
        showEditCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(Category category) {
        showDeleteConfirmationDialog(category);
    }

    private void showEditCategoryDialog(final Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category");

        final EditText input = new EditText(this);
        input.setText(category.getCategoryName());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = input.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    updateCategory(category.getCategoryId(), categoryName);
                } else {
                    Toast.makeText(ItemGroupsActivity.this, "Please enter a category name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateCategory(int categoryId, String categoryName) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "update");
        requestData.put("store_id", Integer.parseInt(storeId));
        requestData.put("category_id", categoryId);
        requestData.put("category_name", categoryName);

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(final Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Category");
        builder.setMessage("Are you sure you want to delete '" + category.getCategoryName() + "'?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCategory(category.getCategoryId());
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteCategory(int categoryId) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "delete");
        requestData.put("store_id", Integer.parseInt(storeId));
        requestData.put("category_id", categoryId);

        Toast.makeText(this, "Data layer removed", Toast.LENGTH_SHORT).show();
    }
}
