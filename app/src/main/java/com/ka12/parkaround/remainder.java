package com.ka12.parkaround;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;


public class remainder extends AppCompatActivity {
    public static final String REMAINDER = "com.ka12.parkaround.this_is_where_remainders_are_saved";
    CardView add_remainder;
    String get_remainders;
    ListView listview;
    //String[] remainders={"abc","a"};
    ArrayList<String> remainder = new ArrayList<>();
    Mainadapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remainer);
        add_remainder = findViewById(R.id.add);
        listview = findViewById(R.id.listview);

        adapter = new Mainadapter(remainder.this, remainder);
        listview.setAdapter(adapter);

        get_the_remainders();

        add_remainder.setOnClickListener(v -> {
            //logic
            add_new_remainder();
        });

        set_up_action_and_status_bar();
    }

    public void get_the_remainders() {
        SharedPreferences get_remainder = getSharedPreferences(REMAINDER, MODE_PRIVATE);
        get_remainders = get_remainder.getString("rem", "");
        String[] split = get_remainders.split("\\#");

        for (String s : split) {
            if (!s.equals("")) {
                remainder.add(s);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void add_new_remainder() {
        AlertDialog.Builder get_text = new AlertDialog.Builder(this);
        get_text.setTitle("Set remainder");
        View custom_edit = getLayoutInflater().inflate(R.layout.custom_textbox, null);
        get_text.setView(custom_edit);
        get_text.setPositiveButton("Set", (dialog, which) ->
        {
            EditText edit = custom_edit.findViewById(R.id.editText);
            if (edit.getText().toString().equals("")) {
                Toast.makeText(this, "PLease enter the text", Toast.LENGTH_SHORT).show();
            } else {
                set_remainder(edit.getText().toString());
            }
        });
        get_text.setNegativeButton("Cancel", (dialog, which) -> {
        });
        get_text.show();
    }

    public void set_remainder(String data) {
        remainder.clear();
        SharedPreferences get_prev = getSharedPreferences(REMAINDER, MODE_PRIVATE);
        String final_data = data + "#" + get_prev.getString("rem", "");

        SharedPreferences.Editor set_prev = getSharedPreferences(REMAINDER, MODE_PRIVATE).edit();
        set_prev.putString("rem", final_data).apply();

        get_the_remainders();
    }

    public void set_up_action_and_status_bar() {
        //hiding the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
        //to get transparent status bar, try changing the themes
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

}

class Mainadapter extends BaseAdapter {
    public static final String REMAINDER = "com.ka12.parkaround.this_is_where_remainders_are_saved";
    public Context context;
    LayoutInflater inflater;
    ArrayList<String> remainder;

    Mainadapter(Context c, ArrayList<String> remainders) {
        context = c;
        this.remainder = remainders;
    }

    @Override
    public int getCount() {
        return remainder.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_remainder, null);
        }
        ImageView img = convertView.findViewById(R.id.cancel_img);
        TextView rem = convertView.findViewById(R.id.rem_text);

        rem.setText(remainder.get(position));

        img.setOnClickListener(v -> {
            //logic
            SharedPreferences get_prev = context.getSharedPreferences(REMAINDER, Context.MODE_PRIVATE);
            String final_data = get_prev.getString("rem", "");
            StringBuilder temp_data = new StringBuilder();
            //use logs and find out whats wrong
            String[] split = final_data.split("\\#");
            Log.e("rem", "before :" + remainder);
            for (String s : split) {
                Log.e("rem", s);
                if (!s.equals(remainder.get(position)) && !s.equals("")) {
                    temp_data.append(s).append("#");
                }
            }
            SharedPreferences.Editor set_prev = context.getSharedPreferences(REMAINDER, Context.MODE_PRIVATE).edit();
            set_prev.putString("rem", temp_data.toString()).apply();
            remainder.remove(position);
            notifyDataSetChanged();
            Toast.makeText(context, "item removed", Toast.LENGTH_SHORT).show();
        });
        return convertView;
    }
}
