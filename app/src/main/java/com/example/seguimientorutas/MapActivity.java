package com.example.seguimientorutas;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mapa;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference locationReference;
    private boolean isRecording = false; // Estado de grabación de la ruta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Configurar la referencia de Firebase para almacenar las rutas
        locationReference = FirebaseDatabase.getInstance().getReference("Routes");

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        // Inicialización del cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verificar permisos
        permisoUbicacion();

        // Botón de iniciar/detener ruta
        Button startRouteButton = findViewById(R.id.startRouteButton);
        startRouteButton.setOnClickListener(v -> {
            if (isRecording) {
                detenerRuta();
            } else {
                iniciarRuta();
            }
        });

        // Botón para mostrar historial de rutas
        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> mostrarHistorialRutas());
    }

    // Iniciar la grabación de la ruta
    private void iniciarRuta() {
        isRecording = true;
        obtenerUbicacionContinua();
        Toast.makeText(this, "Ruta iniciada", Toast.LENGTH_SHORT).show();
    }

    // Detener la grabación de la ruta
    private void detenerRuta() {
        isRecording = false;
        fusedLocationClient.removeLocationUpdates((PendingIntent) null); // Detener actualizaciones de ubicación
        Toast.makeText(this, "Ruta detenida", Toast.LENGTH_SHORT).show();
    }

    // Obtener la ubicación continua y almacenarla en Firebase
    private void obtenerUbicacionContinua() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Crear un LocationRequest usando el constructor Builder
        LocationRequest locationRequest = new LocationRequest.Builder(10000)  // Intervalo de 10 segundos
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)  // Establecer la prioridad de precisión alta
                .setMinUpdateIntervalMillis(5000)  // Establecer el intervalo más rápido a 5 segundos
                .build();

        // Solicitar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        mapa.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));

                        // Almacenar la ubicación en Firebase (sin ID de usuario, solo como una nueva entrada)
                        locationReference.push().setValue(currentLatLng);
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    // Mostrar las rutas almacenadas en Firebase
    private void mostrarHistorialRutas() {
        locationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PolylineOptions polylineOptions = new PolylineOptions();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LatLng location = snapshot.getValue(LatLng.class);
                    if (location != null) {
                        polylineOptions.add(location);
                    }
                }
                mapa.addPolyline(polylineOptions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Error al cargar el historial de rutas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Verificar permisos de ubicación
    private void permisoUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            obtenerUbicacionContinua();
        }
    }

    // Manejo de los permisos de ubicación
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionContinua();
            } else {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Habilitar la ubicación en el mapa
        mapa.setMyLocationEnabled(true);
    }
}
