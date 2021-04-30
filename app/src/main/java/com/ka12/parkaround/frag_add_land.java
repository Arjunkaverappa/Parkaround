package com.ka12.parkaround;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;

public class frag_add_land extends Fragment {
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    LinearLayout add_dialog, enter_details_layout;
    CardView add_location, enter_location_card, use_location, add_address;
    TextInputEditText road_colony_area, house_or_building, pincode;
    Spinner spinner;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double user_latitude, user_longitude;
    String user_house_or_build, user_road_or_col, city, user_pincode, final_address;
    //database
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    String user_phone_number;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_add_land, container, false);
        add_dialog = v.findViewById(R.id.add_dialog);
        add_location = v.findViewById(R.id.add_location);
        enter_location_card = v.findViewById(R.id.enter_location_card);
        use_location = v.findViewById(R.id.use_location);
        add_address = v.findViewById(R.id.add_address);
        spinner = v.findViewById(R.id.spinner);
        road_colony_area = v.findViewById(R.id.road_colony_area);
        house_or_building = v.findViewById(R.id.house_or_building);
        enter_details_layout = v.findViewById(R.id.enter_details_layout);
        pincode = v.findViewById(R.id.pincode);

        //hiding the enter location card
        enter_location_card.setVisibility(View.GONE);

        //setting up adapter for spinner
        String[] cities = new String[]{"Mangalore", "Bangalore", "Mysore", "Hubli", "Kodagu"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, cities);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        city = "Mangalore";
                        break;
                    case 1:
                        city = "Bangalore";
                        break;
                    case 2:
                        city = "Mysore";
                        break;
                    case 3:
                        city = "Hubli";
                        break;
                    case 4:
                        city = "Kodagu";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                city = "Mangalore";
            }
        });

        add_location.setOnClickListener(v1 -> {
            //remove the add dialog and show the main layout
            add_dialog.setVisibility(View.GONE);
            enter_location_card.setVisibility(View.VISIBLE);
        });

        use_location.setOnClickListener(v12 -> {
            //get the location service running or get from sharedPreferences
            Toast.makeText(getActivity(), "Fetching your location!", Toast.LENGTH_SHORT).show();
            check_permission();
        });

        add_address.setOnClickListener(v13 -> {
            //the details will be taken here
            if (!Objects.requireNonNull(road_colony_area.getText()).toString().equals("")
                    || !Objects.requireNonNull(house_or_building.getText()).toString().equals("")
                    || !Objects.requireNonNull(pincode.getText()).toString().equals("")) {
                user_house_or_build = Objects.requireNonNull(house_or_building.getText()).toString().trim();
                user_road_or_col = road_colony_area.getText().toString().trim();
                user_pincode = Objects.requireNonNull(pincode.getText()).toString().trim();
                //we have city in spinner
                final_address = user_house_or_build + ", " + user_road_or_col + ", " + city + ", karnataka. " + user_pincode;
                Log.d("land", final_address);
                // get_the_data_into_database();
                get_location_by_address(final_address);
            } else {
                Toast.makeText(getActivity(), "Enter all the details", Toast.LENGTH_SHORT).show();
            }

        });

        return v;
    }

    public void check_permission() {
        //checking permission if the location permissin is granted
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            Log.e("fetch", "permission is granted");
            fetch_the_location();
        } else {
            //ask for the permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            //check the on request permisiion result
            Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            //we shall use loop to check for permission repeatedly
            new Handler().postDelayed(this::check_permission, 7000);
        }
    }

    @SuppressLint("MissingPermission")
    public void fetch_the_location() {
        Log.e("fetch", "fetch the location initiated");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        //initialising the location manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //checking condition for what type of location service is provided or not
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.e("fetch", "GPS is enabled");
            //the location service is enabled
            //getting the last location . (we have disabled inspection for this method)
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task ->
            {
                //getting the co-ordinates if there was a previous location saved
                Location location = task.getResult();
                if (location != null) {
                    //this method gives the previous location
                    user_latitude = location.getLatitude();
                    user_longitude = location.getLongitude();
                    //this method shouldnt be called here, done for testing purpose only
                    get_the_data_into_database();
                    Log.e("fetch", "lat :" + user_latitude + "\nlong :" + user_longitude + " obtained from last location");
                } else {
                    //when location is null we initialize location request
                    //this is where the actual location is fetched from
                    Log.e("location", "requested");
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(6000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            Location new_location = locationResult.getLastLocation();
                            user_latitude = new_location.getLatitude();
                            user_longitude = new_location.getLongitude();
                            Log.e("fetch", "lati : " + user_latitude + " \n long : " + user_longitude + " fresh location");
                            //this method shouldnt be called here, done for testing purpose only
                            get_the_data_into_database();
                        }
                    };
                    //finally requesting the location update
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            });
        } else {
            //this is when the location service is not enabled
            //this will open the location settings
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle("Disclaimer");
            b.setMessage("Please turn on the location services in order to display the locations.\n" +
                    "Tap on OK to open settings");
            b.setPositiveButton("OK", (dialog, which) ->
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 500));
            b.setNeutralButton("Cancel", (dialog, which) -> {
            });
            b.show();
        }
    }

    public void get_the_data_into_database() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("LOCATIONS");

        //retrievingthe phone number of the user from shared preferences
        SharedPreferences get_number = Objects.requireNonNull(getActivity()).getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
        user_phone_number = get_number.getString("phone", "9977997799");
        /*
        //loading the data into helperclass
        helperclass help = new helperclass();
        help.setLatitude(user_latitude);
        help.setLongitude(user_longitude);
        help.setIs_active("yes");


        reference.child(user_phone_number).setValue(help).addOnCompleteListener(task ->
        {
            //success tasks
            Toast.makeText(getActivity(), "SUCCESS", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            //failure message
            Toast.makeText(getActivity(), "FAILED:" + e.getMessage(), Toast.LENGTH_LONG).show();
        });

         */

        String final_loc = user_latitude + "#" + user_longitude + "#yes";
        reference.child(user_phone_number).setValue(final_loc).addOnCompleteListener(task ->
        {
            //success tasks
            Toast.makeText(getActivity(), "SUCCESS", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {
            //failure message
            Toast.makeText(getActivity(), "FAILED:" + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    public void get_location_by_address(String address) {
        Geocoder coder = new Geocoder(getContext());
        List<Address> get_location;
        try {
            get_location = coder.getFromLocationName(address, 5);
            Address location = get_location.get(0);
            for (int i = 0; i < get_location.size(); i++) {
                Log.e("address", get_location.get(i) + "\n");
            }
            user_latitude = location.getLatitude();
            user_longitude = location.getLongitude();
            Log.e("Location", "lat : " + user_latitude + " lon : " + user_longitude);
            get_the_data_into_database();
        } catch (Exception e) {
            Log.e("address", "ERROR :" + e.getMessage());
        }

    }

    public static class helperclass {
        public Double latitude;
        public Double longitude;
        public String is_active;

        //public String address;
        public helperclass() {

        }

        public helperclass(Double latitude, Double longitude, String is_active) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.is_active = is_active;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getIs_active() {
            return is_active;
        }

        public void setIs_active(String is_active) {
            this.is_active = is_active;
        }
    }
}