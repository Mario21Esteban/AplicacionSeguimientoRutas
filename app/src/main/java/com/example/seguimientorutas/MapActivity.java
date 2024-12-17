package com.example.seguimientorutas;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private float distanciaTotal = 0;          // Para calcular la distancia total
    private Location previousLocation = null;  // Última ubicación registrada
    private long startTime;                    // Tiempo de inicio de la ruta


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
                detenerRuta(startRouteButton);  // Detener ruta y cambiar texto
            } else {
                iniciarRuta(startRouteButton);  // Iniciar ruta y cambiar texto
            }
        });

        // Botón para mostrar historial de rutas
        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> mostrarHistorialRutas());

        Button customizeButton = findViewById(R.id.customizeButton);
        customizeButton.setOnClickListener(v -> cambiarTipoMapa());



    }


    // Iniciar la grabación de la ruta
    private void iniciarRuta(Button startRouteButton) {
        isRecording = true;
        distanciaTotal = 0; // Reiniciar distancia
        previousLocation = null; // Reiniciar la ubicación previa
        startTime = System.currentTimeMillis(); // Guardar la hora de inicio
        obtenerUbicacionContinua(); // Iniciar la captura de ubicación
        startRouteButton.setText("Detener Ruta"); // Cambiar el texto del botón
        Toast.makeText(this, "Ruta iniciada", Toast.LENGTH_SHORT).show();
    }


    // Detener la grabación de la ruta
    private void detenerRuta(Button startRouteButton) {
        isRecording = false;
        long endTime = System.currentTimeMillis(); // Guardar la hora de finalización
        long duracionSegundos = (endTime - startTime) / 1000; // Calcular la duración en segundos
        fusedLocationClient.removeLocationUpdates((PendingIntent) null); // Detener las actualizaciones de ubicación
        startRouteButton.setText("Iniciar Ruta"); // Cambiar el texto del botón

        // Mostrar resumen con distancia y duración
        String resumen = "Distancia recorrida: " + String.format("%.2f", distanciaTotal / 1000) + " km\n" +
                "Duración: " + duracionSegundos + " segundos";
        Toast.makeText(this, resumen, Toast.LENGTH_LONG).show();
    }


    // Obtener la ubicación continua y almacenarla en Firebase
    private void obtenerUbicacionContinua() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(10000)  // Intervalo de 10 segundos
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000)  // Intervalo más rápido de 5 segundos
                .build();

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

                        // Calcular la distancia total
                        if (previousLocation != null) {
                            distanciaTotal += previousLocation.distanceTo(location);
                        }
                        previousLocation = location;

                        // Almacenar la ubicación en Firebase
                        LocationData locationData = new LocationData(location.getLatitude(), location.getLongitude());
                        locationReference.push().setValue(locationData);
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
                if (!dataSnapshot.exists()) {
                    Toast.makeText(MapActivity.this, "No hay rutas guardadas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LocationData locationData = snapshot.getValue(LocationData.class);
                    if (locationData != null) {
                        LatLng location = locationData.toLatLng(); // Convertir LocationData a LatLng
                        polylineOptions.add(location);
                    }
                }

                if (polylineOptions.getPoints().isEmpty()) {
                    Toast.makeText(MapActivity.this, "No se encontraron ubicaciones en el historial.", Toast.LENGTH_SHORT).show();
                } else {
                    mapa.addPolyline(polylineOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Error al cargar el historial de rutas: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void cambiarTipoMapa() {
        // Opciones de tipos de mapa
        CharSequence[] tiposMapa = {"Normal", "Satelital", "Híbrido", "Terreno"};

        // Crear un diálogo para que el usuario elija el tipo de mapa
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona el tipo de mapa");

        builder.setItems(tiposMapa, (dialog, which) -> {
            switch (which) {
                case 0:
                    mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Toast.makeText(this, "Mapa Normal", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    Toast.makeText(this, "Mapa Satelital", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Toast.makeText(this, "Mapa Híbrido", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    mapa.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    Toast.makeText(this, "Mapa de Terreno", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }



}
