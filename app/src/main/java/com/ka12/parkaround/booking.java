package com.ka12.parkaround;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class booking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        //this activity is responsible for bookking the location
        Intent intent = getIntent();
        String data = intent.getStringExtra("details");
        Log.e("loggy", data);
    }
}