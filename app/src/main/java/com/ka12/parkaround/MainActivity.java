package com.ka12.parkaround;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;


public class MainActivity extends AppCompatActivity {
    LinearLayout frag;
    BubbleNavigationConstraintView bottombar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottombar = findViewById(R.id.bottombar);
        frag = findViewById(R.id.frag);

        //changing status bar color and dark text in action bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //hiding the action bar
        ActionBar ab = getSupportActionBar();
        ab.hide();

        //setting up bottom navigationbar
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
}
