package com.hudzah.wearamask;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;

public class CircleManager {

    private Context context;
    private GoogleMap googleMap;
    private static final String TAG = "CircleManager";
    private GeofencingRequest geofencingRequest;
    private PendingIntent pendingIntent;

    private List<Geofence> geofences = new ArrayList<>();


    final DialogAdapter dialogAdapter;

    public CircleManager(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        dialogAdapter = new DialogAdapter((Activity) context);
    }

    public void drawCircleOnMap(int radius, int color, Place place){
        googleMap.clear();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

        MarkerOptions options = new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getAddress());
        googleMap.addMarker(options);
        googleMap.addCircle(
                new CircleOptions()
                        .center(place.getLatLng())
                        .radius(radius)
                        .strokeWidth(3f)
                        .strokeColor(Color.argb(255, red, green, blue))
                        .fillColor(Color.argb(70, red, green, blue))
        );
    }

    public void drawManyCirclesOnMap(ArrayList<Location> locations){
        googleMap.clear();
        for(Location location : locations){
            int red = Color.red(location.getSelectedColor());
            int green = Color.green(location.getSelectedColor());
            int blue = Color.blue(location.getSelectedColor());

            Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

            MarkerOptions options = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(location.getAddress());
            googleMap.addMarker(options);
            googleMap.addCircle(
                    new CircleOptions()
                            .center(location.getLatLng())
                            .radius(location.getSelectedRadius())
                            .strokeWidth(3f)
                            .strokeColor(Color.argb(255, red, green, blue))
                            .fillColor(Color.argb(70, red, green, blue))
            );

            addGeofence(location.getLatLng(), location.getSelectedRadius());
        }

        callGeofencingClient();
    }

    private void addGeofence(LatLng latLng, int radius){

        String ID = String.valueOf(latLng.latitude + latLng.longitude);
        Geofence geofence = MapFragment.geofenceHelper.getGeofence(ID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        geofences.add(geofence);
        Log.d(TAG, "addGeofence: geofence added to list " + geofence);

    }

    private void callGeofencingClient(){
        geofencingRequest = MapFragment.geofenceHelper.getGeofencingRequest(geofences);
        pendingIntent = MapFragment.geofenceHelper.getPendingIntent();

        MapFragment.geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence has been added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = MapFragment.geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: error is " + errorMessage + " raw error message is " + e.getMessage());
                        dialogAdapter.displayErrorDialog(errorMessage);
                    }
                });
    }

    public void clearAllCircles(){
        googleMap.clear();
    }
}
