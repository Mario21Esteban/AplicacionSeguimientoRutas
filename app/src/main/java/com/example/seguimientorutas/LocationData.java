package com.example.seguimientorutas;

import com.google.android.gms.maps.model.LatLng;

public class LocationData {
    private double latitude;
    private double longitude;

    // Constructor vacío necesario para Firebase
    public LocationData() {
    }

    // Constructor con parámetros
    public LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters y Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Método para convertirlo a LatLng
    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }
}
