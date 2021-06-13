package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.sumimakito.awesomeqr.AwesomeQrRenderer;
import com.github.sumimakito.awesomeqr.RenderResult;
import com.github.sumimakito.awesomeqr.option.RenderOption;
import com.github.sumimakito.awesomeqr.option.logo.Logo;
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
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Objects;

import ng.max.slideview.SlideView;

public class frag_land_owner extends Fragment {
    public static final String IS_LAND_ADDED = "com.ka12.parkaround.this_is_where_boolean_of_is_place_added_is_saved";
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    public static final String LAND_ADDRESS = "com.ka12.parkaround.this_is_where_address_of_the_land_is_saved";

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
        mymap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        is_map_ready = true;
        get_the_address();
    };
    String user_address;
    TextView vehicle_name, vehicle_number, vehicle_timings, no_booking, tv_qr;
    RelativeLayout booking_arrived;
    CardView show_qr;
    ImageView img;
    Boolean is_qr_shown = false;
    RelativeLayout linear_map;

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

        vehicle_name = v.findViewById(R.id.vehicle_name);
        vehicle_number = v.findViewById(R.id.vehicle_number);
        vehicle_timings = v.findViewById(R.id.vehicle_timings);
        booking_arrived = v.findViewById(R.id.booking_arrived);
        no_booking = v.findViewById(R.id.no_booking);
        show_qr = v.findViewById(R.id.show_qr);
        img = v.findViewById(R.id.qr_img);
        linear_map = v.findViewById(R.id.linear_map);
        tv_qr = v.findViewById(R.id.tv_qr);

        //checking if the place is added by the land owner or not
        SharedPreferences get_place = getActivity().getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE);
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
            SharedPreferences get_number = getActivity().getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
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

        //testing... delete  this
        active_status.setOnLongClickListener(v1 -> {
            //checking if the place is added by the land owner or not
            SharedPreferences.Editor get_place1 = getActivity().getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE).edit();
            get_place1.putBoolean("is_added", false).apply();
            Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
            return true;
        });

        show_qr.setOnClickListener(v12 -> {
            //camera
            if (!is_qr_shown) {
                show_qr_code();
                is_qr_shown = true;
                tv_qr.setText("Hide Qr-code");
            } else {
                img.setVisibility(View.GONE);
                linear_map.setVisibility(View.VISIBLE);
                tv_qr.setText("Show Qr-code");
                is_qr_shown = false;
            }
        });

        //hiding booking arrived for now
        booking_arrived.setVisibility(View.GONE);

        //checking for booking
        check_for_bookings();

        return v;
    }

    public void vibrate_phone() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(65);
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
            reference.setValue(split[0] + "#" + split[1] + "#" + split[2] + "#" + result + "#" + split[4])
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
                if (Objects.equals(snapshot.getKey(), user_phone_number)) {
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
            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 18));
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

    public void check_for_bookings() {

        //getting user_phone number
        SharedPreferences get_number = getActivity().getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
        user_phone_number = get_number.getString("phone", "9977997795");

        //getting user land address if present
        SharedPreferences set_address = getActivity().getSharedPreferences(LAND_ADDRESS, Context.MODE_PRIVATE);
        user_address = set_address.getString("user_address", "null");

         /* these are the details obtained from the firebase upon booking done
            0) vehicle number
            1) vehicle model
            2) manufacturer
            3) color
            4) user address
            5) time start
            6) time end
            7) booking guy phone number
         */
        //checking if any booking has arrived
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("BOOKINGS");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
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

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        active_status.setText("Currently occupied");

        no_booking.setVisibility(View.GONE);
        booking_arrived.setVisibility(View.VISIBLE);

        String[] split = data.split("\\#");
        vehicle_name.setText("Vehicle  :" + split[2] + " " + split[1] + " (" + split[3] + ")");
        vehicle_number.setText("Number :" + split[0]);
        vehicle_timings.setText("Timings :From " + split[5] + " To " + split[6]);
    }

    public void show_qr_code() {
        //getting the bitmap
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.a_logo_one);
        //setting up logo
        Logo logo = new Logo();
        logo.setBitmap(icon);
        logo.setBorderRadius(10); // radius for logo's corners
        logo.setBorderWidth(10); // width of the border to be added around the logo
        logo.setScale(0.3f); // scale for the logo in the QR code
        logo.setClippingRect(new RectF(0, 0, 200, 200)); // crop the logo image before applying it to the QR code
        //setting up colors
        com.github.sumimakito.awesomeqr.option.color.Color color = new com.github.sumimakito.awesomeqr.option.color.Color();
        color.setLight(0xFFFFFFFF); // for blank spaces
        color.setDark(Color.BLACK); // for non-blank spaces
        color.setBackground(0xFFFFFFFF); // for the background (will be overriden by background images, if set)
        color.setAuto(false); // set to true to automatically pick out colors from the background image
        //setting up rendering options
        RenderOption renderOption = new RenderOption();
        renderOption.setContent(user_phone_number); // content to encode
        renderOption.setSize(800); // size of the final QR code image
        renderOption.setBorderWidth(15); // width of the empty space around the QR code
        renderOption.setEcl(ErrorCorrectionLevel.M); // (optional) specify an error correction level
        renderOption.setPatternScale(0.35f); // (optional) specify a scale for patterns
        renderOption.setRoundedPatterns(true); // (optional) if true, blocks will be drawn as dots instead
        renderOption.setClearBorder(true); // if set to true, the background will NOT be drawn on the border area
        renderOption.setColor(color); // set a color palette for the QR code
        renderOption.setLogo(logo); // set a logo, keep reading to find more about it

        try {
            RenderResult renderer = AwesomeQrRenderer.render(renderOption);
            if (renderer.getBitmap() != null) {
                img.setImageBitmap(renderer.getBitmap());
                linear_map.setVisibility(View.GONE);
                img.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}