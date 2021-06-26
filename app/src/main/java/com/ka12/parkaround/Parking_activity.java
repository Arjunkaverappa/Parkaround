package com.ka12.parkaround;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class Parking_activity extends AppCompatActivity {
    Bitmap bitmap;
    CodeScannerView scannerView;
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    CodeScanner codeScanner;
    String user_phone_number;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);
        scannerView = findViewById(R.id.scanner_view);

        //setting up action bar
        set_up_action_and_status_bar();

        //getting the user phone number
        SharedPreferences get_number = getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
        user_phone_number = get_number.getString("phone", "9977997795");

        //open_scanner()
        check_permission();

        //testing
        // check_the_scan_result("9977997795");
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

        codeScanner = new CodeScanner(this, scannerView);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            //result
            Toast.makeText(Parking_activity.this, result.getText(), Toast.LENGTH_SHORT).show();
            check_the_scan_result(result.getText());
        }));
        try {
            codeScanner.startPreview();
        } catch (Exception e) {
            Log.e("qrcode", "error :" + e.getMessage());
            Toast.makeText(this, "ERROR :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void check_the_scan_result(String result) {
        //check is its the owner number, get the node from firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("BOOKINGS");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                String data = snapshot.getValue(String.class);
                if (data != null) {
                    /* these are the details obtained from the firebase upon booking done
                        0) vehicle number
                        1) vehicle model
                        2) manufacturer
                        3) color
                        4) user address
                        5) time start
                        6) time end
                        7) booking guy phone number
         */
                    Log.e("books", data);
                    String[] split = data.split("\\#");
                    if (split[7].equals(result)) {
                        confrim_booking();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void confrim_booking() {
        Toast.makeText(this, "Booking confirmed", Toast.LENGTH_SHORT).show();
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