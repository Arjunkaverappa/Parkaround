package com.ka12.parkaround;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import io.armcha.elasticview.ElasticView;

public class frag_services extends Fragment {
    // CardView fuel_card, emission_card, traffic_card, remainder_card;
    ElasticView fuel_card, emission_card, traffic_card, remainder_card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_services, container, false);
        fuel_card = v.findViewById(R.id.fuel_card);
        emission_card = v.findViewById(R.id.emission_card);
        traffic_card = v.findViewById(R.id.traffic_card);
        remainder_card = v.findViewById(R.id.remainder_card);

        fuel_card.setOnClickListener(v1 -> {
            //redirecting
            Toast.makeText(getActivity(), "coming soon", Toast.LENGTH_SHORT).show();
        });

        traffic_card.setOnClickListener(v1 -> {
            //redirecting
            Toast.makeText(getActivity(), "coming soon", Toast.LENGTH_SHORT).show();
        });

        remainder_card.setOnClickListener(v1 -> {
            //redirecting
            Toast.makeText(getActivity(), "coming soon", Toast.LENGTH_SHORT).show();
        });

        emission_card.setOnClickListener(v1 -> {
            //redirecting
            Toast.makeText(getActivity(), "coming soon", Toast.LENGTH_SHORT).show();
        });

        return v;
    }
}