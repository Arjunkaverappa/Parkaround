package com.ka12.parkaround;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class settings extends AppCompatActivity {
    TextView edit_address, delete_address, map_style;
    public static final String MAP_TYPE = "com.ka12.parkaround.this_is_where_map_type_is_saved";
    String map_type = "";
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_settings);
        edit_address = findViewById(R.id.edit_address);
        delete_address = findViewById(R.id.delete_address);
        map_style = findViewById(R.id.map_style);
        spinner = findViewById(R.id.spinner);

        String[] options = new String[]{"default", "Hybrid", "satellite", "terrain"};
        ArrayAdapter<String> adapter_manu = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter_manu);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //map_type=options[position];
                SharedPreferences.Editor edit = getSharedPreferences(MAP_TYPE, MODE_PRIVATE).edit();
                edit.putString("type", options[position]).apply();
                Toast.makeText(settings.this, "Map style set successfuly", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                SharedPreferences.Editor edit = getSharedPreferences(MAP_TYPE, MODE_PRIVATE).edit();
                edit.putString("type", options[0]).apply();
            }
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

    public void edit_aaddress() {

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