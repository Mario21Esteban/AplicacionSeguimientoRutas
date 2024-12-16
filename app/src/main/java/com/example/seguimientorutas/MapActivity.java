package com.example.seguimientorutas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean isTracking = false;
    private List<LatLng> currentRoute = new ArrayList<>();
    private Polyline currentPolyline;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        firebaseHelper = new FirebaseHelper();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button startRouteButton = findViewById(R.id.startRouteButton);
        Button historyButton = findViewById(R.id.historyButton);
        Button customizeButton = findViewById(R.id.customizeButton);

        startRouteButton.setOnClickListener(v -> toggleTracking());
        historyButton.setOnClickListener(v -> showHistory());
        customizeButton.setOnClickListener(v -> customizeMap());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void toggleTracking() {
        isTracking = !isTracking;
        if (isTracking) {
            Toast.makeText(this, "Iniciando grabación de ruta", Toast.LENGTH_SHORT).show();
        } else {
            firebaseHelper.saveRoute(currentRoute);
            currentRoute.clear();
            if (currentPolyline != null) currentPolyline.remove();
            Toast.makeText(this, "Ruta guardada", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        // Recuperar y mostrar historial
        firebaseHelper.getRoutes(routes -> {
            for (List<LatLng> route : routes) {
                PolylineOptions polylineOptions = new PolylineOptions().addAll(route).width(5).color(getResources().getColor(R.color.black));
                mMap.addPolyline(polylineOptions);
            }
        });
    }

    private void customizeMap() {
        mMap.setMapType(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL
                ? GoogleMap.MAP_TYPE_SATELLITE
                : GoogleMap.MAP_TYPE_NORMAL);
        Toast.makeText(this, "Mapa personalizado", Toast.LENGTH_SHORT).show();
    }
}

