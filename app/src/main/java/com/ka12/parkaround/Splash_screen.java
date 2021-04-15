package com.ka12.parkaround;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class Splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //changing status bar color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // window.setStatusBarColor(Color.parseColor("#6ABEDF"));

        //hiding the action bar
        ActionBar ab = getSupportActionBar();
        ab.hide();

        //delaying the next activity by 2 seconds
        new Handler().postDelayed(() -> {
            Intent in = new Intent(Splash_screen.this, login.class);
            startActivity(in);
            Animatoo.animateZoom(Splash_screen.this);
            finish();
        }, 1000);
    }
}