package com.ka12.parkaround;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class settings extends AppCompatActivity {
    TextView edit_address, delete_address, map_style;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_settings);
        edit_address = findViewById(R.id.edit_address);
        delete_address = findViewById(R.id.delete_address);
        map_style = findViewById(R.id.map_style);

        map_style.setOnClickListener(v -> {
            //map style
            Toast.makeText(settings.this, "Coming soon", Toast.LENGTH_SHORT).show();
        });

        edit_address.setOnClickListener(v -> {
            //s
            Toast.makeText(settings.this, "Coming soon", Toast.LENGTH_SHORT).show();

        });

        delete_address.setOnClickListener(v -> {
            //s
            Toast.makeText(settings.this, "Coming soon", Toast.LENGTH_SHORT).show();

        });


        //hiding the action bar
        set_up_action_and_status_bar();


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