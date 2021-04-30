package com.ka12.parkaround;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Objects;

public class frag_land_owner extends Fragment {
    public static final String IS_LAND_ADDED = "com.ka12.parkaround.this_is_where_boolean_of_is_place_added_is_saved";
    Boolean is_place_added;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_land_owner, container, false);
        //checking if the place is added by the land owner or not
        SharedPreferences get_place = Objects.requireNonNull(getActivity()).getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE);
        is_place_added = get_place.getBoolean("is_added", false);

        //TODO add this preference to the add land activity
        //show another fragment to take the details
        if (!is_place_added) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.host_fragment, new frag_add_land()).commit();
        }
        return v;
    }
}