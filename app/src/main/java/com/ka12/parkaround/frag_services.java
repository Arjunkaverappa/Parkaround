package com.ka12.parkaround;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import io.armcha.elasticview.ElasticView;

public class frag_services extends Fragment {
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

        Intent in = new Intent(getActivity(), service_activity.class);
        fuel_card.setOnClickListener(v1 -> {
            //redirecting
            startActivity(new Intent(getActivity(), fuel_prices.class));
            Animatoo.animateZoom(getActivity());
        });

        traffic_card.setOnClickListener(v1 -> {
            //redirecting
            in.putExtra("service", "traffic");
            startActivity(in);
            Animatoo.animateZoom(getContext());
        });

        remainder_card.setOnClickListener(v1 -> {
            //redirecting
            startActivity(new Intent(getActivity(), remainder.class));
            Animatoo.animateZoom(getActivity());
        });

        emission_card.setOnClickListener(v1 -> {
            //redirecting
            startActivity(new Intent(getActivity(), emission.class));
            Animatoo.animateZoom(getContext());
        });

        return v;
    }
}