package com.ka12.parkaround;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;


public class Parking_activity extends AppCompatActivity {
    ImageView img;
    Bitmap bitmap;
    Button btn;
    CodeScannerView scannerView;
    CodeScanner codeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);
        img = findViewById(R.id.img);
        btn = findViewById(R.id.btn);
        scannerView = findViewById(R.id.scanner_view);

        //setting up action bar
        set_up_action_and_status_bar();

        //hiding the scanner view
        scannerView.setVisibility(View.GONE);

        btn.setOnClickListener(v -> check_permission());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            if (requestCode == RESULT_OK) {
                open_scanner();
            }
        }
    }

    public void check_permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 500);
        } else {
            open_scanner();
        }
    }

    public void open_scanner() {
        //setting up  visibility
        btn.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        scannerView.setVisibility(View.VISIBLE);

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(this, scannerView);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            //result
            scannerView.setVisibility(View.GONE);
            btn.setText(result.getText());
            img.setVisibility(View.VISIBLE);
            Toast.makeText(Parking_activity.this, result.getText(), Toast.LENGTH_SHORT).show();
        }));
        try {
            codeScanner.startPreview();
        } catch (Exception e) {
            Log.e("qrcode", "error :" + e.getMessage());
            Toast.makeText(this, "ERROR :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (codeScanner != null)
            codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        if (codeScanner != null)
            codeScanner.releaseResources();
        super.onPause();
    }

    public void set_up_action_and_status_bar() {
        //hiding the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        //to get transparent status bar, try changing the themes
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}