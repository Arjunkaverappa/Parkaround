package com.ka12.parkaround;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class Splash_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //delaying the next activity by 2 seconds
        new Handler().postDelayed(() -> {
            Intent in = new Intent(Splash_screen.this, MainActivity.class);
            startActivity(in);
            Animatoo.animateZoom(Splash_screen.this);
            finish();
        }, 1000);
    }
}