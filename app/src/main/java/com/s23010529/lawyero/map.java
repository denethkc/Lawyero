package com.s23010529.lawyero;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class map extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private MapView mapView;
    private GoogleMap gMap;
    private SearchView searchView;
    private Button addButton;

    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        searchView = findViewById(R.id.searchbar);
        addButton = findViewById(R.id.add);

        // MapView lifecycle bundle
        Bundle mapViewBundle = (savedInstanceState != null) ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Search location
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                geoLocate(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Add button click: send selected location back
        addButton.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                String locationAddress = getAddressFromLatLng(selectedLatLng);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("location", locationAddress);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(map.this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
            Toast.makeText(this, "Error finding location", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

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
        return latLng.latitude + ", " + latLng.longitude;
    }

    private void setMarker(LatLng latLng, String title) {
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(latLng).title(title));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        selectedLatLng = latLng;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        // Default location (Sri Lanka)
        LatLng sriLanka = new LatLng(7.8731, 80.7718);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLanka, 7));

        gMap.setOnMapClickListener(latLng -> {
            setMarker(latLng, "Selected Location");
        });
    }

    // MapView lifecycle integration
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

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
