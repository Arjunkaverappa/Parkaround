package com.ka12.parkaround;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;


public class MainActivity extends AppCompatActivity {
    // public static final String MAP_TYPE = "com.ka12.parkaround.this_is_where_map_type_is_saved";
// public static final String USER_LOCATION="com.ka12.parkaround.this_is_where_user_location_is_saved";
    LinearLayout frag;
    BubbleNavigationConstraintView bottombar;
    Boolean is_connected = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottombar = findViewById(R.id.bottombar);
        frag = findViewById(R.id.frag);

        set_up_action_and_status_bar();

        //checking if the location services are enabled or not
        //TODO enable it
        // check_permission();

        //checking if the internet service is enabled or not
        //TODO enable it
        //check_network();

        //setting up bottom navigationbar to the middle element
        bottombar.setCurrentActiveItem(1);
        //getting the maps fragment and setting it as default
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frag, new MapsFragment()).commit();
        bottombar.setNavigationChangeListener((view, position) -> {
            switch (position) {
                case 0://getting the activity fragment
                    FragmentManager activity = getSupportFragmentManager();
                    activity.beginTransaction().remove(new MapsFragment()).remove(new frag_services()).replace(R.id.frag, new frag_land_owner()).commit();
                    break;
                case 1://getting the maps fragment
                    FragmentManager explore = getSupportFragmentManager();
                    explore.beginTransaction().remove(new frag_services()).remove(new frag_land_owner()).replace(R.id.frag, new MapsFragment()).commit();
                    break;
                case 2:
                    //getting the settings fragment
                    FragmentManager settings = getSupportFragmentManager();
                    settings.beginTransaction().remove(new MapsFragment()).remove(new frag_land_owner()).replace(R.id.frag, new frag_services()).commit();
                    break;
            }
        });
    }

    public void set_up_action_and_status_bar() {
        //hiding the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        //changing status bar color
        //to get transparent status bar, try changing the themes
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void check_permission() {
        //checking permission if the location is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted, asking for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking the condition
        Log.e("mymap", "onRequestPermissionResult");
        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            Log.e("mymap", "permission granted in onRquestPermissionResult");
        }
        /*
        else
            {
            Log.e("mymap", "permission denied");
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle("Disclaimer");
            build.setMessage("This permission is important for us to provide you the parking service");
            build.setPositiveButton("Ok", (dialog, which) -> {
              //response for it
            });
            build.show();
        }
        */
    }

    public void check_network() {
        //TODO set up wherever required
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
                    b.setMessage("Please turn on internet");
                    b.setPositiveButton("OK", (dialog, which) -> {
                        //here is the set positive button

                    });
                    b.show();
                    check_network();
                }
            }, 5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
