package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.mahdizareei.mztimepicker.MZTimePicker;
import com.mahdizareei.mztimepicker.models.TimeModel;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Locale;

public class booking extends AppCompatActivity {
    public static final String IS_VEHICLE_ADDED = "com.ka12.this_is_where_boolean_of_is_vehicle_added_is_saved";
    public static final String CLICK_DATA = "com.ka12.this_is_where_boolean_of_is_vehicle_added_is_saved";
    TextView address, pricing, display_time, total_price;
    CardView book_location, set_time;
    LinearLayout change_layout, original_layout;
    Boolean is_vehicle_added;
    String data_from_previous_activity;
    Integer location_price;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        change_layout = findViewById(R.id.change_layout);
        setContentView(R.layout.activity_booking);
        address = findViewById(R.id.address);
        original_layout = findViewById(R.id.original_layout);
        book_location = findViewById(R.id.book_location);
        pricing = findViewById(R.id.pricing);
        set_time = findViewById(R.id.set_time);
        display_time = findViewById(R.id.display_time);
        total_price = findViewById(R.id.total_price);

        //setting up action bar
        set_up_action_and_status_bar();

        //testing
        address.setOnLongClickListener(v -> {
            Toast.makeText(this, "reset", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor set_data = getSharedPreferences(CLICK_DATA, MODE_PRIVATE).edit();
            set_data.putString("prev_data", "").apply();
            SharedPreferences.Editor set_boolean = getSharedPreferences(IS_VEHICLE_ADDED, Context.MODE_PRIVATE).edit();
            set_boolean.putBoolean("is_vehicle", false).apply();
            return false;
        });

        //saving the  data in shared preferences
        Intent in = getIntent();
        data_from_previous_activity = in.getStringExtra("details");

        if (data_from_previous_activity != null) {
            SharedPreferences.Editor set_data = getSharedPreferences(CLICK_DATA, MODE_PRIVATE).edit();
            set_data.putString("prev_data", data_from_previous_activity).apply();
        }

        //checking if vehicle is added
        SharedPreferences get_vehicle = getSharedPreferences(IS_VEHICLE_ADDED, MODE_PRIVATE);
        is_vehicle_added = get_vehicle.getBoolean("is_vehicle", false);

        if (is_vehicle_added) {
            SharedPreferences get_data = getSharedPreferences(CLICK_DATA, MODE_PRIVATE);
            data_from_previous_activity = get_data.getString("prev_data", "null");

            Log.e("loggy", data_from_previous_activity);
            set_up_text_views(data_from_previous_activity);
        } else {
            original_layout.setVisibility(View.GONE);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.change_layout, new add_vehicle())
                    .commit();
        }

        set_time.setOnClickListener(v ->

                new MZTimePicker(booking.this)
                        .setTabColor(Color.DKGRAY)
                        .setConfirmButtonColor((getResources().getColor(R.color.black)))
                        .setDeleteButtonColor((getResources().getColor(R.color.black)))
                        .setConfirmTextColor((getResources().getColor(R.color.white)))
                        .setDeleteTextColor((getResources().getColor(R.color.white)))
                        .BuildTimePicker((time1, time2) -> {
                            //select process
                            String timeString = time1.getHour() + ":" + time1.getMinute() + " - " + time2.getHour() + ":" + time2.getMinute();
                            if (check_if_time_is_valid(time1, time2)) {
                                display_time.setText(timeString);
                            } else {
                                display_time.setText("Add timings");
                            }
                        }));

        //hiding total price
        total_price.setVisibility(View.GONE);

        book_location.setOnClickListener(v -> {
            //update the following in database
            //relevent functions
        });
    }

    @SuppressLint("SetTextI18n")
    public Boolean check_if_time_is_valid(TimeModel time1, TimeModel time2) {
        //getting system time
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.US);
        String str = sdf.format(new Date().getHours());
        int hour_now = Integer.parseInt(str);
        int min_now = LocalTime.now().getMinute();

        Log.d("date", str);

        //getting user start time
        int user_hour_one = Integer.parseInt(time1.getHour());
        int user_min_one = Integer.parseInt(time1.getMinute());
        //getting user end time
        int user_hour_two = Integer.parseInt(time2.getHour());
        int user_min_two = Integer.parseInt(time2.getMinute());

        if (hour_now < user_hour_one) {
            Toast.makeText(this, "Time is lesser than current time", Toast.LENGTH_SHORT).show();
            return false;
        } else if (user_hour_one > user_hour_two) {
            Toast.makeText(this, "Please check the time", Toast.LENGTH_SHORT).show();
            return false;
        }
        //calculating price
        int total_hours = user_hour_two - user_hour_one;
        total_price.setVisibility(View.VISIBLE);
        total_price.setText("Total price : ₹" + total_hours * location_price);//+"\nDuration : "+total_hours+" hours");
        return true;

    }


    @SuppressLint("SetTextI18n")
    public void set_up_text_views(String data) {
        // 0->latitue
        // 1->longitude
        // 2->final_address
        // 3->is_active
        // 4->pricing
        String[] split = data.split("\\#");
        address.setText(split[2]);
        pricing.setText("₹" + split[4] + "/hour");
        location_price = Integer.parseInt(split[4]);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(booking.this, MainActivity.class));
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