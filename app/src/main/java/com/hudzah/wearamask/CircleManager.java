package com.hudzah.wearamask;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;



public enum CircleManager {
    Manager;

    private Context context;
    private GoogleMap googleMap;
    private static final String TAG = "CircleManager";
    private GeofencingRequest geofencingRequest;
    private PendingIntent pendingIntent;
    private List<Geofence> geofences = new ArrayList<>();
    GeofenceHelper geofenceHelper;
    GeofencingClient geofencingClient;


    public void init(Context context, GoogleMap googleMap){
        this.context = context;
        this.googleMap = googleMap;
        geofenceHelper = new GeofenceHelper(context);
        geofencingClient = new GeofencingClient(context);

    }

    public void drawCircleOnMap(int radius, int color, Place place){
        googleMap.clear();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

        MarkerOptions options = new MarkerOptions()
                .position(place.getLatLng())
                .title(place.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(getMarkerIcon(color)));
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

    public int getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        Log.d(TAG, "getMarkerIcon: hsv " + (int)hsv[0]);
        return (int) hsv[0];
    }

    public void drawManyCirclesOnMap(ArrayList<Location> locations){
        if(googleMap != null) {
            googleMap.clear();
        }
        Log.d(TAG, "drawManyCirclesOnMap: geofences are " + geofences);
        for(Location location : locations){
            int red = Color.red(location.getSelectedColor());
            int green = Color.green(location.getSelectedColor());
            int blue = Color.blue(location.getSelectedColor());

            Log.d("aw", "drawCircleOnMap: colours a re " + red + green + blue);

            MarkerOptions options = new MarkerOptions()
                    .position(location.getLatLng())
                    .title(location.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker((float) getMarkerIcon(location.getSelectedColor())));
            googleMap.addMarker(options);
            googleMap.addCircle(
                    new CircleOptions()
                            .center(location.getLatLng())
                            .radius(location.getSelectedRadius())
                            .strokeWidth(3f)
                            .strokeColor(Color.argb(255, red, green, blue))
                            .fillColor(Color.argb(70, red, green, blue))
            );
        }

        if(geofences.isEmpty()) {
            for (Location location : locations) {
                addGeofence(location.getLatLng(), location.getSelectedRadius());
            }
            callGeofencingClient();
        }


    }

    private void addGeofence(LatLng latLng, int radius){

        String ID = String.valueOf(latLng.latitude + latLng.longitude);
        Geofence geofence = geofenceHelper.getGeofence(ID, latLng, radius,
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        geofences.add(geofence);
        Log.d(TAG, "addGeofence: geofence added to list " + geofence);

    }

    private void callGeofencingClient(){
        geofencingRequest = geofenceHelper.getGeofencingRequest(geofences);
        pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Geofence has been added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure: error is " + errorMessage + " raw error message is " + e.getMessage());
                    }
                });
    }

    public void clearAllCircles(){
        googleMap.clear();
    }
}
