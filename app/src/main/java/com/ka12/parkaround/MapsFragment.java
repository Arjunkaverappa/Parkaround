package com.ka12.parkaround;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
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
            LatLng sydney = new LatLng(12.9141, 74.8560);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Manglore"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));
        }
    };
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
            fm.beginTransaction().replace(R.id.frag, new MapsFragment()).commit();        });

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

}
/*
   to change the color of the marker(we can use custom icon also)
   => googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Manglore").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

   to set the type of the map
   => googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 */