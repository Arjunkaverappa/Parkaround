package com.ka12.parkaround;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class frag_add_land extends Fragment {
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    public static final String IS_LAND_ADDED = "com.ka12.parkaround.this_is_where_boolean_of_is_place_added_is_saved";
    public static final String LAND_ADDRESS = "com.ka12.parkaround.this_is_where_address_of_the_land_is_saved";
    // public static final String LAND_ADDRESS = "com.ka12.parkaround.this_is_where_parking_address_is_saved";
    LinearLayout add_dialog, enter_details_layout;
    CardView add_location, enter_location_card, add_address;// , use_location;
    TextInputEditText near_to, road_colony_area, house_or_building, pincode, price;
    Spinner spinner;
    FusedLocationProviderClient fusedLocationProviderClient;
    Double user_latitude, user_longitude;
    String user_house_or_build, user_road_or_col, city, user_pincode, final_address, user_phone_number, address_near_to, user_price;
    //database
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    TextView add_address_text;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_add_land, container, false);
        add_dialog = v.findViewById(R.id.add_dialog);
        add_location = v.findViewById(R.id.add_location);
        enter_location_card = v.findViewById(R.id.enter_location_card);
        //use_location = v.findViewById(R.id.use_location);
        add_address = v.findViewById(R.id.add_address);
        spinner = v.findViewById(R.id.spinner);
        road_colony_area = v.findViewById(R.id.road_colony_area);
        house_or_building = v.findViewById(R.id.house_or_building);
        enter_details_layout = v.findViewById(R.id.enter_details_layout);
        pincode = v.findViewById(R.id.pincode);
        near_to = v.findViewById(R.id.near_to);
        price = v.findViewById(R.id.price);
        add_address_text = v.findViewById(R.id.add_address_text);

        //hiding the enter location card
        enter_location_card.setVisibility(View.GONE);

        //setting up adapter for spinner
        String[] cities = new String[]{"City", "Kodagu", "Bangalore", "Mysore", "Hubli", "Mangalore"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, cities);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city = cities[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //TODO: change  it to mangalore
                city = "Kodagu";
            }
        });

        add_location.setOnClickListener(v1 -> {
            //remove the add dialog and show the main layout
            add_dialog.setVisibility(View.GONE);
            enter_location_card.setVisibility(View.VISIBLE);
        });

        add_address.setOnClickListener(v13 ->
        {
            //the details will be taken here
            if (!Objects.requireNonNull(road_colony_area.getText()).toString().equals("")
                    && !Objects.requireNonNull(house_or_building.getText()).toString().equals("")
                    && !Objects.requireNonNull(near_to.getText()).toString().equals("")
                    && !Objects.requireNonNull(pincode.getText()).toString().equals("")
                    && !city.equals("City")
                    && !Objects.requireNonNull(price.getText()).toString().equals("")) {
                address_near_to = Objects.requireNonNull(near_to.getText()).toString().trim();
                user_house_or_build = Objects.requireNonNull(house_or_building.getText()).toString().trim();
                user_road_or_col = road_colony_area.getText().toString().trim();
                user_pincode = Objects.requireNonNull(pincode.getText()).toString().trim();
                user_price = Objects.requireNonNull(price.getText()).toString().trim();

                add_address_text.setText("Loading...");

                final_address = "Near " + address_near_to + ", " + user_house_or_build + ", " + user_road_or_col + ", " + city + ", karnataka. " + user_pincode;
                //the process of adding location starts from checking permission
                check_permission();
            } else {
                Toast.makeText(getActivity(), "Enter all the details", Toast.LENGTH_SHORT).show();
            }

        });

        return v;
    }

    public void check_permission() {
        //checking permission if the location permissin is granted
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
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
                    //location recorded
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
                            //location recorded
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
            //TODO the activity should start again from check permission in order to refetch it after permission is granted
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

        //retrieving the phone number of the user from shared preferences
        SharedPreferences get_number = getActivity().getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
        user_phone_number = get_number.getString("phone", "9977997795");

        String final_loc = user_latitude + "#" + user_longitude + "#" + final_address + "#yes#" + user_price;
        reference.child(user_phone_number).setValue(final_loc).addOnCompleteListener(task ->
        {
            //success tasks
            Toast.makeText(getActivity(), "SUCCESS", Toast.LENGTH_LONG).show();

            //updating the is land added flag in shared preferences
            SharedPreferences.Editor set_added = getActivity().getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE).edit();
            set_added.putBoolean("is_added", true).apply();

            //updating land address in shared preferences
            SharedPreferences.Editor set_address = getActivity().getSharedPreferences(LAND_ADDRESS, Context.MODE_PRIVATE).edit();
            set_address.putString("user_address", final_address).apply();

            //returning to main sceen after adding
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().remove(new frag_add_land()).replace(R.id.host_fragment, new frag_land_owner()).commit();

        }).addOnFailureListener(e -> {
            //failure message
            Toast.makeText(getActivity(), "FAILED:" + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
    /*
    //the below method provides the latitude and longitude of the location from the address provided by user
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

     */

   /*
   //the following  is the helper class for database which is currently not used
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

    */
}