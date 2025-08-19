package com.s23010529.lawyero;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;

public class searchMap extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;                // MapView for displaying Google Map
    private GoogleMap googleMap;            // GoogleMap object
    private FirebaseFirestore db;           // Firestore instance
    private SearchView searchView;          // Search bar to input location
    private Button searchButton;            // Button to trigger location search

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_map);

        mapView = findViewById(R.id.mapView2);
        searchView = findViewById(R.id.searchView2);
        searchButton = findViewById(R.id.search);

        db = FirebaseFirestore.getInstance();

        // Initialize MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        // Search button click listener
        searchButton.setOnClickListener(v -> {
            String location = searchView.getQuery().toString();
            if (!location.isEmpty()) {
                searchLocation(location); // Move map to searched location
            } else {
                Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;

        // Load all lawyers from "users" collection where role = "lawyer"
        db.collection("users")
                .whereEqualTo("role", "lawyer")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double lat = document.getDouble("latitude");
                            Double lng = document.getDouble("longitude");
                            String name = document.getString("username");
                            String type = document.getString("type");
                            String contact = document.getString("contact");

                            if (lat != null && lng != null) {
                                LatLng lawyerLocation = new LatLng(lat, lng);

                                // Add marker with lawyer's name, type, and contact
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(lawyerLocation)
                                        .title(name != null ? name : "Lawyer")
                                        .snippet(type + "\n" + contact));

                                // Move camera to the first lawyer
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lawyerLocation, 7));
                            }
                        }

                        //  Show marker info window on click
                        googleMap.setOnMarkerClickListener(marker -> {
                            marker.showInfoWindow();
                            return true;
                        });
                    } else {
                        Toast.makeText(this, "Error loading lawyers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Moves the map to a searched location using Geocoder
     */
    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // MapView lifecycle methods
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
