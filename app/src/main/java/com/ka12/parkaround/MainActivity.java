package com.ka12.parkaround;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {
    LinearLayout frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frag = findViewById(R.id.frag);
        ActionBar ab = getSupportActionBar();
        ab.hide();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frag, new MapsFragment()).commit();
    }
}
