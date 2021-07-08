package com.ka12.parkaround;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class emission extends AppCompatActivity {
    GoogleMap mymap;
    ArrayList<String> locations;
    ArrayList<String> address;
    ArrayList<String> id;
    ArrayList<Double> lat;
    ArrayList<Double> lan;
    Boolean is_map_ready = false;
    public final OnMapReadyCallback onMapReadyCallback = googleMap -> {
        mymap = googleMap;
        mymap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        get_the_locations();
        set_up_emission_centers();
        is_map_ready = true;

        mymap.setOnInfoWindowClickListener(marker -> {
            for (int i = 0; i < id.size(); i++) {
                if (marker.getTitle().equals(id.get(i))) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat.get(i) + "," + lan.get(i) + "&avoid=tf");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(this, "Please install Google maps from playstore", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emission);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.owner_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(onMapReadyCallback);
        }

        set_up_action_and_status_bar();


    }

    public void set_up_emission_centers() {
        for (int i = 0; i < locations.size(); i++) {
            String[] split = locations.get(i).split("\\#");
            for (int j = 0; j < 4; j++) {
                Log.e("emiss", split[j]);
                LatLng latLng = new LatLng(Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                mymap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(split[0]));
                //address.add(split[3]);
                id.add(split[0]);
                lat.add(Double.parseDouble(split[1]));
                lan.add(Double.parseDouble(split[2]));
                mymap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            }
        }
    }

    public void get_the_locations() {
        locations = new ArrayList<>();
        address = new ArrayList<>();
        id = new ArrayList<>();
        lat = new ArrayList<>();
        lan = new ArrayList<>();
        locations.add("Durga Emission Testing Centre#12.870340174173672#74.86845585396308#Shyam Complex, opp. Capitanio School, Nanthoor, Pumpwell, Mangalore, Karnataka 575002");
        locations.add("Praveen Emission Testing Centre#12.857038944038699#74.85368811563079#Nandi Gudda, Mangalore, Karnataka 575002");
        locations.add("Shiva Ganesh Emission Testing Centre#12.869864478132616# 74.86298647701334#Shiva Ganesh Complex, opp. OMEGA Hospital, Pumpwell, Mangalore, Karnataka 575002");
        locations.add("Yogi Emission Testing Centre#12.857903124052132# 74.83882200079188#opp. Fiza Forum Mall, Pandeshwar, Mangalore, Karnataka 575001");
        locations.add("Ujjodi Emission Testing Centre#12.866264546139302#74.8664000553018#National Highway 17, Ujjodi, Mangalore, Karnataka 575002");
        locations.add("Srikara Emission Testing Centre#12.863343721890905# 74.84766605763741#24-11-1107, Babugudda Rd, Marnamikatte, Mangalore, Karnataka 575001");
        locations.add("Emission Testing Center#12.883621282039432# 74.85962748641042#Kadri emission Testing Center, IOC Petrol Pump, Shivbagh Rd, Kadri, Mangalore, Karnataka 575002");
        locations.add("Go Green Emission Testing Centre & Auto consultancy#12.872941935739075# 74.8930899676637#3/91-5,WHITE ROSE COMPLEX, Padil, Kodakkal, Karnataka 575007");
        locations.add("Emission Testing Center#12.885964028198615# 74.83404994041126#Gandhinagar 2nd Cross Rd, Gandhinagar, Mangalore, Karnataka 575003");
        locations.add("Good Wheel Emission Testing Center#12.883986589536953#74.87120314135431#D.No. 3E-13-1118/1, opp. Syndicate Bank Bikarnakatte, Mangalore, 575005");
        locations.add("Auto worls emission testing center#12.824940903873573#74.8599734903752#junction, beside petrol pump, Kallapu, Mangalore, Karnataka 575017");
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