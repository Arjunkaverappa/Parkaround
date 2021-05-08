package com.ka12.parkaround;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class frag_profile extends Fragment {
    CardView settings_vard, land_card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_profile, container, false);
        settings_vard = v.findViewById(R.id.settings_card);
        land_card = v.findViewById(R.id.land_card);

        settings_vard.setOnClickListener(v1 -> {
            //something
            Toast.makeText(getActivity(), "Settings", Toast.LENGTH_SHORT).show();
        });

        land_card.setOnClickListener(v12 -> {
            //something
            Toast.makeText(getActivity(), "land", Toast.LENGTH_SHORT).show();
        });
        return v;
    }
}