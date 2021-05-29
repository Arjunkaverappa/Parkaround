package com.ka12.parkaround;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class service_activity extends AppCompatActivity {
    LinearLayout change_service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        change_service = findViewById(R.id.change_service);

        set_up_action_and_status_bar();

        //extracting the intent data from the previous activity
        Intent intent = getIntent();
        String service_name = intent.getStringExtra("service");
        Log.e("trying", service_name);

        //changing the fragment accordingly
        FragmentManager fm = getSupportFragmentManager();
        switch (service_name) {
            case "traffic":
                fm.beginTransaction().replace(R.id.change_service, new sub_frag_traffic()).commit();
                break;
            case "fuel":
            case "remainder":
            case "emission":
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