package com.example.stockpilot;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.example.stockpilot.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.google.zxing.ResultPoint;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Enhanced Scanner_Helper class with improved focusing
 */
public class Scanner_Helper extends AppCompatActivity implements
        DecoratedBarcodeView.TorchListener {

    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private Button switchFlashlightButton;
    private ViewfinderView viewfinderView;
    private boolean isFlashLightOn = false;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                // Return the barcode result to the calling activity
                Intent intent = new Intent();
                intent.putExtra("SCAN_RESULT", result.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_helper);

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);
        
        // Configure camera settings for better focusing
        CameraSettings cameraSettings = new CameraSettings();
        cameraSettings.setRequestedCameraId(0); // Use the back camera
        cameraSettings.setScanInverted(false); // Normal scan mode (not inverted)
        cameraSettings.setAutoFocusEnabled(true); // Enable auto-focus
        cameraSettings.setContinuousFocusEnabled(true); // Enable continuous focus
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        
        // Set status text
        barcodeScannerView.setStatusText("Center barcode in rectangle");

        // Set barcode callback to handle scan results
        barcodeScannerView.decodeContinuous(callback);

        // Initialize capture manager
        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
        
        // Schedule periodic auto-focus
        scheduleAutoFocus();

        // Flash toggle button
        switchFlashlightButton = findViewById(R.id.switch_flashlight);

        // Check if device has flash
        if (!hasFlash()) {
            switchFlashlightButton.setVisibility(View.GONE);
        } else {
            switchFlashlightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switchFlashlight();
                }
            });
        }
    }

    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void switchFlashlight() {
        if (isFlashLightOn) {
            barcodeScannerView.setTorchOff();
        } else {
            barcodeScannerView.setTorchOn();
        }
    }

    @Override
    public void onTorchOn() {
        isFlashLightOn = true;
        switchFlashlightButton.setText("Turn flash OFF");
    }

    @Override
    public void onTorchOff() {
        isFlashLightOn = false;
        switchFlashlightButton.setText("Turn flash ON");
    }

    private void scheduleAutoFocus() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (barcodeScannerView != null) {
                    barcodeScannerView.getBarcodeView().requestFocus();
                    handler.postDelayed(this, 1000); // Re-focus every second
                }
            }
        }, 1000); // Initial delay of 1 second
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
        // Request focus when activity resumes
        if (barcodeScannerView != null) {
            barcodeScannerView.getBarcodeView().requestFocus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
