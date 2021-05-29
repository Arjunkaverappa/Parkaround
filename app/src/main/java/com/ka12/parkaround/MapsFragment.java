package com.ka12.parkaround;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;

/*
   follow the order of data in firebase
 */
public class MapsFragment extends Fragment {
    public static final String MAP_TYPE = "com.ka12.parkaround.this_is_where_map_type_is_saved";
    public GoogleMap mymap;
    public FusedLocationProviderClient fusedLocationProviderClient;
    public Double user_latitude, user_longitude;
    RelativeLayout host_map;
    FABsMenu fab;
    //firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    //following array lists saves the temprory data from the firebase
    ArrayList<String> availabe_locations = new ArrayList<>();
    ProgressBar progressBar;
    Integer count = 0;
    //recycler view
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    public final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NotNull GoogleMap googleMap) {
            Log.e("mymap", "OnMapReadyCallBack initiated");
            mymap = googleMap;
            //retrieving the maps activity
            //TODO : reset the preference while exiting the app
            SharedPreferences get_map_type = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE);
            switch (get_map_type.getString("type", "none")) {
                case "satellite":
                    mymap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case "terrain":
                    mymap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case "hybrid":
                    mymap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                default:
                    mymap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

            mymap.setOnMarkerClickListener(marker -> {
                String which_marker = marker.getTitle();
                for (int i = 0; i < availabe_locations.size(); i++) {
                    String[] split_it = availabe_locations.get(i).split("\\#");
                    if (which_marker != null && which_marker.equals(split_it[2])) {
                        //setting up onlick listeners
                        Intent go_to_booking = new Intent(getActivity(), booking.class);
                        go_to_booking.putExtra("details", availabe_locations.get(i));
                        startActivity(go_to_booking);
                        Animatoo.animateZoom(Objects.requireNonNull(getContext()));
                    }
                }
                return true;
            });
            //mymap.setIndoorEnabled(true);
            // mymap.setBuildingsEnabled(true);
            check_permission();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        fab = v.findViewById(R.id.fab);
        host_map = v.findViewById(R.id.host_map);
        recyclerView = v.findViewById(R.id.recycler_view);
        progressBar = v.findViewById(R.id.progress_bar);
        TitleFAB hybrid = v.findViewById(R.id.first_icon);
        TitleFAB terrian_mode = v.findViewById(R.id.second_icon);
        TitleFAB satellite_mode = v.findViewById(R.id.third_icon);
        TitleFAB noraml = v.findViewById(R.id.normal);
        noraml.setOnClickListener(view -> {
            SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
            setMap.putString("type", "noramal").apply();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.frag, new MapsFragment()).commit();
        });
        satellite_mode.setOnClickListener(view ->
                {
                    SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
                    setMap.putString("type", "satellite").apply();
                    mymap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    fab.collapse();
                }
        );
        terrian_mode.setOnClickListener(view ->
                {
                    SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
                    setMap.putString("type", "terrain").apply();
                    mymap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    fab.collapse();
                }
        );
        hybrid.setOnClickListener(view -> {
            SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
            setMap.putString("type", "hybrid").apply();
            mymap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            fab.collapse();
        });
        Log.e("mymap", "initializing onCreate ");

        initilise_recycler_view();
        return v;
    }

    public void initilise_recycler_view() {
        Log.e("recycler", "initilise recycler view ");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        //sending list of locations to recyclerview
        recyclerViewAdapter = new RecyclerViewAdapter(availabe_locations, getActivity());
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @SuppressLint("MissingPermission")
    public void fetch_the_location() {
        Log.e("result", "fetch the location initiated");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        //initialising the location manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //checking condition for what type of location service is provided or not
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.e("result", "GPS is enabled");
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
                } else {
                    //when location is null we initialize location request
                    //this is where the actual location is fetched from
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(3000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            Location new_location = locationResult.getLastLocation();
                            user_latitude = new_location.getLatitude();
                            user_longitude = new_location.getLongitude();
                            Log.e("mymap", "latitude : " + user_latitude + "\nlongitude : " + user_longitude);
                        }
                    };
                    //finally requesting the location update
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
                //fetching the location from firebase
                get_location_from_firebase();
                mymap.setMyLocationEnabled(true);
            });
        } else {
            open_settings_and_turn_on_location();
        }
    }

    public void check_permission() {
        Log.d("result", "running check permission");
        //checking permission if the location permissin is granted
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            fetch_the_location();
            //  get_location_from_firebase();
        } else {
            //ask for the permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            //check the on request permisiion result
            //Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            //we shall use loop to check for permission repeatedly
            new Handler().postDelayed(this::check_permission, 7000);
        }
    }

    public void open_settings_and_turn_on_location() {
        Log.e("result", "open_settings_and_turn_on_location");
        //this is when the location service is not enabled
        LocationSettingsRequest mLocationSettingsRequest;
        SettingsClient mSettingsClient = new SettingsClient(Objects.requireNonNull(getContext()));

        LocationSettingsRequest.Builder request_location = new LocationSettingsRequest.Builder();
        request_location.addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
        request_location.setAlwaysShow(true);
        mLocationSettingsRequest = request_location.build();

        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnCompleteListener(task -> {
                    //successful
                    //this means the dialog has been displayed
                    Log.e("result", "okkk");
                }).addOnFailureListener(e -> {
            //failed
            if (e instanceof ResolvableApiException) {
                //location is not obtained, but can be fixed by showing a dialog
                try {
                    ResolvableApiException r = (ResolvableApiException) e;
                    startIntentSenderForResult(r.getResolution().getIntentSender(), 500, null, 0, 0, 0, null);
                } catch (Exception ex) {
                    //nothing can be done
                }
            }

        }).addOnCanceledListener(() -> {
            //user has cancled
            Toast.makeText(getActivity(), "Cancled", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO :onActivity Result is called spontainously, fix it
        if (requestCode == 500) {
            if (Activity.RESULT_OK == resultCode) {
                Log.e("result", "request code 500");
                fetch_the_location();
            } else if (Activity.RESULT_CANCELED == resultCode) {
                //user canceled
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 100);
            }
        } else if (requestCode == 100) {
            LocationManager locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGpsEnabled) {
                Log.e("result", "request code 100 not enabled");
                Toast.makeText(getActivity(), "Leave!!", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("result", "request code 100");
                fetch_the_location();
            }
        }
    }

    public void get_location_from_firebase() {
        Log.e("result", "initiated get_location_from_database");
        reset_map();
        // 0->latitue
        // 1->longitude
        // 2->final_address
        // 3->is_active
        // 4->user_price
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("LOCATIONS");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String data = snapshot.getValue(String.class);
                Log.d("runit", data);
                //spliting the data from database
                if (data != null) {
                    String[] spliting = data.split("\\#");
                    availabe_locations.add(data);
                    //setting up the latitude and longitude after splitting
                    set_the_marker(Double.parseDouble(spliting[0]), Double.parseDouble(spliting[1]), spliting[2]);
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                get_location_from_firebase();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                get_location_from_firebase();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                get_location_from_firebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                get_location_from_firebase();
            }
        });
    }

    public void set_the_marker(Double lat, Double lon, String address) {
        LatLng mylocation = new LatLng(lat, lon);
        mymap.addMarker(new MarkerOptions()
                .position(mylocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(address));
        mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 13));
    }

    public void reset_map() {
        progressBar.setVisibility(View.GONE);
        Log.e("recycler", "reset initiated");
        count = 0;
        availabe_locations.clear();

        //NOTE: mymap variable is not an arraylist, its a GoogleMap instance
        mymap.clear();
        /*
        if (user_longitude != null && user_latitude != null) {
            LatLng mylocation = new LatLng(user_latitude, user_longitude);
            mymap.addMarker(new MarkerOptions()
                    .position(mylocation)
                    .title("My location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 12));
        }

         */
    }
}
/*
   to change the color of the marker(we can use custom icon also)
   => googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Manglore").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

 */