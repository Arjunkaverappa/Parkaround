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
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;

public class MapsFragment extends Fragment {
    public static final String MAP_TYPE = "com.ka12.parkaround.this_is_where_map_type_is_saved";
    public GoogleMap mymap;
    public final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.e("mymap", "OnMapReadyCallBack initiated");
            mymap = googleMap;
            //retrieving the maps activity
            //TODO : reset the preference while exiting the app
            SharedPreferences get_map_type = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE);
            switch (get_map_type.getString("type", "none")) {
                case "satellite":
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case "terrain":
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
                case "hybrid":
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                default:
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            /*
            LatLng sydney = new LatLng(12.16217, 75.84665);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Manglore"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
            Log.e("checking", "just checking");

             */
        }
    };
    //getting the user-location
    FusedLocationProviderClient fusedLocationProviderClient;
    Double user_latitude, user_longitude;
    //my attributes
    RelativeLayout host_map;
    FABsMenu fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        fab = v.findViewById(R.id.fab);
        host_map = v.findViewById(R.id.host_map);
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
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.frag, new MapsFragment()).commit();
                }
        );
        terrian_mode.setOnClickListener(view ->
                {
                    SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
                    setMap.putString("type", "terrain").apply();
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.beginTransaction().replace(R.id.frag, new MapsFragment()).commit();
                }
        );
        hybrid.setOnClickListener(view -> {
            SharedPreferences.Editor setMap = Objects.requireNonNull(getActivity()).getSharedPreferences(MAP_TYPE, Context.MODE_PRIVATE).edit();
            setMap.putString("type", "hybrid").apply();
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.frag, new MapsFragment()).commit();
        });

        check_permission();
        Log.e("mymap", "initializing onCreate ");
        return v;
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

    public void check_permission() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //checking permission if the location permissin is granted
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            fetch_the_location();
        } else {
            //ask for the permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                    , 100);
            //check the on request permisiion result
        }
    }

    @SuppressLint("MissingPermission")
    public void fetch_the_location() {
        Log.e("mymap", "fetch the location initiated");
        //initialising the location manager
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //checking condition for what type of location service is provided
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.e("mymap", "GPS is enabled");
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
                    LatLng sydney = new LatLng(user_latitude, user_longitude);
                    mymap.addMarker(new MarkerOptions().position(sydney).title("Marker in default mode"));
                    mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
                    Log.e("mymap", "Previous location was loaded");
                } else {
                    //when location is null we initialize location request
                    //this is where the actual location is fetched from
                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(9000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            Location new_location = locationResult.getLastLocation();
                            user_latitude = new_location.getLatitude();
                            user_longitude = new_location.getLongitude();
                            Log.e("mymap", "lati : " + user_latitude + " \n long : " + user_longitude);
                            //testing
                            // LatLng sydney = new LatLng(12.16217, 75.84665);
                            LatLng sydney = new LatLng(user_latitude, user_longitude);
                            mymap.addMarker(new MarkerOptions().position(sydney).title("Marker set successfully"));
                            mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
                            Log.e("mymap", "location fetched successfully");
                        }
                    };
                    //finally requesting the location update
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            });
        } else {
            //this is when the location service is not enabled
            //this will open the location settings
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking the condition
        if (requestCode == 100 && grantResults.length > 0 && (grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
            fetch_the_location();
        } else {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            build.setTitle("Disclaimer");
            build.setMessage("This permission is important for us to provide you the parking service");
            build.setPositiveButton("Ok", (dialog, which) -> {

            });
            build.show();
        }
    }
}
/*
   to change the color of the marker(we can use custom icon also)
   => googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Manglore").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

   to set the type of the map
   => googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 */