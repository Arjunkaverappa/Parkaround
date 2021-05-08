package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import ng.max.slideview.SlideView;

public class frag_land_owner extends Fragment {
    public static final String IS_LAND_ADDED = "com.ka12.parkaround.this_is_where_boolean_of_is_place_added_is_saved";
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    Boolean is_place_added;
    String user_phone_number;
    CardView host_card;
    //firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    //ui
    TextView address, active_status;
    SlideView swipe;
    Boolean is_active;
    String data_from_firebase;
    Boolean is_connected = true, is_map_ready = false;
    //google map
    GoogleMap mymap;
    Double user_latitude, user_longitude;
    String get_address, user_address_from_firebase = "";
    public final OnMapReadyCallback onMapReadyCallback = googleMap -> {
        mymap = googleMap;
        is_map_ready = true;
        get_the_address();
    };

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_land_owner, container, false);
        host_card = v.findViewById(R.id.host_card);
        address = v.findViewById(R.id.address);
        swipe = v.findViewById(R.id.swipe);
        active_status = v.findViewById(R.id.active_status);

        //checking if the place is added by the land owner or not
        SharedPreferences get_place = Objects.requireNonNull(getActivity()).getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE);
        is_place_added = get_place.getBoolean("is_added", false);

        //TODO add this preference to the add land activity
        //show another fragment to take the details
        if (!is_place_added) {
            //frag_add_land is a decendent of frag_land_owner
            host_card.setVisibility(View.GONE);
            swipe.setVisibility(View.GONE);
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction()
                    .remove(new frag_land_owner())
                    .replace(R.id.host_fragment, new frag_add_land())
                    .commit();
        } else {
            //retrieving the phone number
            SharedPreferences get_number = Objects.requireNonNull(getActivity()).getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
            user_phone_number = get_number.getString("phone", "9977997795");

            //fetching the corresponding address from firebase is done in OnMapReady callback
        }

        //TODO enable this
        // check_network();

        swipe.setOnSlideCompleteListener(slideView ->
        {
            if (is_active) {
                vibrate_phone();
                swipe.setText("Swipe to disable location");
                //if active change to inactive state
                swipe.setSlideBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EF6767")));
                //changing the field in database
                change_the_is_active_status(false);
                is_active = false;
                //setting up textview
                active_status.setText("Currently Inactive");
                active_status.setTextColor(Color.RED);
            } else {
                vibrate_phone();
                swipe.setText("Swipe to enable location");
                swipe.setSlideBackgroundColor(ColorStateList.valueOf(Color.parseColor("#66BB6A")));
                //if not active, change to active state
                change_the_is_active_status(true);
                is_active = true;
                //setting up textview
                active_status.setText("Currently Active");
                active_status.setTextColor(Color.GREEN);
            }
        });

        return v;
    }

    public void vibrate_phone() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
    }

    public void change_the_is_active_status(Boolean change_to) {
        String result;
        if (change_to) {
            result = "yes";
        } else {
            result = "no";
        }
        if (user_address_from_firebase != null && is_connected) {
            String[] split = user_address_from_firebase.split("\\#");
            firebaseDatabase = FirebaseDatabase.getInstance();
            reference = firebaseDatabase.getReference().child("LOCATIONS").child(user_phone_number);
            reference.setValue(split[0] + "#" + split[1] + "#" + split[2] + "#" + result)
                    .addOnCompleteListener(task ->
                            Toast.makeText(getActivity(), "Location set successfully", Toast.LENGTH_SHORT).show());

        } else {
            Toast.makeText(getActivity(), "NullPointerException from firebase...", Toast.LENGTH_SHORT).show();
        }

    }

    public void get_the_address() {
        Log.e("set_map", "initiated with " + user_phone_number);
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("LOCATIONS");//.child(user_phone_number.trim());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                data_from_firebase = snapshot.getValue(String.class);
                if (snapshot.getKey().equals(user_phone_number)) {
                    Log.e("set_map", "data from firebase :" + data_from_firebase);
                    //copy the data into another string
                    user_address_from_firebase = data_from_firebase;
                    show_address_in_ui(data_from_firebase);
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

    @SuppressLint("SetTextI18n")
    public void show_address_in_ui(String user_address) {
        // 0->latitue
        // 1->longitude
        // 2->final_address
        // 3->is_active

        String[] split = user_address.split("\\#");
        address.setText(split[2]);

        //setting the location in google map

        user_latitude = Double.parseDouble(split[0]);
        user_longitude = Double.parseDouble(split[1]);

        if (is_map_ready) {
            Log.e("set_map", "callback initiated");
            LatLng mylocation = new LatLng(user_latitude, user_longitude);
            mymap.addMarker(new MarkerOptions().position(mylocation).title(get_address));
            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 14));
        }

        get_address = split[2];

        is_active = split[3].equals("yes");
        if (is_active) {
            swipe.setSlideBackgroundColor(ColorStateList.valueOf(Color.parseColor("#66BB6A")));
            swipe.setText("Swipe to disable location");
            active_status.setText("Currently Active");
            active_status.setTextColor(Color.GREEN);
        } else {
            swipe.setText("Swipe to enable location");
            swipe.setSlideBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EF6767")));
            active_status.setText("Currently Inactive");
            active_status.setTextColor(Color.RED);
        }
    }

    public void check_network() {
        //TODO set up wherever required
        try {
            Log.d("zoom", "checking network");
            new Handler().postDelayed(() ->
            {
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifi_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                NetworkInfo data_conn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if ((wifi_conn != null && wifi_conn.isConnected()) || (data_conn != null && data_conn.isConnected())) {
                    is_connected = true;
                } else {
                    is_connected = false;
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle("Disclaimer");
                    b.setMessage("Please connect to internet");
                    b.setPositiveButton("OK", (dialog, which) -> {
                        //here is the set positive button

                    });
                    b.show();
                    check_network();
                }
            }, 4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.e("set_map", "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.owner_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(onMapReadyCallback);
        }
    }
}