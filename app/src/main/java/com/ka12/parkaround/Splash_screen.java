package com.ka12.parkaround;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

public class Splash_screen extends AppCompatActivity {
    public static final String LOGIN = "com.ka12.parkaround.this_is_where_login_details_are_saved";
    Boolean is_logged_in;

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
        assert ab != null;
        ab.hide();

        //getting the login details
        SharedPreferences getlogin = getSharedPreferences(LOGIN, MODE_PRIVATE);
        is_logged_in = getlogin.getBoolean("is_login", true);

        //delaying the next activity by 2 seconds
        new Handler().postDelayed(() -> {
            Intent in;
            if (is_logged_in) {
                in = new Intent(Splash_screen.this, MainActivity.class);
            } else {
                in = new Intent(Splash_screen.this, login.class);
            }
            startActivity(in);
            Animatoo.animateZoom(Splash_screen.this);
            finish();
        }, 1500);
    }
}