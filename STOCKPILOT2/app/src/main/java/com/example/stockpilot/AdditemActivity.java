package com.example.stockpilot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.example.stockpilot.ApiUrls;
import com.example.stockpilot.Constants;
import com.example.stockpilot.AuthInterceptor;
import com.example.stockpilot.UserSession;

public class AdditemActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_PERMISSION = 100;

    private ImageView productImage;
    private Bitmap selectedImageBitmap;
    private TextInputEditText etProductName, etSku, etPrice, etQuantity, etBarcode;
    private AutoCompleteTextView spinnerCategory;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additem);

        
        initViews();
        setupClickListeners();
        checkPermissions();
    }

    private void initViews() {
        productImage = findViewById(R.id.product_image);
        etProductName = findViewById(R.id.et_product_name);
        etSku = findViewById(R.id.et_sku);
        spinnerCategory = findViewById(R.id.spinner_category);
        etPrice = findViewById(R.id.et_price);
        etQuantity = findViewById(R.id.et_quantity);
        etBarcode = findViewById(R.id.et_barcode);

        AuthInterceptor.setTokenProvider(() -> UserSession.getInstance(this).getToken());
    }

    private void setupClickListeners() {
        findViewById(R.id.back_icon).setOnClickListener(v -> finish());

        findViewById(R.id.btn_camera).setOnClickListener(v -> openCamera());
        findViewById(R.id.btn_gallery).setOnClickListener(v -> openGallery());

        findViewById(R.id.save_icon).setOnClickListener(v -> {
            if (validateFields()) {
                saveItem();
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CAMERA);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                if (requestCode == REQUEST_CAMERA) {
                    selectedImageBitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    productImage.setImageBitmap(selectedImageBitmap);
                } else if (requestCode == REQUEST_GALLERY) {
                    Uri imageUri = data.getData();
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    productImage.setImageBitmap(selectedImageBitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateFields() {
        return etProductName.getText() != null && !etProductName.getText().toString().trim().isEmpty()
                && etPrice.getText() != null && !etPrice.getText().toString().trim().isEmpty()
                && etQuantity.getText() != null && !etQuantity.getText().toString().trim().isEmpty();
    }

    private void saveItem() {
        // Create a map for form data
        Map<String, RequestBody> params = new HashMap<>();
        params.put("product_name", RequestBody.create(MediaType.parse("text/plain"), etProductName.getText() != null ? etProductName.getText().toString() : ""));
        params.put("sku", RequestBody.create(MediaType.parse("text/plain"), etSku.getText() != null ? etSku.getText().toString() : ""));
        params.put("category", RequestBody.create(MediaType.parse("text/plain"), spinnerCategory.getText() != null ? spinnerCategory.getText().toString() : ""));
        params.put("price", RequestBody.create(MediaType.parse("text/plain"), etPrice.getText() != null ? etPrice.getText().toString() : ""));
        params.put("quantity", RequestBody.create(MediaType.parse("text/plain"), etQuantity.getText() != null ? etQuantity.getText().toString() : ""));
        params.put("barcode", RequestBody.create(MediaType.parse("text/plain"), etBarcode.getText() != null ? etBarcode.getText().toString() : ""));

        // Prepare image part
        MultipartBody.Part imagePart = null;
        if (selectedImageBitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] byteArray = stream.toByteArray();
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            imagePart = MultipartBody.Part.createFormData("image", "product.jpg", requestFile);
        } else {
            // Create an empty part if no image is selected
            RequestBody emptyBody = RequestBody.create(MediaType.parse("text/plain"), "");
            imagePart = MultipartBody.Part.createFormData("image", "", emptyBody);
        }

        ApiUrls.getApiService().createItem(params, imagePart).enqueue(new retrofit2.Callback<Item>() {
            @Override
            public void onResponse(retrofit2.Call<Item> call, retrofit2.Response<Item> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdditemActivity.this, "Item created", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdditemActivity.this, Constants.ERROR_SERVER, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Item> call, Throwable t) {
                Toast.makeText(AdditemActivity.this, Constants.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
