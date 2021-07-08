package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class fuel_prices extends AppCompatActivity {
    TextView bio_disel_price, petrol_price, disel_price, cng_price;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_prices);
        petrol_price = findViewById(R.id.petrol_price);
        disel_price = findViewById(R.id.disel_price);
        bio_disel_price = findViewById(R.id.bio_disel_price);
        cng_price = findViewById(R.id.cng_price);

        get_the_values_from_firebase();

        set_up_action_and_status_bar();
    }

    private void get_the_values_from_firebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("PRICES");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                String data = snapshot.getValue(String.class);
                Log.e("data", data);
                set_up_prices(data);
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

    @SuppressLint("SetTextI18n")
    private void set_up_prices(String data) {
        // 0=> petrol
        // 1=> disel
        // 2=> cng
        // 3=> bio
        String[] sp = data.split("\\#");
        for (int i = 0; i < sp.length; i++) {
            if (sp[i] != null) {
                if (i == 0) {
                    petrol_price.setText("Petrol\n" + sp[i]);
                } else if (i == 1) {
                    disel_price.setText("Disel\n" + sp[i]);
                } else if (i == 2) {
                    cng_price.setText("CNG\n" + sp[i]);
                } else if (i == 3) {
                    bio_disel_price.setText("Bio Disel\n" + sp[i]);
                }
            }
        }
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