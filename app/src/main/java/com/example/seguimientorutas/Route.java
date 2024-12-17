package com.example.seguimientorutas;

import java.util.List;

public class Route {
    private String routeId;  // ID único de la ruta
    private String date;     // Fecha de la ruta
    private List<LocationData> points; // Lista de puntos de la ruta

    // Constructor vacío necesario para Firebase
    public Route() {
    }

    // Constructor con parámetros
    public Route(String routeId, String date, List<LocationData> points) {
        this.routeId = routeId;
        this.date = date;
        this.points = points;
    }

    // Getters y Setters
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<LocationData> getPoints() {
        return points;
    }

    public void setPoints(List<LocationData> points) {
        this.points = points;
    }
}
