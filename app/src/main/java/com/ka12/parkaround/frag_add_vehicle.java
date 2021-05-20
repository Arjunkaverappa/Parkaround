package com.ka12.parkaround;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class frag_add_vehicle extends Fragment {
    public static final String PHONE_NUMBER = "com.ka12.parkaround.this_is_where_phone_number_of_a_user_is_saved";
    public static final String IS_VEHICLE_ADDED = "com.ka12.this_is_where_boolean_of_is_vehicle_added_is_saved";
    public static final String USER_VEHICLE = "com.ka12.this_is_where_user_vehicle_is_saved";
    Spinner manufacturer_spinner, color_spinner;
    TextInputEditText vehicle_number, vehicle_model;
    CardView add_vehicle;
    String user_vehicle_number, user_vehicle_model, user_manufacturer, user_vehicle_color, user_phone_number;
    //firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    String set_data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_add_vehicle, container, false);
        manufacturer_spinner = v.findViewById(R.id.manufacturer_spinner);
        color_spinner = v.findViewById(R.id.color_spinner);
        vehicle_model = v.findViewById(R.id.vehicle_model);
        vehicle_number = v.findViewById(R.id.vehicle_number);
        add_vehicle = v.findViewById(R.id.add_vehicle);

        //setting up adapters for spinners
        String[] colors = new String[]{"color", "White", "Black", "Red", "Green", "Blue", "yellow"};
        String[] manufacturer = new String[]{"Manufacturer", "Maruti Suzuki", "Hyundai Motors", "Tata Motors", "Toyota", "Kia Motors", "Skoda",
                "MG", "Mercedes", "Volkswagen", "Honda", "Renault", "Mahindra", "BMW", "Jeep", "Ford", "Nissan", "Datsun", "Audi", "Tesla", "others"};

        ArrayAdapter<String> adapter_manu = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, manufacturer);
        ArrayAdapter<String> adapter_col = new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, colors);
        manufacturer_spinner.setAdapter(adapter_manu);
        color_spinner.setAdapter(adapter_col);

        //assigning values to variables
        manufacturer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_manufacturer = manufacturer[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                user_manufacturer = "";
            }
        });
        color_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_vehicle_color = colors[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                user_vehicle_color = "";
            }
        });

        add_vehicle.setOnClickListener(v1 -> {
            if (Objects.requireNonNull(vehicle_number.getText()).toString().trim().equals("")
                    || Objects.requireNonNull(vehicle_model.getText()).toString().trim().equals("")
                    || user_manufacturer.equals("")
                    || user_vehicle_color.equals("")) {
                Toast.makeText(getActivity(), "Please check the inputs", Toast.LENGTH_SHORT).show();
            } else {
                add_to_firebase(vehicle_number.getText().toString().trim(), Objects.requireNonNull(vehicle_model.getText()).toString().trim());
            }
        });

        return v;
    }

    public void add_to_firebase(String v_number, String v_model) {

        //retrieving the phone number of the user from shared preferences
        SharedPreferences get_number = Objects.requireNonNull(getActivity()).getSharedPreferences(PHONE_NUMBER, Context.MODE_PRIVATE);
        user_phone_number = get_number.getString("phone", "9977997795");

        String final_data = v_number + "#" + v_model + "#" + user_manufacturer + "#" + user_vehicle_color;

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference()
                .child("USERS")
                .child("DETAILS")
                .child(user_phone_number)
                .child("VEHICLES");
        reference.setValue(final_data).addOnCompleteListener(task ->
        {
            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();

            //updating the shared preferences
            SharedPreferences.Editor set_boolean = getActivity().getSharedPreferences(IS_VEHICLE_ADDED, Context.MODE_PRIVATE).edit();
            set_boolean.putBoolean("is_vehicle", true).apply();

            //saving the user vehicle information
            SharedPreferences.Editor set_vehicle = getActivity().getSharedPreferences(USER_VEHICLE, Context.MODE_PRIVATE).edit();
            set_vehicle.putString("user_vehicle", final_data).apply();
             /*
            1) vehicle number
            2) vehicle model
            3) manufacturer
            4) color
             */

            //going back to the previous activity
            startActivity(new Intent(getContext(), booking.class));
            Animatoo.animateZoom(getContext());
            //getActivity().finish();

        }).addOnFailureListener(e -> {
            //record the error messsage here to logs and toast
            Toast.makeText(getActivity(), "Something went wrong " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}