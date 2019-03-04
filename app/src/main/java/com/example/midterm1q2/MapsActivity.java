package com.example.midterm1q2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    LatLng currentPosition, savedLoc;

    double savedLat, savedLng, toSaveLat, toSaveLng;

    Marker currentMarker;

    String intent;

    String addressToSave, favAddress;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Intent i = getIntent();
        intent = i.getStringExtra("intent");

        if ( intent.equals("fav") ) {
            savedLat = i.getDoubleExtra("lat", 0);
            savedLng = i.getDoubleExtra("lng", 0);
            favAddress = i.getStringExtra("address");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if ( intent.equals("fav") ) {
            savedLoc = new LatLng(savedLat, savedLng);

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(savedLoc).title(favAddress));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(savedLoc, 15));
        }

        else if ( intent.equals("add") ) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    currentMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                            .title("Your current location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .draggable(false));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(14));

                    locationManager.removeUpdates(locationListener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    addressToSave = "";
                    toSaveLat = latLng.latitude;
                    toSaveLng = latLng.longitude;

                    Geocoder geocoder =  new Geocoder(getApplicationContext(), Locale.getDefault());

                    String address = "";
                    List<Address> addressList = null;
                    try {
                        addressList = geocoder.getFromLocation(toSaveLat, toSaveLng, 1);
                        if (addressList != null && addressList.size() > 0) {
                            if ( addressList.get(0).getThoroughfare() != null ) {
                                if (addressList.get(0).getSubThoroughfare() != null)
                                    address += addressList.get(0).getSubThoroughfare()+ " ";
                                address += addressList.get(0).getThoroughfare();
                            }

                            else
                                address = "";
                        }//if addressList != null

                        if( address.equals("") ) {
                            address = "Latitude: " + toSaveLat + " Longitude: " + toSaveLng;
                        }
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    addressToSave += address;

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            });

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                else
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( intent.equals("add") ) {
            MainActivity.placesArrayList.add(addressToSave);
            MainActivity.placesArrayAdapter.notifyDataSetChanged();
            MainActivity.latArray.add(toSaveLat);
            MainActivity.latArrayAdapter.notifyDataSetChanged();
            MainActivity.lngArray.add(toSaveLng);
            MainActivity.lngArrayAdapter.notifyDataSetChanged();

            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            try {
                sharedPreferences.edit().putString("PLACES", ObjectSerializer.serialize(MainActivity.placesArrayList)).apply();
                sharedPreferences.edit().putString("LAT", ObjectSerializer.serialize(MainActivity.latArray)).apply(); // ??????????
                sharedPreferences.edit().putString("LNG", ObjectSerializer.serialize(MainActivity.lngArray)).apply(); // ????????

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
