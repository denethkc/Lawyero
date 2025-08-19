package com.s23010529.lawyero;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class map extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private MapView mapView;
    private GoogleMap gMap;
    private SearchView searchView;
    private Button addButton;

    private LatLng selectedLatLng; // Stores currently selected location
    private FusedLocationProviderClient fusedLocationClient; // For current location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //  Initialize MapView and UI elements
        mapView = findViewById(R.id.mapView);
        searchView = findViewById(R.id.searchbar);
        addButton = findViewById(R.id.add);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // MapView lifecycle management
        Bundle mapViewBundle = (savedInstanceState != null) ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        //  SearchView listener to find location by name
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                geoLocate(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        //  Add button: send selected location back to calling activity
        addButton.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                String locationAddress = getAddressFromLatLng(selectedLatLng);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("location", locationAddress);
                resultIntent.putExtra("latitude", selectedLatLng.latitude);
                resultIntent.putExtra("longitude", selectedLatLng.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(map.this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        //  Enable user location if permission granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        gMap.setMyLocationEnabled(true);
        getCurrentLocation();

        //  Default view (Sri Lanka) if current location unavailable
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));

        // Map click listener to set marker
        gMap.setOnMapClickListener(latLng -> setMarker(latLng, "Selected Location"));
    }

    // Get last known location and set marker
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && gMap != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        setMarker(currentLatLng, "Current Location");
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show()
                );
    }

    // Geocode search query into LatLng and set marker
    private void geoLocate(String locationName) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                setMarker(latLng, locationName);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
        }
    }

    //  Place marker on map and store as selected location
    private void setMarker(LatLng latLng, String title) {
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(latLng).title(title));
        selectedLatLng = latLng;
    }

    // Convert LatLng to human-readable address
    private String getAddressFromLatLng(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addressList.isEmpty()) {
                return addressList.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng.latitude + ", " + latLng.longitude; // fallback
    }

    //  Handle runtime permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //  MapView lifecycle management
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
