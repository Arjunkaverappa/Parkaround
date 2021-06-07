package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    LinearLayout frag;
    BubbleNavigationConstraintView bottombar;
    Boolean is_connected = true;
    long back_pressed, TIME_INTERVAL_BACK = 2000;
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    public static final String LAND_ADDRESS = "com.ka12.parkaround.this_is_where_address_of_the_land_is_saved";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    //to check booking
    String user_phone_number, user_address;
    //the following are in the booking custom view
    TextView cancel_btn, vehicle_name, vehicle_number, vehicle_timings, btn_next;
    CardView booking_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        bottombar = findViewById(R.id.bottombar);
        frag = findViewById(R.id.frag);
        //this is for now booking arrives
        cancel_btn = findViewById(R.id.cancel_btn);
        vehicle_name = findViewById(R.id.vehicle_name);
        vehicle_number = findViewById(R.id.vehicle_number);
        vehicle_timings = findViewById(R.id.vehicle_timings);
        btn_next = findViewById(R.id.btn_next);
        booking_layout = findViewById(R.id.booking_layout);
        //hiding the booking layout
        booking_layout.setVisibility(View.GONE);

        set_up_action_and_status_bar();

        //checking if the location services are enabled or not (currently disabled)
        //check_permission();

        //checking if the internet service is enabled or not
        check_network();

        //setting up bottom navigationbar to the middle element
        bottombar.setCurrentActiveItem(1);

        //getting the maps fragment and setting-it as default
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frag, new MapsFragment()).commit();
        bottombar.setNavigationChangeListener((view, position) -> {
            switch (position) {
                case 0:
                    vibrate_phone();
                    //getting the activity fragment
                    FragmentManager activity = getSupportFragmentManager();
                    activity.beginTransaction().remove(new MapsFragment())
                            .remove(new frag_land_owner())
                            .replace(R.id.frag, new frag_services())
                            .commit();
                    break;
                case 1:
                    vibrate_phone();
                    //getting the maps fragment
                    FragmentManager explore = getSupportFragmentManager();
                    explore.beginTransaction().remove(new frag_services())
                            .remove(new frag_land_owner())
                            .replace(R.id.frag, new MapsFragment())
                            .commit();
                    break;
                case 2:
                    vibrate_phone();
                    //getting the settings fragment
                    FragmentManager settings = getSupportFragmentManager();
                    settings.beginTransaction().remove(new MapsFragment())
                            .remove(new frag_land_owner())
                            .replace(R.id.frag, new frag_profile())
                            .commit();
                    break;
            }
        });
        //checking for new booking
        check_for_bookings();
    }

    public void vibrate_phone() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(30);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking the condition
        Log.e("mymap", "onRequestPermissionResult");
        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            Log.e("mymap", "permission granted in onRquestPermissionResult");
        }
    }

    public void check_network() {
        try {
            Log.d("zoom", "checking network");
            new Handler().postDelayed(() ->
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo data_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if ((wifi_conn != null && wifi_conn.isConnected()) || (data_conn != null && data_conn.isConnected())) {
                    is_connected = true;
                } else {
                    is_connected = false;
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle("Disclaimer");
                    b.setMessage("Please check your network connection");
                    b.setPositiveButton("OK", (dialog, which) -> {
                        //here is the set positive button

                    });
                    b.show();
                    //check_network();
                }
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void check_for_bookings() {

        //getting user_phone number
        SharedPreferences get_number = Objects.requireNonNull(getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE));
        user_phone_number = get_number.getString("phone", "9977997795");

        //getting user land address if present
        SharedPreferences set_address = getSharedPreferences(LAND_ADDRESS, Context.MODE_PRIVATE);
        user_address = set_address.getString("user_address", "null");

         /* these are the details obtained from the firebase upon booking done
            0) vehicle number
            1) vehicle model
            2) manufacturer
            3) color
            4) user address
            5) time start
            6) time end
         */
        //checking if any booking has arrived
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("BOOKINGS");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                /*
                // String key=snapshot.getKey();
                String data = snapshot.getValue(String.class);
                if (data != null) {
                    String[] split = data.split("\\#");
                    //comparing address
                    if (user_address.equals(split[4])) {
                        Log.d("tempo", data);
                        show_booking_arrived(data);
                    }
                }
                 */

                String key = snapshot.getKey();
                Log.e("temp", key + " and user phone : " + user_phone_number);
                if (user_phone_number.equals(key)) {
                    String data = snapshot.getValue(String.class);
                    if (data != null) {
                        //setting up booking arrived cardview
                        Log.e("temp", data);
                        show_booking_arrived(data);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                check_for_bookings();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                check_for_bookings();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                check_for_bookings();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                check_for_bookings();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void show_booking_arrived(String data) {
         /* these are the details obtained from the firebase upon booking done
            0) vehicle number
            1) vehicle model
            2) manufacturer
            3) color
            4) user address
            5) time start
            6) time end
            7) booking_guy_phone_number(key)
         */
        Log.d("tempo", "running");
        booking_layout.setVisibility(View.VISIBLE);
        String[] split = data.split("\\#");
        vehicle_name.setText("Vehicle  :" + split[2] + " " + split[1] + " (" + split[3] + ")");
        vehicle_number.setText("Number :" + split[0]);
        vehicle_timings.setText("Timings :From " + split[5] + " To " + split[6]);

        //booking cancel
        cancel_btn.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setTitle("Disclaimer");
            b.setMessage("Do you want to cancel this booking request?");
            b.setPositiveButton("yes", (dialog, which) -> {
                //Remove the booking from firebase
                booking_layout.setVisibility(View.GONE);
                cancel_the_booking(split[7]);
            });
            b.setNegativeButton("No", (dialog, which) -> {
            });
            b.show();
        });
        //booking confirm
        btn_next.setOnClickListener(v -> {
            //go next
            Toast.makeText(MainActivity.this, "submit", Toast.LENGTH_SHORT).show();
        });
    }

    public void cancel_the_booking(String phone_number) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("BOOKINGS").child(phone_number);
        reference.removeValue().addOnCompleteListener(task -> {
            //node deleted
            Toast.makeText(MainActivity.this, "Booking canceled", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            //error handler
            Toast.makeText(MainActivity.this, "ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + TIME_INTERVAL_BACK > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }
    /*
    //currently not used
    public void check_permission() {
        //checking permission if the location is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted, asking for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

     */
}
