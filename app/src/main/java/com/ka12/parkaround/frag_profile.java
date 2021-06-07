package com.ka12.parkaround;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

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
            //navigation to settings activity
            Intent in = new Intent(getActivity(), settings.class);
            startActivity(in);
            Animatoo.animateZoom(getContext());
        });

        land_card.setOnClickListener(v12 -> {
            //opening host land
            startActivity(new Intent(getActivity(), host_land.class));
            Animatoo.animateZoom(getContext());
        });

        return v;
    }
}