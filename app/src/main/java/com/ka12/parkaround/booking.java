package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mcsoft.timerangepickerdialog.RangeTimePickerDialog;

import java.util.Objects;

import ng.max.slideview.SlideView;

public class booking extends AppCompatActivity implements RangeTimePickerDialog.ISelectedTime {
    public static final String IS_VEHICLE_ADDED = "com.ka12.this_is_where_boolean_of_is_vehicle_added_is_saved";
    public static final String CLICK_DATA = "com.ka12.this_is_where_boolean_of_is_vehicle_added_is_saved";
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    public static final String USER_VEHICLE = "com.ka12.this_is_where_user_vehicle_is_saved";
    TextView address, pricing, display_time, total_price;
    CardView set_time;
    LinearLayout change_layout, original_layout;
    Boolean is_vehicle_added;
    String data_from_previous_activity;
    Integer location_price;
    SlideView swipe;
    //firebaseDatabase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    String user_phone_number, user_address, user_time_start, user_time_end, user_vehicle_details;
    Boolean was_time_range_selected = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        change_layout = findViewById(R.id.change_layout);
        setContentView(R.layout.activity_booking);
        address = findViewById(R.id.address);
        original_layout = findViewById(R.id.original_layout);
        pricing = findViewById(R.id.pricing);
        set_time = findViewById(R.id.set_time);
        display_time = findViewById(R.id.display_time);
        total_price = findViewById(R.id.total_price);
        swipe = findViewById(R.id.swipe);

        //setting up action bar
        set_up_action_and_status_bar();

        //testing //remove this later
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
            //spit the text and set up textviews
            set_up_text_views(data_from_previous_activity);
        } else {
            //the vehicle is not added, show  a different fragment
            original_layout.setVisibility(View.GONE);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.change_layout, new frag_add_vehicle())
                    .commit();
        }
        set_time.setOnClickListener(v -> {

            RangeTimePickerDialog dialog = new RangeTimePickerDialog();
            dialog.newInstance();
            dialog.setRadiusDialog(50); // Set radius of dialog (default is 50)
            dialog.setIs24HourView(false); // Indicates if the format should be 24 hours
            dialog.setColorBackgroundHeader(R.color.colorPrimary); // Set Color of Background header dialog
            dialog.setColorTextButton(R.color.colorPrimaryDark); // Set Text color of button
            android.app.FragmentManager fragmentManager = getFragmentManager();
            dialog.show(fragmentManager, "");
        });

        swipe.setOnSlideCompleteListener(slideView ->
        {
            if (was_time_range_selected) {

                Vibrator v = (Vibrator) Objects.requireNonNull(getSystemService(Context.VIBRATOR_SERVICE));
                v.vibrate(65);
                confrim_booking();
            } else {
                Toast.makeText(this, "Please set the timings!!", Toast.LENGTH_SHORT).show();
            }
        });

        //hiding total price for now
        total_price.setVisibility(View.GONE);
    }

    public void confrim_booking() {

        //getting user_phone number
        SharedPreferences get_number = Objects.requireNonNull(getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE));
        user_phone_number = get_number.getString("phone", "9977997795");

        //getting user car details
        SharedPreferences get_vehicle = getSharedPreferences(USER_VEHICLE, Context.MODE_PRIVATE);
        user_vehicle_details = get_vehicle.getString("user_vehicle", "something went wrong");

        //the final input sent to firebase
        String input_text = user_vehicle_details + "#" + user_address + "#" + user_time_start + "#" + user_time_end + "#" + user_phone_number;

         /*
            0) vehicle number
            1) vehicle model
            2) manufacturer
            3) color
            4) user address
            5) time start
            6) time end
            7) booking_guy_phone_number(key)
         */

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("BOOKINGS");
        reference.child(user_phone_number).setValue(input_text).addOnCompleteListener(task -> {
            //update the is_active_status in firebase for revelent field
            //this is to change the active status of the location booked, firstly get the address
            get_the_address();
        }).addOnFailureListener(e -> {
            //on failure listener
            Toast.makeText(booking.this, "Something went wrong : ERROR :" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    public void change_the_values_in_database(String user_address_from_firebase) {
        //setting active status to busy
        if (user_address_from_firebase != null) {
            String[] split = user_address_from_firebase.split("\\#");
            firebaseDatabase = FirebaseDatabase.getInstance();
            reference = firebaseDatabase.getReference().child("LOCATIONS").child(user_phone_number);
            reference.setValue(split[0] + "#" + split[1] + "#" + split[2] + "#" + "busy" + "#" + split[4])
                    .addOnCompleteListener(task ->
                            Toast.makeText(this, "Location booked!", Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(this, "NullPointerException from firebase...", Toast.LENGTH_SHORT).show();
        }
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

        //save the data to upload onto firebase if the user decides to book the location
        user_address = split[2];
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onSelectedTime(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        //calculating price
        int total_hours = hourEnd - hourStart;

        String timeString = hourStart + ":" + minuteStart + " - " + hourEnd + ":" + minuteEnd;
        display_time.setText(timeString);

        total_price.setVisibility(View.VISIBLE);
        total_price.setText("Duration : " + total_hours + " hours\nTotal price : ₹" + total_hours * location_price + " ");

        //for firebase
        was_time_range_selected = true;
        user_time_start = hourStart + ":" + minuteStart;
        user_time_end = hourEnd + ":" + minuteEnd;
    }

    public void get_the_address() {
        Log.e("set_map", "initiated with " + user_phone_number);
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("LOCATIONS");//.child(user_phone_number.trim());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String data_from_firebase = snapshot.getValue(String.class);
                if (Objects.equals(snapshot.getKey(), user_phone_number)) {
                    Log.e("set_map", "data from firebase :" + data_from_firebase);
                    //copy the data into another string
                    change_the_values_in_database(data_from_firebase);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                get_the_address();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                get_the_address();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                get_the_address();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                get_the_address();
            }
        });
    }
}