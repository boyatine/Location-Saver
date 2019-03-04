package com.example.midterm1q2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static ArrayList<String> placesArrayList = new ArrayList<>();
    static ArrayAdapter placesArrayAdapter, latArrayAdapter, lngArrayAdapter;
    static ArrayList<Double> latArray = new ArrayList<>();
    static ArrayList<Double> lngArray = new ArrayList<>();

    SharedPreferences sharedPreferences;
    static ListView listView, latFakeView, lngFakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getApplicationContext().getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        listView = findViewById(R.id.listView);
        latFakeView = findViewById(R.id.latFakeView);
        lngFakeView = findViewById(R.id.lngFakeView);

        // load past data
        try {
            placesArrayList = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("PLACES", ObjectSerializer.serialize(new ArrayList<String>())));
            latArray = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("LAT", ObjectSerializer.serialize(new ArrayList<Double>())));
            lngArray = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("LNG", ObjectSerializer.serialize(new ArrayList<Double>())));
        }
        catch (Exception e){
            e.printStackTrace();
        }



        if ( placesArrayList.size() == 0 ) {
            placesArrayList.add("Add a new place...");
            latArray.add(0.0);
            lngArray.add(0.0);
        }

        //Show the notes on listView
        placesArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, placesArrayList);
        latArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, latArray);
        lngArrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, lngArray);

        listView.setAdapter(placesArrayAdapter);
        latFakeView.setAdapter(latArrayAdapter);
        lngFakeView.setAdapter(lngArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ( position > 0 ) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class );
                    intent.putExtra("intent", "fav");
                    intent.putExtra("lat", latArray.get(position));
                    intent.putExtra("lng", lngArray.get(position));
                    intent.putExtra("address", placesArrayList.get(position));
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class );
                    intent.putExtra("intent", "add");
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if ( position > 0 ) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Are you sure?")
                            .setMessage("Do you want to delete this note?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            placesArrayList.remove(position);
                                            placesArrayAdapter.notifyDataSetChanged();
                                            latArray.remove(position);
                                            latArrayAdapter.notifyDataSetChanged();
                                            lngArray.remove(position);
                                            lngArrayAdapter.notifyDataSetChanged();

                                            try {
                                                sharedPreferences.edit().putString("PLACES", ObjectSerializer.serialize(MainActivity.placesArrayList)).apply();
                                                sharedPreferences.edit().putString("LAT", ObjectSerializer.serialize(MainActivity.latArray)).apply(); // ??????????
                                                sharedPreferences.edit().putString("LNG", ObjectSerializer.serialize(MainActivity.lngArray)).apply(); // ??????????
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                            )
                            .setNegativeButton("NO", null)
                            .show();
                }

                return true;
            }
        });
    }
}
