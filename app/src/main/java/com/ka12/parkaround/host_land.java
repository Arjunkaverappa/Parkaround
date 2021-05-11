package com.ka12.parkaround;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class host_land extends AppCompatActivity {
    LinearLayout change_frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_land);
        change_frag = findViewById(R.id.change_frag);

        set_up_action_and_status_bar();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.change_frag, new frag_land_owner()).commit();

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