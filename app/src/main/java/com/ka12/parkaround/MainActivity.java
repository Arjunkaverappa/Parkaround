package com.ka12.parkaround;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
    public static final String MAP_TYPE = "com.ka12.parkaround.this_is_where_map_type_is_saved";
    LinearLayout frag;
    BubbleNavigationConstraintView bottombar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottombar = findViewById(R.id.bottombar);
        frag = findViewById(R.id.frag);

        set_up_action_and_status_bar();

        check_permission();

        //changing status bar color and dark text in action bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //setting up bottom navigationbar to the middle element
        bottombar.setCurrentActiveItem(1);
        //getting the maps fragment and setting it as default
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frag, new MapsFragment()).commit();
        bottombar.setNavigationChangeListener((view, position) -> {
            switch (position) {
                case 0://getting the activity fragment
                    FragmentManager activity = getSupportFragmentManager();
                    activity.beginTransaction().remove(new MapsFragment()).remove(new fragment_settings()).replace(R.id.frag, new fragment_acitivity()).commit();
                    break;
                case 1://getting the maps fragment
                    FragmentManager explore = getSupportFragmentManager();
                    explore.beginTransaction().remove(new fragment_settings()).remove(new fragment_acitivity()).replace(R.id.frag, new MapsFragment()).commit();
                    break;
                case 2:
                    //getting the settings fragment
                    FragmentManager settings = getSupportFragmentManager();
                    settings.beginTransaction().remove(new MapsFragment()).remove(new fragment_acitivity()).replace(R.id.frag, new fragment_settings()).commit();
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
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void check_permission() {
        //checking permission if the location permissin is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission granted
            AlertDialog.Builder b = new AlertDialog.Builder(this);
        } else {
            //ask for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                    , 100);
            //check the on request permisiion result
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
}
