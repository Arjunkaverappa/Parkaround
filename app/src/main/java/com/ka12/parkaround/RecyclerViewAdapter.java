package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    //same list will be copied into the main activity
    ArrayList<String> available_locations;
    Context mContext;

    public RecyclerViewAdapter(ArrayList<String> locations, Context context) {
        this.available_locations = locations;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.e("recycler", "called onCreateViewHolder");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_lists, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.e("recycler", "called onBindViewHolder");

        //split the available_locations here using int position
        String[] split = available_locations.get(position).split("\\#");
        // 0->latitue
        // 1->longitude
        // 2->final_address
        // 3->is_active

        holder.address.setText(split[2]);
        if (split[3].equals("yes")) {
            holder.active_status.setText("Currently active");
            holder.active_status.setTextColor(Color.GREEN);
        } else {
            holder.active_status.setText("Currently Inactive");
            holder.active_status.setTextColor(Color.RED);
        }

        holder.location_card.setOnClickListener(v ->
        {
            //setting up onlick listeners
            Toast.makeText(mContext, "item clicked at " + position, Toast.LENGTH_SHORT).show();
            Intent go_to_booking = new Intent(mContext, booking.class);
            go_to_booking.putExtra("details", available_locations.get(position));
            mContext.startActivity(go_to_booking);
        });
    }

    @Override
    public int getItemCount() {
        Log.e("recycler", "called getItemCount :" + available_locations.size());
        return available_locations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView address, active_status;
        CardView location_card;

        //declare all the views from custom location_lists
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.address);
            location_card = itemView.findViewById(R.id.location_card);
            active_status = itemView.findViewById(R.id.active_status);
        }
    }
}
