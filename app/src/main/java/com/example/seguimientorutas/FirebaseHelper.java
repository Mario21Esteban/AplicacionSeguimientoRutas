package com.example.seguimientorutas;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {

    private final DatabaseReference databaseReference;

    public FirebaseHelper() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("UserRoutes");
    }

    public void saveRoute(List<LatLng> route) {
        databaseReference.push().setValue(route);
    }

    public void getRoutes(FirebaseCallback<List<List<LatLng>>> callback) {
        databaseReference.get().addOnCompleteListener(task -> {
            List<List<LatLng>> routes = new ArrayList<>();
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                List<LatLng> route = snapshot.getValue(new GenericTypeIndicator<List<LatLng>>() {});
                routes.add(route);
            }
            callback.onCallback(routes);
        });
    }

    public interface FirebaseCallback<T> {
        void onCallback(T data);
    }
}
