package com.ka12.parkaround;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class frag_land_owner extends Fragment {
    public static final String IS_LAND_ADDED = "com.ka12.parkaround.this_is_where_boolean_of_is_place_added_is_saved";
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    Boolean is_place_added;
    String user_phone_number;
    //firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    //ui
    TextView address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_land_owner, container, false);
        address = v.findViewById(R.id.address);

        //checking if the place is added by the land owner or not
        SharedPreferences get_place = Objects.requireNonNull(getActivity()).getSharedPreferences(IS_LAND_ADDED, Context.MODE_PRIVATE);
        is_place_added = get_place.getBoolean("is_added", false);

        //TODO add this preference to the add land activity
        //show another fragment to take the details
        if (!is_place_added) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.host_fragment, new frag_add_land()).commit();
        } else {
            //retrieving the phone number
            SharedPreferences get_number = Objects.requireNonNull(getActivity()).getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
            user_phone_number = get_number.getString("phone", "9977997798");

            //fetching the corresponding address from firebase
            get_the_address();
        }

        return v;
    }

    public void get_the_address() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference().child("LOCATIONS").child(user_phone_number);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String data = snapshot.getValue(String.class);
                if (data != null) {
                    show_address_in_ui(data);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void show_address_in_ui(String user_address) {
        // 0->latitue
        // 1->longitude
        // 2->final_address
        // 3->is_active

        String[] split = user_address.split("\\#");
        address.setText(split[2]);
        if (split[3].equals("yes")) {

        }
    }

}