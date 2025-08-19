package com.s23010529.lawyero;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class cusMap extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView; // MapView to display Google Map
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"; // key for saving state

    // Default values (Colombo location)
    private double lat = 6.9271;
    private double lng = 79.8612;
    private String name = "Location"; // default marker title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cus_map);

        // Get data from Intent
        name = getIntent().getStringExtra("name");        // Marker title
        lat = getIntent().getDoubleExtra("lat", lat);     // Latitude
        lng = getIntent().getDoubleExtra("lng", lng);     // Longitude

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            // Restore map state if available
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);

        // Notify when map is ready
        mapView.getMapAsync(this);
    }

    // Callback when the map is fully loaded and ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Create a LatLng object for the location
        LatLng loc = new LatLng(lat, lng);

        // Add a marker at the location with the given name
        googleMap.addMarker(new MarkerOptions().position(loc).title(name));

        // Move camera to the marker with zoom level 15
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
    }

    // ----------------- MapView lifecycle methods -----------------

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); // Resume map rendering
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart(); // Start map lifecycle
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop(); // Stop map lifecycle
    }

    @Override
    public void onPause() {
        mapView.onPause(); // Pause map rendering
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy(); // Cleanup resources
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory(); // Handle low memory case
    }

    // Save map state during screen rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
